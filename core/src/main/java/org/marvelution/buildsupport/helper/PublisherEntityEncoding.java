package org.marvelution.buildsupport.helper;

import java.io.*;

import com.atlassian.marketplace.client.*;
import com.atlassian.marketplace.client.impl.*;
import org.slf4j.*;

/**
 * Delegating {@link EntityEncoding} that will log generated jsons when debug is enabled.
 *
 * @author Mark Rekveld
 */
public class PublisherEntityEncoding
		implements EntityEncoding
{

	private static final Logger LOGGER = LoggerFactory.getLogger(PublisherEntityEncoding.class);
	private final EntityEncoding delegate = new JsonEntityEncoding();

	@Override
	public <T> T decode(
			InputStream stream,
			Class<T> type)
			throws MpacException
	{
		return delegate.decode(stream, type);
	}

	@Override
	public <T> void encode(
			OutputStream stream,
			T entity,
			boolean includeReadOnlyFields)
			throws MpacException
	{
		if (LOGGER.isDebugEnabled())
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			delegate.encode(outputStream, entity, includeReadOnlyFields);
			LOGGER.debug("Generated json: {}", outputStream);
			try
			{
				outputStream.writeTo(stream);
			}
			catch (IOException e)
			{
				throw new MpacException(e);
			}
		}
		else
		{
			delegate.encode(stream, entity, includeReadOnlyFields);
		}
	}

	@Override
	public <T> void encodeChanges(
			OutputStream stream,
			T original,
			T updated)
			throws MpacException
	{
		if (LOGGER.isDebugEnabled())
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			delegate.encodeChanges(outputStream, original, updated);
			LOGGER.debug("Generated json path: {}", outputStream);
			try
			{
				outputStream.writeTo(stream);
			}
			catch (IOException e)
			{
				throw new MpacException(e);
			}
		}
		else
		{
			delegate.encodeChanges(stream, original, updated);
		}
	}
}
