package com.santimattius.kmp.playground

/**
 * Simple greeting that uses the current [Platform] name.
 * Example of shared logic that works on all targets without platform-specific code.
 */
class Greeting {
    private val platform = getPlatform()

    /**
     * Returns a greeting string that includes the current platform name.
     * @return e.g. "Hello, Android 34!" or "Hello, iOS 17.0!"
     */
    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}