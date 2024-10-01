package org.marvelution.buildsupport.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.atlassian.plugin.marketing.bean.PluginMarketing;
import com.atlassian.plugin.marketing.bean.ProductCompatibility;
import com.atlassian.plugin.tool.PluginDetails;
import com.atlassian.plugin.tool.bean.PluginConfigurationBean;
import com.atlassian.plugin.tool.bean.PluginInfoBean;

public class PluginAppDetails
        implements AppDetails
{

    private static final String PLUGIN_TYPE = "plugin-type";
    private final PluginDetails pluginDetails;
    private final AppType appType;

    public PluginAppDetails(PluginDetails pluginDetails)
    {
        this.pluginDetails = pluginDetails;
        appType = Optional.of(pluginDetails)
                .map(PluginDetails::getPluginBean)
                .map(PluginConfigurationBean::getPluginInfo)
                .map(PluginInfoBean::getParameters)
                .orElse(Collections.emptyList())
                .stream()
                .filter(param -> PLUGIN_TYPE.equals(param.getName()))
                .findFirst()
                .map(AppType::fromParam)
                .orElse(AppType.SERVER);
    }

    @Override
    public AppType getAppType()
    {
        return appType;
    }

    @Override
    public String getKey()
    {
        return pluginDetails.getPluginBean()
                .getKey();
    }

    @Override
    public String getName()
    {
        return pluginDetails.getPluginBean()
                .getName();
    }

    @Override
    public String getVersion()
    {
        return pluginDetails.getPluginBean()
                .getPluginInfo()
                .getVersion();
    }

    @Override
    public String getVendorName()
    {
        return pluginDetails.getPluginBean()
                .getPluginInfo()
                .getVendor()
                .getName();
    }

    @Override
    public List<ProductCompatibility> getCompatibilities()
    {
        return Optional.ofNullable(pluginDetails.getMarketingBean())
                .map(PluginMarketing::getCompatibility)
                .orElseGet(Collections::emptyList);
    }
}
