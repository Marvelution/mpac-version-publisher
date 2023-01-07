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

import ch.qos.logback.classic.pattern.*;
import ch.qos.logback.classic.spi.*;

import static ch.qos.logback.core.CoreConstants.*;

/**
 * This custom {@link ThrowableProxyConverter} is used to add a new line after the throwable.
 *
 * @author Mark Rekveld
 */
public class PublisherThrowableProxyConverter
		extends ThrowableProxyConverter
{

	@Override
	protected String throwableProxyToString(IThrowableProxy tp)
	{
		return super.throwableProxyToString(tp) + LINE_SEPARATOR;
	}
}
