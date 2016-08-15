[![Build Status](https://travis-ci.org/soabase/soabase-web.svg?branch=master)](https://travis-ci.org/soabase/soabase-web)
[![Maven Central](https://img.shields.io/maven-central/v/io.soabase/soabase-web.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.soabase%22%20AND%20a%3A%22soabase-web%22)

# soabase-web

Soabase Web is a [Dropwizard](http://www.dropwizard.io) that enables easy serving of multi-language web apps using the Java version of [Handlebars](https://github.com/jknack/handlebars.java) with the following features:

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

Both return `WebConfiguration` instance containing configuration info for Soabase Web. See below for the configuration details. `getAdminConfiguration()` returns an Optional. Return an `Optional.empty()` if you don't want a Soabase Web admin implementation.

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
| cacheModels | true | If true, the Handlebars model for a given path and language is cached |
| requestLanguage | DefaultRequestLanguageFactory | the langauge handler (see below) |
