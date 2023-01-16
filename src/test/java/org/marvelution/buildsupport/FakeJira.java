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

import org.marvelution.buildsupport.helper.*;

import com.github.tomakehurst.wiremock.*;

public class FakeJira
{

	public static void main(String... args)
	{
		WireMockServer jira = new WireMockServer(new FakeJiraOptions());
		jira.start();
		System.out.println("Jira URL: " + jira.baseUrl());
		String msg = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		do
		{
			try
			{
				msg = reader.readLine();

			}
			catch (IOException ignored)
			{
			}
		}
		while (!"Q".equalsIgnoreCase(msg));
		System.out.println("Stopping Jira");
		jira.stop();
	}
}
