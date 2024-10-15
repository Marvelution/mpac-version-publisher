package org.marvelution.buildsupport.model;

import java.util.List;

/**
 * @author Mark Rekveld
 */
public class SearchRequest
{

    private String jql;
    private int startAt;
    private int maxResults;
    private List<String> fields;

    public String getJql()
    {
        return jql;
    }

    public void setJql(String jql)
    {
        this.jql = jql;
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

    public List<String> getFields()
    {
        return fields;
    }

    public void setFields(List<String> fields)
    {
        this.fields = fields;
    }
}
