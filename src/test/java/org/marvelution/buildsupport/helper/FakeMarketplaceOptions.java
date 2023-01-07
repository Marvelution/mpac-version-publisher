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
import java.nio.file.*;
import java.util.*;

import org.marvelution.testing.wiremock.*;

import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.*;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.*;
import com.github.tomakehurst.wiremock.extension.*;
import com.github.tomakehurst.wiremock.extension.responsetemplating.*;
import com.github.tomakehurst.wiremock.http.*;
import org.apache.commons.text.*;

/**
 * {@link WireMockServer} options for a preconfigured fake Marketplace instance.
 *
 * @author Mark Rekveld
 */
public class FakeMarketplaceOptions
		extends WireMockConfiguration
{

	public FakeMarketplaceOptions()
	{
		dynamicPort();
		try
		{
			usingFilesUnderDirectory(
					Paths.get(Optional.ofNullable(getClass().getClassLoader().getResource("fake-marketplace")).orElseThrow().toURI())
							.toString());
		}
		catch (URISyntaxException e)
		{
			throw new IllegalStateException("unable to locate fake-marketplace WireMock files", e);
		}
		extensions(new ResponseTemplateTransformer(false, "escape", new QuoteEscaperHelper()), new ResponseAsMappingTransformer(),
		           new BasicAuthenticationRequestFilter());
		notifier(new LoggingNotifier("FakeMarketplace"));
	}

	/**
	 * {@link ResponseDefinitionTransformer} that registers a new stub mapping when a new version is published.
	 */
	private static class ResponseAsMappingTransformer
			extends ResponseDefinitionTransformer
	{

		@Override
		public String getName()
		{
			return "response-as-mapping";
		}

		@Override
		public ResponseDefinition transform(
				Request request,
				ResponseDefinition responseDefinition,
				FileSource files,
				Parameters parameters)
		{
			HttpHeader location = responseDefinition.getHeaders().getHeader("Location");
			HttpHeader mappingFile = responseDefinition.getHeaders().getHeader("MappingFile");
			if (location.isPresent() && mappingFile.isPresent())
			{
				files.writeBinaryFile(mappingFile.firstValue(), responseDefinition.getByteBody());
			}
			return responseDefinition;
		}

		@Override
		public boolean applyGlobally()
		{
			return false;
		}
	}

	/**
	 * Simple {@link Helper} that escapes single and double quotes.
	 */
	private static class QuoteEscaperHelper
			implements Helper<Object>
	{

		@Override
		public Object apply(
				Object context,
				Options options)
				throws IOException
		{
			return StringEscapeUtils.escapeJava(options.fn(context).toString());
		}
	}
}
