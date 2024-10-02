package org.marvelution.buildsupport.model;

import java.util.stream.Stream;

import org.marvelution.testing.TestSupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.atlassian.marketplace.client.api.HostingType.DATA_CENTER;
import static com.atlassian.marketplace.client.api.HostingType.SERVER;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void testGetBuildNumber()
    {
        assertThat(new ReleaseDetails("1.0.0").getBuildNumber(SERVER)).isLessThan(new ReleaseDetails("1.1.0").getBuildNumber(SERVER))
                .isGreaterThan(new ReleaseDetails("0.1.0").getBuildNumber(SERVER))
                .isLessThan(new ReleaseDetails("1.0.0").getBuildNumber(DATA_CENTER));
    }

    private static Stream<Arguments> buildNumbers()
    {
        return Stream.of(Arguments.of("1.0.0", 100_000_000L, 100_000_001L),
                Arguments.of("1.0.0-SNAPSHOT", 100_000_000L, 100_000_001L),
                Arguments.of("1.1.0-SNAPSHOT", 100_100_000L, 100_100_001L),
                Arguments.of("1.1.1-SNAPSHOT", 100_100_100L, 100_100_101L));
    }
}
