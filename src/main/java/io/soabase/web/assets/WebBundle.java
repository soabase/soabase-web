package io.soabase.web.assets;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.Preconditions;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.soabase.web.ConfigAccessor;
import io.soabase.web.RootFilter;
import io.soabase.web.WebConfiguration;
import io.soabase.web.context.ContextFactory;
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

    public Handlebars getHandlebars()
    {
        Preconditions.checkNotNull(assetServlet, "run() has not been called yet");
        return assetServlet.getHandlebars();
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

    private void addServlet(Environment environment, WebConfiguration configuration, boolean isAdmin)
    {
        Preconditions.checkArgument(configuration.assetsFile.exists(), configuration.assetsFile + " does not exist");

        assetServlet = new InternalAssetServlet(configuration, contextFactory);
        ServletEnvironment servlets = isAdmin ? environment.admin() : environment.servlets();
        servlets.addServlet("assets", assetServlet).addMapping(configuration.uriPath + "/*");
        servlets.addFilter("root", new RootFilter(configuration.uriPath + configuration.defaultFile)).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/");
    }

}
