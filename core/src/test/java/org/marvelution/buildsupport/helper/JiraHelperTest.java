package org.marvelution.buildsupport.helper;

import java.util.Optional;

import org.marvelution.buildsupport.model.JiraVersion;
import org.marvelution.testing.TestSupport;
import org.marvelution.testing.wiremock.WireMockOptions;
import org.marvelution.testing.wiremock.WireMockServer;

import com.atlassian.marketplace.client.http.HttpConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.marvelution.buildsupport.helper.BasicAuthenticationRequestFilter.ADMIN;

/**
 * Tests for {@link JiraHelper}.
 *
 * @author Mark Rekveld
 */
class JiraHelperTest
        extends TestSupport
{

    private JiraHelper helper;

    @BeforeEach
    void setUp(
            @WireMockOptions(optionsClass = FakeJiraOptions.class)
            WireMockServer jira)
    {
        helper = new JiraHelper(jira.serverUri(), new HttpConfiguration.Credentials(ADMIN, ADMIN));
    }

    @Test
    void testGetProjectVersion()
    {
        Optional<JiraVersion> version = helper.getProjectVersion("TP", "server-1.0.0");

        assertThat(version).isPresent()
                .get()
                .extracting(JiraVersion::getId)
                .isEqualTo("12090");
    }

    @Test
    void testGetProjectVersionNextPage()
    {
        Optional<JiraVersion> version = helper.getProjectVersion("TP", "server-1.1.0");

        assertThat(version).isPresent()
                .get()
                .extracting(JiraVersion::getId)
                .isEqualTo("12190");
    }
}
