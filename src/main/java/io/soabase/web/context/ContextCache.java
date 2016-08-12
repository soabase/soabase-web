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
package io.soabase.web.context;

import com.github.jknack.handlebars.Context;
import com.google.common.collect.Maps;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.concurrent.ConcurrentMap;

public class ContextCache
{
    private final ConcurrentMap<HostLanguage, Context> cache = Maps.newConcurrentMap();
    private final ContextFactory contextFactory;
    private final TextLoader textLoader;
    private final boolean debug;

    public ContextCache(ContextFactory contextFactory, File assets, String textDir, boolean debug)
    {
        this.contextFactory = contextFactory;
        this.textLoader = new TextLoader(assets, textDir);
        this.debug = debug;
    }

    public Context getContext(HttpServletRequest request)
    {
        HostLanguage hostLanguage = new HostLanguage(request.getServerName(), contextFactory.getLanguageCode(request));
        if ( debug )
        {
            return makeLanguageContext(request, hostLanguage);
        }
        return cache.computeIfAbsent(hostLanguage, x -> makeLanguageContext(request, hostLanguage));
    }

    private Context makeLanguageContext(HttpServletRequest request, HostLanguage hostLanguage)
    {
        Context context = Context.newContext(contextFactory.getModel(request));
        return Context.newContext(context, textLoader.getFor(hostLanguage.getLanguage()));
    }
}
