package io.soabase.web.assets;

import com.github.jknack.handlebars.io.TemplateLoader;
import java.net.URL;

public interface WebTemplateLoader extends TemplateLoader
{
    URL getResourceUrl(String absoluteRequestedResourcePath);
}
