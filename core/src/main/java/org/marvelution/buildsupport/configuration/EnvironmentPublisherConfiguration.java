package org.marvelution.buildsupport.configuration;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.marvelution.buildsupport.logging.LoggingConfigurator;

import com.atlassian.marketplace.client.api.EnumWithKey;
import com.atlassian.marketplace.client.model.AddonVersionStatus;
import com.atlassian.marketplace.client.model.PaymentModel;
import org.apache.commons.lang3.StringUtils;

/**
 * Pipe Configuration holder.
 *
 * @author Mark Rekveld
 */
public class EnvironmentPublisherConfiguration
        implements PublisherConfiguration
{
    public static final String DEBUG = "DEBUG";
    public static final String DRY_RUN = "DRY_RUN";
    public static final String MARKETPLACE = "MARKETPLACE";
    public static final String VERSION_ARTIFACT = "VERSION_ARTIFACT";
    public static final String RELEASE_NOTES_PATH = "RELEASE_NOTES_PATH";
    public static final String VERSION_STATUS = "VERSION_STATUS";
    public static final String VERSION_PAYMENT_MODEL = "VERSION_PAYMENT_MODEL";
    public static final String JIRA_API = "JIRA_API";
    public static final String JIRA_PROJECT_KEY = "JIRA_PROJECT_KEY";
    public static final String JIRA_VERSION_FORMAT = "JIRA_VERSION_FORMAT";
    public static final String ADDITIONAL_JQL = "ADDITIONAL_JQL";
    public static final String ISSUE_SECURITY_LEVEL_FILTER = "ISSUE_SECURITY_LEVEL_FILTER";
    private static final String BASE_URL = "_BASE_URL";
    public static final String MARKETPLACE_BASE_URL = MARKETPLACE + BASE_URL;
    public static final String JIRA_BASE_URL = "JIRA" + BASE_URL;
    private static final String USER = "_USER";
    public static final String MARKETPLACE_USER = MARKETPLACE + USER;
    public static final String JIRA_API_USER = JIRA_API + USER;
    private static final String TOKEN = "_TOKEN";
    public static final String MARKETPLACE_TOKEN = MARKETPLACE + TOKEN;
    public static final String JIRA_API_TOKEN = JIRA_API + TOKEN;
    private final UnaryOperator<String> operator;
    private Path workdir;

    public EnvironmentPublisherConfiguration()
    {
        this(System::getenv, null);
    }

    public EnvironmentPublisherConfiguration(UnaryOperator<String> operator)
    {
        this(operator, null);
    }

    public EnvironmentPublisherConfiguration(
            UnaryOperator<String> operator,
            Path workdir)
    {
        this.operator = operator;
        this.workdir = workdir;
        if (getBoolean(DEBUG) || Boolean.getBoolean(DEBUG))
        {
            new LoggingConfigurator(true).configure();
        }
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
            return Paths.get("")
                    .toAbsolutePath();
        }
    }

    @Override
    public Optional<URI> getMarketplaceBaseUrl()
    {
        return getUri(MARKETPLACE_BASE_URL);
    }

    @Override
    public String getMarketplaceUsername()
    {
        return getRequiredVariable(MARKETPLACE_USER);
    }

    @Override
    public String getMarketplaceToken()
    {
        return getRequiredVariable(MARKETPLACE_TOKEN);
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
        return getBoolean(DRY_RUN);
    }

    @Override
    public Optional<String> getReleaseNotesPath()
    {
        return getVariable(RELEASE_NOTES_PATH);
    }

    @Override
    public Optional<URI> getJiraBaseUrl()
    {
        return getUri(JIRA_BASE_URL);
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
        return getBoolean(ISSUE_SECURITY_LEVEL_FILTER);
    }

    protected boolean getBoolean(String key)
    {
        return getVariable(key).map(Boolean::parseBoolean)
                .orElse(false);
    }

    protected Optional<URI> getUri(String key)
    {
        return getVariable(key).map(URI::create);
    }

    protected <E extends Enum<E>> Optional<E> getEnum(
            String key,
            Class<E> type)
    {
        E[] values = type.getEnumConstants();
        return getVariable(key).flatMap(v -> Stream.of(values)
                .filter(e -> e.name()
                                     .equalsIgnoreCase(v) || (e instanceof EnumWithKey && ((EnumWithKey) e).getKey()
                        .equalsIgnoreCase(v)))
                .findFirst());
    }

    protected Optional<String> getVariable(String key)
    {
        return Optional.ofNullable(operator.apply(key))
                .filter(StringUtils::isNotBlank);
    }

    protected String getRequiredVariable(String key)
    {
        return getVariable(key).orElseThrow(() -> new IllegalArgumentException("Missing " + key + " variable"));
    }
}
