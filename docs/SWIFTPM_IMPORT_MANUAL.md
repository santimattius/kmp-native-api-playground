# Swift Package Manager (SwiftPM) Import — Manual

**Summary:** This guide describes how to use the SwiftPM import integration in the Kotlin Gradle
Plugin to consume Swift Package Manager dependencies in Kotlin Multiplatform (KMP) projects
targeting Apple platforms. You can import Objective-C APIs from SwiftPM packages into your
Kotlin/Native iOS (and other Apple) targets.

---

## Table of contents

1. [Prerequisites](#prerequisites)
2. [Getting started](#getting-started)
3. [Using APIs from SwiftPM packages in Kotlin](#using-apis-from-swiftpm-packages-in-kotlin)
4. [Integration with Xcode builds](#integration-with-xcode-builds)
5. [Additional features](#additional-features)

---

## Prerequisites

### Environment

| Requirement      | Details                                                                                           |
|------------------|---------------------------------------------------------------------------------------------------|
| **Kotlin**       | Project must use Kotlin **2.2.21** (or the SwiftPM-capable version indicated in the plugin docs). |
| **Xcode**        | **Xcode 16.4** or **Xcode 26.0** (26.1+ may also work).                                           |
| **Repositories** | JetBrains Maven repository for the Kotlin Gradle Plugin and dependencies.                         |

### Repository configuration

Add the following to `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
        mavenCentral()
        gradlePluginPortal()
    }
}
```

### Kotlin Gradle Plugin version

Use the SwiftPM-capable Kotlin Multiplatform plugin (e.g. **2.2.21-titan-211**).

**Option A — Version catalog** (`gradle/libs.versions.toml`):

```toml
[versions]
kotlin = "2.2.21-titan-211"
```

**Option B — Build script** (`build.gradle.kts`):

```kotlin
plugins {
    kotlin("multiplatform") version "2.2.21-titan-211"
}
```

### Enabling the `swiftPMDependencies` block

If the `swiftPMDependencies` block does not appear after updating the Kotlin version, add this in
the **root** project’s build script:

```kotlin
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21-titan-211!!")
    }
}
```

### KMP IntelliJ plugin and framework path

If you use the **KMP IntelliJ plugin** to build the iOS app, point the plugin to your Xcode project
in the script where the Kotlin/Native framework is declared:

```kotlin
kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = false
        }
    }
    swiftPMDependencies {
        xcodeProjectPathForKmpIJPlugin.set(
            layout.projectDirectory.file("../iosApp/iosApp.xcodeproj")
        )
    }
}
```

Replace `../iosApp/iosApp.xcodeproj` with the path to the `.xcodeproj` that uses the
`embedAndSignAppleFrameworkForXcode` integration.

### Sample projects

Official CocoaPods-based samples were adapted to use SwiftPM on the `spm_import` branch:

- [KMP with CocoaPods Compose sample (SwiftPM branch)](https://github.com/Kotlin/kmp-with-cocoapods-compose-sample/tree/spm_import)
- [KMP with CocoaPods Firebase sample (SwiftPM branch)](https://github.com/Kotlin/kmp-with-cocoapods-firebase-sample/tree/spm_import)

---

## Getting started

### Adding SwiftPM dependencies

Declare SwiftPM dependencies inside the `swiftPMDependencies` block of the Kotlin extension. The
plugin imports **Objective-C** APIs from those packages for your Apple targets.

**Example — Firebase Analytics:**

```kotlin
kotlin {
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    swiftPMDependencies {
        package(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
        version = from("12.5.0"),
        products = listOf(product("FirebaseAnalytics"))
        )
    }
}
```

**Optional — Transitive dependency (e.g. swift-protobuf):**

You can pin a transitive dependency explicitly if needed:

```kotlin
package(
url = url("https://github.com/apple/swift-protobuf.git"),
version = exact("1.32.0"),
products = listOf()
)
```

### Module discovery

SwiftPM import works by importing **Clang modules**. By default, the plugin discovers accessible
Clang modules automatically and exposes them to Kotlin, similar to Swift/Objective-C visibility.

To control which modules are imported:

- Set **`discoverModulesImplicitly = false`** so that only explicitly listed modules are used.
- Use **`importedModules`** when the Clang module name does not match the product name.

**Example — Explicit modules (e.g. Firebase):**

```kotlin
kotlin {
    swiftPMDependencies {
        discoverModulesImplicitly = false
        package(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
        version = from("12.5.0"),
        products = listOf(product("FirebaseAnalytics"), product("FirebaseFirestore")),
        importedModules = listOf(
            "FirebaseAnalytics",
            "FirebaseFirestoreInternal"  // Objective-C APIs for Firestore
        )
        )
    }
}
```

---

## Using APIs from SwiftPM packages in Kotlin

Imported Objective-C APIs are placed under a namespace:

**`swiftPMImport.<gradleProjectGroup>.<gradleProjectName>`**

**Example:**

```kotlin
// subproject/build.gradle.kts
group = "groupName"

// subproject/src/iosMain/kotlin/useFirebaseAnalytics.kt
import swiftPMImport . groupName . subproject . FIRAnalytics
        import swiftPMImport . groupName . subproject . FIRApp
```

Use these types in your `iosMain` (or other Apple) Kotlin source sets as usual.

---

## Integration with Xcode builds

When your Xcode project uses **SwiftPM dependencies** together with the *
*embedAndSignAppleFrameworkForXcode** integration, the tooling may prompt you to apply extra
configuration so that:

- SwiftPM dependencies are correctly linked into the final app, and
- Linker and runtime errors are avoided.

You may be asked to run the **`integrateLinkagePackage`** Gradle task **once**. This adds the
internal Swift package and linkage; it does **not** need to be added to your Xcode target’s build
phases. Follow the on-screen instructions from the integration when building from Xcode.

---

## Additional features

### Local SwiftPM packages

You can depend on a **local** Swift Package (e.g. in a sibling directory).

**1. Package manifest** (e.g. `/path/to/GrpcSwiftWrapper/Package.swift`):

```swift
let package = Package(
    name: "GrpcSwiftWrapper",
    platforms: [.iOS("15.0")],
    products: [
        .library(name: "GrpcSwiftWrapper", targets: ["GrpcSwiftWrapper"]),
    ],
    dependencies: [
        .package(url: "https://github.com/grpc/grpc-swift.git", exact: "1.27.0"),
    ],
    targets: [
        .target(
            name: "GrpcSwiftWrapper",
            dependencies: [.product(name: "GRPC", package: "grpc-swift")]
        ),
    ]
)
```

**2. Build script — declare the local package:**

```kotlin
kotlin {
    swiftPMDependencies {
        localPackage(
            path = projectDir.resolve("../path/to/GrpcSwiftWrapper"),
            products = listOf("GrpcSwiftWrapper")
        )
    }
}
```

**3. Creating a new local package (optional):**

```bash
cd /path/to/shared
mkdir LocalPackage
cd LocalPackage
swift package init --type library --name LocalPackage
# Add sources, e.g.:
# echo 'import Foundation
# @objc public class HelloFromLocalPackage: NSObject {
#   @objc public func hello() { print("Hello from local package") }
# }' > Sources/LocalPackage/LocalPackage.swift
```

**4. Use in Kotlin:**

```kotlin
// shared/src/appleMain/kotlin/useLocalPackage.kt
import swiftPMImport.shared.HelloFromLocalPackage

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
fun useLocalPackage() {
    HelloFromLocalPackage().hello()
}
```

### Platform constraints

Some SwiftPM packages only support certain platforms (e.g. iOS only). Use the **`platforms`**
parameter so the dependency is applied only to the relevant compilations:

```kotlin
kotlin {
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    macosArm64()
    swiftPMDependencies {
        package(
            url = url("https://github.com/googlemaps/ios-maps-sdk.git"),
        version = exact("10.3.0"),
        products = listOf(
            product(
                "GoogleMaps",
                platforms = setOf(iOS())  // Only iOS compilations
            )
        )
        )
    }
}
```

### Deployment version

If a dependency requires a higher minimum deployment version, set it in the Kotlin extension:

```kotlin
kotlin {
    swiftPMDependencies {
        iosDeploymentVersion.set("16.0")
    }
}
```

### Dependency specification (location and version)

**Location** — use either:

- **`url`** — Git repository URL.
- **`repository`** — Swift Package Registry ID (
  see [Apple’s SwiftPM registry documentation](https://docs.swift.org/swiftpm/documentation/packagemanagerdocs/usingswiftpackageregistry)).

**Version** — use one of:

| API                                 | Description                               |
|-------------------------------------|-------------------------------------------|
| `from("1.0")`                       | Minimum version (Gradle-style “require”). |
| `exact("2.0")`                      | Exact version (Gradle-style “strict”).    |
| `branch("...")` / `revision("...")` | Git branch or revision.                   |

**Example:**

```kotlin
package(
url = url("https://github.com/firebase/firebase-ios-sdk.git"),
version = from("12.5.0")
)
```

### Transitive SwiftPM dependencies

SwiftPM dependencies of your project are **transitively** applied to consumers (e.g. when running
Kotlin/Native tests or linking the framework). The Kotlin Gradle Plugin provisions the required
native code from these transitive SwiftPM dependencies automatically; no extra configuration is
needed.
