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

TBD
