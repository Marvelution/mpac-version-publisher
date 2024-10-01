package org.marvelution.buildsupport.logging;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;

import static ch.qos.logback.core.CoreConstants.LINE_SEPARATOR;

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
