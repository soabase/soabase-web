package io.soabase.web.language;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultRequestLanguageFactory.class)
public interface RequestLanguageFactory extends Discoverable
{
    RequestLanguage buildAndInstall(Environment environment, ServletEnvironment servletEnvironment);
}
