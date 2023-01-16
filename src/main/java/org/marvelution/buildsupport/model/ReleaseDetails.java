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
package org.marvelution.buildsupport.model;

import com.atlassian.marketplace.client.api.*;
import com.atlassian.marketplace.client.model.*;

/**
 * @author Mark Rekveld
 */
public class ReleaseDetails
{

	private final String version;
	private Version parsedVersion;
	private String releaseSummary;
	private String releaseNotes;
	private AddonVersionStatus status;
	private PaymentModel paymentModel;
	private String publisher;

	public ReleaseDetails(String version)
	{
		this.version = version;
	}

	public String getVersion()
	{
		return version;
	}

	public String getReleaseSummary()
	{
		return releaseSummary;
	}

	public ReleaseDetails setReleaseSummary(String releaseSummary)
	{
		this.releaseSummary = releaseSummary;
		return this;
	}

	public String getReleaseNotes()
	{
		return releaseNotes;
	}

	public ReleaseDetails setReleaseNotes(String releaseNotes)
	{
		this.releaseNotes = releaseNotes;
		return this;
	}

	public AddonVersionStatus getStatus()
	{
		return status;
	}

	public ReleaseDetails setStatus(AddonVersionStatus status)
	{
		this.status = status;
		return this;
	}

	public PaymentModel getPaymentModel()
	{
		return paymentModel;
	}

	public ReleaseDetails setPaymentModel(PaymentModel paymentModel)
	{
		this.paymentModel = paymentModel;
		return this;
	}

	public long getBuildNumber(HostingType hostingType)
	{
		if (parsedVersion == null)
		{
			parsedVersion = Version.parseVersion(version);
		}
		return parsedVersion.getMajorVersion() * 100_000_000L + parsedVersion.getMinorVersion() * 100_000L +
				parsedVersion.getPatchLevel() * 100L + hostingType.ordinal();
	}

	public String getPublisher()
	{
		return publisher;
	}

	public ReleaseDetails setPublisher(String publisher)
	{
		this.publisher = publisher;
		return this;
	}
}
