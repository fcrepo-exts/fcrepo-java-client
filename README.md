Java Client for fcrepo4
=======================

This project serves as a client library for interacting with Fedora 4
using Java.

[![Build Status](https://travis-ci.org/fcrepo4-exts/fcrepo-java-client.png?branch=master)](https://travis-ci.org/fcrepo4-exts/fcrepo-java-client)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.fcrepo.client/fcrepo-java-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.fcrepo.client/fcrepo-java-client/)

Usage Examples
--------------

###Create a Fedora client
```java
FcrepoClient testClient = FcrepoClient.client().build();
```

###CRUD

Retrieving a resource in RDF+XML format:
```java
try (FcrepoResponse response = new GetBuilder(uri, testClient)
        .accept("application/rdf+xml")
        .perform()) {
  String turtleContent = IOUtils.toString(response.getBody(), "UTF-8");
}
```

Retrieving a binary/Non-RDF source:
```java
try (FcrepoResponse response = new GetBuilder(binaryUri, testClient)
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
try (FcrepoResponse response = new GetBuilder(uri, testClient)
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
try (FcrepoResponse response = new PostBuilder(uri, testClient)
        .body(turtleFile, "text/turtle")
        .perform()) {
  URI location = response.getLocation();
  logger.debug("Container creation status and location: {}, {}", response.getStatusCode(), location);
}
```

Uploaded file with checksum mismatch:
```java
try (FcrepoResponse response = new PostBuilder(uri, testClient)
        .body(pictureFile, "image/jpg")
        .digest("checksumdoesntmatch")
        .perform()) {
  String errorMessage = IOUtils.toString(response.getBody(), "UTF-8");
  logger.debug("Response status code and message: {}, {}", response.getStatusCode(), errorMessage);
}
```

Replace triples on resource:
```java
try (FcrepoResponse response = new PutBuilder(uri, testClient)
      .body(turtleFile, "text/turtle")
      .perform()) {
    logger.debug("Response status code: {}", response.getStatusCode());
}
```

Delete a resource:
```java
try (FcrepoResponse response = new DeleteBuilder(uri, testClient).perform()) {
    logger.debug("Resource deletion status: {}", response.getStatusCode());
}
```

Move a resource:
```java
try (FcrepoResponse response = new MoveBuilder(source, destination, testClient)
        .perform()) {
  URI destinationLocation = response.getLocation();
  logger.debug("Response status code and location: {}, {}", response.getStatusCode(), destinationLocation);
}
```

###Versioning

Create a version:
```java
URI uri = URI.create("fedoraurl/fcr:versions");
try (FcrepoResponse response = new PostBuilder(uri, testClient)
        .slug("version1")
        .perform()) {
    URI location = response.getLocation();
    logger.debug("Version creation status and location: {}, {}", response.getStatusCode(), location);
}
```

Delete a version:
```java
URI uri = URI.create("fedoraurl/fcr:versions/version1");
try (FcrepoResponse response = new DeleteBuilder(uri, testClient).perform()) {
    logger.debug("Version deletion status: {}", response.getStatusCode());
}
```

Revert a version:
```java
URI uri = URI.create("fedoraurl/fcr:versions/version1");
try (FcrepoResponse response = new PatchBuilder(uri, testClient).perform()) {
    logger.debug("Version reversion status: {}", response.getStatusCode());
}
```

###Fixity
Fixity check:
```java
URI uri = URI.create("fedoraurl/fcr:fixity");
try (FcrepoResponse response = new GetBuilder(uri, testClient).perform()) {
    String turtleContent = IOUtils.toString(response.getBody(), "UTF-8");
}
```

###Batch atomic operations
Create a transaction:
```java
URI uri = URI.create("fedoraurl/fcr:tx");
try (FcrepoResponse response = new PostBuilder(uri, testClient).perform()) {
    URI location = response.getLocation();
    logger.debug("Transcation creation status and location: {}, {}", response.getStatusCode(), location);
}
```

Get a transaction status:
```java
URI uri = URI.create("fedoraurl/tx:xxxx");
try (FcrepoResponse response = new GetBuilder(uri, testClient).perform()) {
    String turtleContent = IOUtils.toString(response.getBody(), "UTF-8");
}
```

Commit a transaction:
```java
URI uri = URI.create("fedoraurl/tx:xxxx/fcr:tx/fcr:commit");
try (FcrepoResponse response = new PostBuilder(uri, testClient).perform()) {
    logger.debug("Transcation commit status: {}", response.getStatusCode());
}
```

Rollback a transaction:
```java
URI uri = URI.create("fedoraurl/tx:xxxx/fcr:tx/fcr:rollback");
try (FcrepoResponse response = new PostBuilder(uri, testClient).perform()) {
    logger.debug("Transcation rollback status: {}", response.getStatusCode());
}
```

###AuthZ
Define a authorization
```java
try (FcrepoResponse response = new PatchBuilder(uri, testClient)
        .body(authorizationSparqlFile, "application/sparql-update")
        .perform()) {
    logger.debug("Response status code: {}", response.getStatusCode());
}
```

Link acl to a container
```java
try (FcrepoResponse response = new PatchBuilder(uri, testClient)
        .body(aclSparqlFile, "application/sparql-update")
        .perform()) {
    logger.debug("Response status code: {}", response.getStatusCode());
}
```

Maven Project Settings
----------------------

Include this library dependency in the pom.xml:
```
    <dependency>
      <groupId>org.fcrepo.client</groupId>
      <artifactId>fcrepo-java-client</artifactId>
      <version>${fcrepo-client.version}</version>
    </dependency>
```
Include log4j logger dependency:
```
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>${logback.version}</version>
    </dependency>
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

