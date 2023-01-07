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
public class JiraVersion
{

	private URI self;
	private String id;
	private String name;
	private String description;
	private boolean archived;
	private boolean released;
	private String startDate;
	private String releaseDate;
	private boolean overdue;
	private String userStartDate;
	private String userReleaseDate;
	private long projectId;

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

	public boolean isArchived()
	{
		return archived;
	}

	public void setArchived(boolean archived)
	{
		this.archived = archived;
	}

	public boolean isReleased()
	{
		return released;
	}

	public void setReleased(boolean released)
	{
		this.released = released;
	}

	public String getStartDate()
	{
		return startDate;
	}

	public void setStartDate(String startDate)
	{
		this.startDate = startDate;
	}

	public String getReleaseDate()
	{
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate)
	{
		this.releaseDate = releaseDate;
	}

	public boolean isOverdue()
	{
		return overdue;
	}

	public void setOverdue(boolean overdue)
	{
		this.overdue = overdue;
	}

	public String getUserStartDate()
	{
		return userStartDate;
	}

	public void setUserStartDate(String userStartDate)
	{
		this.userStartDate = userStartDate;
	}

	public String getUserReleaseDate()
	{
		return userReleaseDate;
	}

	public void setUserReleaseDate(String userReleaseDate)
	{
		this.userReleaseDate = userReleaseDate;
	}

	public long getProjectId()
	{
		return projectId;
	}

	public void setProjectId(long projectId)
	{
		this.projectId = projectId;
	}
}
