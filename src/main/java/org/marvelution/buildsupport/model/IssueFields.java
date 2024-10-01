package org.marvelution.buildsupport.model;

/**
 * @author Mark Rekveld
 */
public class IssueFields
{

    private String summary;
    private IssueType issuetype;

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public IssueType getIssuetype()
    {
        return issuetype;
    }

    public void setIssuetype(IssueType issuetype)
    {
        this.issuetype = issuetype;
    }
}
