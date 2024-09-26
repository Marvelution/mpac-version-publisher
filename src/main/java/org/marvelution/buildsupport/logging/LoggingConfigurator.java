package org.marvelution.buildsupport.logging;

import org.marvelution.buildsupport.configuration.*;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.*;
import ch.qos.logback.core.encoder.*;
import ch.qos.logback.core.spi.*;
import org.slf4j.*;

import static ch.qos.logback.classic.Level.*;
import static org.slf4j.Logger.*;

/**
 * Logback {@link Configurator} used to enable debug logging if the {@code DEBUG} environment variable is set to {@link Boolean#TRUE}.
 * Debug logging with also result in full exception stack-traces being logged.
 *
 * @author Mark Rekveld
 */
public class LoggingConfigurator
		extends ContextAwareBase
		implements Configurator
{

	private final boolean debugEnabled;

	public LoggingConfigurator()
	{
		this(Boolean.parseBoolean(System.getenv(EnvironmentPublisherConfiguration.DEBUG)));
	}

	public LoggingConfigurator(boolean debugEnabled)
	{
		this.debugEnabled = debugEnabled;
	}

	public void configure()
	{
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		setContext(loggerContext);
		configure(loggerContext);
	}

	@Override
	public ExecutionStatus configure(LoggerContext loggerContext)
	{
		addInfo("Setting up logging configuration.");

		PatternLayout layout = new PatternLayout();
		layout.getDefaultConverterMap().put("ex", PublisherThrowableProxyConverter.class.getName());
		layout.setPattern("%-10([%level]) %msg%n%ex{" + (debugEnabled ? "full" : "short") + "}");
		layout.setContext(loggerContext);
		layout.start();

		LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
		encoder.setContext(loggerContext);
		encoder.setLayout(layout);

		ConsoleAppender<ILoggingEvent> console = new ConsoleAppender<>();
		console.setContext(loggerContext);
		console.setName("console");
		console.setEncoder(encoder);
		console.start();

		Logger rootLogger = loggerContext.getLogger(ROOT_LOGGER_NAME);
		rootLogger.detachAndStopAllAppenders();
		rootLogger.addAppender(console);
		if (debugEnabled)
		{
			rootLogger.setLevel(DEBUG);
		}
		else
		{
			rootLogger.setLevel(INFO);
		}
		return ExecutionStatus.NEUTRAL;
	}
}
