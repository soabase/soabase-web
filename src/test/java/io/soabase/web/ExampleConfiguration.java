package io.soabase.web;

import io.dropwizard.Configuration;
import io.soabase.web.config.WebConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ExampleConfiguration extends Configuration
{
    @NotNull
    @Valid
    public WebConfiguration web = new WebConfiguration();
}
