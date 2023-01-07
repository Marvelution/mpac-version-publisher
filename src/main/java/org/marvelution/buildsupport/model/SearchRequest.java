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
