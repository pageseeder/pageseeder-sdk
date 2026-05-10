# PageSeeder SDK

A Java 11 SDK for interacting with the [PageSeeder service API](https://dev.pageseeder.com/api.html).

> [!WARNING]
> This SDK is being actively redeveloped as a replacement for PageSeeder Bridge. It is not ready for production use yet.

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
- `json().tree()`, `json().map(...)`, `json().at(...)` — JSON parsing (requires `sdk-model`)

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
ClientRegistration registration = new ClientRegistration("client-id", "client-secret", instance);

TokenRequest request = TokenRequest.newClientCredentials(registration);
TokenResponse response = request.execute();

if (response.isSuccessful()) {
  BearerToken token = response.accessToken();
}
```

Convert an OpenID token response to a `Member` (requires `sdk-model`):

```java
Member member = TokenResponses.toMember(response);
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
