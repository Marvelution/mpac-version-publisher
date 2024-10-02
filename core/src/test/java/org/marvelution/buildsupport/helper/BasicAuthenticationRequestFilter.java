package org.marvelution.buildsupport.helper;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.StubRequestFilter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.security.Authenticator;
import com.github.tomakehurst.wiremock.security.BasicAuthenticator;

/**
 * @author Mark Rekveld
 */
public class BasicAuthenticationRequestFilter
        extends StubRequestFilter
{

    public static final String ADMIN = "admin";
    private final Authenticator authenticator = new BasicAuthenticator(ADMIN, ADMIN);

    @Override
    public String getName()
    {
        return "basic-auth-required";
    }

    @Override
    public RequestFilterAction filter(Request request)
    {
        if (authenticator.authenticate(request))
        {
            return RequestFilterAction.continueWith(request);
        }
        else
        {
            return RequestFilterAction.stopWith(new ResponseDefinitionBuilder().withStatus(401)
                    .build());
        }
    }
}
