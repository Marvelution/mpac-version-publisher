package org.marvelution.buildsupport.helper;

import com.github.tomakehurst.wiremock.common.*;
import org.slf4j.*;

/**
 * @author Mark Rekveld
 */
public class LoggingNotifier
		implements Notifier
{

	private final Logger logger;

	public LoggingNotifier(String system)
	{
		this.logger = LoggerFactory.getLogger(system);
	}

	@Override
	public void info(String message)
	{
		logger.info(message);
	}

	@Override
	public void error(String message)
	{
		logger.error(message);
	}

	@Override
	public void error(
			String message,
			Throwable t)
	{
		logger.error(message, t);
	}
}
