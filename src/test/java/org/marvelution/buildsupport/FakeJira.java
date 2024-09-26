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
