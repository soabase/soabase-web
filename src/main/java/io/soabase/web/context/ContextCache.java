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
import io.soabase.web.cache.HostLanguage;
import io.soabase.web.cache.ModelCache;
import io.soabase.web.cache.ModelMaker;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.concurrent.ConcurrentMap;

public class ContextCache implements ModelMaker
{
    private final ConcurrentMap<HostLanguage, Context> cache = Maps.newConcurrentMap();
    private final ContextFactory contextFactory;
    private final ModelCache modelCache;
    private final TextLoader textLoader;
    private final boolean debug;

    public ContextCache(ContextFactory contextFactory, File assets, String textDir, boolean debug, ModelCache modelCache)
    {
        this.contextFactory = contextFactory;
        this.modelCache = modelCache;
        this.textLoader = new TextLoader(assets, textDir, debug);
        this.debug = debug;
    }

    public Context getContext(HttpServletRequest request, String languageCode)
    {
        HostLanguage hostLanguage = new HostLanguage(request.getServerName(), languageCode);
        Object model = debug ? createModel(request, hostLanguage) : modelCache.getModel(request, hostLanguage, this);
        Context context = Context.newContext(model);
        return Context.newContext(context, textLoader.getFor(hostLanguage.getLanguage()));
    }

    @Override
    public Object createModel(HttpServletRequest request, HostLanguage hostLanguage)
    {
        return contextFactory.getModel(request, hostLanguage.getLanguage());
    }
}
