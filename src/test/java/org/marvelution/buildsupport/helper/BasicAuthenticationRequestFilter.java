package org.marvelution.buildsupport.helper;

import com.github.tomakehurst.wiremock.client.*;
import com.github.tomakehurst.wiremock.extension.requestfilter.*;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.security.*;

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
		if (authenticator.authenticate(request)){
			return RequestFilterAction.continueWith(request);
		}
		else {
			return RequestFilterAction.stopWith(new ResponseDefinitionBuilder().withStatus(401).build());
		}
	}
}
