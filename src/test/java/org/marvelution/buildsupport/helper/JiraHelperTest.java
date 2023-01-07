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

import java.util.*;

import org.marvelution.buildsupport.model.*;
import org.marvelution.testing.*;
import org.marvelution.testing.wiremock.*;

import com.atlassian.marketplace.client.http.*;
import org.junit.jupiter.api.*;

import static org.marvelution.buildsupport.helper.BasicAuthenticationRequestFilter.*;

import static org.assertj.core.api.Assertions.*;

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
	void setUp(@WireMockOptions(optionsClass = FakeJiraOptions.class) WireMockServer jira)
	{
		helper = new JiraHelper(jira.serverUri(), new HttpConfiguration.Credentials(ADMIN, ADMIN));
	}

	@Test
	void testGetProjectVersion()
	{
		Optional<JiraVersion> version = helper.getProjectVersion("TP", "server-1.0.0");

		assertThat(version).isPresent().get().extracting(JiraVersion::getId).isEqualTo("12090");
	}

	@Test
	void testGetProjectVersionNextPage()
	{
		Optional<JiraVersion> version = helper.getProjectVersion("TP", "server-1.1.0");

		assertThat(version).isPresent().get().extracting(JiraVersion::getId).isEqualTo("12190");
	}
}
