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
import java.util.stream.*;
import java.util.zip.*;
import javax.annotation.*;

import org.marvelution.buildsupport.configuration.*;
import org.marvelution.buildsupport.model.*;

import com.atlassian.marketplace.client.*;
import com.atlassian.marketplace.client.api.*;
import com.atlassian.marketplace.client.impl.*;
import com.atlassian.marketplace.client.model.*;
import com.atlassian.plugin.marketing.bean.*;
import com.atlassian.plugin.tool.*;
import com.atlassian.plugin.tool.bean.*;
import org.apache.commons.io.*;
import org.joda.time.*;
import org.slf4j.*;

import static com.atlassian.marketplace.client.MarketplaceClientFactory.*;
import static com.atlassian.marketplace.client.api.AddonVersionSpecifier.*;
import static com.atlassian.marketplace.client.http.HttpConfiguration.*;
import static com.atlassian.marketplace.client.model.ModelBuilders.*;
import static com.atlassian.plugin.tool.PluginInfoTool.*;
import static io.atlassian.fugue.Option.*;
import static java.util.stream.Collectors.*;

/**
 * Helper for interacting with the Atlassian Marketplace.
 *
 * @author Mark Rekveld
 */
public class MarketplaceHelper
		implements Closeable
{

	public static final URI MARKETPLACE_AGREEMENT_URI = URI.create("http://www.atlassian.com/licensing/marketplace/publisheragreement");
	private static final Logger LOGGER = LoggerFactory.getLogger(MarketplaceHelper.class);
	private static final Set<String> SUPPORTED_ADDON_ARTIFACT_EXTENSIONS = Stream.of("jar", "obr").collect(toSet());
	private static final String PLUGIN_TYPE = "plugin-type";
	private final boolean dryRun;
	private final URI baseUri;
	private final String username;
	private final MarketplaceClient client;

	public MarketplaceHelper(PublisherConfiguration configuration)
	{
		dryRun = configuration.dryRun();
		baseUri = configuration.getMarketplaceBaseUrl().orElse(DEFAULT_MARKETPLACE_URI);
		username = configuration.getMarketplaceUsername();
		client = new DefaultMarketplaceClient(baseUri, new CommonsHttpTransport(
				builder().credentials(some(new Credentials(username, configuration.getMarketplaceToken()))).build(), baseUri),
		                                      new PublisherEntityEncoding());
	}

	public static PluginDetails parseAddonArtifact(File addonArtifact)
			throws IOException
	{
		String filename = addonArtifact.getName().toLowerCase(Locale.ENGLISH);
		try (InputStream inputStream = Files.newInputStream(addonArtifact.toPath(), StandardOpenOption.READ))
		{
			if (filename.endsWith(".jar"))
			{
				return getPluginDetailsFromJar(inputStream);
			}
			else if (filename.endsWith(".obr"))
			{
				ZipInputStream obr = new ZipInputStream(inputStream);

				ZipEntry entry;
				do
				{
					if ((entry = obr.getNextEntry()) == null)
					{
						throw new IllegalArgumentException("obr file does not contain an valid jar file");
					}
				}
				while (entry.getName().contains("/") || !entry.getName().toLowerCase().endsWith(".jar"));

				return getPluginDetailsFromJar(obr);
			}
			else
			{
				throw new IllegalArgumentException("Unsupported addon artifact.");
			}
		}
	}

	@Override
	public void close()
	{
		client.close();
	}

	public URI getBaseUri()
	{
		return baseUri;
	}

	public Optional<Addon> getAddon(String addonKey)
	{
		try
		{
			return client.addons().safeGetByKey(addonKey, AddonQuery.builder().build());
		}
		catch (MpacException e)
		{
			throw handleMpacException("Failed to get addon for key: " + addonKey, e);
		}
	}

	public AddonVersion publishVersion(
			PluginDetails addonDetails,
			ReleaseDetails releaseDetails,
			File addonArtifact)
	{
		String extension = FilenameUtils.getExtension(addonArtifact.getName());
		if (!SUPPORTED_ADDON_ARTIFACT_EXTENSIONS.contains(extension))
		{
			throw new IllegalArgumentException("Unsupported addon artifact extension '" + extension + "'.");
		}
		try
		{
			PluginType pluginType = Optional.of(addonDetails)
					.map(PluginDetails::getPluginBean)
					.map(PluginConfigurationBean::getPluginInfo)
					.map(PluginInfoBean::getParameters)
					.orElse(Collections.emptyList())
					.stream()
					.filter(param -> PLUGIN_TYPE.equals(param.getName()))
					.findFirst()
					.map(PluginType::fromParam)
					.orElse(PluginType.SERVER);

			LOGGER.info("Uploading {}", addonArtifact);
			ArtifactId addonArtifactId;
			if (dryRun)
			{
				addonArtifactId = ArtifactId.fromUri(addonArtifact.toURI());
			}
			else
			{
				addonArtifactId = client.assets().uploadAddonArtifact(addonArtifact);
				LOGGER.debug("Uploaded {} to {}", addonArtifact, addonArtifactId);
			}

			LOGGER.info("Locating latest version to base new version on.");
			AddonVersion latest = getLatestAddonVersion(addonDetails.getPluginBean().getKey(), pluginType);
			LOGGER.info("Found current latest version {} (#{})", latest.getName().getOrElse("Unknown"), latest.getBuildNumber());

			AddonVersion newVersion = createNewVersion(releaseDetails, latest, addonArtifactId, pluginType,
			                                           addonDetails.getMarketingBean());
			LOGGER.info("Created version {} (#{}) for artifact {}", releaseDetails.getVersion(), newVersion.getBuildNumber(),
			            addonArtifactId);
			if (dryRun)
			{
				LOGGER.warn("Skipping the publishing of {} to Marketplace", releaseDetails.getVersion());
				return newVersion;
			}
			else
			{
				LOGGER.info("Publishing version {} to the Marketplace", releaseDetails.getVersion());
				return client.addons().createVersion(addonDetails.getPluginBean().getKey(), newVersion);
			}
		}
		catch (MpacException e)
		{
			throw handleMpacException("Failed to publish version.", e);
		}
	}

	private AddonVersion getLatestAddonVersion(
			String addonKey,
			PluginType pluginType)
			throws MpacException
	{
		for (HostingType hostingType : pluginType.hostingTypes())
		{
			Optional<AddonVersion> addonVersion = client.addons()
					.safeGetVersion(addonKey, latest(), AddonVersionsQuery.builder().hosting(Optional.of(hostingType)).build());
			if (addonVersion.isPresent())
			{
				return addonVersion.get();
			}
		}
		throw new IllegalStateException("No latest version to base the new version on.");
	}

	private AddonVersion createNewVersion(
			ReleaseDetails releaseDetails,
			AddonVersion latest,
			ArtifactId addonArtifactId,
			PluginType pluginType,
			@Nullable PluginMarketing pluginMarketing)
			throws ModelBuilders.InvalidModelException
	{
		ModelBuilders.AddonVersionBuilder newVersion = addonVersion(latest).name(releaseDetails.getVersion())
				.buildNumber(releaseDetails.getBuildNumber(HostingType.SERVER))
				.artifact(option(addonArtifactId))
				.releaseDate(LocalDate.now())
				.releaseSummary(option(releaseDetails.getReleaseSummary()))
				.releaseNotes(option(releaseDetails.getReleaseNotes()).map(HtmlString::html))
				.releasedBy(option(releaseDetails.getPublisher()).orElse(some(username)))
				.status(releaseDetails.getStatus())
				.paymentModel(releaseDetails.getPaymentModel())
				.agreement(MARKETPLACE_AGREEMENT_URI);

		if (PluginType.DATA_CENTER == pluginType || PluginType.BOTH == pluginType)
		{
			newVersion.dataCenterBuildNumber(releaseDetails.getBuildNumber(HostingType.DATA_CENTER));
		}

		List<VersionCompatibility> compatibilities = Optional.ofNullable(pluginMarketing)
				.map(marketing -> getVersionCompatibilitiesFromAddonMarketing(pluginType, pluginMarketing, latest))
				.orElseGet(() -> getVersionCompatibilitiesFromLatestVersion(pluginType, latest));
		if (compatibilities.isEmpty())
		{
			throw new IllegalArgumentException("Unable to determine product compatibilities.");
		}
		newVersion.compatibilities(compatibilities);

		return newVersion.build();
	}

	private List<VersionCompatibility> getVersionCompatibilitiesFromAddonMarketing(
			PluginType pluginType,
			PluginMarketing pluginMarketing,
			AddonVersion latest)
	{
		return pluginMarketing.getCompatibility().stream().map(productCompatibility -> {
			ApplicationKey applicationKey = ApplicationKey.valueOf(productCompatibility.getProduct().name());
			int minBuild = getBuildNumber(applicationKey, productCompatibility.getMin());
			int maxBuild = Math.max(getBuildNumber(applicationKey, productCompatibility.getMax()),
			                        StreamSupport.stream(latest.getCompatibilities().spliterator(), false)
					                        .filter(vc -> Objects.equals(applicationKey, vc.getApplication()))
					                        .findFirst()
					                        .map(vc -> {
						                        if (vc.isDataCenterCompatible())
						                        {
							                        return vc.getDataCenterMaxBuild().getOrElse(0);
						                        }
						                        else if (vc.isServerCompatible())
						                        {
							                        return vc.getServerMaxBuild().getOrElse(0);
						                        }
						                        else
						                        {
							                        return 0;
						                        }
					                        })
					                        .orElse(0));

			return getVersionCompatibility(pluginType, applicationKey, minBuild, maxBuild);
		}).collect(toList());
	}

	private int getBuildNumber(
			ApplicationKey applicationKey,
			String version)
	{
		try
		{
			ApplicationVersionSpecifier versionSpecifier;
			try
			{
				versionSpecifier = ApplicationVersionSpecifier.buildNumber(Integer.parseInt(version));
			}
			catch (NumberFormatException e)
			{
				versionSpecifier = ApplicationVersionSpecifier.versionName(version);
			}
			return client.applications()
					.safeGetVersion(applicationKey, versionSpecifier)
					.map(ApplicationVersion::getBuildNumber)
					.orElseThrow(() -> new IllegalStateException(
							"Unable to determine build number for " + applicationKey.getKey() + " version " + version));
		}
		catch (MpacException e)
		{
			throw handleMpacException(
					"Failed to determine build number for " + applicationKey.getKey() + " version " + version + "; " + e.getMessage(), e);
		}
	}

	private List<VersionCompatibility> getVersionCompatibilitiesFromLatestVersion(
			PluginType pluginType,
			AddonVersion latest)
	{
		List<VersionCompatibility> compatibilities = new ArrayList<>();
		latest.getCompatibilities().forEach(versionCompatibility -> {
			int minBuild = 0;
			int maxBuild = 0;
			if (versionCompatibility.isDataCenterCompatible())
			{
				minBuild = versionCompatibility.getDataCenterMinBuild().getOrElse(0);
				maxBuild = versionCompatibility.getDataCenterMaxBuild().getOrElse(0);
			}
			else if (versionCompatibility.isServerCompatible())
			{
				minBuild = versionCompatibility.getServerMinBuild().getOrElse(0);
				maxBuild = versionCompatibility.getServerMaxBuild().getOrElse(0);
			}
			if (minBuild != 0 && maxBuild != 0)
			{
				compatibilities.add(getVersionCompatibility(pluginType, versionCompatibility.getApplication(), minBuild, maxBuild));
			}
		});
		return compatibilities;
	}

	private VersionCompatibility getVersionCompatibility(
			PluginType pluginType,
			ApplicationKey applicationKey,
			int minBuild,
			int maxBuild)
	{
		switch (pluginType)
		{
			case SERVER:
				return versionCompatibilityForServer(applicationKey, minBuild, maxBuild);
			case DATA_CENTER:
				return versionCompatibilityForDataCenter(applicationKey, minBuild, maxBuild);
			case BOTH:
				return versionCompatibilityForServerAndDataCenter(applicationKey, minBuild, maxBuild, minBuild, maxBuild);
			default:
				throw new IllegalArgumentException("Unsupported plugin type " + pluginType);
		}
	}

	private RuntimeException handleMpacException(
			String message,
			MpacException exception)
	{
		LOGGER.error(message);
		if (exception instanceof MpacException.ServerError)
		{
			LOGGER.error("Server Error [{}]: {}", ((MpacException.ServerError) exception).getStatus(), exception.getMessage());
			((MpacException.ServerError) exception).getErrorDetails()
					.forEach(errorDetail -> LOGGER.error(" - {}; code: {}, path: {}", errorDetail.getMessage(),
					                                     errorDetail.getCode().getOrElse("unknown"),
					                                     errorDetail.getPath().getOrElse("unknown")));
		}
		return new IllegalStateException(message, exception);
	}
}
