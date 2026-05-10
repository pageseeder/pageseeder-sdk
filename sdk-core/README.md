# sdk-core

The foundation module of the PageSeeder SDK. Provides the HTTP client, service catalog, authentication, OAuth 2.0, and XML parsing utilities. Has no dependencies beyond SLF4J.

## Packages

| Package | Key types |
|---|---|
| `org.pageseeder.sdk` | `PageSeederInstance` |
| `org.pageseeder.sdk.client` | `PageSeederClient`, `PageSeederResponse`, `XmlResponseBody`, `BodyDecoder<T>` |
| `org.pageseeder.sdk.service` | `ServiceCall`, `ServiceCatalog`, `ServiceEndpoint`, `PathTemplate` |
| `org.pageseeder.sdk.auth` | `Credentials`, `BearerToken`, `SessionCookie`, `BasicCredentials` |
| `org.pageseeder.sdk.oauth` | `TokenRequest`, `TokenResponse`, `AuthorizationRequest`, `ClientRegistration` |
| `org.pageseeder.sdk.exception` | `ServiceError`, `ServiceErrorException`, `HttpStatusException`, `TransportException`, `ParsingException` |
| `org.pageseeder.sdk.xml.sax` | `BasicHandler<T>` — base class for SAX-based parsing |
| `org.pageseeder.sdk.xml.stax` | `BasicXMLStreamHandler<T>` — base class for StAX-based parsing |

## Usage

### Basic request flow

```java
// 1. Describe the PageSeeder deployment
PageSeederInstance instance = new PageSeederInstance(URI.create("https://example.pageseeder.com"));

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
    .parameter("q", "john")
    .parameter("max", 20);

// Form body (POST)
ServiceCall post = ServiceCall.of(ServiceCatalog.endpoint("POST", "/groups/{group}/comments"))
    .pathVariable("group", "my-project")
    .formParameter("title", "Hello")
    .formParameter("content", "World");
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
