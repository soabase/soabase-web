package io.soabase.web.assets;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import java.io.IOException;
import java.util.function.Function;

public class ConcatHelper implements Helper<String>
{
    private final Function<String, Template> templateFunction;

    public ConcatHelper(Function<String, Template> templateFunction)
    {
        this.templateFunction = templateFunction;
    }

    @Override
    public CharSequence apply(String baseDir, Options options) throws IOException
    {
        StringBuilder content = new StringBuilder();
        if ( !baseDir.endsWith("/") )
        {
            baseDir += "/";
        }
        if ( !baseDir.startsWith("/") )
        {
            baseDir = "/" + baseDir;
        }
        for ( Object spec : options.params )
        {
            Template template = templateFunction.apply(baseDir + spec);
            content.append(options.apply(template)).append('\n');
        }

        return content.toString();
    }
}
