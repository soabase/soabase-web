package io.soabase.web.assets;

import com.github.jknack.handlebars.io.TemplateSource;
import com.github.jknack.handlebars.io.URLTemplateLoader;
import com.google.common.collect.Maps;
import io.soabase.web.WebConfiguration;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentMap;

class WebZipTemplateLoader extends URLTemplateLoader implements WebTemplateLoader
{
    private final WebConfiguration configuration;
    private final ConcurrentMap<String, TemplateSource> sourceCache = Maps.newConcurrentMap();
    private final URLClassLoader classLoader;

    WebZipTemplateLoader(WebConfiguration configuration, File zipFile)
    {
        this.configuration = configuration;
        URLClassLoader classLoader;
        try
        {
            classLoader = new URLClassLoader(new URL[]{zipFile.toURI().toURL()});
        }
        catch ( MalformedURLException e )
        {
            throw new RuntimeException(e);
        }
        this.classLoader = classLoader;

        setSuffix("");
        setPrefix("");
    }

    @Override
    public URL getResourceUrl(String absoluteRequestedResourcePath)
    {
        try
        {
            return getResource(absoluteRequestedResourcePath);
        }
        catch ( IOException e )
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected URL getResource(String location) throws IOException
    {
        if ( location.startsWith("/") )
        {
            location = (location.length() > 1) ? location.substring(1) : "";
        }
        return classLoader.getResource(location);
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
}
