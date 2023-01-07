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
