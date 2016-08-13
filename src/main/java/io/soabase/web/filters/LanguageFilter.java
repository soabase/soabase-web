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
package io.soabase.web.filters;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.soabase.web.language.RequestLanguage;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class LanguageFilter implements Filter, RequestLanguage
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String cookieName;
    private final String queryParameterName;
    private final String defaultLanguageCode;
    private final Set<String> validLanguageCodes;

    public LanguageFilter(String cookieName, String queryParameterName, String defaultLanguageCode, Set<String> validLanguageCodes)
    {
        this.cookieName = Preconditions.checkNotNull(cookieName, "cookieName cannot be null");
        this.queryParameterName = Preconditions.checkNotNull(queryParameterName, "queryParameterName cannot be null");
        this.defaultLanguageCode = Preconditions.checkNotNull(defaultLanguageCode, "defaultLanguageCode cannot be null");
        this.validLanguageCodes = ImmutableSet.copyOf(Preconditions.checkNotNull(validLanguageCodes, "validLanguageCodes cannot be null"));
    }

    @Override
    public String getLanguageCode(HttpHeaders httpHeaders, UriInfo uriInfo)
    {
        javax.ws.rs.core.Cookie cookie = httpHeaders.getCookies().get(cookieName);
        Optional<String> cookieValue = Optional.ofNullable((cookie != null) ? cookie.getValue() : null);
        return getLanguageCode(uriInfo.getRequestUri().getQuery(), cookieValue);
    }

    @Override
    public String getLanguageCode(HttpServletRequest request)
    {
        return getLanguageCode(request.getQueryString(), getCookie(request));
    }

    @Override
    public String getLanguageCode(String queryString, Optional<String> cookie)
    {
        String code = getFromQueryString(queryString, new AtomicReference<>());
        if ( code != null )
        {
            return code;
        }

        if ( cookie.isPresent() )
        {
            try
            {
                return validate(cookie.get());
            }
            catch ( IllegalArgumentException e )
            {
                log.debug("Cookie set to invalid language", cookie.get());
            }
        }
        return defaultLanguageCode;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // NOP
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        if ( request instanceof HttpServletRequest )
        {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            AtomicReference<String> fixedQueryString = new AtomicReference<>();
            String queryStringCode = getFromQueryString(httpRequest.getQueryString(), fixedQueryString);
            String expectedLanguageCode = MoreObjects.firstNonNull(queryStringCode, getLanguageCode(null, getCookie(httpRequest)));
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            Optional<String> foundCookie = getCookie(httpRequest);
            if ( !foundCookie.isPresent() || !foundCookie.get().equals(expectedLanguageCode) )
            {
                Cookie cookie = new Cookie(cookieName, expectedLanguageCode);
                httpResponse.addCookie(cookie);
            }

            if ( queryStringCode != null )
            {
                StringBuffer redirectUrl = httpRequest.getRequestURL();
                if ( !fixedQueryString.get().isEmpty() )
                {
                    redirectUrl.append("?").append(fixedQueryString.get());
                }
                ((HttpServletResponse)response).sendRedirect(redirectUrl.toString());
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
        // NOP
    }

    private String getFromQueryString(String queryString, AtomicReference<String> fixedQueryString)
    {
        if ( (queryString != null) && !queryString.trim().isEmpty() )
        {
            fixedQueryString.set(queryString);
            List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(queryString, Charsets.UTF_8);
            Optional<NameValuePair> first = nameValuePairs.stream()
                .filter(vp -> vp.getName().equals(queryParameterName))
                .findFirst();
            if ( first.isPresent() )
            {
                try
                {
                    List<NameValuePair> copy = nameValuePairs.stream()
                        .filter(p -> p != first.get())
                        .collect(Collectors.toList());
                    fixedQueryString.set(URLEncodedUtils.format(copy, Charsets.UTF_8));
                    return validate(first.get().getValue());
                }
                catch ( IllegalArgumentException ignore )
                {
                    log.debug("Query param set to invalid language", first.get());
                }
            }
        }
        return null;
    }

    private String validate(String code)
    {
        return (validLanguageCodes.isEmpty() || validLanguageCodes.contains(code)) ? code : defaultLanguageCode;
    }

    private Optional<String> getCookie(HttpServletRequest httpRequest)
    {
        return getCookies(httpRequest).stream()
            .filter(cookie -> cookie.getName().equals(cookieName))
            .map(Cookie::getValue)
            .findFirst();
    }

    private static List<Cookie> getCookies(HttpServletRequest httpRequest)
    {
        if ( httpRequest.getCookies() != null )
        {
            return Arrays.asList(httpRequest.getCookies());
        }
        return ImmutableList.of();
    }
}
