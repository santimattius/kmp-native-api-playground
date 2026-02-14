# Migration from CocoaPods to Swift Package Manager (SwiftPM)

**Summary:** This guide walks you through migrating a Kotlin Multiplatform (KMP) project from the *
*CocoaPods** Gradle plugin (`kotlin("native.cocoapods")`) to **Swift Package Manager (SwiftPM)** for
iOS (and other Apple) dependencies. After migration, the iOS app uses
the [Kotlin/Native direct integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html)
and SwiftPM instead of CocoaPods for the shared framework and its dependencies.

---

## Table of contents

1. [Overview](#overview)
2. [Step 1 — Add SwiftPM dependencies alongside CocoaPods](#step-1--add-swiftpm-dependencies-alongside-cocoapods)
3. [Step 2 — Migrate framework configuration to the binaries API](#step-2--migrate-framework-configuration-to-the-binaries-api)
4. [Step 3 — Migrate CocoaPods API usage to SwiftPM-imported APIs](#step-3--migrate-cocoapods-api-usage-to-swiftpm-imported-apis)
5. [Step 4 — Reconfigure Xcode for direct integration](#step-4--reconfigure-xcode-for-direct-integration)
   - [If you see: "SwiftPM dependencies with embedAndSign integration"](#if-you-see-swiftpm-dependencies-with-embedandsign-integration)
6. [Step 5 — Disable CocoaPods in the iOS app](#step-5--disable-cocoapods-in-the-ios-app)
7. [Step 6 — Remove the CocoaPods plugin from the build](#step-6--remove-the-cocoapods-plugin-from-the-build)
8. [Verification](#verification)

---

## Overview

| Before                                             | After                                                                                                                           |
|----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| CocoaPods Gradle plugin + Podfile + `.xcworkspace` | SwiftPM in Kotlin build + [direct integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html) |
| Build via Xcode workspace                          | Build via `.xcodeproj`                                                                                                          |
| `pod 'Shared', :path => '...'` in Podfile          | Framework embedded via Gradle task `embedAndSignAppleFrameworkForXcode`                                                         |

Do **not** remove CocoaPods until the Xcode project has been reconfigured and you have verified the
app builds. The steps below keep CocoaPods in place until the final stages.

---

## Step 1 — Add SwiftPM dependencies alongside CocoaPods

In the **same** build script where CocoaPods is configured, add a `swiftPMDependencies` block and
declare the SwiftPM equivalent of each CocoaPods dependency. Do **not** disable the CocoaPods plugin
yet.

**Example — adding Firebase Analytics via SwiftPM while keeping CocoaPods:**

```kotlin
kotlin {
    swiftPMDependencies {
        package(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
        version = from("12.5.0"),
        products = listOf(product("FirebaseAnalytics"))
        )
    }
    cocoapods {
        // Keep existing CocoaPods configuration for now
        pod("FirebaseAnalytics") {
            version = "12.5.0"
        }
        // ... rest of cocoapods { }
    }
}
```

Use the same (or compatible) versions for both CocoaPods and SwiftPM so behavior stays consistent
during the transition.

---

## Step 2 — Migrate framework configuration to the binaries API

If you had framework options in the `cocoapods { framework { ... } }` block, move that configuration
to the **binaries** API so the framework is declared without CocoaPods.

**Before (CocoaPods):**

```kotlin
kotlin {
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    cocoapods {
        framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}
```

**After (binaries):**

```kotlin
kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}
```

Adjust `baseName` and `isStatic` to match your previous CocoaPods setup.

---

## Step 3 — Migrate CocoaPods API usage to SwiftPM-imported APIs

Update Kotlin and Swift code that referenced CocoaPods-imported APIs to use the
APIs [imported by the SwiftPM integration](https://docs.google.com/document/d/18Rt0lIDw_OGvQJ-9Z4v226cSEJ50yrhyLLcjobZq0Pk/edit?tab=t.0#heading=h.der0asglr4cq).
Import paths and types may differ; typically you will switch from `cocoapods.<PodName>.*` to
`swiftPMImport.<group>.<project>.*`. Build the shared module and fix any compile errors.

---

## Step 4 — Reconfigure Xcode for direct integration

Reconfigure the iOS app target to use the Kotlin/Native **direct integration** (embed and sign the
framework via a Gradle task) instead of CocoaPods. You can use the **migration command** provided by
the tooling or follow
the [manual steps](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html#connect-the-framework-to-your-project).

### Obtaining the migration command

**Option A — Build in Xcode**

Build the **CocoaPods workspace** in Xcode (open the `.xcworkspace`) with the scheme you normally
use. If the project is not yet migrated, the build may fail and the output will include a *
*migration command**.

**Option B — Build from the command line**

1. Change to the directory that contains your `Podfile` and `.xcworkspace` (e.g. the iOS app
   folder):

   ```bash
   cd /path/to/iosApp
   ```

2. Run a build for the iOS Simulator and capture the output:

   ```bash
   xcodebuild -scheme "<YourAppScheme>" \
     -workspace *.xcworkspace \
     -destination 'generic/platform=iOS Simulator' \
     ARCHS=arm64 2>&1 | grep -A 5 'What went wrong'
   ```

   Replace `<YourAppScheme>` with the scheme that builds your app (often the same as the workspace
   name). If you need to discover the scheme name, use:

   ```bash
   xcodebuild -workspace *.xcworkspace -list -json
   ```

3. In the build output, look for a line similar to:

   ```bash
   XCODEPROJ_PATH='/path/to/kmp-project/iosApp.xcodeproj' \
   GRADLE_PROJECT_PATH=':shared' \
   '/path/to/kmp-project/gradlew' -p '/path/to/kmp-project' \
     ':shared:integrateEmbedAndSign' ':shared:integrateLinkagePackage'
   ```

### Running the migration command

Run the **exact** command printed by the tooling in your terminal (from the project root or the path
indicated). It will:

- Update your `.xcodeproj` so that the **embedAndSignAppleFrameworkForXcode** Gradle task runs at
  the correct time during the build.
- Optionally add the internal Swift package and linkage (e.g. for SwiftPM dependencies).

You only need to run this once. Do **not** add the `integrateLinkagePackage` task to your Xcode
target’s build phases.

### If you see: "SwiftPM dependencies with embedAndSign integration"

When your Xcode project uses **SwiftPM dependencies** (e.g. in `swiftPMDependencies`) together with
the **embedAndSignAppleFrameworkForXcode** integration, the build may fail with:

```text
error: You have SwiftPM dependencies with embedAndSign integration.
error: Please integrate with synthetic import linkage project by
error: running the following command:
error: XCODEPROJ_PATH='.../iosApp/iosApp.xcodeproj' '.../gradlew' -p '...' ':shared:integrateLinkagePackage' -i
```

**Fix:** Run the command exactly as shown in the error (from your project root). For example:

```bash
XCODEPROJ_PATH='/path/to/your-project/iosApp/iosApp.xcodeproj' ./gradlew -p '/path/to/your-project' ':shared:integrateLinkagePackage' -i
```

This one-time step adds the synthetic import linkage to your `.xcodeproj` so SwiftPM dependencies
are correctly linked into the app. After it succeeds, build again from Xcode; the error should be
resolved.

---

## Step 5 — Disable CocoaPods in the iOS app

### If the iOS app only used CocoaPods for KMP

Deintegrate CocoaPods entirely:

```bash
cd /path/to/iosApp
pod deintegrate
```

Then open the **`.xcodeproj`** (not the `.xcworkspace`) and build. The app should link the shared
framework via the direct integration.

### If the iOS app still needs CocoaPods for other pods

If you have other CocoaPods dependencies that do **not** overlap with the ones you moved to SwiftPM:

1. Open the **Podfile**.
2. Remove **only** the line that references the KMP shared framework, for example:

   ```ruby
   target 'iosApp' do
     # Remove this line and run "pod install" again:
     # pod 'Shared', :path => '../shared'
     pod 'SomeOtherPod', '~> 1.0'
   end
   ```

3. Run:

   ```bash
   pod install
   ```

4. Continue to use the **`.xcworkspace`** for Xcode if you have other pods; the shared framework
   will still be embedded via the Gradle integration.

---

## Step 6 — Remove the CocoaPods plugin from the build

Once the iOS app builds successfully with the direct integration (and optionally with other
CocoaPods):

1. Remove the **`kotlin("native.cocoapods")`** plugin from the root and shared module
   `build.gradle.kts`.
2. Remove the **`cocoapods { ... }`** block from the Kotlin extension in the shared module.
3. Sync the Gradle project and run a full clean build:

   ```bash
   ./gradlew clean assemble
   ```

4. Build the iOS app from Xcode (using `.xcodeproj` if you ran `pod deintegrate`, or `.xcworkspace`
   if you kept other pods).

---

## Verification

- **Gradle:** `./gradlew :shared:assemble` (or equivalent) completes without errors.
- **Xcode:** The iOS app target builds and runs in the simulator or on device.
- **Runtime:** App launches and any code that uses the former CocoaPods (now SwiftPM) APIs behaves
  as before.

If you encounter linker or runtime errors related to the framework or SwiftPM, ensure the migration
command was run and that you are building with the configuration described
in [Integration with Xcode builds](SWIFTPM_IMPORT_MANUAL.md#integration-with-xcode-builds) in the
SwiftPM import manual.
