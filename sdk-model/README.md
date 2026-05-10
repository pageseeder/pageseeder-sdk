# sdk-model

Domain types and Jackson-based decoders for the PageSeeder SDK. Depends on `sdk-core` and Jackson.

## Packages

| Package | Key types |
|---|---|
| `org.pageseeder.sdk.model` | `Member`, `Group`, `Membership`, `Comment`, `Version`, `ResultPage<T>`, `TokenResponses` |
| `org.pageseeder.sdk.model.codec` | `Decoders`, `JsonResponseBody`, `XmlPageSeederParser`, `JsonPageSeederParser` |

## Domain types

| Type | Represents |
|---|---|
| `Member` | A PageSeeder user account |
| `Group` | A PageSeeder group or project |
| `Membership` | A member's role and settings within a group |
| `Comment` | A comment or task |
| `Version` | The PageSeeder server version |
| `ResultPage<T>` | A paginated result set |
| `ResourceUri` | A URI resource in PageSeeder |

## Decoders

`Decoders` provides `BodyDecoder<T>` instances for use with `PageSeederClient.execute(call, decoder)`:

```java
// Single object
Member member = client.execute(
    ServiceCall.of(ServiceCatalog.MEMBER).pathVariable("member", "jdoe"),
    Decoders.object(Member.class));

// List
List<Membership> memberships = client.execute(
    ServiceCall.of(ServiceCatalog.MEMBER_MEMBERSHIPS).pathVariable("member", "jdoe"),
    Decoders.list(Membership.class));

// Paginated result
ResultPage<Membership> page = client.execute(call, Decoders.page(Membership.class));
int total = page.total();
List<Membership> items = page.items();
```

Both XML and JSON response formats are supported. The decoder detects the content type automatically.

## OAuth token to Member

When using OpenID Connect, convert a `TokenResponse` to a `Member` after a successful token exchange:

```java
TokenResponse tokenResponse = tokenRequest.execute();
if (tokenResponse.isSuccessful()) {
  Member member = TokenResponses.toMember(tokenResponse);
}
```

## Custom JSON parsing

`JsonResponseBody` provides Jackson-based helpers when using the raw response:

```java
PageSeederResponse response = client.execute(call);
JsonNode tree = response.json().tree();
MyType obj = response.json().map(MyType.class);
MyType nested = response.json().at("/data/item", MyType.class);
List<MyType> list = response.json().listAt("/data/items", MyType.class);
```

## Dependency

```kotlin
// build.gradle.kts
implementation("org.pageseeder.sdk:sdk-model:VERSION")
```

This transitively pulls in `sdk-core`, `jackson-databind`, `jackson-dataformat-xml`, and `jackson-datatype-jsr310`.
