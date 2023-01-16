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
package org.marvelution.buildsupport.configuration;

import java.net.*;
import java.nio.file.*;
import java.util.*;

import com.atlassian.marketplace.client.model.*;

/**
 * Pipe Configuration holder.
 *
 * @author Mark Rekveld
 */
public interface PublisherConfiguration
{

	Path getWorkingDirectory();

	Optional<URI> getMarketplaceBaseUrl();

	String getMarketplaceUsername();

	String getMarketplaceToken();

	Optional<AddonVersionStatus> getVersionStatus();

	Optional<PaymentModel> getPaymentModel();

	String getVersionArtifactPath();

	boolean dryRun();

    Optional<String> getReleaseNotesPath();

	Optional<URI> getJiraBaseUrl();

	String getJiraUsername();

	String getJiraToken();

	String getJiraProjectKey();

	Optional<String> getJiraVersionFormat();

	Optional<String> getAdditionalJQL();

	boolean useIssueSecurityFilter();
}
