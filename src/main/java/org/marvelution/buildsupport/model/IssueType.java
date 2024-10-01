package org.marvelution.buildsupport.model;

import java.net.URI;

/**
 * @author Mark Rekveld
 */
public class IssueType
{

    private URI self;
    private String id;
    private String name;
    private String description;
    private URI iconUrl;
    private boolean subtask;
    private long avatarId;

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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public URI getIconUrl()
    {
        return iconUrl;
    }

    public void setIconUrl(URI iconUrl)
    {
        this.iconUrl = iconUrl;
    }

    public boolean isSubtask()
    {
        return subtask;
    }

    public void setSubtask(boolean subtask)
    {
        this.subtask = subtask;
    }

    public long getAvatarId()
    {
        return avatarId;
    }

    public void setAvatarId(long avatarId)
    {
        this.avatarId = avatarId;
    }
}
