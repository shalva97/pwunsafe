# pwunsafe — Agent Instructions

## Project Overview

**pwunsafe** is a developer-only, local password manager for the Android emulator. It stores test account credentials (username, password, passkeys) so the developer doesn't have to type them manually during testing. There are no real users and no sensitive data — the name is intentional.

- Fully local: no network, no sync, no account, no encryption
- Runs on Android
- Stores: service name, username, password, passkeys

Kotlin Multiplatform (KMP) project targeting **Android only**. Uses Compose Multiplatform for UI and Material3 for design.

- **Package**: `com.example.pwunsafe`
- **Application ID**: `com.example.pwunsafe`
- **Kotlin**: 2.3.20 | **AGP**: 8.11.2 | **Compose Multiplatform**: 1.10.3
- **Min SDK**: 24 | **Compile/Target SDK**: 36 | **JVM Target**: 11

All shared logic goes in `commonMain`; Android-specific code goes in `androidMain`.

## Key Rules

### Kotlin / Compose
- Follow **Kotlin official code style** (`kotlin.code.style=official` in `gradle.properties`).
- UI is built with **Jetpack Compose / Compose Multiplatform**.
- Use `@Composable` functions; state hoisting is preferred over local `remember` in complex components.
- Add `@Preview` annotations to composables for IDE previews.
- Don't use JVM only API if possible

### Architecture
- use MVVM.
- keep business or configuration logic separate from UI.
- Follow Googles Android app architecture Guide.

## What NOT to Suggest

- Do not suggest encryption, keystores, or security hardening — this is intentionally unencrypted by design.
- Do not suggest cloud sync, accounts, or remote backup.
- Do not add biometric auth or lock screens — it's a dev tool, not a production app.
