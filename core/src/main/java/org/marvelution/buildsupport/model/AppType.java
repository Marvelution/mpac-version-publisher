package org.marvelution.buildsupport.model;

import com.atlassian.marketplace.client.api.*;
import com.atlassian.plugin.tool.bean.*;

/**
 * @author Mark Rekveld
 */
public enum AppType
{

	CLOUD("cloud", HostingType.CLOUD),
	SERVER("server", HostingType.SERVER),
	DATA_CENTER("data-center", HostingType.DATA_CENTER),
	BOTH("both", HostingType.DATA_CENTER, HostingType.SERVER);

	final String name;
	final HostingType[] hostingTypes;

	AppType(
			String name,
			HostingType... hostingTypes)
	{
		this.name = name;
		this.hostingTypes = hostingTypes;
	}

	public static AppType fromParam(Parameter parameter)
	{
		for (AppType appType : values())
		{
			if (appType.name.equals(parameter.getValue()))
			{
				return appType;
			}
		}
		throw new IllegalArgumentException("unknown app type: " + parameter.getValue());
	}

	public HostingType[] hostingTypes()
	{
		return hostingTypes;
	}
}
