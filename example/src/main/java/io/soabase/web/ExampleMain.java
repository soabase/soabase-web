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
        System.setProperty("dw.web.assetsFile", args[0]);
        System.setProperty("dw.web.debug", "true");
        main.run("server");
    }

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap)
    {
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
                return Optional.empty();
            }
        };
        bootstrap.addBundle(new WebBundle<>(this::makeModel, configAccessor));
    }

    private Object makeModel(HttpServletRequest request, String languageCode)
    {
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
