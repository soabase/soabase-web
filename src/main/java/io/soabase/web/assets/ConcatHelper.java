/**
 * Copyright 2016 Jordan Zimmerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
