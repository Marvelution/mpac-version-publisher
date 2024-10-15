package org.marvelution.buildsupport;

import java.net.URI;
import java.util.Optional;

import org.marvelution.buildsupport.configuration.PublisherConfiguration;
import org.marvelution.buildsupport.configuration.PublisherConfigurationBuilder;
import org.marvelution.buildsupport.helper.MarketplaceHelper;
import org.marvelution.buildsupport.helper.ReleaseNotesHelper;
import org.marvelution.buildsupport.helper.Selector;
import org.marvelution.buildsupport.model.AppDetails;
import org.marvelution.buildsupport.model.ReleaseDetails;

import com.atlassian.marketplace.client.model.*;
import io.atlassian.fugue.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.marketplace.client.model.AddonVersionStatus.PUBLIC;
import static org.marvelution.buildsupport.helper.AppDetailsHelper.parseAppArtifact;

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
                ReleaseDetails releaseDetails = new ReleaseDetails(appDetails.getVersion()).setStatus(configuration.getVersionStatus()
                                .orElse(PUBLIC))
                        .setPaymentModel(configuration.getPaymentModel()
                                .orElse(PaymentModel.FREE))
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
                String versionName = newVersion.getName()
                        .getOrNull();
                LOGGER.info("Published version {} (build #{}) at: {}",
                        versionName,
                        newVersion.getBuildNumber(),
                        marketplace.getBaseUri()
                                .resolve(versionUrl));
            }
        }
    }

    URI resolveAddonArtifact()
    {
        Selector selector = new Selector(configuration.getWorkingDirectory(), configuration.getVersionArtifactPath());
        return selector.requireUnique()
                .toUri();
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
}
