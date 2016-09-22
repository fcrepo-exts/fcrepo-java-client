Java Client for fcrepo4
=======================

This project serves as a client library for interacting with Fedora 4
using Java.

[![Build Status](https://travis-ci.org/fcrepo4-exts/fcrepo-java-client.png?branch=master)](https://travis-ci.org/fcrepo4-exts/fcrepo-java-client)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.fcrepo.client/fcrepo-java-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.fcrepo.client/fcrepo-java-client/)

Usage Examples
--------------

Retrieving a resource in RDF+XML format:
```java
try (FcrepoResponse response = testClient.get(uri)
        .accept("application/rdf+xml")
        .perform()) {
  String turtleContent = IOUtils.toString(response.getBody(), "UTF-8");
}
```

Retrieving a binary/Non-RDF source:
```java
try (FcrepoResponse response = testClient.get(binaryUri)
        .perform()) {
  InputStream body = response.getBody();
  String contentType = response.getContentType();
  
  Map<String, String> disposition = response.getContentDisposition();
  String filename = disposition.get(FedoraHeaderConstants.CONTENT_DISPOSITION_FILENAME);
  String size = disposition.get(FedoraHeaderConstants.CONTENT_DISPOSITION_SIZE);
}
```

Retrieving a resource with links to other resources, including/excluding specific preferences:
```java
List<URI> includes = Arrays.asList(
      URI.create("http://fedora.info/definitions/v4/repository#InboundReferences"));

List<URI> omits = Arrays.asList(
      URI.create("http://www.w3.org/ns/ldp#PreferMembership"),
      URI.create("http://www.w3.org/ns/ldp#PreferContainment"));
try (FcrepoResponse response = testClient.get(uri)
        .preferRepresentation(includes, omits)
        .perform()) {
  // ...
}
```

Retrieving a resource with external content (which redirects to the URL specified in the
`message/external-body` Content-Type property by default):
```java
try (FcrepoResponse response = testClient.get(uri).disableRedirects().perform()) {
  // ...
}
```

Create a new container with RDF properties:
```java
try (FcrepoResponse response = testClient.post(uri)
        .body(turtleFile, "text/turtle")
        .perform()) {
  URI newResourceLocation = response.getLocation();
}
```

Uploaded file with checksum mismatch:
```java
try (FcrepoResponse response = testClient.post(uri)
        .body(pictureFile, "image/jpg")
        .digest("checksumdoesntmatch")
        .perform()) {
  assertEquals(409, response.getStatusCode());
  String errorMessage = IOUtils.toString(response.getBody(), "UTF-8");
}
```

Replace triples on resource:
```java
try (FcrepoResponse response = testClient.put(uri)
      .body(turtleFile, "text/turtle")
      .perform()) {
  // ...
}
```

Delete a resource:
```java
try (FcrepoResponse response = testClient.delete(uri)
      .perform()) {
```

Move a resource:
```java
try (FcrepoResponse response = testClient.move(source, destination)
        .perform()) {
  URI destinationLocation = response.getLocation();
}
```

History
-------

The stateless core of this codebase was written as part of the 
fcrepo-camel project but has since been extracted to be an independent
library so that it may be used in other applications.

Including in your project
-------------------------

You can include the `fcrepo-java-client` library in your project with the following coordinates:

### Maven (`pom.xml`)

```
<dependency>
  <groupId>org.fcrepo.client</groupId>
  <artifactId>fcrepo-java-client</artifactId>
  <version>${fcrepo-java-client.version}</version>
</dependency>
```

### Gradle (`build.gradle`)

```
dependencies {
    compile group: 'org.fcrepo.client', name: 'fcrepo-java-client', version: fcrepoJavaClientVersion
}
```

Maintainers
-----------

Current maintainers:
* [Aaron Coburn](https://github.com/acoburn)
* [Daniel Lamb](https://github.com/dannylamb)
* [Mike Durbin](https://github.com/mikedurbin)
