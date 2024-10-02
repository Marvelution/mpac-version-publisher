package org.marvelution.buildsupport.configuration;

public class PublisherConfigurationBuilder
{

    public static PublisherConfiguration build(String... args)
    {
        if (args != null && args.length > 0)
        {
            return new CommandLinePublisherConfiguration(args);
        }
        else
        {
            return new EnvironmentPublisherConfiguration();
        }
    }
}
