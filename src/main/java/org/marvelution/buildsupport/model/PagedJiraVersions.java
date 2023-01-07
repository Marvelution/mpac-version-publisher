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
import java.util.*;

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
