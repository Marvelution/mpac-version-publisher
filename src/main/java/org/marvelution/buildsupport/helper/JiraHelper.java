/*
 * Copyright (c) 2023-present Marvelution Holding B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.marvelution.buildsupport.helper;

import java.io.*;
import java.net.*;
import java.util.*;

import org.marvelution.buildsupport.model.*;

import com.atlassian.marketplace.client.*;
import com.atlassian.marketplace.client.api.*;
import com.atlassian.marketplace.client.http.*;
import com.atlassian.marketplace.client.impl.*;
import com.atlassian.marketplace.client.util.*;
import com.google.gson.*;

import static com.atlassian.marketplace.client.http.HttpConfiguration.*;
import static io.atlassian.fugue.Option.*;

/**
 * Helper for interacting with Jira.
 *
 * @author Mark Rekveld
 */
public class JiraHelper
		implements Closeable
{

	private static final UriTemplate VERSION_URI_TEMPLATE = UriTemplate.create("/rest/api/latest/project/{projectKey}/version");
	private static final String SEARCH_URL = "/rest/api/latest/search";
	private static final String APPLICATION_JSON = "application/json";
	private final URI baseUri;
	private final HttpTransport httpTransport;
	private final Gson gson;

	public JiraHelper(
			URI baseUri,
			HttpConfiguration.Credentials credentials)
	{
		this(baseUri, new CommonsHttpTransport(builder().credentials(some(credentials)).build(), baseUri));
	}

	private JiraHelper(
			URI baseUri,
			HttpTransport httpTransport)
	{
		URI norm = baseUri.normalize();
		this.baseUri = norm.getPath().endsWith("/") ? norm : URI.create(norm + "/");
		this.httpTransport = httpTransport;
		gson = new GsonBuilder().disableHtmlEscaping().create();
	}

	@Override
	public void close()
	{
		try
		{
			httpTransport.close();
		}
		catch (IOException ignore)
		{
		}
	}

	public URI getBaseUri()
	{
		return baseUri;
	}

	public Optional<JiraVersion> getProjectVersion(
			String projectKey,
			String versionName)
	{
		URI versionUri = UriBuilder.fromUri(VERSION_URI_TEMPLATE.resolve(Map.of("projectKey", projectKey)))
				.queryParam("query", versionName)
				.build();
		return getProjectVersion(projectKey, versionName, versionUri);
	}

	private Optional<JiraVersion> getProjectVersion(
			String projectKey,
			String versionName,
			URI versionUri)
	{
		try (SimpleHttpResponse response = httpTransport.get(resolveLink(versionUri)))
		{
			if (response.getStatus() == 204)
			{
				return Optional.empty();
			}
			else if (response.getStatus() >= 400)
			{
				throw new JiraException("Failed to get version " + versionName + " of project " + projectKey,
				                        decode(response, ErrorMessages.class));
			}
			else
			{
				PagedJiraVersions pagedJiraVersions = decode(response, PagedJiraVersions.class);
				Optional<JiraVersion> first = pagedJiraVersions.getValues()
						.stream()
						.filter(version -> Objects.equals(version.getName(), versionName))
						.findFirst();
				if (first.isPresent())
				{
					return first;
				}
				else if (!pagedJiraVersions.isLast() && pagedJiraVersions.getNextPage() != null)
				{
					return getProjectVersion(projectKey, versionName, pagedJiraVersions.getNextPage());
				}
				else
				{
					return Optional.empty();
				}
			}
		}
		catch (MpacException e)
		{
			throw new JiraException(e);
		}
	}

	public SearchResults searchIssues(SearchRequest request)
	{
		URI searchURI = UriBuilder.fromUri(SEARCH_URL).build();

		byte[] bytes = encode(request);

		try (SimpleHttpResponse response = httpTransport.post(resolveLink(searchURI), new ByteArrayInputStream(bytes), bytes.length,
		                                                      APPLICATION_JSON, APPLICATION_JSON, Optional.empty()))
		{
			if (response.getStatus() == 204 || response.getStatus() >= 400)
			{
				throw new JiraException("Failed to query Jira for issues using jql: " + request.getJql(),
				                        decode(response, ErrorMessages.class));
			}
			return decode(response, SearchResults.class);
		}
		catch (MpacException e)
		{
			throw new JiraException(e);
		}
	}

	private <T> byte[] encode(T entity)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (OutputStreamWriter writer = new OutputStreamWriter(bos))
		{
			gson.toJson(entity, writer);
		}
		catch (Exception e)
		{
			throw new JiraException(e.getMessage(), e);
		}
		return bos.toByteArray();
	}

	private <T> T decode(
			SimpleHttpResponse response,
			Class<T> type)
	{
		try (InputStream stream = response.getContentStream();
		     Reader reader = new InputStreamReader(stream))
		{
			return gson.fromJson(reader, type);
		}
		catch (MpacException e)
		{
			throw new JiraException(e);
		}
		catch (IOException e)
		{
			throw new JiraException(e.getMessage(), e);
		}
	}

	private URI resolveLink(URI href)
	{
		return href.isAbsolute() ? href : baseUri.resolve(href.toString());
	}

	public static class JiraException
			extends RuntimeException
	{

		private final ErrorMessages errors;

		public JiraException(String message)
		{
			this(message, (ErrorMessages) null);
		}

		public JiraException(
				String message,
				Throwable cause)
		{
			super(message, cause);
			errors = null;
		}

		public JiraException(MpacException cause)
		{
			super(cause.getMessage(), cause.getCause());
			errors = null;
		}


		public JiraException(
				String message,
				ErrorMessages errors)
		{
			super(message);
			this.errors = errors;
		}

		@Override
		public String getMessage()
		{
			if (errors == null)
			{
				return super.getMessage();
			}
			else
			{
				return super.getMessage() + " " + errors;
			}
		}
	}
}
