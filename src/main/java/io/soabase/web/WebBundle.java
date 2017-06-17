/**
 * Copyright 2016 Jordan Zimmerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.soabase.web;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.Preconditions;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.soabase.web.assets.InternalAssetServlet;
import io.soabase.web.config.ConfigAccessor;
import io.soabase.web.config.WebConfiguration;
import io.soabase.web.context.ContextFactory;
import io.soabase.web.filters.RootFilter;
import io.soabase.web.language.RequestLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.Optional;

public class WebBundle<T extends Configuration> implements ConfiguredBundle<T>
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ContextFactory contextFactory;
    private final ConfigAccessor<T> configAccessor;
    private volatile InternalAssetServlet assetServlet;
    private volatile RequestLanguage requestLanguage;

    public WebBundle(ContextFactory contextFactory, ConfigAccessor<T> configAccessor)
    {
        this.contextFactory = contextFactory;
        this.configAccessor = configAccessor;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
        // NOP
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception
    {
        addServlet(environment, configAccessor.getAppConfiguration(configuration), false);
        Optional<WebConfiguration> adminConfiguration = configAccessor.getAdminConfiguration(configuration);
        if ( adminConfiguration.isPresent() )
        {
            addServlet(environment, adminConfiguration.get(), true);
        }
    }

    public Handlebars getHandlebars()
    {
        Preconditions.checkNotNull(assetServlet, "Handlebars not available until run() has been called");
        return assetServlet.getHandlebars();
    }

    public RequestLanguage getRequestLanguage()
    {
        Preconditions.checkNotNull(requestLanguage, "RequestLanguage not available until run() has been called");
        return requestLanguage;
    }

    protected InternalAssetServlet newInternalAssetServlet(Environment environment, WebConfiguration configuration, ContextFactory contextFactory, RequestLanguage requestLanguage)
    {
        return new InternalAssetServlet(configuration, contextFactory, requestLanguage, environment);
    }

    private void addServlet(Environment environment, WebConfiguration configuration, boolean isAdmin)
    {
        ServletEnvironment servlets = isAdmin ? environment.admin() : environment.servlets();
        requestLanguage = configuration.requestLanguage.buildAndInstall(environment, servlets);
        assetServlet = newInternalAssetServlet(environment, configuration, contextFactory, requestLanguage);
        servlets.addServlet("soabase-web-asset-servlet", assetServlet).addMapping(configuration.uriPath + "/*");
        if ( configuration.addRootFilter )
        {
            servlets.addFilter("soabase-web-root-filter", new RootFilter(configuration.uriPath + configuration.defaultFile)).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/");
        }
    }
}
