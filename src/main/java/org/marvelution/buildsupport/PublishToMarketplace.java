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

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.function.*;

import org.marvelution.buildsupport.helper.*;
import org.marvelution.buildsupport.model.*;

import com.atlassian.marketplace.client.model.*;
import com.atlassian.plugin.tool.*;
import io.atlassian.fugue.*;
import org.slf4j.*;

import static org.marvelution.buildsupport.helper.MarketplaceHelper.*;

import static com.atlassian.marketplace.client.model.AddonVersionStatus.*;

/**
 * Entry point of publish-marketplace-version pipe.
 *
 * @author Mark Rekveld
 */
public class PublishToMarketplace
{

	private static final Logger LOGGER = LoggerFactory.getLogger(PublishToMarketplace.class);
	private final PublisherConfiguration configuration;

	public PublishToMarketplace(PublisherConfiguration configuration)
	{
		this.configuration = configuration;
	}

	public static void main(String... args)
	{
		try
		{
			new PublishToMarketplace(new EnvironmentPublisherConfiguration()).run();
		}
		catch (IllegalArgumentException | IllegalStateException e)
		{
			LOGGER.error(e.getMessage());
			System.exit(1);
		}
		catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
			System.exit(1);
		}
	}

	public void run()
			throws Exception
	{
		try (MarketplaceHelper marketplace = new MarketplaceHelper(configuration);
		     ReleaseDetailsHelper releaseDetailsHelper = new ReleaseDetailsHelper(configuration))
		{
			File addonArtifact = resolveAddonArtifact();
			LOGGER.info("Resolved app artifact {}", addonArtifact.getName());
			PluginDetails addonDetails = parseAddonArtifact(addonArtifact);

			Optional<Addon> addon = marketplace.getAddon(addonDetails.getPluginBean().getKey());
			if (addon.isEmpty())
			{
				LOGGER.error("Failed to locate app with key '{}'.", addonDetails.getPluginBean().getKey());
				System.exit(1);
			}
			else
			{
				LOGGER.info("Processing app artifact to collect release details...");
				ReleaseDetails releaseDetails = new ReleaseDetails(addonDetails.getPluginBean().getPluginInfo().getVersion()).setStatus(
								configuration.getVersionStatus().orElse(PUBLIC))
						.setPaymentModel(configuration.getPaymentModel().orElse(PaymentModel.FREE))
						.setPublisher(addon.map(Addon::getVendor)
								              .flatMap(Option::toOptional)
								              .map(VendorBase::getName)
								              .orElseGet(() -> addonDetails.getPluginBean().getPluginInfo().getVendor().getName()));

				releaseDetailsHelper.populateReleaseSummaryAndNotes(releaseDetails);

				AddonVersion newVersion = marketplace.publishVersion(addonDetails, releaseDetails, addonArtifact);

				String versionUrl = newVersion.getLinks()
						.getLink("alternate", "text/html")
						.map(Link::getUri)
						.getOrElse(newVersion.getSelfUri())
						.toASCIIString();
				String versionName = newVersion.getName().getOrNull();
				LOGGER.info("Published version {} (build #{}) at: {}", versionName, newVersion.getBuildNumber(),
				            marketplace.getBaseUri().resolve(versionUrl));
			}
		}
	}

	File resolveAddonArtifact()
	{
		Path workdir = configuration.getWorkingDirectory();
		Predicate<Path> pathFilter;

		String versionArtifact = normalizePath(configuration.getVersionArtifactPath());
		if (versionArtifact.contains("*"))
		{
			LOGGER.info("Looking up version artifact using glob: {}", versionArtifact);
			PathMatcher pathMatcher = workdir.getFileSystem().getPathMatcher("glob:" + versionArtifact);
			pathFilter = pathMatcher::matches;
		}
		else
		{
			pathFilter = entry -> Objects.equals(workdir.relativize(entry).toString(), versionArtifact);
		}

		Set<Path> paths = new HashSet<>();
		try
		{
			Files.walkFileTree(workdir, new SimpleFileVisitor<>()
			{
				@Override
				public FileVisitResult visitFile(
						Path file,
						BasicFileAttributes attrs)
				{
					if (pathFilter.test(file))
					{
						paths.add(file);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Unable to walk trough directory " + workdir, e);
		}

		if (paths.size() == 1)
		{
			return paths.iterator().next().toFile();
		}
		else
		{
			throw new IllegalArgumentException("Unable to locate a single artifact using " + versionArtifact);
		}
	}

	private String normalizePath(String path)
	{
		return path.startsWith("/") ? path.substring(1) : path;
	}
}
