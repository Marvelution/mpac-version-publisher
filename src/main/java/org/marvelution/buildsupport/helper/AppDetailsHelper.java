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
import java.util.zip.*;

import org.marvelution.buildsupport.model.*;

import com.atlassian.plugin.tool.*;

import static com.atlassian.plugin.tool.PluginInfoTool.*;

public class AppDetailsHelper
{

	public static AppDetails parseAppArtifact(URI addonArtifact)
			throws IOException
	{
		Path artifactPath = Path.of(addonArtifact);
		if (Files.isRegularFile(artifactPath))
		{
			PluginDetails pluginDetails;
			String filename = artifactPath.getFileName().toString().toLowerCase(Locale.ENGLISH);
			try (InputStream inputStream = Files.newInputStream(artifactPath, StandardOpenOption.READ))
			{
				if (filename.endsWith(".jar"))
				{
					pluginDetails = getPluginDetailsFromJar(inputStream);
				}
				else if (filename.endsWith(".obr"))
				{
					ZipInputStream obr = new ZipInputStream(inputStream);

					ZipEntry entry;
					do
					{
						if ((entry = obr.getNextEntry()) == null)
						{
							throw new IllegalArgumentException("obr file does not contain an valid jar file");
						}
					}
					while (entry.getName().contains("/") || !entry.getName().toLowerCase().endsWith(".jar"));

					pluginDetails = getPluginDetailsFromJar(obr);
				}
				else
				{
					throw new IllegalArgumentException("Unsupported addon artifact.");
				}
			}
			if (pluginDetails != null)
			{
				return new PluginAppDetails(pluginDetails);
			}
		}
		// TODO Load plugin details from URI location
		throw new IllegalArgumentException("Unable to load app details from " + addonArtifact);
	}
}
