package io.soabase.web.assets;

import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.Maps;
import io.soabase.web.config.WebConfiguration;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentMap;

class WebFileTemplateLoader extends FileTemplateLoader implements WebTemplateLoader
{
    private final WebConfiguration configuration;
    private final ConcurrentMap<String, TemplateSource> sourceCache = Maps.newConcurrentMap();
    private final File assetsDir;

    WebFileTemplateLoader(WebConfiguration configuration, File assetsDir)
    {
        super(assetsDir, "");
        this.configuration = configuration;
        this.assetsDir = assetsDir;
    }

    @Override
    public TemplateSource sourceAt(String uri) throws IOException
    {
        if ( configuration.debug )
        {
            return super.sourceAt(uri);
        }
        return sourceCache.computeIfAbsent(uri, this::getTemplateSource);
    }

    private TemplateSource getTemplateSource(String key)
    {
        try
        {
            return super.sourceAt(key);
        }
        catch ( IOException e )
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getResourceUrl(String absoluteRequestedResourcePath)
    {
        try
        {
            return new URL("file://" + new File(assetsDir, absoluteRequestedResourcePath));
        }
        catch ( MalformedURLException e )
        {
            throw new RuntimeException(e);
        }
    }
}
