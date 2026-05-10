# sdk-legacy

Transition adapters from [PageSeeder Bridge](https://github.com/pageseeder/bridge) to the PageSeeder SDK. Depends on `sdk-core` and `pso-bridge`.

Use this module if you have existing code that relies on the Bridge API and want to adopt the new SDK incrementally without rewriting everything at once.

## What's provided

| Class | Purpose |
|---|---|
| `PSConfigs` | Convert a Bridge `PSConfig` to a SDK `PageSeederInstance` |
| `LegacyHandlers` | Wrap Bridge SAX handlers as SDK `BodyDecoder<T>` instances |

## Migration path

The goal is to eventually remove this dependency. The recommended approach:

1. Replace Bridge `Request`/`Response` with `PageSeederClient` + `ServiceCall`.
2. Replace `PS*` model types with the SDK equivalents in `sdk-model`.
3. Replace Bridge SAX handlers with `BasicHandler<T>` subclasses or `Decoders` from `sdk-model`.
4. Drop this module once nothing references `org.pageseeder.bridge` anymore.

See the [migration table in the root README](../README.md#migrating-from-pageseeder-bridge) for type-level mappings.

## Dependency

```kotlin
// build.gradle.kts
implementation("org.pageseeder.sdk:sdk-legacy:VERSION")
```
