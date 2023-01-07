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

import java.util.*;

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
