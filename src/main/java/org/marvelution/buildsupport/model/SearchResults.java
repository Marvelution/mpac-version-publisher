package org.marvelution.buildsupport.model;

import java.util.List;

/**
 * @author Mark Rekveld
 */
public class SearchResults
{
    private String expand;
    private int startAt;
    private int maxResults;
    private int total;
    private List<Issue> issues;
    private List<String> warningMessages;

    public String getExpand()
    {
        return expand;
    }

    public void setExpand(String expand)
    {
        this.expand = expand;
    }

    public int getStartAt()
    {
        return startAt;
    }

    public void setStartAt(int startAt)
    {
        this.startAt = startAt;
    }

    public int getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(int maxResults)
    {
        this.maxResults = maxResults;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }

    public void setIssues(List<Issue> issues)
    {
        this.issues = issues;
    }

    public List<String> getWarningMessages()
    {
        return warningMessages;
    }

    public void setWarningMessages(List<String> warningMessages)
    {
        this.warningMessages = warningMessages;
    }
}
