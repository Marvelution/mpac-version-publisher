package org.marvelution.buildsupport.model;

import java.net.URI;
import java.util.List;

/**
 * @author Mark Rekveld
 */
public class PagedJiraVersions
{

    private URI self;
    private URI nextPage;
    private int maxResults;
    private long startAt;
    private long total;
    private boolean isLast;
    private List<JiraVersion> values;

    public URI getSelf()
    {
        return self;
    }

    public void setSelf(URI self)
    {
        this.self = self;
    }

    public URI getNextPage()
    {
        return nextPage;
    }

    public void setNextPage(URI nextPage)
    {
        this.nextPage = nextPage;
    }

    public int getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(int maxResults)
    {
        this.maxResults = maxResults;
    }

    public long getStartAt()
    {
        return startAt;
    }

    public void setStartAt(long startAt)
    {
        this.startAt = startAt;
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public boolean isLast()
    {
        return isLast;
    }

    public void setLast(boolean last)
    {
        isLast = last;
    }

    public List<JiraVersion> getValues()
    {
        return values;
    }

    public void setValues(List<JiraVersion> values)
    {
        this.values = values;
    }
}
