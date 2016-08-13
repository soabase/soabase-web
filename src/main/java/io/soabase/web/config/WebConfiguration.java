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
package io.soabase.web.config;

import com.google.common.collect.Sets;
import io.soabase.web.language.DefaultRequestLanguageFactory;
import io.soabase.web.language.RequestLanguageFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Set;

public class WebConfiguration
{
    public boolean debug = false;

    @NotNull
    public String defaultFile = "/index.html";

    @NotNull
    public String uriPath = "/web";

    @NotNull
    public Set<String> templateExtensions = Sets.newHashSet("js", "html", "htm", "css", "template");

    @NotNull
    public String textDir = "text";

    @NotNull
    public File assetsFile = null;

    public boolean addRootFilter = true;

    @NotNull
    @Valid
    public RequestLanguageFactory requestLanguage = new DefaultRequestLanguageFactory();
}
