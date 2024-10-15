package org.marvelution.buildsupport.model;

import java.net.URI;

/**
 * @author Mark Rekveld
 */
public class Issue
{

    private URI self;
    private String id;
    private String key;
    private IssueFields fields;

    public URI getSelf()
    {
        return self;
    }

    public void setSelf(URI self)
    {
        this.self = self;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public IssueFields getFields()
    {
        return fields;
    }

    public void setFields(IssueFields fields)
    {
        this.fields = fields;
    }
}
