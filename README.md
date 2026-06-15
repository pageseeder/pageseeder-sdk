[![Maven Central](https://img.shields.io/maven-central/v/org.pageseeder.sdk/pageseeder-sdk-core)](https://central.sonatype.com/artifact/org.pageseeder.sdk/pageseeder-sdk-core)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pageseeder_pageseeder-sdk&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pageseeder_pageseeder-sdk)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=pageseeder_pageseeder-sdk&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=pageseeder_pageseeder-sdk)
[![javadoc](https://javadoc.io/badge2/org.pageseeder.sdk/pageseeder-sdk-core/javadoc.svg)](https://javadoc.io/doc/org.pageseeder.sdk/pageseeder-sdk-core)

# PageSeeder SDK

A Java 17 SDK for interacting with the [PageSeeder service API](https://dev.pageseeder.com/api.html).

> [!WARNING]
> **This SDK is being actively redeveloped as a replacement for [PageSeeder Bridge](https://github.com/pageseeder/bridge). It is not ready for production use yet.**

## Modules

The project is a Gradle multi-module build. Dependencies flow left to right:

```
sdk-core  →  sdk-model  →  sdk-cli
    ↑
sdk-legacy
```

| Module | Artifact | Purpose |
|---|---|---|
| `sdk-core` | `org.pageseeder.sdk:sdk-core` | HTTP client, service catalog, auth, OAuth, XML parsing |
| `sdk-model` | `org.pageseeder.sdk:sdk-model` | Domain POJOs and Jackson-based decoders |
| `sdk-cli` | `org.pageseeder.sdk:sdk-cli` | Command-line tools |
| `sdk-legacy` | `org.pageseeder.sdk:sdk-legacy` | Transition adapters from PageSeeder Bridge |

## Quick start

### 1. Create a client

```java
PageSeederInstance instance = PageSeederInstance.of("https://example.pageseeder.com");

PageSeederClient client = PageSeederClient.builder()
    .instance(instance)
    .credentials(new BearerToken("your-token"))
    .build();
```

### 2. Execute a service call

Use a named endpoint from `ServiceCatalog`, attach path variables and parameters, then execute:

```java
ServiceEndpoint endpoint = ServiceEndpoint.of("GET", "/members/{member}");
ServiceCall call = ServiceCall.of(endpoint)
    .pathVariable("member", "jdoe");

Member member = client.execute(call, Decoders.object(Member.class));
```

For lists and paginated results:

```java
ServiceEndpoint endpoint = ServiceEndpoint.of("GET", "/members/{member}/memberships");
List<Membership> memberships = client.execute(
    ServiceCall.of(endpoint).pathVariable("member", "jdoe"),
    Decoders.list(Membership.class));
```

### 3. Handle the raw response

When built-in mapping isn't enough, work directly with the `PageSeederResponse`:

```java
PageSeederResponse response = client.execute(call);

MyMember member = response.xml().saxItem(new BasicHandler<MyMember>() {
  private MyMember current;

  @Override
  public void startElement(String element, Attributes atts) {
    if (isElement("member")) {
      this.current = new MyMember(atts.getValue("username"), atts.getValue("email"));
    }
  }

  @Override
  public void endElement(String element) {
    if (isElement("member")) add(this.current);
  }
});
```

`PageSeederResponse` also supports:
- `xml().staxItem(...)`, `xml().staxList(...)` — StAX-based parsing
- `xml().document()`, `xml().elements(...)` — DOM-based parsing
- `new JsonResponseBody(response.body()).tree()` — JSON parsing (requires `sdk-model`)

## Authentication

Three credential types are supported:

```java
// OAuth bearer token
new BearerToken("eyJ...")

// Session cookie (browser-based flows)
new SessionCookie("JSESSIONID", "abc123")

// Basic auth (service accounts)
new BasicCredentials("username", "password")
```

Per-call credential override:

```java
client.execute(call, overrideCredentials, Decoders.object(Member.class));
```

## OAuth 2.0

```java
ClientCredentials clientCredentials = new ClientCredentials("client-id", "client-secret");
TokenRequest request = TokenRequest.clientCredentials(instance, clientCredentials);
TokenResponse response = request.execute();

if (response.isSuccessful()) {
  AccessToken token = response.accessToken();
}
```

Convert an OpenID token response to a `Member` (requires `sdk-model`):

```java
Member member = TokenResponses.toMember(response);
```

## Search (since v0.2.0)

The `org.pageseeder.sdk.search` package provides immutable, fluent builders for PageSeeder search requests. Build a query, bind it to a `SearchScope`, and execute it like any other service call.

**Question search** — full-text search within a group:

```java
ServiceCall call = QuestionSearch.of("annual report")
    .withType("document")
    .withStatus("Approved")
    .facet("psstatus")
    .page(2)
    .toServiceCall(SearchScope.group("my-group"));

SearchResponse results = client.execute(call, Decoders.search());
```

**Project-wide search** — search all groups in a project on behalf of a member:

```java
ServiceCall call = QuestionSearch.of("design spec")
    .toServiceCall(SearchScope.project("my-project", "jdoe"));
```

**Facet search** — extract facet counts without returning full results:

```java
ServiceCall call = FacetSearch.of("report")
    .facet("pstype")
    .facet("psstatus")
    .toServiceCall(SearchScope.group("my-group"));
```

**Predicate search** — Lucene predicate search:

```java
ServiceCall call = PredicateSearch.of("psstatus:Approved AND pstype:document")
    .toServiceCall(SearchScope.project("my-project", "jdoe"));
```

## Website resources (since v0.2.1)

`ResourceCall` fetches PageSeeder website resources (documents, images) that live outside the `/api/` namespace. Use `client.fetch(...)` instead of `client.execute(...)`:

```java
// Fetch a document by its document ID
PageSeederResponse response = client.fetch(ResourceCall.docId("my-doc-id"));

// Fetch a document by its numeric URI ID
PageSeederResponse response = client.fetch(ResourceCall.uri(12345L));

// Fetch a thumbnail or resized image
PageSeederResponse thumbnail = client.fetch(ResourceCall.thumbnail(12345L));
PageSeederResponse image     = client.fetch(ResourceCall.image(12345L));
```

Typed decoding and per-call credential overrides work the same way as with `ServiceCall`:

```java
MyDoc doc = client.fetch(ResourceCall.docId("my-doc-id"), Decoders.object(MyDoc.class));
```

## Build

```bash
# Build all modules
./gradlew build

# Run tests for a specific module
./gradlew :sdk-core:test
./gradlew :sdk-model:test

# Run a single test class
./gradlew :sdk-core:test --tests "org.pageseeder.sdk.client.PageSeederClientTest"
```

Version is managed in `version.txt`.

## Migrating from PageSeeder Bridge

| Bridge | SDK |
|---|---|
| `Request` / `Response` | `PageSeederClient` + `ServiceCall` |
| `PSCredentials`, `PSToken`, `PSSession` | `Credentials`, `BearerToken`, `SessionCookie` |
| `PS*Handler` | `PageSeederResponse.xml().sax(...)` |
| `PSMember`, `PSGroup`, etc. | `Member`, `Group`, etc. in `sdk-model` |
| Bridge managers | No direct replacement; use `PageSeederClient` directly |

The `sdk-legacy` module provides adapters to help with the transition.

## License

Apache Software License, Version 2.0 — see [LICENCE.md](LICENCE.md).
