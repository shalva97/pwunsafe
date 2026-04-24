# pwunsafe — Claude Code Instructions

## Project Overview

Kotlin Multiplatform (KMP) project targeting **Android only**. Uses Compose Multiplatform for UI and Material3 for design.

- **Package**: `com.example.pwunsafe`
- **Application ID**: `com.example.pwunsafe`
- **Kotlin**: 2.3.20 | **AGP**: 8.11.2 | **Compose Multiplatform**: 1.10.3
- **Min SDK**: 24 | **Compile/Target SDK**: 36 | **JVM Target**: 11

## Source Layout

```
composeApp/src/
  androidMain/kotlin/com/example/pwunsafe/   # Android-specific code
    App.kt          – Root @Composable
    MainActivity.kt – Activity entry point (edge-to-edge enabled)
    Platform.kt     – AndroidPlatform + getPlatform()
    Greeting.kt     – Sample domain class
  androidUnitTest/kotlin/com/example/pwunsafe/
    ComposeAppAndroidUnitTest.kt
gradle/
  libs.versions.toml  – Single version catalog for all deps/plugins
```

All shared logic goes in `commonMain`; Android-specific code goes in `androidMain`.

## Build Commands

```bash
# Assemble debug APK
./gradlew :composeApp:assembleDebug

# Assemble release APK
./gradlew :composeApp:assembleRelease

# Install debug APK on connected device
./gradlew :composeApp:installDebug

# Run unit tests
./gradlew :composeApp:test

# Full clean
./gradlew clean
```

## Key Rules

### Dependencies
- **Always use version catalog aliases** (`libs.compose.material3`, etc.) — never hardcode versions in `build.gradle.kts`.
- Add new versions/libraries to `gradle/libs.versions.toml` first, then reference the alias.
- `local.properties` is gitignored; never read or commit it.

### Kotlin / Compose
- Follow **Kotlin official code style** (`kotlin.code.style=official` in `gradle.properties`).
- UI is built with **Jetpack Compose / Compose Multiplatform** — no XML layouts.
- Use `@Composable` functions; state hoisting is preferred over local `remember` in complex components.
- Add `@Preview` annotations to composables for IDE previews.
- JVM target is **11** — do not use Java 17+ APIs.

### Architecture
- Platform abstraction pattern: define a class/interface in `commonMain`, implement it in `androidMain`.
- ViewModel is available via `libs.androidx.lifecycle.viewmodelCompose`.
- `enableEdgeToEdge()` is called in `MainActivity` — respect system bar insets in composables (`safeContentPadding`, `WindowInsets`).

### Build System
- Gradle **configuration cache** and **build caching** are both enabled — keep tasks cache-friendly (no `System.currentTimeMillis()` in task outputs, etc.).
- `android.nonTransitiveRClass=true` — only reference R resources from the module that declares them.
- `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` is active in `settings.gradle.kts`.

## What NOT to Do

- Do not add targets (iOS, Desktop, JS) unless explicitly requested — the project intentionally targets Android only.
- Do not use `kapt`; prefer KSP for annotation processing if needed.
- Do not bypass configuration cache with `--no-configuration-cache`.
- Do not commit `local.properties`, `*.iml`, `.gradle/`, or `build/` directories.
- Do not use deprecated `compose.foundation.Image` import — use `androidx.compose.foundation.layout` correctly.
