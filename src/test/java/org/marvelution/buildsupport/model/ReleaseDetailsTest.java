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
package org.marvelution.buildsupport.model;

import java.util.stream.*;

import org.marvelution.testing.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import static com.atlassian.marketplace.client.api.HostingType.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ReleaseDetails}.
 *
 * @author Mark Rekveld
 */
class ReleaseDetailsTest
		extends TestSupport
{

	@ParameterizedTest
	@MethodSource("buildNumbers")
	void testGetBuildNumber(
			String version,
			long expectedServerBuildNumber,
			long expectedDataCenterBuildNumber)
	{
		ReleaseDetails releaseDetails = new ReleaseDetails(version);
		assertThat(releaseDetails.getBuildNumber(SERVER)).isEqualTo(expectedServerBuildNumber);
		assertThat(releaseDetails.getBuildNumber(DATA_CENTER)).isEqualTo(expectedDataCenterBuildNumber);
	}

	private static Stream<Arguments> buildNumbers()
	{
		return Stream.of(Arguments.of("1.0.0", 100_000_000L, 100_000_001L), Arguments.of("1.0.0-SNAPSHOT", 100_000_000L, 100_000_001L),
		                 Arguments.of("1.1.0-SNAPSHOT", 100_100_000L, 100_100_001L),
		                 Arguments.of("1.1.1-SNAPSHOT", 100_100_100L, 100_100_101L));
	}

	@Test
	void testGetBuildNumber()
	{
		assertThat(new ReleaseDetails("1.0.0").getBuildNumber(SERVER)).isLessThan(new ReleaseDetails("1.1.0").getBuildNumber(SERVER))
				.isGreaterThan(new ReleaseDetails("0.1.0").getBuildNumber(SERVER))
				.isLessThan(new ReleaseDetails("1.0.0").getBuildNumber(DATA_CENTER));
	}
}
