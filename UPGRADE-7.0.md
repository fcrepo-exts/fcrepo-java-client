# Upgrading to fcrepo-java-client 7.0

This release upgrades every dependency of the client to its latest available
version and aligns the project with the Fedora 7 platform. The library now
builds as `7.0.0-SNAPSHOT` against `fcrepo-parent` / `fcrepo-webapp` 7.0.0.

## Changes that impact users of this library

### Java 21 is now required

The project inherits from `fcrepo-parent` 7.0.0, which compiles with
`--release 21` (previously Java 11). Consumers must run on a Java 21 (or
newer) runtime to use this version of the client.

### Public API is unchanged

No classes, methods, or signatures in `org.fcrepo.client` were added, removed,
or modified. Apart from the new platform requirements below, this version is a
drop-in replacement.

The client continues to expose Apache HttpClient 4.x types
(`CloseableHttpClient`, `CloseableHttpResponse`, etc.) in its API. It was
deliberately **not** migrated to HttpClient 5 (`org.apache.httpcomponents.client5`),
which is a different artifact with an incompatible API; `httpclient` 4.5.14 is
the latest release of the 4.x line and remains maintained.

### Transitive (compile-scope) dependency upgrades

These land on the classpath of every consumer and are the changes most likely
to interact with other libraries in your application:

| Dependency | Old | New | Notes |
|---|---|---|---|
| `org.springframework:spring-web` | 5.3.10 | 7.0.8 | Spring Framework 7 is Jakarta-based and requires Java 17+. The client only uses `org.springframework.http.ContentDisposition` (for the `filename(...)` builder methods), so any Spring 6.x/7.x on your classpath is compatible. Applications still pinned to Spring 5 should manage the version down locally — the `ContentDisposition` usage is source-compatible back to 5.3. |
| `org.slf4j:slf4j-api` | 1.7.32 | 2.0.18 | Requires an SLF4J 2-compatible logging backend (Logback ≥ 1.3, `log4j-slf4j2-impl`, etc.). SLF4J 1.x bindings such as Logback 1.2 are **not** picked up by SLF4J 2. |
| `org.apache.httpcomponents:httpclient` | 4.5.13 | 4.5.14 | Patch release, no API changes. |
| `org.apache.commons:commons-lang3` | 3.12.0 | 3.20.0 | No API changes affecting this library. |

### Fedora compatibility

Integration tests now run against **Fedora (fcrepo-webapp) 7.0.0** deployed on
Jetty 12 (Jakarta EE 10). The Fedora HTTP API used by this client is unchanged
between Fedora 6.x and 7.x, so the client remains wire-compatible with Fedora
6 servers, but 7.0.0 is the version this release is verified against.

One Fedora 7 server behavior change surfaced by the integration tests, which
client users should be aware of:

* **`PATCH` responses no longer carry the updated `ETag`.** Against Fedora 6,
  the `ETag` header on a successful `PATCH` response reflected the new state
  of the resource. Against Fedora 7.0.0 it reflects the state prior to the
  update. If you need the post-update `ETag` (e.g. for optimistic locking with
  `If-Match`), issue a fresh `HEAD`/`GET` after the `PATCH`.

## Changes that do not affect consumers at runtime

Test- and build-scope upgrades, listed for completeness:

| Dependency / plugin | Old | New |
|---|---|---|
| JUnit | 4.12 (`junit:junit`, transitive) | 5.14.4 (`org.junit.jupiter:junit-jupiter`) |
| `org.mockito:mockito-core` | 2.23.0 | 5.23.0 (plus `mockito-junit-jupiter` for the JUnit 5 extension) |
| `org.mock-server:mockserver-*` | 5.4.1 | 7.0.0 |
| `org.glassfish.jersey.core:jersey-common` | 2.35 | 4.0.2 |
| `org.apache.jena:jena-core` / `jena-arq` | 4.2.0 | 6.1.0 |
| `ch.qos.logback:logback-classic` | 1.2.6 | 1.5.34 |
| `commons-io:commons-io` | 2.11.0 | 2.22.0 |
| `jetty-maven-plugin` | 9.4.44 (`org.eclipse.jetty`) | 12.1.10 (`org.eclipse.jetty.ee10:jetty-ee10-maven-plugin`) |

Notable build/test details:

* **JUnit 5 migration**: the whole test suite (unit and integration) was
  migrated from JUnit 4 to JUnit 5 Jupiter. `@Test(expected = ...)` became
  `assertThrows`, message-first assertion arguments were reordered to
  Jupiter's message-last convention, `@RunWith(MockitoJUnitRunner.class)`
  became `@ExtendWith(MockitoExtension.class)` (with class-level
  `@MockitoSettings(strictness = Strictness.LENIENT)` where shared
  `@BeforeEach` stubs are not used by every test, matching the old runner's
  per-class validation), and `ConnectionManagementTest` now starts/stops a
  MockServer `ClientAndServer` itself instead of using the JUnit 4
  `MockServerRule`.
* **Jakarta namespace**: with Jersey 4 the integration tests use
  `jakarta.ws.rs.*` instead of `javax.ws.rs.*`; the unused `javax.ws.rs-api`
  version property was removed from the pom.
* **Jetty 12 test harness**: the Fedora war is deployed with the `start-war`
  goal of the EE10 Jetty plugin. The old `jetty-test.xml` was removed — the
  HTTP connector port and the `fcrepo` `HashLoginService` (basic-auth users
  for the authentication ITs) are now configured directly in the plugin's
  `<httpConnector>` and `<loginServices>` blocks, mirroring how fcrepo-webapp
  7.0.0 configures its own integration tests.
* **Mockito as Java agent**: JDK 21+ warns about dynamically attached agents
  (and future JDKs will disallow them), so `mockito-core` is now attached via
  `-javaagent` in the surefire `argLine`, using a path property exposed by the
  `maven-dependency-plugin` `properties` goal.
* **Timing-sensitive ITs**: `FcrepoClientIT#testPatchEtagUpdated` and
  `FcrepoTransactionIT#testTransactionKeepAlive` asserted on values with
  second granularity (weak ETags derived from the last-modified timestamp,
  `Atomic-Expires` dates) and failed when requests executed within the same
  second; they now cross a second boundary before re-checking.
* **CI**: the GitHub Actions workflow builds with Temurin JDK 21
  (`actions/setup-java@v4`).
