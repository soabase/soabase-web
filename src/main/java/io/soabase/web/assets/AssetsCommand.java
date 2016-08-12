package io.soabase.web.assets;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.net.MediaType;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.servlets.assets.AssetServlet;
import io.dropwizard.setup.Environment;
import io.soabase.core.features.config.ComposedConfigurationAccessor;
import io.soabase.web.ContextFactory;
import io.soabase.web.RootFilter;
import io.soabase.web.WebConfiguration;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

public class AssetsCommand<T extends Configuration> extends ServerCommand<T>
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ContextFactory contextFactory;
    private volatile File assetsFile;
    private volatile InternalAssetServlet assetServlet;

    public AssetsCommand(Application<T> application, ContextFactory contextFactory)
    {
        super(application);
        this.contextFactory = contextFactory;
    }

    @Override
    public void configure(Subparser subparser)
    {
        super.configure(subparser);

        subparser.addArgument("assets").type(Arguments.fileType()).required(true).help("assets directory or zip/jar file");
    }

    public File getAssetsFile()
    {
        Preconditions.checkNotNull(assetsFile, "run() has not been called yet");
        return assetsFile;
    }

    public TextLoader getTextLoader()
    {
        Preconditions.checkNotNull(assetServlet, "run() has not been called yet");
        return assetServlet.textLoader;
    }

    public Handlebars getHandlebars()
    {
        Preconditions.checkNotNull(assetServlet, "run() has not been called yet");
        return assetServlet.handlebars;
    }

    @Override
    protected void run(Environment environment, Namespace namespace, T configuration) throws Exception
    {
        assetsFile = namespace.get("assets");

        WebConfiguration webConfiguration = ComposedConfigurationAccessor.access(configuration, environment, WebConfiguration.class);

        assetServlet = new InternalAssetServlet(webConfiguration);
        environment.servlets().addServlet("assets", assetServlet).addMapping(webConfiguration.uriPath + "/*");
        environment.servlets().addFilter("root", new RootFilter(webConfiguration.uriPath + webConfiguration.defaultFile)).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/");

        super.run(environment, namespace, configuration);
    }

    private class InternalAssetServlet extends AssetServlet
    {
        private final WebConfiguration configuration;
        private final Handlebars handlebars;
        private final ConcurrentMap<String, Template> templateCache = Maps.newConcurrentMap();
        private final ConcurrentMap<String, TemplateSource> sourceCache = Maps.newConcurrentMap();
        private final TextLoader textLoader;
        private final WebTemplateLoader templateLoader;

        public InternalAssetServlet(WebConfiguration configuration)
        {
            super("/", configuration.uriPath, configuration.defaultFile.substring(1), Charsets.UTF_8);
            this.configuration = configuration;
            textLoader = new TextLoader(assetsFile, configuration.textDir);

            templateLoader = assetsFile.isFile() ? new WebZipTemplateLoader(configuration, sourceCache, assetsFile) : new WebFileTemplateLoader(configuration, sourceCache, assetsFile);
            handlebars = new Handlebars(templateLoader);
            StringHelpers.register(handlebars);
            handlebars.registerHelper("concat", new ConcatHelper(this::getTemplate));
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
                String content = template.apply(contextFactory.getContext(request));
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
}
