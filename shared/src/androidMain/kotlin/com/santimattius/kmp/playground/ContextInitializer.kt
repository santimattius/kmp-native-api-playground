package com.santimattius.kmp.playground

import android.content.Context
import androidx.startup.Initializer

/**
 * Application context captured at startup for use by [PlatformTracker] (Bugsnag initialization).
 * Set by [ContextInitializer]; null until the app's [Initializer] runs.
 */
internal var applicationContext: Context? = null
    private set

/**
 * [Initializer] that stores the application [Context] in [applicationContext]
 * so the shared module can initialize Bugsnag without the app passing context into the KMP API.
 * Register in the Android manifest under `androidx.startup`.
 */
class ContextInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        applicationContext = context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}