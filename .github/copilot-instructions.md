# GitHub Copilot Instructions — pwunsafe

## Purpose

Developer-only credential store for the Android emulator. Saves test account credentials (service, username, password, passkeys) so the developer doesn't retype them during testing. **Intentionally unencrypted.** No sync, no accounts, no production security hardening needed or wanted.

## Stack

- **Kotlin** 2.3.20, Multiplatform (Android target only)
- **Compose Multiplatform** 1.10.3 with **Material3**
- **Gradle** Kotlin DSL + version catalog at `gradle/libs.versions.toml`
- Min SDK 24, Compile/Target SDK 36, JVM 11
- Package: `com.example.pwunsafe`

## Source Locations

| Source set | Path | Purpose |
|------------|------|---------|
| `androidMain` | `composeApp/src/androidMain/kotlin/com/example/pwunsafe/` | Android-specific implementations |
| `commonMain` | `composeApp/src/commonMain/kotlin/` | Shared cross-platform logic |
| `androidUnitTest` | `composeApp/src/androidUnitTest/kotlin/com/example/pwunsafe/` | Unit tests |

## Dependency Rule

All dependencies live in `gradle/libs.versions.toml`. Reference them as `libs.<alias>` in `build.gradle.kts`. Never hardcode a version string in a build file.

## Compose Patterns

- Root composable: `App()` in `App.kt`
- Always use `@Preview` on composables
- Edge-to-edge is active: wrap top-level composables with `Modifier.safeContentPadding()` or handle `WindowInsets` explicitly
- No XML — all UI is Compose only

## Platform Abstraction

```kotlin
// commonMain
class Greeting { fun greet(): String = "Hello, ${getPlatform().name}!" }

// androidMain
class AndroidPlatform { val name: String = "Android ${Build.VERSION.SDK_INT}" }
fun getPlatform() = AndroidPlatform()
```

Follow this pattern for any platform-specific functionality.

## Testing

Run unit tests with `./gradlew :composeApp:test`.
Use `kotlin-test` (catalog alias `libs.kotlin.test`) in `commonTest`.
Use JUnit 4 (`libs.junit`) in `androidUnitTest`.

## Constraints

- JVM target 11 — no Java 17+ APIs
- `android.nonTransitiveRClass=true` — only reference R from the declaring module
- Configuration cache is on — keep all Gradle tasks cache-compatible
- Do not add KMP targets beyond Android without explicit request
