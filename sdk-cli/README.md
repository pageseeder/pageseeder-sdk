# sdk-cli

Command-line tools for the PageSeeder SDK. Depends on `sdk-core` and `sdk-model`.

## Running

```bash
# Via Gradle application plugin
./gradlew :sdk-cli:run --args="<command> [options]"

# Or build a distribution and run the script
./gradlew :sdk-cli:installDist
sdk-cli/build/install/sdk-cli/bin/sdk-cli <command> [options]
```

## Commands

| Command | Description |
|---|---|
| `version` | Print the PageSeeder server version |
| `help` | List available commands |

## Dependency

```kotlin
// build.gradle.kts
implementation("org.pageseeder.sdk:sdk-cli:VERSION")
```
