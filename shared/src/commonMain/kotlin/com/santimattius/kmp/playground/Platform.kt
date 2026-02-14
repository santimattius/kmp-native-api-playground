package com.santimattius.kmp.playground

/**
 * Abstraction of the current platform (Android or iOS) for shared code.
 * Used to display platform name/version or branch logic when needed.
 */
interface Platform {
    /** Human-readable platform identifier (e.g. "Android 34" or "iOS 17.0"). */
    val name: String
}

/**
 * Returns the [Platform] implementation for the current target (Android or iOS).
 */
expect fun getPlatform(): Platform