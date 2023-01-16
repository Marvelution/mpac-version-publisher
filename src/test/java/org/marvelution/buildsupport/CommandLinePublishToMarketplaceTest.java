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

import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import javax.annotation.*;

import org.marvelution.buildsupport.configuration.*;

import com.atlassian.marketplace.client.model.*;

/**
 * Tests for {@link PublishToMarketplace} using {@link CommandLinePublisherConfiguration}.
 *
 * @author Mark Rekveld
 */
class CommandLinePublishToMarketplaceTest
		extends BaseTestPublishToMarketplace
{


	@Override
	PublisherConfiguration createConfiguration(
			@Nullable String marketplaceUrl,
			@Nullable String marketplaceUser,
			@Nullable String marketplaceToken,
			String versionArtifact,
			@Nullable String releaseNotesPath,
			@Nullable AddonVersionStatus versionStatus,
			@Nullable PaymentModel paymentModel,
			@Nullable String jiraUrl,
			@Nullable String jiraUser,
			@Nullable String jiraToken,
			@Nullable String project,
			@Nullable String versionFormat,
			boolean useIssueSecurity,
			@Nullable String additionalJql,
			Path workDir)
	{
		List<String> args = new ArrayList<>();

		args.add("-m");
		args.add(String.valueOf(marketplaceUrl));
		args.add("-mu");
		args.add(String.valueOf(marketplaceUser));
		args.add("-mt");
		args.add(String.valueOf(marketplaceToken));
		addArgument(args, "vap", versionArtifact);
		addArgument(args, "rnp", releaseNotesPath);
		addArgument(args, "vs", versionStatus, AddonVersionStatus::getKey);
		addArgument(args, "pm", paymentModel, PaymentModel::getKey);
		addArgument(args, "j", jiraUrl);
		addArgument(args, "ju", jiraUser);
		addArgument(args, "jt", jiraToken);
		addArgument(args, "jpk", project);
		addArgument(args, "jvf", versionFormat);
		addArgument(args, "jql", additionalJql);
		if (useIssueSecurity)
		{
			args.add("-isf");
		}
		args.add("-w");
		args.add(workDir.toString());
		args.add("-D");

		return new CommandLinePublisherConfiguration(args.toArray(String[]::new));
	}

	void addArgument(
			List<String> args,
			String option,
			String value)
	{
		Optional.ofNullable(value).ifPresent(o -> {
			args.add("-" + option);
			args.add(value);
		});
	}

	<T> void addArgument(
			List<String> args,
			String option,
			T value,
			Function<T, String> valueTransformer)
	{
		Optional.ofNullable(value).ifPresent(o -> addArgument(args, option, valueTransformer.apply(value)));
	}
}
