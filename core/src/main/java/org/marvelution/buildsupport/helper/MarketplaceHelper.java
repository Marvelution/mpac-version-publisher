package org.marvelution.buildsupport.helper;

import java.io.Closeable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.marvelution.buildsupport.configuration.PublisherConfiguration;
import org.marvelution.buildsupport.model.AppDetails;
import org.marvelution.buildsupport.model.AppType;
import org.marvelution.buildsupport.model.ReleaseDetails;

import com.atlassian.marketplace.client.MarketplaceClient;
import com.atlassian.marketplace.client.MpacException;
import com.atlassian.marketplace.client.api.*;
import com.atlassian.marketplace.client.impl.CommonsHttpTransport;
import com.atlassian.marketplace.client.impl.DefaultMarketplaceClient;
import com.atlassian.marketplace.client.model.*;
import com.atlassian.plugin.marketing.bean.ProductCompatibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.marketplace.client.MarketplaceClientFactory.DEFAULT_MARKETPLACE_URI;
import static com.atlassian.marketplace.client.api.AddonVersionSpecifier.buildNumber;
import static com.atlassian.marketplace.client.api.AddonVersionSpecifier.latest;
import static com.atlassian.marketplace.client.http.HttpConfiguration.Credentials;
import static com.atlassian.marketplace.client.http.HttpConfiguration.builder;
import static com.atlassian.marketplace.client.model.ModelBuilders.*;
import static io.atlassian.fugue.Option.option;
import static io.atlassian.fugue.Option.some;
import static java.util.stream.Collectors.toList;

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
    private final boolean dryRun;
    private final URI baseUri;
    private final String username;
    private final MarketplaceClient client;

    public MarketplaceHelper(PublisherConfiguration configuration)
    {
        dryRun = configuration.dryRun();
        baseUri = configuration.getMarketplaceBaseUrl()
                .orElse(DEFAULT_MARKETPLACE_URI);
        username = configuration.getMarketplaceUsername();
        client = new DefaultMarketplaceClient(baseUri,
                new CommonsHttpTransport(builder().credentials(some(new Credentials(username, configuration.getMarketplaceToken())))
                        .build(), baseUri),
                new PublisherEntityEncoding());
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
            return client.addons()
                    .safeGetByKey(addonKey,
                            AddonQuery.builder()
                                    .build());
        }
        catch (MpacException e)
        {
            throw handleMpacException("Failed to get addon for key: " + addonKey, e);
        }
    }

    public AddonVersion publishVersion(
            AppDetails appDetails,
            ReleaseDetails releaseDetails,
            URI addonArtifact)
    {
        try
        {
            LOGGER.info("Uploading {}", addonArtifact);
            ArtifactId addonArtifactId;
            Path addonArtifactPath = Path.of(addonArtifact);
            if (dryRun || !Files.isRegularFile(addonArtifactPath))
            {
                addonArtifactId = ArtifactId.fromUri(addonArtifact);
            }
            else
            {
                addonArtifactId = client.assets()
                        .uploadAddonArtifact(addonArtifactPath.toFile());
                LOGGER.debug("Uploaded {} to {}", addonArtifact, addonArtifactId);
            }

            LOGGER.info("Locating latest version to base new version on.");
            AddonVersion latest = getLatestAddonVersion(appDetails.getKey(), appDetails.getAppType());
            LOGGER.info("Found current latest version {} (#{})",
                    latest.getName()
                            .getOrElse("Unknown"),
                    latest.getDataCenterBuildNumber()
                            .getOr(latest::getBuildNumber));

            AddonVersion newVersion = createNewVersion(appDetails, releaseDetails, latest, addonArtifactId);
            LOGGER.info("Created version {} (#{}) for artifact {}",
                    releaseDetails.getVersion(),
                    newVersion.getDataCenterBuildNumber()
                            .getOr(latest::getBuildNumber),
                    addonArtifactId);
            if (dryRun)
            {
                LOGGER.warn("Skipping the publishing of {} to Marketplace", releaseDetails.getVersion());
                return newVersion;
            }
            else
            {
                LOGGER.info("Publishing version {} to the Marketplace", releaseDetails.getVersion());
                try
                {
                    return client.addons()
                            .createVersion(appDetails.getKey(), newVersion);
                }
                catch (MpacException e)
                {
                    LOGGER.warn("Initial create failed, checking if the version was created anyway.");

                    Optional<AddonVersion> addonVersion = client.addons()
                            .safeGetVersion(appDetails.getKey(),
                                    buildNumber(newVersion.getDataCenterBuildNumber()
                                            .getOr(latest::getBuildNumber)),
                                    AddonVersionsQuery.builder()
                                            .includePrivate(true)
                                            .build());
                    return addonVersion.orElseThrow(() -> e);
                }
            }
        }
        catch (MpacException e)
        {
            throw handleMpacException("Failed to publish version.", e);
        }
    }

    private AddonVersion getLatestAddonVersion(
            String addonKey,
            AppType appType)
            throws MpacException
    {
        for (HostingType hostingType : appType.hostingTypes())
        {
            Optional<AddonVersion> addonVersion = client.addons()
                    .safeGetVersion(addonKey,
                            latest(),
                            AddonVersionsQuery.builder()
                                    .hosting(Optional.of(hostingType))
                                    .build());
            if (addonVersion.isPresent())
            {
                return addonVersion.get();
            }
        }
        throw new IllegalStateException("No latest version to base the new version on.");
    }

    private AddonVersion createNewVersion(
            AppDetails appDetails,
            ReleaseDetails releaseDetails,
            AddonVersion latest,
            ArtifactId addonArtifactId)
            throws ModelBuilders.InvalidModelException
    {
        ModelBuilders.AddonVersionBuilder newVersion = addonVersion(latest).name(releaseDetails.getVersion())
                .artifact(option(addonArtifactId))
                .releaseDate(LocalDate.now())
                .releaseSummary(option(releaseDetails.getReleaseSummary()))
                .releaseNotes(option(releaseDetails.getReleaseNotes()).map(HtmlString::html))
                .releasedBy(option(releaseDetails.getPublisher()).orElse(some(username)))
                .status(releaseDetails.getStatus())
                .paymentModel(releaseDetails.getPaymentModel())
                .agreement(MARKETPLACE_AGREEMENT_URI);

        switch (appDetails.getAppType())
        {
            case CLOUD:
                newVersion.buildNumber(latest.getBuildNumber() + 1);
                break;
            case SERVER:
                newVersion.buildNumber(releaseDetails.getBuildNumber(HostingType.SERVER));
                break;
            case DATA_CENTER:
                newVersion.dataCenterBuildNumber(releaseDetails.getBuildNumber(HostingType.DATA_CENTER));
                break;
            case BOTH:
                newVersion.buildNumber(releaseDetails.getBuildNumber(HostingType.SERVER))
                        .dataCenterBuildNumber(releaseDetails.getBuildNumber(HostingType.DATA_CENTER));
                break;
        }

        List<VersionCompatibility> compatibilities = Optional.ofNullable(appDetails.getCompatibilities())
                .filter(list -> !list.isEmpty())
                .map(productCompatibilities -> getVersionCompatibilitiesFromAddonMarketing(appDetails.getAppType(),
                        productCompatibilities,
                        latest))
                .filter(list -> !list.isEmpty())
                .orElseGet(() -> getVersionCompatibilitiesFromLatestVersion(appDetails.getAppType(), latest));
        if (compatibilities.isEmpty())
        {
            throw new IllegalArgumentException("Unable to determine product compatibilities.");
        }
        newVersion.compatibilities(compatibilities);

        return newVersion.build();
    }

    private List<VersionCompatibility> getVersionCompatibilitiesFromAddonMarketing(
            AppType appType,
            List<ProductCompatibility> compatibilities,
            AddonVersion latest)
    {
        return compatibilities.stream()
                .map(productCompatibility -> {
                    ApplicationKey applicationKey = ApplicationKey.valueOf(productCompatibility.getProduct()
                            .name());
                    int minBuild = getBuildNumber(applicationKey, productCompatibility.getMin());
                    int maxBuild = Math.max(getBuildNumber(applicationKey, productCompatibility.getMax()),
                            StreamSupport.stream(latest.getCompatibilities()
                                            .spliterator(), false)
                                    .filter(vc -> Objects.equals(applicationKey, vc.getApplication()))
                                    .findFirst()
                                    .map(vc -> {
                                        if (vc.isDataCenterCompatible())
                                        {
                                            return vc.getDataCenterMaxBuild()
                                                    .getOrElse(0);
                                        }
                                        else if (vc.isServerCompatible())
                                        {
                                            return vc.getServerMaxBuild()
                                                    .getOrElse(0);
                                        }
                                        else
                                        {
                                            return 0;
                                        }
                                    })
                                    .orElse(0));

                    return getVersionCompatibility(appType, applicationKey, minBuild, maxBuild);
                })
                .collect(toList());
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
            AppType appType,
            AddonVersion latest)
    {
        List<VersionCompatibility> compatibilities = new ArrayList<>();
        latest.getCompatibilities()
                .forEach(versionCompatibility -> {
                    int minBuild = 0;
                    int maxBuild = 0;
                    if (versionCompatibility.isDataCenterCompatible())
                    {
                        minBuild = versionCompatibility.getDataCenterMinBuild()
                                .getOrElse(0);
                        maxBuild = versionCompatibility.getDataCenterMaxBuild()
                                .getOrElse(0);
                    }
                    else if (versionCompatibility.isServerCompatible())
                    {
                        minBuild = versionCompatibility.getServerMinBuild()
                                .getOrElse(0);
                        maxBuild = versionCompatibility.getServerMaxBuild()
                                .getOrElse(0);
                    }
                    if (minBuild != 0 && maxBuild != 0)
                    {
                        compatibilities.add(getVersionCompatibility(appType, versionCompatibility.getApplication(), minBuild, maxBuild));
                    }
                });
        return compatibilities;
    }

    private VersionCompatibility getVersionCompatibility(
            AppType appType,
            ApplicationKey applicationKey,
            int minBuild,
            int maxBuild)
    {
        switch (appType)
        {
            case SERVER:
                return versionCompatibilityForServer(applicationKey, minBuild, maxBuild);
            case DATA_CENTER:
                return versionCompatibilityForDataCenter(applicationKey, minBuild, maxBuild);
            case BOTH:
                return versionCompatibilityForServerAndDataCenter(applicationKey, minBuild, maxBuild, minBuild, maxBuild);
            default:
                throw new IllegalArgumentException("Unsupported plugin type " + appType);
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
                    .forEach(errorDetail -> LOGGER.error(" - {}; code: {}, path: {}",
                            errorDetail.getMessage(),
                            errorDetail.getCode()
                                    .getOrElse("unknown"),
                            errorDetail.getPath()
                                    .getOrElse("unknown")));
        }
        return new IllegalStateException(message, exception);
    }
}
