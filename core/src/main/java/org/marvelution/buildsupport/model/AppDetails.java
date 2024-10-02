package org.marvelution.buildsupport.model;

import java.util.List;

import com.atlassian.plugin.marketing.bean.ProductCompatibility;

public interface AppDetails
{

    AppType getAppType();

    String getKey();

    String getName();

    String getVersion();

    String getVendorName();

    List<ProductCompatibility> getCompatibilities();
}
