# pwunsafe — Agent Instructions

## Project

Kotlin Multiplatform project targeting **Android only**, using Compose Multiplatform for UI.

- **Language**: Kotlin 2.3.20
- **UI**: Compose Multiplatform 1.10.3 + Material3
- **Package**: `com.example.pwunsafe`
- **Min SDK**: 24 | **Target/Compile SDK**: 36 | **JVM**: 11
- **Build**: Gradle with Kotlin DSL (`build.gradle.kts`) + version catalog (`gradle/libs.versions.toml`)

## Source Structure

```
composeApp/src/
  androidMain/kotlin/com/example/pwunsafe/
    App.kt            Root @Composable
    MainActivity.kt   Activity (edge-to-edge, sets content to App())
    Platform.kt       AndroidPlatform class + getPlatform() factory
    Greeting.kt       Domain class (uses Platform)
  androidUnitTest/kotlin/com/example/pwunsafe/
gradle/
  libs.versions.toml  Version catalog (all deps and plugins)
```

## Essential Commands

| Task | Command |
|------|---------|
| Build debug | `./gradlew :composeApp:assembleDebug` |
| Build release | `./gradlew :composeApp:assembleRelease` |
| Install on device | `./gradlew :composeApp:installDebug` |
| Run tests | `./gradlew :composeApp:test` |
| Clean | `./gradlew clean` |

## Coding Guidelines

1. **Dependencies**: always add to `gradle/libs.versions.toml` and reference as `libs.<alias>` — never hardcode versions.
2. **No XML layouts**: UI is entirely Compose — `@Composable` functions only.
3. **Platform abstraction**: shared interfaces in `commonMain`, Android implementations in `androidMain`.
4. **State**: hoist state up; prefer ViewModel (`libs.androidx.lifecycle.viewmodelCompose`) for screen-level state.
5. **Code style**: Kotlin official. No unused imports. Explicit types on public APIs.
6. **JVM 11**: do not use APIs from Java 17+.
7. **R resources**: `android.nonTransitiveRClass=true` — reference only resources declared in the same module.
8. **Edge-to-edge**: insets are enabled globally — use `safeContentPadding()` or `WindowInsets` in composables.

## Do Not

- Add new Gradle targets (iOS, JVM Desktop, JS/WASM) without explicit instruction.
- Use `kapt`; use KSP if annotation processing is needed.
- Hardcode SDK versions — always read from `libs.versions.android.*`.
- Commit `local.properties`, `.gradle/`, `build/`, or `.idea/`.
- Break Gradle configuration cache (`--no-configuration-cache` is forbidden).
