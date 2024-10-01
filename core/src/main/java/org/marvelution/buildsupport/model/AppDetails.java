package org.marvelution.buildsupport.model;

import java.util.*;

import com.atlassian.marketplace.client.model.*;
import com.atlassian.plugin.marketing.bean.*;

public interface AppDetails
{

	AppType getAppType();

	String getKey();

	String getName();

	String getVersion();

	String getVendorName();

	List<ProductCompatibility> getCompatibilities();
}
