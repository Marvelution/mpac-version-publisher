package org.marvelution.buildsupport.configuration;

import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.marvelution.buildsupport.logging.*;

import com.atlassian.marketplace.client.model.*;
import org.apache.commons.cli.*;

public class CommandLinePublisherConfiguration
		implements PublisherConfiguration
{

	private final Option workDir = Option.builder().option("w").longOpt("work-dir").hasArg().desc("Work directory").build();
	private final Option marketplaceBaseUrl = Option.builder()
			.option("m")
			.longOpt("marketplace-base-url")
			.hasArg()
			.desc("Atlassian Marketplace base url")
			.build();
	private final Option marketplaceUsername = Option.builder()
			.option("mu")
			.longOpt("marketplace-user")
			.hasArg()
			.desc("Atlassian Marketplace username")
			.required()
			.build();
	private final Option marketplaceToken = Option.builder()
			.option("mt")
			.longOpt("marketplace-token")
			.hasArg()
			.desc("Atlassian Marketplace token")
			.required()
			.build();
	private final Option jiraBaseUrl = Option.builder().option("j").longOpt("jira-base-url").hasArg().desc("Jira base url").build();
	private final Option jiraUsername = Option.builder().option("ju").longOpt("jira-user").hasArg().desc("Jira username").build();
	private final Option jiraToken = Option.builder().option("jt").longOpt("jira-token").hasArg().desc("Jira token").build();
	private final Option jiraProjectKey = Option.builder()
			.option("jpk")
			.longOpt("jira-project-key")
			.hasArg()
			.desc("Jira project key")
			.build();
	private final Option jiraVersionFormat = Option.builder()
			.option("jvf")
			.longOpt("jira-version-format")
			.hasArg()
			.desc("Jira version format")
			.build();
	private final Option additionalJql = Option.builder().option("jql").hasArg().desc("Additional JQL").build();
	private final Option useIssueSecurityFilter = Option.builder().option("isf").desc("Use Issue Security Filter").build();
	private final Option appVersionStatus = Option.builder().option("vs").longOpt("version-status").hasArg().desc("Version Status").build();
	private final Option appPaymentModel = Option.builder().option("pm").longOpt("payment-model").hasArg().desc("Payment Model").build();
	private final Option appArtifact = Option.builder()
			.option("vap")
			.longOpt("version-artifact-path")
			.hasArg()
			.desc("Version artifact path")
			.required()
			.build();
	private final Option releaseNotesPath = Option.builder()
			.option("rnp")
			.longOpt("release-notes-path")
			.hasArg()
			.desc("Release notes path")
			.build();
	private final Option dryRun = Option.builder().option("dr").longOpt("dry-run").desc("Dry run the publishing").build();
	private final Option debug = Option.builder().option("D").longOpt("debug").desc("Enable debug logging").build();
	private final Option help = Option.builder().option("h").longOpt("help").desc("Print this help message").build();
	private final Options options = new Options().addOption(workDir)
			.addOption(marketplaceBaseUrl)
			.addOption(marketplaceUsername)
			.addOption(marketplaceToken)
			.addOption(appArtifact)
			.addOption(releaseNotesPath)
			.addOption(jiraBaseUrl)
			.addOption(jiraUsername)
			.addOption(jiraToken)
			.addOption(jiraProjectKey)
			.addOption(jiraVersionFormat)
			.addOption(additionalJql)
			.addOption(useIssueSecurityFilter)
			.addOption(appVersionStatus)
			.addOption(appPaymentModel)
			.addOption(dryRun)
			.addOption(debug)
			.addOption(help);
	private CommandLine cmd;

	public CommandLinePublisherConfiguration(String... args)
	{
		HelpFormatter helper = new HelpFormatter();
		try
		{
			CommandLineParser parser = new DefaultParser();
			cmd = parser.parse(options, args);

			if (cmd.hasOption(help))
			{
				helper.printHelp("Usage:", options);
				System.exit(0);
			}
			else if (cmd.hasOption(jiraBaseUrl))
			{
				List<Option> missingOptions = new ArrayList<>();
				if (!cmd.hasOption(jiraUsername))
				{
					missingOptions.add(jiraUsername);
				}
				else if (!cmd.hasOption(jiraToken))
				{
					missingOptions.add(jiraToken);
				}
				else if (!cmd.hasOption(jiraProjectKey))
				{
					missingOptions.add(jiraProjectKey);
				}
				if (!missingOptions.isEmpty())
				{
					throw new MissingOptionException(missingOptions);
				}
			}
			if (cmd.hasOption(debug))
			{
				new LoggingConfigurator(true).configure();
			}
		}
		catch (ParseException e)
		{
			System.out.println(e.getMessage());
			helper.printHelp("jar", options, true);
			System.exit(1);
		}
	}

	@Override
	public Path getWorkingDirectory()
	{
		if (cmd.hasOption(workDir))
		{
			return Paths.get(cmd.getOptionValue(workDir)).toAbsolutePath();
		}
		else
		{
			return Paths.get("").toAbsolutePath();
		}
	}

	@Override
	public Optional<URI> getMarketplaceBaseUrl()
	{
		return Optional.ofNullable(cmd.getOptionValue(marketplaceBaseUrl)).map(URI::create);
	}

	@Override
	public String getMarketplaceUsername()
	{
		return getRequiredOptionValue(marketplaceUsername);
	}

	@Override
	public String getMarketplaceToken()
	{
		return getRequiredOptionValue(marketplaceToken);
	}

	@Override
	public Optional<AddonVersionStatus> getVersionStatus()
	{
		return Optional.ofNullable(cmd.getOptionValue(appVersionStatus))
				.flatMap(v -> Stream.of(AddonVersionStatus.values())
						.filter(e -> e.name().equalsIgnoreCase(v) || e.getKey().equalsIgnoreCase(v))
						.findFirst());
	}

	@Override
	public Optional<PaymentModel> getPaymentModel()
	{
		return Optional.ofNullable(cmd.getOptionValue(appPaymentModel))
				.flatMap(v -> Stream.of(PaymentModel.values())
						.filter(e -> e.name().equalsIgnoreCase(v) || e.getKey().equalsIgnoreCase(v))
						.findFirst());
	}

	@Override
	public String getVersionArtifactPath()
	{
		return getRequiredOptionValue(appArtifact);
	}

	@Override
	public boolean dryRun()
	{
		return cmd.hasOption(dryRun);
	}

	@Override
	public Optional<String> getReleaseNotesPath()
	{
		return Optional.ofNullable(cmd.getOptionValue(releaseNotesPath));
	}

	@Override
	public Optional<URI> getJiraBaseUrl()
	{
		return Optional.ofNullable(cmd.getOptionValue(jiraBaseUrl)).map(URI::create);
	}

	@Override
	public String getJiraUsername()
	{
		return getRequiredOptionValue(jiraUsername);
	}

	@Override
	public String getJiraToken()
	{
		return getRequiredOptionValue(jiraToken);
	}

	@Override
	public String getJiraProjectKey()
	{
		return getRequiredOptionValue(jiraProjectKey);
	}

	@Override
	public Optional<String> getJiraVersionFormat()
	{
		return Optional.ofNullable(cmd.getOptionValue(jiraVersionFormat));
	}

	@Override
	public Optional<String> getAdditionalJQL()
	{
		return Optional.ofNullable(cmd.getOptionValue(additionalJql));
	}

	@Override
	public boolean useIssueSecurityFilter()
	{
		return cmd.hasOption(useIssueSecurityFilter);
	}

	private String getRequiredOptionValue(Option option)
	{
		return Optional.ofNullable(cmd.getOptionValue(option))
				.orElseThrow(() -> new IllegalArgumentException("Missing option " + option.getOpt() + "|" + option.getLongOpt()));
	}
}
