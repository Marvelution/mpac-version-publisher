package org.marvelution.buildsupport.model;

import com.atlassian.marketplace.client.api.HostingType;
import com.atlassian.marketplace.client.model.AddonVersionStatus;
import com.atlassian.marketplace.client.model.PaymentModel;

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
