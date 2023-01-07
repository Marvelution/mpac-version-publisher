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
package org.marvelution.buildsupport;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import javax.annotation.*;

import org.marvelution.buildsupport.helper.*;
import org.marvelution.buildsupport.model.*;
import org.marvelution.testing.*;
import org.marvelution.testing.wiremock.*;

import com.atlassian.marketplace.client.*;
import com.atlassian.marketplace.client.impl.*;
import com.atlassian.marketplace.client.model.*;
import com.github.tomakehurst.wiremock.matching.*;
import com.github.tomakehurst.wiremock.stubbing.*;
import com.google.gson.*;
import org.joda.time.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import static org.marvelution.buildsupport.Variables.*;
import static org.marvelution.buildsupport.helper.BasicAuthenticationRequestFilter.*;
import static org.marvelution.buildsupport.helper.MarketplaceHelper.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static java.lang.String.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link PublishToMarketplace}.
 *
 * @author Mark Rekveld
 */
class PublishToMarketplaceTest
		extends TestSupport
{

	private Map<String, String> configuration;
	private PublishToMarketplace publishToMarketplace;

	private static Stream<Arguments> resolveAddonArtifact()
	{
		Path cloneDir = getWorkDir();
		return Stream.of(Arguments.of("/simple-app/simple-app-1.0.0.jar", cloneDir.resolve("simple-app/simple-app-1.0.0.jar")),
		                 Arguments.of("simple-app/simple-app-1.0.0.jar", cloneDir.resolve("simple-app/simple-app-1.0.0.jar")),
		                 Arguments.of("**/simple-app-1.0.0.jar", cloneDir.resolve("simple-app/simple-app-1.0.0.jar")));
	}

	private static Path getWorkDir()
	{
		try
		{
			return Paths.get(
					Optional.ofNullable(PublishToMarketplace.class.getClassLoader().getResource("work-dir")).orElseThrow().toURI());
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	@BeforeEach
	void setUp()
	{
		configuration = new HashMap<>();
		publishToMarketplace = new PublishToMarketplace(new EnvironmentPublisherConfiguration(configuration::get, getWorkDir()));
	}

	@ParameterizedTest
	@MethodSource("resolveAddonArtifact")
	void testResolveAddonArtifact(
			String versionArtifact,
			Path expectedPath)
	{
		configuration.put(VERSION_ARTIFACT, versionArtifact);

		File resolvedVersionArtifact = publishToMarketplace.resolveAddonArtifact();
		assertThat(resolvedVersionArtifact).isEqualTo(expectedPath.toFile());
	}

	@Test
	void testResolveAddonArtifact()
	{
		configuration.put(VERSION_ARTIFACT, "**/*.jar");

		assertThatThrownBy(publishToMarketplace::resolveAddonArtifact).hasMessage("Unable to locate a single artifact using **/*.jar")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Nested
	@ExtendWith(WireMockExtension.class)
	class EndToEnd
	{

		private final EntityEncoding encoding = new JsonEntityEncoding();
		private WireMockServer marketplace;
		private WireMockServer jira;

		@BeforeEach
		void setUp(
				@WireMockOptions(optionsClass = FakeMarketplaceOptions.class) WireMockServer marketplace,
				@WireMockOptions(optionsClass = FakeJiraOptions.class) WireMockServer jira)
		{
			this.marketplace = marketplace;
			this.jira = jira;
		}

		@Test
		void testPublishVersionMinimalConfiguration()
				throws Exception
		{
			configuration.put(MARKETPLACE_BASE_URL, marketplace.baseUrl());
			configuration.put(MARKETPLACE_USER, ADMIN);
			configuration.put(MARKETPLACE_TOKEN, ADMIN);
			configuration.put(VERSION_ARTIFACT, "**/simple-app-1.0.0.obr");
			configuration.put(DEBUG, "true");

			publishToMarketplace.run();

			Link link = assertArtifactUploaded("simple-app-1.0.0.obr");

			JsonObject json = assertNewVersionPosted(link);

			assertThat(json.getAsJsonPrimitive("status").getAsString()).isEqualTo(AddonVersionStatus.PUBLIC.getKey());
			assertThat(json.getAsJsonPrimitive("paymentModel").getAsString()).isEqualTo(PaymentModel.FREE.getKey());
			assertThat(json.getAsJsonObject("text").getAsJsonPrimitive("releaseSummary").getAsString()).isEqualTo(
					"Release with new features.");
			assertThat(json.getAsJsonObject("text").getAsJsonPrimitive("releaseNotes").getAsString()).isEqualTo("");
		}

		@Test
		void testPublishDataCenterVersionMinimalConfiguration()
				throws Exception
		{
			configuration.put(MARKETPLACE_BASE_URL, marketplace.baseUrl());
			configuration.put(MARKETPLACE_USER, ADMIN);
			configuration.put(MARKETPLACE_TOKEN, ADMIN);
			configuration.put(VERSION_ARTIFACT, "**/simple-app-dc-1.0.0.jar");
			configuration.put(DEBUG, "true");

			publishToMarketplace.run();

			Link link = assertArtifactUploaded("simple-app-dc-1.0.0.jar");

			JsonObject json = assertNewVersionPosted(link, "100000001");

			assertThat(json.getAsJsonPrimitive("status").getAsString()).isEqualTo(AddonVersionStatus.PUBLIC.getKey());
			assertThat(json.getAsJsonPrimitive("paymentModel").getAsString()).isEqualTo(PaymentModel.FREE.getKey());
			assertThat(json.getAsJsonObject("text").getAsJsonPrimitive("releaseSummary").getAsString()).isEqualTo(
					"Release with new features.");
			assertThat(json.getAsJsonObject("text").getAsJsonPrimitive("releaseNotes").getAsString()).isEqualTo("");
		}

		@Test
		void testPublishVersionMinimalConfigurationNoMarketing()
				throws Exception
		{
			configuration.put(MARKETPLACE_BASE_URL, marketplace.baseUrl());
			configuration.put(MARKETPLACE_USER, ADMIN);
			configuration.put(MARKETPLACE_TOKEN, ADMIN);
			configuration.put(VERSION_ARTIFACT, "**/simple-app-no-marketing-1.0.0.jar");
			configuration.put(DEBUG, "true");

			publishToMarketplace.run();

			Link link = assertArtifactUploaded("simple-app-no-marketing-1.0.0.jar");

			JsonObject json = assertNewVersionPosted(link);

			assertThat(json.getAsJsonPrimitive("status").getAsString()).isEqualTo(AddonVersionStatus.PUBLIC.getKey());
			assertThat(json.getAsJsonPrimitive("paymentModel").getAsString()).isEqualTo(PaymentModel.FREE.getKey());
			assertThat(json.getAsJsonObject("text").getAsJsonPrimitive("releaseSummary").getAsString()).isEqualTo(
					"Release with new features.");
			assertThat(json.getAsJsonObject("text").getAsJsonPrimitive("releaseNotes").getAsString()).isEqualTo("");
		}

		@Test
		void testPublishVersionFullConfiguration()
				throws Exception
		{
			configuration.put(MARKETPLACE_BASE_URL, marketplace.baseUrl());
			configuration.put(MARKETPLACE_USER, ADMIN);
			configuration.put(MARKETPLACE_TOKEN, ADMIN);
			configuration.put(VERSION_ARTIFACT, "**/simple-app-1.0.0.obr");
			configuration.put(VERSION_STATUS, AddonVersionStatus.PRIVATE.getKey());
			configuration.put(VERSION_PAYMENT_MODEL, PaymentModel.PAID_VIA_ATLASSIAN.getKey());
			configuration.put(JIRA_BASE_URL, jira.baseUrl());
			configuration.put(JIRA_API_USER, ADMIN);
			configuration.put(JIRA_API_TOKEN, ADMIN);
			configuration.put(JIRA_PROJECT_KEY, "TP");
			configuration.put(JIRA_VERSION_FORMAT, "server-%s");
			configuration.put(ISSUE_SECURITY_LEVEL_FILTER, "true");
			configuration.put(ADDITIONAL_JQL, "category = Open-Source");
			configuration.put(DEBUG, "true");

			publishToMarketplace.run();

			RequestPattern requestPattern = RequestPatternBuilder.newRequestPattern(POST, urlPathEqualTo("/rest/api/latest/search"))
					.build();
			ServeEvent serveEvent = getServeEvent(jira, requestPattern);
			SearchRequest searchRequest = encoding.decode(new ByteArrayInputStream(serveEvent.getRequest().getBody()), SearchRequest.class);
			assertThat(searchRequest.getJql()).isEqualTo(
					"project = 'TP' AND fixVersion = 'server-1.0.0' AND statusCategory = Done AND level is EMPTY AND category = Open-Source ORDER BY priority DESC, issuekey ASC");
			assertThat(searchRequest.getFields()).containsOnly("summary", "issuetype");
			assertThat(searchRequest.getStartAt()).isEqualTo(0);
			assertThat(searchRequest.getMaxResults()).isEqualTo(10);

			Link link = assertArtifactUploaded("simple-app-1.0.0.obr");
			JsonObject json = assertNewVersionPosted(link);
			assertThat(json.getAsJsonPrimitive("status").getAsString()).isEqualTo(AddonVersionStatus.PRIVATE.getKey());
			assertThat(json.getAsJsonPrimitive("paymentModel").getAsString()).isEqualTo(PaymentModel.PAID_VIA_ATLASSIAN.getKey());
			assertThat(json.getAsJsonObject("text").getAsJsonPrimitive("releaseSummary").getAsString()).isEqualTo("Initial Release");
			assertThat(json.getAsJsonObject("text").getAsJsonPrimitive("releaseNotes").getAsString()).contains(
							format("<li><a href=\"%s/browse/TP-624\">TP-624</a> Get work done (Story)</li>", jira.baseUrl()))
					.contains(format("<li><a href=\"%s/browse/TP-610\">TP-610</a> Get more work done (Story)</li>", jira.baseUrl()))
					.contains(format("<li><a href=\"%s/browse/TP-600\">TP-600</a> Yet more work to do (Story)</li>", jira.baseUrl()))
					.contains(
							format("<li><a href=\"%s/browse/TP-597\">TP-597</a> Oops, should have thought a bit more (Technical Debt)</li>",
							       jira.baseUrl()))
					.contains(
							format("<li><a href=\"%s/browse/TP-581\">TP-581</a> We need to fix this (Technical Debt)</li>", jira.baseUrl()))
					.contains(format("<li><a href=\"%s/browse/TP-579\">TP-579</a> New feature please (Story)</li>", jira.baseUrl()))
					.contains(format("<li><a href=\"%s/browse/TP-562\">TP-562</a> Optimize this release note (Technical Debt)</li>",
					                 jira.baseUrl()))
					.contains(format("<li><a href=\"%s/browse/TP-558\">TP-558</a> Cleanup in aisle three (Technical Debt)</li>",
					                 jira.baseUrl()))
					.contains(format("<li><a href=\"%s/browse/TP-549\">TP-549</a> Almost complete (Technical Debt)</li>", jira.baseUrl()))
					.contains(format("<li><a href=\"%s/browse/TP-547\">TP-547</a> Release initial version (Story)</li>", jira.baseUrl()))
					.contains("Showing 10 of ", "37 issues", format("%s/issues?jql=", jira.baseUrl()));
		}

		private JsonObject assertNewVersionPosted(Link artifactUri)
		{
			return assertNewVersionPosted(artifactUri, null);
		}

		private JsonObject assertNewVersionPosted(
				Link artifactUri,
				@Nullable String dataCenterBuildNumber)
		{
			RequestPattern requestPattern = RequestPatternBuilder.newRequestPattern(POST, urlPathEqualTo(
					"/rest/2/addons/org.marvelution.buildsupport.test.simple-app/versions")).build();
			ServeEvent versionEvent = getServeEvent(marketplace, requestPattern);

			String dataCenterBuildNumberHeader = versionEvent.getRequest().getHeader("X-Mpac-DataCenter-BuildNumber");
			if (dataCenterBuildNumber == null)
			{
				assertThat(dataCenterBuildNumberHeader).isNull();
			}
			else
			{
				assertThat(dataCenterBuildNumberHeader).isEqualTo(dataCenterBuildNumber);
			}
			JsonObject json = new JsonParser().parse(versionEvent.getRequest().getBodyAsString()).getAsJsonObject();

			assertThat(json.getAsJsonObject("_links").getAsJsonObject("artifact").getAsJsonPrimitive("href").getAsString()).isEqualTo(
					artifactUri.getUri().toASCIIString());
			assertThat(json.getAsJsonObject("_links").getAsJsonObject("agreement").getAsJsonPrimitive("href").getAsString()).isEqualTo(
					MARKETPLACE_AGREEMENT_URI.toASCIIString());
			assertThat(json.getAsJsonPrimitive("name").getAsString()).isEqualTo("1.0.0");
			assertThat(json.getAsJsonPrimitive("buildNumber").getAsString()).isEqualTo("100000000");
			assertThat(json.getAsJsonObject("release").getAsJsonPrimitive("date").getAsString()).isEqualTo(LocalDate.now().toString());
			assertThat(json.getAsJsonObject("release").getAsJsonPrimitive("releasedBy").getAsString()).isEqualTo("Marvelution B.V.");
			return json;
		}

		private Link assertArtifactUploaded(String file)
				throws MpacException
		{
			RequestPattern requestPattern = RequestPatternBuilder.newRequestPattern(POST, urlPathEqualTo("/rest/2/assets/artifact"))
					.withQueryParam("file", equalTo(file))
					.build();
			ServeEvent uploadEvent = getServeEvent(marketplace, requestPattern);
			return encoding.decode(new ByteArrayInputStream(uploadEvent.getResponse().getBody()), InternalModel.MinimalLinks.class)
					.getLinks()
					.getLink("self")
					.getOrError(() -> "missing link");
		}

		private ServeEvent getServeEvent(
				WireMockServer server,
				RequestPattern requestPattern)
		{
			return server.getServeEvents()
					.getServeEvents()
					.stream()
					.filter(se -> requestPattern.match(se.getRequest()).isExactMatch())
					.findFirst()
					.orElseThrow(AssertionError::new);
		}
	}
}
