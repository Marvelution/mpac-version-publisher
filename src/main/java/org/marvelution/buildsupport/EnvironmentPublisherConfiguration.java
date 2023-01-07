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
package org.marvelution.buildsupport;

import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import com.atlassian.marketplace.client.api.*;
import com.atlassian.marketplace.client.model.*;
import org.apache.commons.lang3.*;

import static org.marvelution.buildsupport.Variables.*;

/**
 * Pipe Configuration holder.
 *
 * @author Mark Rekveld
 */
public class EnvironmentPublisherConfiguration
		implements PublisherConfiguration
{

	private final UnaryOperator<String> operator;
	private Path workdir;

	public EnvironmentPublisherConfiguration()
	{
		this(System::getenv);
	}

	public EnvironmentPublisherConfiguration(UnaryOperator<String> operator)
	{
		this.operator = operator;
	}

	public EnvironmentPublisherConfiguration(
			UnaryOperator<String> operator,
			Path workdir)
	{
		this.operator = operator;
		this.workdir = workdir;
	}

	@Override
	public Path getWorkingDirectory()
	{
		if (workdir != null)
		{
			return workdir;
		}
		else
		{
			return Paths.get("").toAbsolutePath();
		}
	}

	@Override
	public Optional<URI> getMarketplaceBaseUrl()
	{
		return getUri(Variables.MARKETPLACE_BASE_URL);
	}

	@Override
	public String getMarketplaceUsername()
	{
		return getVariable(Variables.MARKETPLACE_USER).orElseThrow(() -> new IllegalArgumentException("Missing marketplace username"));
	}

	@Override
	public String getMarketplaceToken()
	{
		return getVariable(MARKETPLACE_TOKEN).orElseThrow(() -> new IllegalArgumentException("Missing marketplace token"));
	}

	@Override
	public Optional<URI> getJiraBaseUrl()
	{
		return getUri(Variables.JIRA_BASE_URL);
	}

	@Override
	public String getJiraUsername()
	{
		return getVariable(JIRA_API_USER).orElseThrow(() -> new IllegalArgumentException("Missing jira username"));
	}

	@Override
	public String getJiraToken()
	{
		return getVariable(JIRA_API_TOKEN).orElseThrow(() -> new IllegalArgumentException("Missing jira token"));
	}

	@Override
	public String getJiraProjectKey()
	{
		return getVariable(JIRA_PROJECT_KEY).orElseThrow(() -> new IllegalArgumentException("Missing Jira project key"));
	}

	@Override
	public Optional<String> getJiraVersionFormat()
	{
		return getVariable(JIRA_VERSION_FORMAT);
	}

	@Override
	public Optional<String> getAdditionalJQL()
	{
		return getVariable(ADDITIONAL_JQL);
	}

	@Override
	public boolean useIssueSecurityFilter()
	{
		return getVariable(ISSUE_SECURITY_LEVEL_FILTER).map(Boolean::parseBoolean).orElse(false);
	}

	@Override
	public Optional<AddonVersionStatus> getVersionStatus()
	{
		return getEnum(VERSION_STATUS, AddonVersionStatus.class);
	}

	@Override
	public Optional<PaymentModel> getPaymentModel()
	{
		return getEnum(VERSION_PAYMENT_MODEL, PaymentModel.class);
	}

	@Override
	public String getVersionArtifactPath()
	{
		return getVariable(VERSION_ARTIFACT).orElseThrow(() -> new IllegalArgumentException("missing version artifact path"));
	}

	private Optional<URI> getUri(String key)
	{
		return getVariable(key).map(URI::create);
	}

	private <E extends Enum<E>> Optional<E> getEnum(
			String key,
			Class<E> type)
	{
		E[] values = type.getEnumConstants();
		return getVariable(key).flatMap(v -> Stream.of(values)
				.filter(e -> e.name().equalsIgnoreCase(v) || (e instanceof EnumWithKey && ((EnumWithKey) e).getKey().equalsIgnoreCase(v)))
				.findFirst());
	}

	private Optional<String> getVariable(String key)
	{
		return Optional.ofNullable(operator.apply(key)).filter(StringUtils::isNotBlank);
	}
}
