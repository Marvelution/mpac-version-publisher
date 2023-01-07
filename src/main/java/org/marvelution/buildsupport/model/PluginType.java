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

import com.atlassian.marketplace.client.api.*;
import com.atlassian.plugin.tool.bean.*;

/**
 * @author Mark Rekveld
 */
public enum PluginType
{

	SERVER("server", HostingType.SERVER),
	DATA_CENTER("data-center", HostingType.DATA_CENTER),
	BOTH("both", HostingType.DATA_CENTER, HostingType.SERVER);

	final String name;
	final HostingType[] hostingTypes;

	PluginType(
			String name,
			HostingType... hostingTypes)
	{
		this.name = name;
		this.hostingTypes = hostingTypes;
	}

	public static PluginType fromParam(Parameter parameter)
	{
		for (PluginType pluginType : values())
		{
			if (pluginType.name.equals(parameter.getValue()))
			{
				return pluginType;
			}
		}
		throw new IllegalArgumentException("unknown plugin type: " + parameter.getValue());
	}

	public HostingType[] hostingTypes()
	{
		return hostingTypes;
	}
}
