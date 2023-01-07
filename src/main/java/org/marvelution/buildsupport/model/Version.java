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

import java.util.regex.*;
import javax.annotation.*;

/**
 * @author Mark Rekveld
 */
public class Version
		implements Comparable<Version>
{

	private static final Pattern VERSION_SEPARATOR = Pattern.compile("[-_./;:]");
	private final int major;
	private final int minor;
	private final int patch;
	private final String snapshot;

	public Version(int major)
	{
		this(major, 0, 0, null);
	}

	public Version(
			int major,
			int minor)
	{
		this(major, minor, 0, null);
	}

	public Version(
			int major,
			int minor,
			int patch,
			@Nullable String snapshot)
	{
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.snapshot = snapshot;
	}

	public int getMajorVersion()
	{
		return major;
	}

	public int getMinorVersion()
	{
		return minor;
	}

	public int getPatchLevel()
	{
		return patch;
	}

	@Nullable
	public String getSnapshot()
	{
		return snapshot;
	}

	@Override
	public int compareTo(@Nonnull Version other)
	{
		if (major != other.major)
		{
			return Integer.compare(major, other.major);
		}
		else if (minor != other.minor)
		{
			return Integer.compare(minor, other.minor);
		}
		else
		{
			return patch != other.patch ? Integer.compare(patch, other.patch) : 0;
		}
	}

	public boolean isGreaterThan(@Nonnull Version version)
	{
		return compareTo(version) > 0;
	}

	public boolean isGreaterOrEqualTo(@Nonnull Version version)
	{
		return !isLessThan(version);
	}

	public boolean isLessThan(@Nonnull Version version)
	{
		return compareTo(version) < 0;
	}

	public static Version parseVersion(String versionStr)
	{
		if (versionStr == null)
		{
			return null;
		}
		versionStr = versionStr.trim();
		if (versionStr.length() == 0)
		{
			return null;
		}
		String[] parts = VERSION_SEPARATOR.split(versionStr);
		// Let's not bother if there's no separate parts; otherwise use whatever we got
		if (parts.length < 2)
		{
			return null;
		}
		int major = parseVersionPart(parts[0]);
		int minor = parseVersionPart(parts[1]);
		int patch = (parts.length > 2) ? parseVersionPart(parts[2]) : 0;
		String snapshot = (parts.length > 3) ? parts[3] : null;
		return new Version(major, minor, patch, snapshot);
	}

	protected static int parseVersionPart(String partStr)
	{
		int len = partStr.length();
		int number = 0;
		for (int i = 0;
		     i < len;
		     ++i)
		{
			char c = partStr.charAt(i);
			if (c > '9' || c < '0')
			{
				break;
			}
			number = (number * 10) + (c - '0');
		}
		return number;
	}
}
