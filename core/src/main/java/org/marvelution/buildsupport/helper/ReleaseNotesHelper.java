package org.marvelution.buildsupport.helper;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.marvelution.buildsupport.configuration.PublisherConfiguration;
import org.marvelution.buildsupport.model.*;

import com.atlassian.marketplace.client.http.HttpConfiguration;
import com.atlassian.marketplace.client.util.UriBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.stripEnd;

/**
 * Helper for generating release summary and notes.
 *
 * @author Mark Rekveld
 */
public class ReleaseNotesHelper
        implements Closeable
{

    public static final String MORE_ISSUES_FORMAT = "<p>Showing %1$s of <a href=\"%3$s\">%2$s issues</a>.</p>";
    public static final String ISSUE_LINK_FORMAT = "<li><a href=\"%4$s/browse/%1$s\">%1$s</a> %2$s (%3$s)</li>";
    public static final String JQL =
            "project = '%s' AND fixVersion = '%s' AND statusCategory = Done %s ORDER BY priority DESC, issuekey ASC";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseNotesHelper.class);
    private final JiraHelper jiraHelper;
    private final String versionNameFormat;
    private final Selector notesSelector;
    private String projectKey;
    private String additionalJql;

    public ReleaseNotesHelper(PublisherConfiguration configuration)
    {
        notesSelector = configuration.getReleaseNotesPath()
                .map(selector -> new Selector(configuration.getWorkingDirectory(), selector))
                .orElse(null);
        jiraHelper = configuration.getJiraBaseUrl()
                .map(uri -> new JiraHelper(uri,
                        new HttpConfiguration.Credentials(configuration.getJiraUsername(), configuration.getJiraToken())))
                .orElse(null);
        versionNameFormat = configuration.getJiraVersionFormat()
                .orElse("%s");
        if (jiraHelper != null)
        {
            projectKey = configuration.getJiraProjectKey();
            if (configuration.useIssueSecurityFilter())
            {
                additionalJql = "level is EMPTY";
            }
            additionalJql += configuration.getAdditionalJQL()
                    .map(jql -> " AND " + jql)
                    .orElse("");
        }
    }

    @Override
    public void close()
    {
        if (jiraHelper != null)
        {
            jiraHelper.close();
        }
    }

    public void populateReleaseSummaryAndNotes(ReleaseDetails releaseDetails)
    {
        if (jiraHelper == null)
        {
            releaseDetails.setReleaseSummary(getDefaultReleaseSummary(releaseDetails.getVersion()))
                    .setReleaseNotes(getDefaultReleaseNotes());
        }
        else
        {
            String versionName = format(versionNameFormat, releaseDetails.getVersion());
            Optional<JiraVersion> jiraVersion = jiraHelper.getProjectVersion(projectKey, versionName);

            jiraVersion.map(JiraVersion::getDescription)
                    .filter(StringUtils::isNotBlank)
                    .ifPresentOrElse(releaseDetails::setReleaseSummary,
                            () -> releaseDetails.setReleaseSummary(getDefaultReleaseSummary(releaseDetails.getVersion())));
            jiraVersion.map(JiraVersion::getName)
                    .flatMap(this::getReleaseNotes)
                    .ifPresentOrElse(releaseDetails::setReleaseNotes, () -> releaseDetails.setReleaseNotes(getDefaultReleaseNotes()));
        }
    }

    private Optional<String> getReleaseNotes(String version)
    {
        SearchRequest request = new SearchRequest();
        request.setJql(format(JQL, projectKey, version, isNotBlank(additionalJql) ? "AND " + additionalJql : ""));
        request.setStartAt(0);
        request.setMaxResults(10);
        request.setFields(List.of("summary", "issuetype"));
        LOGGER.info("Searching for issues using JQL '{}'", request.getJql());
        try
        {
            SearchResults searchResults = jiraHelper.searchIssues(request);
            if (searchResults.getTotal() > 0)
            {
                LOGGER.info("Found {} issues, including {} in the release notes.", searchResults.getTotal(), searchResults.getMaxResults());
                String jiraDisplayUrl = stripEnd(jiraHelper.getBaseUri()
                        .toASCIIString(), "/");
                StringBuilder notes = new StringBuilder().append("<p><ul>");

                searchResults.getIssues()
                        .stream()
                        .map(issue -> format(ISSUE_LINK_FORMAT,
                                issue.getKey(),
                                issue.getFields()
                                        .getSummary(),
                                issue.getFields()
                                        .getIssuetype()
                                        .getName(),
                                jiraDisplayUrl))
                        .forEach(notes::append);
                notes.append("</ul></p>");
                if (searchResults.getTotal() > searchResults.getMaxResults())
                {
                    URI link = UriBuilder.fromUri(jiraHelper.getBaseUri())
                            .path("issues")
                            .queryParam("jql", request.getJql())
                            .build();
                    notes.append(format(MORE_ISSUES_FORMAT, searchResults.getMaxResults(), searchResults.getTotal(), link));
                }
                return Optional.of(notes.toString());
            }
            else
            {
                LOGGER.info("Found no issues");
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error searching for issues of version {}; {}", version, e.getMessage(), e);
        }
        return Optional.empty();
    }

    private String getDefaultReleaseSummary(String releaseVersion)
    {
        Version version = Version.parseVersion(releaseVersion);
        if (version.isGreaterThan(new Version(version.getMajorVersion(), version.getMinorVersion())))
        {
            return "Release featuring bug fixes.";
        }
        else if (version.isGreaterThan(new Version(version.getMajorVersion())))
        {
            return "Release featuring minor improvements and bug fixes.";
        }
        else
        {
            return "Release with new features.";
        }
    }

    private String getDefaultReleaseNotes()
    {
        if (notesSelector != null)
        {
            return notesSelector.unique()
                    .map(path -> {
                        try
                        {
                            String content = String.join("", Files.readAllLines(path, StandardCharsets.UTF_8));
                            return content.replaceAll("<!--(.*?)-->", "");
                        }
                        catch (IOException e)
                        {
                            LOGGER.error("Error reading content of {}", path, e);
                            return null;
                        }
                    })
                    .orElse("");
        }
        else
        {
            return "";
        }
    }
}
