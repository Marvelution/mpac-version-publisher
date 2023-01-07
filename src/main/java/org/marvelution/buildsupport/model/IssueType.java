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

import java.net.*;

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
