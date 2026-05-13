# pwunsafe

Developer-only, local password manager for the Android emulator. Stores test account credentials (service name, username, password, passkeys) so you don't retype them during development. **Intentionally unencrypted** — no real users, no sensitive data.

## Features

- Store and manage test credentials: service, username, password, passkeys
- Fully local — no network, no sync, no account
- Can work on real device if you really want it

## Stack

- **Kotlin** 2.3.20 · Kotlin Multiplatform (Android target)
- **Compose Multiplatform** 1.10.3 · Material3
- **Gradle** Kotlin DSL + version catalog
- Min SDK 24 · Compile/Target SDK 36 · JVM 11

## Build

```bash
# Debug APK
./gradlew :composeApp:assembleDebug

# Release APK
./gradlew :composeApp:assembleRelease

# Install on connected device/emulator
./gradlew :composeApp:installDebug

# Run unit tests
./gradlew :composeApp:test
```

## Releases

Tagged releases are published automatically via GitHub Actions. Each `v*` tag triggers a release build and attaches the APK to the GitHub Release.

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Source Layout

```
composeApp/src/
  androidMain/kotlin/com/example/pwunsafe/
    App.kt           – Root @Composable
    MainActivity.kt  – Activity entry point (edge-to-edge)
    Platform.kt      – AndroidPlatform + getPlatform()
  commonMain/        – Shared logic
  androidUnitTest/   – Unit tests
gradle/
  libs.versions.toml – Version catalog
```
