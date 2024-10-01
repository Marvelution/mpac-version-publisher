package org.marvelution.buildsupport.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.marvelution.buildsupport.model.AppDetails;
import org.marvelution.buildsupport.model.PluginAppDetails;

import com.atlassian.plugin.tool.PluginDetails;

import static com.atlassian.plugin.tool.PluginInfoTool.getPluginDetailsFromJar;

public class AppDetailsHelper
{

    public static AppDetails parseAppArtifact(URI addonArtifact)
            throws IOException
    {
        Path artifactPath = Path.of(addonArtifact);
        if (Files.isRegularFile(artifactPath))
        {
            PluginDetails pluginDetails;
            String filename = artifactPath.getFileName()
                    .toString()
                    .toLowerCase(Locale.ENGLISH);
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
                    while (entry.getName()
                                   .contains("/") || !entry.getName()
                            .toLowerCase()
                            .endsWith(".jar"));

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
