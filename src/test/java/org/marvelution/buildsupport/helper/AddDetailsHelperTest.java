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

import org.marvelution.buildsupport.model.*;
import org.marvelution.testing.*;

import com.atlassian.plugin.marketing.bean.*;
import org.assertj.core.api.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import static org.assertj.core.api.Assertions.*;

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
