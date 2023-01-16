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

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.marvelution.testing.wiremock.WireMockServer;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.extension.WireMockServices;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

/**
 * {@link WireMockServer} options for a preconfigured fake Marketplace instance.
 *
 * @author Mark Rekveld
 */
public class FakeMarketplaceOptions
        extends WireMockConfiguration
{

    public FakeMarketplaceOptions()
    {
        dynamicPort();
        try
        {
            usingFilesUnderDirectory(Paths.get(Optional.ofNullable(getClass().getClassLoader()
                                    .getResource("fake-marketplace"))
                            .orElseThrow()
                            .toURI())
                    .toString());
        }
        catch (URISyntaxException e)
        {
            throw new IllegalStateException("unable to locate fake-marketplace WireMock files", e);
        }
        extensions(new BasicAuthenticationRequestFilter());
        extensions(services -> List.of(new ResponseAsMappingTransformer(services)));
        notifier(new LoggingNotifier("FakeMarketplace"));
    }

    private static class ResponseAsMappingTransformer
            implements ResponseDefinitionTransformerV2
    {
        private final WireMockServices services;

        private ResponseAsMappingTransformer(WireMockServices services)
        {
            this.services = services;
        }

        @Override
        public String getName()
        {
            return "response-as-mapping";
        }

        @Override
        public ResponseDefinition transform(ServeEvent serveEvent)
        {
            ResponseTemplateTransformer transformer = services.getExtensions()
                    .ofType(ResponseTemplateTransformer.class)
                    .values()
                    .stream()
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
            ResponseDefinition responseDefinition = transformer.transform(serveEvent);
            HttpHeader location = responseDefinition.getHeaders()
                    .getHeader("Location");
            HttpHeader mappingFile = responseDefinition.getHeaders()
                    .getHeader("MappingFile");
            if (location.isPresent() && mappingFile.isPresent())
            {
                try
                {
                    services.getFiles()
                            .writeBinaryFile(mappingFile.firstValue(), responseDefinition.getByteBody());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            return responseDefinition;
        }

        @Override
        public boolean applyGlobally()
        {
            return false;
        }
    }
}
