# sdk-core

The foundation module of the PageSeeder SDK. Provides the HTTP client, service catalog, authentication, OAuth 2.0, and XML parsing utilities. Has no dependencies beyond SLF4J.

## Packages

| Package | Key types |
|---|---|
| `org.pageseeder.sdk` | `PageSeederInstance` |
| `org.pageseeder.sdk.client` | `PageSeederClient`, `PageSeederResponse`, `XmlResponseBody`, `BodyDecoder<T>` |
| `org.pageseeder.sdk.service` | `ServiceCall`, `ServiceCatalog`, `ServiceEndpoint`, `PathTemplate` |
| `org.pageseeder.sdk.search` | `QuestionSearch`, `FacetSearch`, `PredicateSearch`, `SearchScope`, `Facet`, `Filter`, `Range` |
| `org.pageseeder.sdk.auth` | `Credentials`, `BearerToken`, `SessionCookie`, `BasicCredentials` |
| `org.pageseeder.sdk.oauth` | `TokenRequest`, `TokenResponse`, `AuthorizationRequest`, `ClientRegistration` |
| `org.pageseeder.sdk.exception` | `ServiceError`, `ServiceErrorException`, `HttpStatusException`, `TransportException`, `ParsingException` |
| `org.pageseeder.sdk.xml.sax` | `BasicHandler<T>` — base class for SAX-based parsing |
| `org.pageseeder.sdk.xml.stax` | `BasicXMLStreamHandler<T>` — base class for StAX-based parsing |

## Usage

### Basic request flow

```java
// 1. Describe the PageSeeder deployment
PageSeederInstance instance = PageSeederInstance.of("https://example.pageseeder.com");

// 2. Build a client
PageSeederClient client = PageSeederClient.builder()
    .instance(instance)
    .credentials(new BearerToken("your-token"))
    .timeout(Duration.ofSeconds(15))
    .build();

// 3. Build a service call
ServiceCall call = ServiceCall.of(ServiceCatalog.MEMBER)
    .pathVariable("member", "jdoe");

// 4. Execute
PageSeederResponse response = client.execute(call);
```

### Typed decoding with a BodyDecoder

`BodyDecoder<T>` is a functional interface. Supply one to get a typed result directly:

```java
// Inline lambda (no sdk-model dependency needed)
String version = client.execute(
    ServiceCall.of(ServiceCatalog.VERSION),
    response -> response.xml().saxItem(new VersionHandler()));
```

`sdk-model`'s `Decoders` provides ready-made decoders for all domain types.

### Raw XML decoding

```java
PageSeederResponse response = client.execute(call);

// SAX
MyObject item = response.xml().saxItem(new MyHandler());
List<MyObject> list = response.xml().saxList(new MyHandler());

// StAX
MyObject item = response.xml().staxItem(new MyStreamHandler());

// DOM
Document doc = response.xml().document();
List<Element> els = response.xml().elements("member");
```

### Query and form parameters

```java
ServiceCall call = ServiceCall.of(ServiceCatalog.endpoint("GET", "/members"))
    .query("q", "john")
    .query("max", "20");

// Form body (POST)
ServiceCall post = ServiceCall.of(ServiceCatalog.endpoint("POST", "/groups/{group}/comments"))
    .pathVariable("group", "my-project")
    .form("title", "Hello")
    .form("content", "World");
```

### Search builders

The `org.pageseeder.sdk.search` package provides immutable builders for the PageSeeder search services. Build the query first, then bind it to a group, project, or member-wide scope to get an executable `ServiceCall`.

```java
ServiceCall call = QuestionSearch.of("annual report")
    .questionFields(Fields.TITLE, Fields.CONTENT)
    .withType("document")
    .withStatus("Approved")
    .facet(Fields.STATUS)
    .facet(Fields.PRIORITY, true)
    .page(2)
    .pageSize(25)
    .sortField("-" + Fields.MODIFIEDDATE)
    .toServiceCall(SearchScope.group("my-project-docs"));
```

Facet extraction and Lucene predicate searches use the same scope model:

```java
ServiceCall facets = FacetSearch.of("annual report")
    .facet(Facet.rangeFacet(Fields.DATE, "2024", "2025", "2026"))
    .toServiceCall(SearchScope.project("my-project", "jdoe", "drafts", "published"));

ServiceCall predicate = PredicateSearch.of("pstitle:report AND pstype:document")
    .defaultField(Fields.CONTENT)
    .pageSize(50)
    .toServiceCall(SearchScope.global("jdoe"));
```

### Using an endpoint not in ServiceCatalog

```java
ServiceEndpoint ep = ServiceCatalog.endpoint("GET", "/members/{member}/groups/{group}/uris");
ServiceCall call = ServiceCall.of(ep)
    .pathVariable("member", "jdoe")
    .pathVariable("group", "my-project");
```

### Error handling

```java
try {
  PageSeederResponse response = client.execute(call);
} catch (ServiceErrorException e) {
  ServiceError error = e.error(); // structured PageSeeder error
} catch (HttpStatusException e) {
  int code = e.statusCode();     // unexpected HTTP status
} catch (TransportException e) {
  // I/O or connection failure
}
```

## Service catalog

All known PageSeeder API endpoints are declared in:

```
src/main/resources/org/pageseeder/sdk/service/services.txt
```

`ServiceCatalog` loads this file at startup. Named constants (`MEMBER`, `GROUP`, etc.) cover the most-used endpoints. Use `ServiceCatalog.endpoint(method, path)` for anything else.

## Dependency

```kotlin
// build.gradle.kts
implementation("org.pageseeder.sdk:sdk-core:VERSION")
```
