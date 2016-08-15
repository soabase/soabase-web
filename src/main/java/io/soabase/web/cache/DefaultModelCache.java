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
package io.soabase.web.cache;

import com.google.common.collect.Maps;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentMap;

public class DefaultModelCache implements ModelCache
{
    private final ConcurrentMap<HostLanguage, Object> cache = Maps.newConcurrentMap();

    @Override
    public Object getModel(HttpServletRequest request, HostLanguage hostLanguage, ModelMaker modelMaker)
    {
        return cache.computeIfAbsent(hostLanguage, __ -> modelMaker.createModel(request, hostLanguage));
    }
}
