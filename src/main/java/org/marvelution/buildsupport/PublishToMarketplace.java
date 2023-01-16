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
import java.util.*;

import org.marvelution.buildsupport.configuration.*;
import org.marvelution.buildsupport.helper.*;
import org.marvelution.buildsupport.model.*;

import com.atlassian.marketplace.client.model.*;
import io.atlassian.fugue.*;
import org.slf4j.*;

import static org.marvelution.buildsupport.helper.AppDetailsHelper.*;

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
			new PublishToMarketplace(PublisherConfigurationBuilder.build(args)).run();
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
		     ReleaseNotesHelper releaseNotesHelper = new ReleaseNotesHelper(configuration))
		{
			URI addonArtifact = resolveAddonArtifact();
			LOGGER.info("Resolved app artifact {}", addonArtifact);
			AppDetails appDetails = parseAppArtifact(addonArtifact);

			Optional<Addon> addon = marketplace.getAddon(appDetails.getKey());
			if (addon.isEmpty())
			{
				LOGGER.error("Failed to locate app with key '{}'.", appDetails.getKey());
				System.exit(1);
			}
			else
			{
				LOGGER.info("Processing app artifact to collect release details...");
				ReleaseDetails releaseDetails = new ReleaseDetails(appDetails.getVersion()).setStatus(
								configuration.getVersionStatus().orElse(PUBLIC))
						.setPaymentModel(configuration.getPaymentModel().orElse(PaymentModel.FREE))
						.setPublisher(addon.map(Addon::getVendor)
								              .flatMap(Option::toOptional)
								              .map(VendorBase::getName)
								              .orElseGet(appDetails::getVendorName));

				releaseNotesHelper.populateReleaseSummaryAndNotes(releaseDetails);

				AddonVersion newVersion = marketplace.publishVersion(appDetails, releaseDetails, addonArtifact);

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

	URI resolveAddonArtifact()
	{
		Selector selector = new Selector(configuration.getWorkingDirectory(), configuration.getVersionArtifactPath());
		return selector.requireUnique().toUri();
	}
}
