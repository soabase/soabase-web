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

import com.google.common.base.Preconditions;

class HostLanguage
{
    private final String host;
    private final String language;

    HostLanguage(String host, String language)
    {
        this.host = Preconditions.checkNotNull(host, "host cannot be null");
        this.language = Preconditions.checkNotNull(language, "language cannot be null");
    }

    String getHost()
    {
        return host;
    }

    String getLanguage()
    {
        return language;
    }

    @Override
    public boolean equals(Object o)
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        HostLanguage that = (HostLanguage)o;

        //noinspection SimplifiableIfStatement
        if ( !host.equals(that.host) )
        {
            return false;
        }
        return language.equals(that.language);

    }

    @Override
    public int hashCode()
    {
        int result = host.hashCode();
        result = 31 * result + language.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "HostLanguage{" + "host='" + host + '\'' + ", language='" + language + '\'' + '}';
    }
}