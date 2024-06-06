package com.santimattius.kmp.playground

import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration as BugsnagConfiguration

actual typealias Configuration = BugsnagConfiguration

actual typealias TrackableException = Throwable

actual fun Throwable.asTrackableException() = this

internal actual class PlatformTracker {
    actual fun initialize(config: Configuration) {
        val context = applicationContext ?: run {
            // TODO: add logging later
            return
        }
        Bugsnag.start(context, config)
    }

    actual fun track(exception: TrackableException) {
        Bugsnag.notify(exception)
    }
}