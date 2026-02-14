import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
//    alias(libs.plugins.cocoaPods)
    alias(libs.plugins.bugsnagAndroid)
    //alias(libs.plugins.skie)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidLibrary {
        namespace = "com.santimattius.kmp.playground"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withJava()
    }

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
    swiftPMDependencies {
        xcodeProjectPathForKmpIJPlugin.set(
            // Specify path to the xcodeproj with :embedAndSignAppleFrameworkForXcode integration in this parameter
            layout.projectDirectory.file("../iosApp/iosApp.xcodeproj")
        )
        `package`(
            url = url("https://github.com/bugsnag/bugsnag-cocoa.git"),
            version = from("6.35.0"),
            products = listOf(product("Bugsnag")),
        )
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            api(libs.bugsnag.android)
            implementation(libs.androidx.startup.runtime)
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}
