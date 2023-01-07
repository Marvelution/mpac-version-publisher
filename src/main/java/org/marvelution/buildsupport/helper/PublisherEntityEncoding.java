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
