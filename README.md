[![Build Status](https://travis-ci.org/soabase/soabase-web.svg?branch=master)](https://travis-ci.org/soabase/soabase-web)
[![Maven Central](https://img.shields.io/maven-central/v/io.soabase/soabase-web.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.soabase%22%20AND%20a%3A%22soabase-web%22)

# soabase-web

Soabase Web is a [Dropwizard](http://www.dropwizard.io) extension that enables easy serving of multi-language web apps using the Java version of [Handlebars](https://github.com/jknack/handlebars.java) with the following features:

* Can serve from a directory or a ZIP/JAR file
  * Serving from a directory makes development simpler
  * Serving from a ZIP/JAR makes production deployment/management easier
* Can serve a main application and, optionally, a separate admin application
* Two modes:
  * Development - application assets are reloaded on each request for real-time development
  * Production - application assets are cached using Dropwizard's standard `CachedAsset`
* Supports multi-language subsitutions using Handlebars templates
* Optional Root filter that redirects from "/" to the configured main page
* Optional Language filter for managing page language via a cookie

## Example

See the [Example](example) for an annotated example of using Soabase Web.

## Usage

Soabase Web is available from Maven Central:

* GroupId - `io.soabase`
* ArtifactId - `soabase-web`

Soabase Web is a [Dropwizard](http://www.dropwizard.io) bundle. Add the `WebBundle` to your Dropwizard application's bootstrap. `WebBundle` takes two arguments: `ContextFactory` and `ConfigAccessor`.

### ConfigAccessor

You must pass an instance to a `ConfigAccessor` implementation to the `WebBundle` constructor. Soabase Web uses this instance to access its configuration information. `ConfigAccessor` has two methods:

* `getAppConfiguration()`
* `getAdminConfiguration()`

Both return a `WebConfiguration` instance containing configuration info for Soabase Web. See below for the configuration details. `getAdminConfiguration()` returns an Optional. Return an `Optional.empty()` if you don't want a Soabase Web admin implementation.

### ContextFactory

The `ContextFactory` returns a [Handlebars](https://github.com/jknack/handlebars.java) model object for the given request and language. You can return an empty HashMap or any type of object you'd like to use. Note: Soabase web (unless configured not to) will cache the model object for the request URL and language.

### WebConfiguration

| Field | Default | Description |
| ----- | ------- | ----------- |
| debug | false   | If true no files or models are cached |
| defaultFile | "/index.html" | The path to use when none is specified in the URL |
| uriPath | "/web" | The base URI path. This portion of the URI is ignored when searching for the file to return |
| templateExtensions | "js", "html", "htm", "css", "template" | Extensions for files that should be passed through the Handlebars template processor |
| textDir | "text" | The directory relative path that contains language files (see below) |
| assetsFile | null | Path to the directory or zip file with the website assets |
| addRootFilter | true | If true, adds a servlet filter that routes "root" requests (i.e. "/") to the default file | 
| requestLanguage | DefaultRequestLanguageFactory | the langauge handler (see below) |
| modelCache | DefaultModelCacheFactory | The model cache factory (see below) |

### Language Files

Soabase Web can enhance your Handlebars models with language specific text. This makes it much easier to translate your web application. A language file is a simple text file. The name of the file is `LANG.txt` where LANG is the language code (e.g. `en.txt`). The file format is also simple: each line starts with a key for the text followed by a whitespace (or an '=' sign) and then the text. The key is added to the Handlebars model on each request with the appropriate value for the request language. E.g.

**en.txt**
```
nameStr   Enter your name
```

**es.txt**
```
nameStr   Introduzca su nombre
```

**index.html**
```
...
<div>{{nameStr}}</div>
...
```

### RequestLanguage and RequestLanguageFactory

`RequestLanguageFactory` and `RequestLanguage` is used to determine the language code for each request. You can add custom RequestLanguageFactory instances using the standard Dropwizard method (see [Polymorphic configuration](http://www.dropwizard.io/1.0.0/docs/manual/configuration.html)). The default implementation, `DefaultRequestLanguageFactory`, uses cookies and query params to store/change the language. It has the following available configuration:

| Field | Default | Description |
| ----- | ------- | ----------- |
| cookieName | "soabase_web_langauge"   | The name of the cookie that stores the language code |
| queryParameterName | "lang" | The name of the query parameter used to change the language |
| defaultLanguageCode | "en" | The default language code if none is set |
| validLanguageCodes | empty | Used to limit which language codes are accepted |

The `DefaultRequestLanguageFactory` adds a servlet filter that looks for the configured queryParameterName on every request. If found, the language cookie is set.

The `RequestLanguage` interface is used to return the language code for a request. The `DefaultRequestLanguageFactory` returns the value of the cookie or the default.

### ModelCache and ModelCacheFactory

In most cases, it doesn't make sense to recreate a Handlebars model on each request. Therefore, Soabase Web supports caching the model.  You can add custom ModelCacheFactory instances using the standard Dropwizard method (see [Polymorphic configuration](http://www.dropwizard.io/1.0.0/docs/manual/configuration.html)). The default factory, `DefaultModelCacheFactory` caches the model for each unique host name and language. 

The `ModelCache` instance is used to return either a cached model or to use the given method to create a new model, cache it, and return it.

### Handlebars Extensions

* The [Handlebars StringHelper](https://github.com/jknack/handlebars.java/blob/master/handlebars/src/main/java/com/github/jknack/handlebars/helper/StringHelpers.java) is automatically added by Soabase Web
* Language Files (see above)
* ConcatHelper - makes it easy to concat files together at runtime. Usage: `{{{concat "base-dir" "file-1" ... "file-2"}}}`. All the specified files from the given relative directory are concatenated as if they were a single file
