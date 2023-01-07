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
		extensions(new ResponseTemplateTransformer(false), new BasicAuthenticationRequestFilter());
		notifier(new LoggingNotifier("FakeJira"));
	}
}
