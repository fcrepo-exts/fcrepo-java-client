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
FcrepoClient client = FcrepoClient.client().build();
```

####Create a Fedora client with credentials
```java
FcrepoClient client = FcrepoClient.client().credentials(username, password).build();
```

###CRUD
* Create a new container with RDF properties:
```java
try (FcrepoResponse response = new PostBuilder(uri, client)
        .body(turtleFile, "text/turtle")
        .perform()) {
  URI location = response.getLocation();
  logger.debug("Container creation status and location: {}, {}", response.getStatusCode(), location);
}
```

* Uploaded file with checksum mismatch:
```java
try (FcrepoResponse response = new PostBuilder(uri, client)
        .body(pictureFile, "image/jpg")
        .digest("checksumdoesntmatch")
        .perform()) {
  String errorMessage = IOUtils.toString(response.getBody(), "UTF-8");
  logger.debug("Response status code and message: {}, {}", response.getStatusCode(), errorMessage);
}
```

* Replace triples on resource:
```java
try (FcrepoResponse response = new PutBuilder(uri, client)
      .body(turtleFile, "text/turtle")
      .preferLenient()
      .perform()) {
    logger.debug("Response status code: {}", response.getStatusCode());
}
```

* Retrieving a resource in RDF+XML format:
```java
try (FcrepoResponse response = new GetBuilder(uri, client)
        .accept("application/rdf+xml")
        .perform()) {
  String turtleContent = IOUtils.toString(response.getBody(), "UTF-8");
}
```

* Retrieving a binary/Non-RDF source:
```java
try (FcrepoResponse response = new GetBuilder(binaryUri, client)
        .perform()) {
  InputStream body = response.getBody();
  String contentType = response.getContentType();
  
  Map<String, String> disposition = response.getContentDisposition();
  String filename = disposition.get(FedoraHeaderConstants.CONTENT_DISPOSITION_FILENAME);
  String size = disposition.get(FedoraHeaderConstants.CONTENT_DISPOSITION_SIZE);
}
```

* Retrieving a resource with links to other resources, including/excluding specific preferences:
```java
List<URI> includes = Arrays.asList(
      URI.create("http://fedora.info/definitions/v4/repository#InboundReferences"));

List<URI> omits = Arrays.asList(
      URI.create("http://www.w3.org/ns/ldp#PreferMembership"),
      URI.create("http://www.w3.org/ns/ldp#PreferContainment"));
try (FcrepoResponse response = new GetBuilder(uri, client)
        .preferRepresentation(includes, omits)
        .perform()) {
  // ...
}
```

* Delete a resource:
```java
try (FcrepoResponse response = new DeleteBuilder(uri, client).perform()) {
    logger.debug("Resource deletion status: {}", response.getStatusCode());
}
```

###Versioning
* After the first version is created on a resource, you can see a triple on the resource with predicate fedora:hasVersions like below
```
<fedoraurl/resource1> fedora:hasVersions <fedoraurl/resource1/fcr:versions>
```

* Create a version:
```java
URI uri = URI.create("fedoraurl/fcr:versions");
try (FcrepoResponse response = new PostBuilder(uri, client)
        .slug("version1")
        .perform()) {
    URI location = response.getLocation();
    logger.debug("Version creation status and location: {}, {}", response.getStatusCode(), location);
}
```

* Delete a version:
```java
URI uri = URI.create("fedoraurl/fcr:versions/version1");
try (FcrepoResponse response = new DeleteBuilder(uri, client).perform()) {
    logger.debug("Version deletion status: {}", response.getStatusCode());
}
```

* Revert a version:
```java
URI uri = URI.create("fedoraurl/fcr:versions/version1");
try (FcrepoResponse response = new PatchBuilder(uri, client).perform()) {
    logger.debug("Version reversion status: {}", response.getStatusCode());
}
```

###Fixity
* Fixity only applies to Binary resources. You can see a triple on NonRdfSourceDescription with predicate fedora:hasFixityService like below
```
<fedoraurl/node1> fedora:hasFixityService <fedoraurl/node1/fcr:fixity>
```

* Fixity check:
```java
URI uri = URI.create("fedoraurl/fcr:fixity");
try (FcrepoResponse response = new GetBuilder(uri, client).perform()) {
    String turtleContent = IOUtils.toString(response.getBody(), "UTF-8");
}
```

###Batch atomic operations
* A triple on the repository root with predicate fedora:hasTransactionProvider defines the location of the transaction provider:
```
<fedoraurl/tx:transactionid/> fedora:hasTransactionProvider <fedoraurl/fcr:tx>
```

* Create a transaction:
```java
URI uri = URI.create("fedoraurl/fcr:tx");
try (FcrepoResponse response = new PostBuilder(uri, client).perform()) {
    URI location = response.getLocation();
    logger.debug("Transaction creation status and location: {}, {}", response.getStatusCode(), location);
}
```

* Keep an existing transaction alive:
```java
URI uri = URI.create("fedoraurl/tx:xxxx/fcr:tx");
try (FcrepoResponse response = new PostBuilder(uri, client).perform()) {
    logger.debug("Response status: {}", response.getStatusCode());
}
```

* Commit a transaction:
```java
URI uri = URI.create("fedoraurl/tx:xxxx/fcr:tx/fcr:commit");
try (FcrepoResponse response = new PostBuilder(uri, client).perform()) {
    logger.debug("Transaction commit status: {}", response.getStatusCode());
}
```

* Rollback a transaction:
```java
URI uri = URI.create("fedoraurl/tx:xxxx/fcr:tx/fcr:rollback");
try (FcrepoResponse response = new PostBuilder(uri, client).perform()) {
    logger.debug("Transaction rollback status: {}", response.getStatusCode());
}
```

###Processing link headers:
```java
try (FcrepoResponse response = new GetBuilder(uriForBinary, client).perform()) {
    final List<URI> links = response.getLinkHeaders(FedoraHeaderConstants.DESCRIBED_BY);
    logger.debug("'describedby' Link headers: {}", links);
}
```

* Container Link Headers
```
<http://www.w3.org/ns/ldp#Resource>;rel="type",
<http://www.w3.org/ns/ldp#Container>;rel="type",
<http://www.w3.org/ns/ldp#BasicContainer>;rel="type"
```

* NonRDFSource Link Headers
```
<http://www.w3.org/ns/ldp#Resource>;rel="type",
<http://www.w3.org/ns/ldp#NonRDFSource>;rel="type",
<http://fedoraurl/fcr:metadata>; rel="describedby"

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
* [Daniel Lamb](https://github.com/dannylamb)
* [Mike Durbin](https://github.com/mikedurbin)

