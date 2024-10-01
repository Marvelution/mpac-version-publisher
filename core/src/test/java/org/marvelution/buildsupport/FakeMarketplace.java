package org.marvelution.buildsupport;

import java.io.*;

import org.marvelution.buildsupport.helper.*;

import com.github.tomakehurst.wiremock.*;

public class FakeMarketplace
{

	public static void main(String... args)
	{
		WireMockServer marketplace = new WireMockServer(new FakeMarketplaceOptions());
		marketplace.start();
		System.out.println("Marketplace URL: " + marketplace.baseUrl());
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
		System.out.println("Stopping Marketplace");
		marketplace.stop();
	}
}
