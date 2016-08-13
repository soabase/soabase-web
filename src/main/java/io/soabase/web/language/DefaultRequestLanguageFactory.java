package io.soabase.web.language;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Environment;
import io.soabase.web.filters.LanguageFilter;
import javax.servlet.DispatcherType;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@JsonTypeName("default")
public class DefaultRequestLanguageFactory implements RequestLanguageFactory
{
    @NotNull
    public String cookieName = "soabase_web_langauge";

    @NotNull
    public String queryParameterName = "lang";

    @NotNull
    public String defaultLanguageCode = "en";

    @NotNull
    public Set<String> validLanguageCodes = Collections.emptySet();

    @Override
    public RequestLanguage buildAndInstall(Environment environment, ServletEnvironment servlets)
    {
        LanguageFilter languageFilter = new LanguageFilter(cookieName, queryParameterName, defaultLanguageCode, validLanguageCodes);
        servlets.addFilter("soabase-web-language-filter", languageFilter).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        return languageFilter;
    }
}
