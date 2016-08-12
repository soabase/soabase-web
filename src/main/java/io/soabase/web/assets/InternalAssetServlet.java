package io.soabase.web.assets;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.net.MediaType;
import io.dropwizard.servlets.assets.AssetServlet;
import io.soabase.web.WebConfiguration;
import io.soabase.web.context.ContextCache;
import io.soabase.web.context.ContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.concurrent.ConcurrentMap;

class InternalAssetServlet extends AssetServlet
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final WebConfiguration configuration;
    private final Handlebars handlebars;
    private final ConcurrentMap<String, Template> templateCache = Maps.newConcurrentMap();
    private final WebTemplateLoader templateLoader;
    private final ContextCache contextCache;

    InternalAssetServlet(WebConfiguration configuration, ContextFactory contextFactory)
    {
        super("/", configuration.uriPath, configuration.defaultFile.substring(1), Charsets.UTF_8);
        this.configuration = configuration;

        templateLoader = configuration.assetsFile.isFile() ? new WebZipTemplateLoader(configuration, configuration.assetsFile) : new WebFileTemplateLoader(configuration, configuration.assetsFile);
        handlebars = new Handlebars(templateLoader);
        StringHelpers.register(handlebars);
        handlebars.registerHelper("concat", new ConcatHelper(this::getTemplate));

        contextCache = new ContextCache(contextFactory, configuration.assetsFile, configuration.textDir, configuration.debug);
    }

    Handlebars getHandlebars()
    {
        return handlebars;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String path = (request.getPathInfo() != null) ? request.getPathInfo() : configuration.defaultFile;
        String pathExtension = Files.getFileExtension(path);
        if ( configuration.templateExtensions.contains(pathExtension) )
        {
            serveTemplate(request, response, path);
        }
        else
        {
            super.doGet(request, response);
        }
    }

    private void serveTemplate(HttpServletRequest request, HttpServletResponse response, String path)
    {
        try
        {
            Template template = getTemplate(path);
            String content = template.apply(contextCache.getContext(request));
            String mimeTypeOfExtension = request.getServletContext().getMimeType(request.getRequestURI());
            MediaType mediaType = MediaType.parse(mimeTypeOfExtension);
            response.setContentType(mediaType.type() + '/' + mediaType.subtype());
            if ( mediaType.type().equals("text") || mediaType.subtype().equals("javascript") )
            {
                response.setCharacterEncoding(Charsets.UTF_8.name());
            }
            response.setContentLength(content.length());
            CharStreams.copy(new StringReader(content), response.getWriter());
        }
        catch ( Exception e )
        {
            log.error("Could not serve template: " + path, e);
        }
    }

    private Template getTemplate(String path)
    {
        return configuration.debug ? compile(path) : templateCache.computeIfAbsent(path, key -> compile(path));
    }

    private Template compile(String path)
    {
        try
        {
            return handlebars.compile(path.substring(1));
        }
        catch ( IOException e )
        {
            throw new RuntimeException("Could not compile: " + path, e);
        }
    }

    @Override
    protected URL getResourceUrl(String absoluteRequestedResourcePath)
    {
        return templateLoader.getResourceUrl(absoluteRequestedResourcePath);
    }
}
