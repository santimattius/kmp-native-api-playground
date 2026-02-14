# KMP Native API Playground

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-2.3-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![Android](https://img.shields.io/badge/Android-API%2024+-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![iOS](https://img.shields.io/badge/iOS-14.0+-000000?logo=apple&logoColor=white)](https://developer.apple.com/ios/)
[![Gradle](https://img.shields.io/badge/Gradle-8.14-02303A?logo=gradle&logoColor=white)](https://gradle.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-BOM-4285F4)](https://developer.android.com/jetpack/compose)
[![SwiftUI](https://img.shields.io/badge/SwiftUI-iOS-F05138?logo=swift&logoColor=white)](https://developer.apple.com/xcode/swiftui/)
[![CocoaPods](https://img.shields.io/badge/CocoaPods-1.0-EE3322?logo=cocoapods&logoColor=white)](https://cocoapods.org)
[![Bugsnag](https://img.shields.io/badge/Bugsnag-Crash%20reporting-4949E4)](https://www.bugsnag.com/)

A **Kotlin Multiplatform (KMP)** project that targets **Android** and **iOS**, sharing business logic and native API integrations while using platform-specific UI (Jetpack Compose on Android, SwiftUI on iOS).

This repo is the companion code for the article **[From Native Libraries to Libraries in KMP: Good Design and Expect/Actual for our MVP in KMP](https://medium.com/@santimattius/from-native-libraries-to-libraries-in-kmp-good-design-and-expect-actual-for-our-mvp-in-kmp-d6533bce56a9)** (Santiago Mattiauda, Medium). It demonstrates how to wrap existing native SDKs (e.g. [Bugsnag](https://www.bugsnag.com/)) so they can be used from shared KMP code without full rewrites, using Kotlin’s **expect/actual** mechanism and a clear, platform-agnostic API.

---

## Overview

This repository serves as a playground for:

- **Shared Kotlin code** across Android and iOS (business logic, crash reporting, platform APIs).
- **Wrapping native SDKs** (Bugsnag on Android and iOS) behind a common API using **expect/actual**.
- **Native integrations** via CocoaPods (e.g. Bugsnag on iOS) and Android libraries.
- **Swift–Kotlin interop** using [SKIE](https://skie.touchlab.co/) for improved API ergonomics on iOS.
- The official **Android Gradle Library Plugin for KMP** (`com.android.kotlin.multiplatform.library`) for the shared Android target.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin 2.3.x |
| **Build** | Gradle 8.14+, AGP 8.10 |
| **Android UI** | Jetpack Compose |
| **iOS UI** | SwiftUI (native) |
| **Shared logic** | Kotlin Multiplatform, Kotlin Coroutines |
| **Crash reporting** | Bugsnag (Android SDK, iOS CocoaPod) |
| **iOS interop** | CocoaPods, SKIE |

---

## Project Structure

```
kmp-native-api-playground/
├── shared/                    # KMP library (common + Android + iOS)
│   └── src/
│       ├── commonMain/       # Shared Kotlin code
│       ├── androidMain/      # Android-specific code
│       └── iosMain/          # iOS-specific code (Kotlin/Native)
├── composeApp/               # Android application (Compose)
└── iosApp/                   # iOS application (SwiftUI, Xcode project)
```

- **`shared`** — Kotlin Multiplatform module. Contains `commonMain` (shared logic), `androidMain` (Android implementations), and `iosMain` (iOS implementations and CInterop). Uses the [Android KMP Library plugin](https://developer.android.com/kotlin/multiplatform/plugin), CocoaPods, and SKIE.
- **`composeApp`** — Android app that depends on `shared` and uses Jetpack Compose for the UI.
- **`iosApp`** — Native iOS app (Xcode project) that consumes the `Shared` framework via CocoaPods and uses SwiftUI.

---

## Design: Expect/Actual and Native SDK Wrapping

This project follows the approach described in the [referenced article](https://medium.com/@santimattius/from-native-libraries-to-libraries-in-kmp-good-design-and-expect-actual-for-our-mvp-in-kmp-d6533bce56a9): **wrap platform-specific SDKs behind a common API** so shared code can use one abstraction while each platform supplies the real implementation.

### How it works

1. **Common API (`commonMain`)**  
   Declare the public contract with `expect`: e.g. `expect class Configuration`, `expect class TrackableException`, and an internal `expect class PlatformTracker` with `initialize(config)` and `track(exception)`. A single `Bugsnag` object in common code calls into this tracker.

2. **Platform implementations (`androidMain` / `iosMain`)**  
   Provide `actual` declarations: use **typealias** to map to native types (`Configuration` → Bugsnag’s config type, `TrackableException` → `Throwable` on Android, `NSException` on iOS), and implement `PlatformTracker` with the real Bugsnag SDK calls.

3. **Usage from shared code**  
   Shared logic (e.g. `CrashRepository`) can call `Bugsnag.track(exception)` and use an expect/actual extension like `Throwable.asTrackableException()` so exceptions are reported the same way on both platforms without duplicating business logic.

4. **Android context**  
   Bugsnag on Android needs an application context. The project uses [Android Startup](https://developer.android.com/topic/libraries/app-startup) in `androidMain` to capture `applicationContext` and register an initializer in the manifest; the app then calls `Bugsnag.initialize(config)` from its `Application` class.

### Trade-offs

| Pros | Cons |
|------|------|
| Reuse existing native solutions for KMP MVPs without full rewrites. | Native APIs must be adapted and kept in sync; changes in vendor APIs require updates in the KMP layer. |
| Encourages consistent design and a single API surface across platforms. | Extra maintenance (one more abstraction layer and platform-specific actuals). |
| Familiar developer experience (e.g. same API-level concepts on Android). | Not all native SDKs align (e.g. Swift-only iOS libs without ObjC export are not usable via CocoaPods). |

This approach is best when you need a **third-party SDK that has no official KMP support** and want to reuse it in shared code; evaluate the cost of maintaining the wrapper versus alternatives (e.g. a KMP-native library or platform-specific call sites).

---

## Prerequisites

- **JDK 17**
- **Android Studio** (latest stable) or **IntelliJ IDEA** with Android and Kotlin Multiplatform support
- **Xcode** (for iOS; recent version compatible with Kotlin/Native)
- **CocoaPods** (for iOS): `gem install cocoapods`

---

## Getting Started

### Clone and open

```bash
git clone <repository-url>
cd kmp-native-api-playground
```

Open the project in **Android Studio** or **IntelliJ IDEA** (the root directory is the Gradle project).

### Build from Gradle

```bash
# Build all targets (shared library + Android app)
./gradlew assemble

# Build only the Android debug APK
./gradlew :composeApp:assembleDebug

# Compile shared Kotlin for iOS (simulator)
./gradlew :shared:compileKotlinIosSimulatorArm64
```

### Run on Android

1. Connect a device or start an emulator.
2. Run the `composeApp` configuration, or:
   ```bash
   ./gradlew :composeApp:installDebug
   ```

### Run on iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Install CocoaPods dependencies (if needed):
   ```bash
   cd iosApp && pod install
   ```
3. Open the generated `.xcworkspace` and run the app on a simulator or device.

---

## Key Dependencies

Dependency versions are centralized in `gradle/libs.versions.toml`.

| Dependency | Purpose |
|------------|---------|
| Kotlin Multiplatform + Compose Compiler | Shared code and Compose on Android |
| Android KMP Library plugin | Official Android target for the KMP library |
| Kotlinx Coroutines | Concurrency in shared and Android code |
| Bugsnag (Android + iOS) | Crash and error reporting |
| SKIE | Improved Swift API for Kotlin code from the shared framework |
| Jetpack Compose BOM | Android UI |

---

## Configuration Notes

- **JVM target:** JDK 17 (via `jvmToolchain(17)` in the shared module).
- **Android:** `compileSdk` / `targetSdk` 36; `minSdk` 24.
- **iOS:** Deployment target 14.0; CocoaPods framework name `Shared`.
- **Bugsnag:** Applied in `shared` (Android dependency + CocoaPods pod); mapping upload is intended from the application module (e.g. `composeApp`), not from the library.

---

## Documentation

### Related article

- **[From Native Libraries to Libraries in KMP: Good Design and Expect/Actual for our MVP in KMP](https://medium.com/@santimattius/from-native-libraries-to-libraries-in-kmp-good-design-and-expect-actual-for-our-mvp-in-kmp-d6533bce56a9)** (Santiago Mattiauda, Medium) — design, expect/actual pattern, and Bugsnag wrapping in detail.

### Official and SDK docs

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Expect and actual declarations](https://kotlinlang.org/docs/multiplatform-expect-actual.html)
- [Android dependencies in KMP](https://kotlinlang.org/docs/multiplatform-android-dependencies.html) · [iOS dependencies in KMP](https://kotlinlang.org/docs/multiplatform-ios-dependencies.html)
- [Android Gradle Library Plugin for KMP](https://developer.android.com/kotlin/multiplatform/plugin)
- [SKIE – Swift Kotlin Interop](https://skie.touchlab.co/)
- [CocoaPods with Kotlin/Native](https://kotlinlang.org/docs/native-cocoapods.html)
- [Bugsnag Android](https://docs.bugsnag.com/platforms/android/) · [Bugsnag iOS](https://docs.bugsnag.com/platforms/ios/)

---

## License

See the repository license file, if present.
