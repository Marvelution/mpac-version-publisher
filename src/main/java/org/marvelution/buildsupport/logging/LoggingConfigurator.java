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
		this(Boolean.parseBoolean(System.getenv(Variables.DEBUG)));
	}

	public LoggingConfigurator(boolean debugEnabled)
	{
		this.debugEnabled = debugEnabled;
	}

	public void configure()
	{
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		configure(loggerContext);
	}

	@Override
	public void configure(LoggerContext loggerContext)
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
	}
}
