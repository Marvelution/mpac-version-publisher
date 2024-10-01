package org.marvelution.buildsupport.helper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Optional;

import org.marvelution.buildsupport.model.AppDetails;
import org.marvelution.testing.TestSupport;

import com.atlassian.plugin.marketing.bean.ProductCompatibility;
import com.atlassian.plugin.marketing.bean.ProductEnum;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link AppDetailsHelper}.
 *
 * @author Mark Rekveld
 */
class AddDetailsHelperTest
        extends TestSupport
{

    @ParameterizedTest
    @ValueSource(strings = {"simple-app-1.0.0.obr",
            "simple-app-1.0.0.jar"})
    void testParseAddonArtifact(String artifactName)
            throws IOException
    {
        AppDetails appDetails = AppDetailsHelper.parseAppArtifact(getAddonArtifact(artifactName));

        assertThat(appDetails).isNotNull()
                .returns("org.marvelution.buildsupport.test.simple-app", AppDetails::getKey)
                .returns("1.0.0", AppDetails::getVersion)
                .returns("Marvelution B.V.", AppDetails::getVendorName);
        Condition<ProductCompatibility> jira = new Condition<>(pc -> pc.getProduct() == ProductEnum.JIRA && pc.getMin()
                .equals("8.0.0") && pc.getMax()
                                                                             .equals("8.6.0"), "jira");
        assertThat(appDetails.getCompatibilities()).areExactly(1, jira);
    }

    @Test
    void testParseNotSupportedAddonArtifact()
    {
        assertThatThrownBy(() -> AppDetailsHelper.parseAppArtifact(getAddonArtifact("simple-app.json"))).hasMessage(
                        "Unsupported addon artifact.")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParseObrAddonArtifactWithoutJar()
    {
        assertThatThrownBy(() -> AppDetailsHelper.parseAppArtifact(getAddonArtifact("simple-app-no-jar-1.0.0.obr"))).hasMessage(
                        "obr file does not contain an valid jar file")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParseAddonArtifactWithoutMarketingXml()
            throws IOException
    {
        AppDetails appDetails = AppDetailsHelper.parseAppArtifact(getAddonArtifact("simple-app-no-marketing-1.0.0.jar"));

        assertThat(appDetails).isNotNull()
                .returns("org.marvelution.buildsupport.test.simple-app", AppDetails::getKey)
                .returns("1.0.0", AppDetails::getVersion)
                .returns("Marvelution B.V.", AppDetails::getVendorName);
        assertThat(appDetails.getCompatibilities()).isEmpty();
    }

    private URI getAddonArtifact(String name)
    {
        try
        {
            return Paths.get(Optional.ofNullable(getClass().getClassLoader()
                                    .getResource("work-dir/simple-app/" + name))
                            .orElseThrow()
                            .toURI())
                    .toUri();
        }
        catch (URISyntaxException e)
        {
            throw new AssertionError(e);
        }
    }
}
