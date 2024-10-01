package org.marvelution.buildsupport;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.marvelution.buildsupport.configuration.EnvironmentPublisherConfiguration;
import org.marvelution.buildsupport.configuration.PublisherConfiguration;

import com.atlassian.marketplace.client.model.AddonVersionStatus;
import com.atlassian.marketplace.client.model.PaymentModel;

import static org.marvelution.buildsupport.configuration.EnvironmentPublisherConfiguration.*;

/**
 * Tests for {@link PublishToMarketplace} using {@link EnvironmentPublisherConfiguration}.
 *
 * @author Mark Rekveld
 */
class EnvironmentVariablesPublishToMarketplaceTest
        extends BaseTestPublishToMarketplace
{

    @Override
    PublisherConfiguration createConfiguration(
            @Nullable
            String marketplaceUrl,
            @Nullable
            String marketplaceUser,
            @Nullable
            String marketplaceToken,
            String versionArtifact,
            @Nullable
            String releaseNotesPath,
            @Nullable
            AddonVersionStatus versionStatus,
            @Nullable
            PaymentModel paymentModel,
            @Nullable
            String jiraUrl,
            @Nullable
            String jiraUser,
            @Nullable
            String jiraToken,
            @Nullable
            String project,
            @Nullable
            String versionFormat,
            boolean useIssueSecurity,
            @Nullable
            String additionalJql,
            Path workDir)
    {
        Map<String, String> configuration = new HashMap<>();

        configuration.put(MARKETPLACE_BASE_URL, marketplaceUrl);
        configuration.put(MARKETPLACE_USER, marketplaceUser);
        configuration.put(MARKETPLACE_TOKEN, marketplaceToken);
        configuration.put(VERSION_ARTIFACT, versionArtifact);
        configuration.put(RELEASE_NOTES_PATH, releaseNotesPath);
        if (versionStatus != null)
        {
            configuration.put(VERSION_STATUS, versionStatus.getKey());
        }
        if (paymentModel != null)
        {
            configuration.put(VERSION_PAYMENT_MODEL, paymentModel.getKey());
        }
        configuration.put(JIRA_BASE_URL, jiraUrl);
        configuration.put(JIRA_API_USER, jiraUser);
        configuration.put(JIRA_API_TOKEN, jiraToken);
        configuration.put(JIRA_PROJECT_KEY, project);
        configuration.put(JIRA_VERSION_FORMAT, versionFormat);
        configuration.put(ISSUE_SECURITY_LEVEL_FILTER, Boolean.toString(useIssueSecurity));
        configuration.put(ADDITIONAL_JQL, additionalJql);
        configuration.put(DEBUG, "true");

        return new EnvironmentPublisherConfiguration(configuration::get, workDir);
    }
}
