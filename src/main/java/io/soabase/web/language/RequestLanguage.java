package io.soabase.web.language;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

public interface RequestLanguage
{
    String getLanguageCode(HttpHeaders httpHeaders, UriInfo uriInfo);

    String getLanguageCode(HttpServletRequest request);

    String getLanguageCode(String queryString, Optional<String> cookie);
}
