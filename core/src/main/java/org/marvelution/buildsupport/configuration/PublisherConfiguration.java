package org.marvelution.buildsupport.configuration;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

import com.atlassian.marketplace.client.model.AddonVersionStatus;
import com.atlassian.marketplace.client.model.PaymentModel;

/**
 * Pipe Configuration holder.
 *
 * @author Mark Rekveld
 */
public interface PublisherConfiguration
{

    Path getWorkingDirectory();

    Optional<URI> getMarketplaceBaseUrl();

    String getMarketplaceUsername();

    String getMarketplaceToken();

    Optional<AddonVersionStatus> getVersionStatus();

    Optional<PaymentModel> getPaymentModel();

    String getVersionArtifactPath();

    boolean dryRun();

    Optional<String> getReleaseNotesPath();

    Optional<URI> getJiraBaseUrl();

    String getJiraUsername();

    String getJiraToken();

    String getJiraProjectKey();

    Optional<String> getJiraVersionFormat();

    Optional<String> getAdditionalJQL();

    boolean useIssueSecurityFilter();
}
