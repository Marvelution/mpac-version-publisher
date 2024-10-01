package org.marvelution.buildsupport.helper;

import org.marvelution.testing.wiremock.*;

import com.github.tomakehurst.wiremock.core.*;
import com.github.tomakehurst.wiremock.extension.responsetemplating.*;

/**
 * {@link WireMockServer} options for a preconfigured fake Jira instance.
 *
 * @author Mark Rekveld
 */
public class FakeJiraOptions
		extends WireMockConfiguration
{

	public FakeJiraOptions()
	{
		dynamicPort();
		usingFilesUnderClasspath("fake-jira");
		extensions(new BasicAuthenticationRequestFilter());
		notifier(new LoggingNotifier("FakeJira"));
	}
}
