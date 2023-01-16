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
package org.marvelution.buildsupport.configuration;

import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import com.atlassian.marketplace.client.api.*;
import com.atlassian.marketplace.client.model.*;
import org.apache.commons.lang3.*;

import static org.marvelution.buildsupport.configuration.Variables.*;

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
		return getRequiredVariable(Variables.MARKETPLACE_USER);
	}

	@Override
	public String getMarketplaceToken()
	{
		return getRequiredVariable(MARKETPLACE_TOKEN);
	}

	@Override
	public Optional<URI> getJiraBaseUrl()
	{
		return getUri(Variables.JIRA_BASE_URL);
	}

	@Override
	public String getJiraUsername()
	{
		return getRequiredVariable(JIRA_API_USER);
	}

	@Override
	public String getJiraToken()
	{
		return getRequiredVariable(JIRA_API_TOKEN);
	}

	@Override
	public String getJiraProjectKey()
	{
		return getRequiredVariable(JIRA_PROJECT_KEY);
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
		return getRequiredVariable(VERSION_ARTIFACT);
	}

	@Override
	public boolean dryRun()
	{
		return getVariable(DRY_RUN).map(Boolean::parseBoolean).orElse(false);
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

	private String getRequiredVariable(String key)
	{
		return getVariable(key).orElseThrow(() -> new IllegalArgumentException("Missing " + key + " variable"));
	}
}
