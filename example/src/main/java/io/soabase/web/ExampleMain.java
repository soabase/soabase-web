package io.soabase.web;

import com.google.common.collect.Maps;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.soabase.web.config.ConfigAccessor;
import io.soabase.web.config.WebConfiguration;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * A standard Dropwizard application/main
 */
public class ExampleMain extends Application<ExampleConfiguration>
{
    public static void main(String[] args) throws Exception
    {
        if ( args.length != 1 )
        {
            System.out.println("missing argument: path to assets");
            System.exit(0);
        }

        ExampleMain main = new ExampleMain();
        System.setProperty("dw.web.assetsFile", args[0]);   // normally config is set via a config
        System.setProperty("dw.web.debug", "true");         // file - but for this example this was easier
        main.run("server");
    }

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap)
    {
        // The config accessor allows Soabase Web to get its config
        ConfigAccessor<ExampleConfiguration> configAccessor = new ConfigAccessor<ExampleConfiguration>()
        {
            @Override
            public WebConfiguration getAppConfiguration(ExampleConfiguration configuration)
            {
                return configuration.web;
            }

            @Override
            public Optional<WebConfiguration> getAdminConfiguration(ExampleConfiguration configuration)
            {
                return Optional.empty();    // in this example, we're not using Sobase Web for admin
            }
        };
        bootstrap.addBundle(new WebBundle<>(this::makeModel, configAccessor));
    }

    private Object makeModel(HttpServletRequest request, String languageCode)
    {
        /*
            Use this technique to have language specific assets. See the "img"
            directory. There is a "en" directory and a "es" directory. Both
            have an image with the same name but the actual image is language specific.
         */
        Map<String, String> model = Maps.newHashMap();
        model.put("lang", languageCode);
        return model;
    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) throws Exception
    {
        // NOP
    }
}
