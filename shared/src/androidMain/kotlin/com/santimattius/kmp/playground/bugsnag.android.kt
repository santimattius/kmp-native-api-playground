package com.santimattius.kmp.playground

import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration as BugsnagConfiguration

/** Android actual: [Configuration] is the Bugsnag Android [BugsnagConfiguration]. */
actual typealias Configuration = BugsnagConfiguration

/** Android actual: [TrackableException] is [Throwable]. */
actual typealias TrackableException = Throwable

/** Android actual: [Throwable] is already [TrackableException], so returns this. */
actual fun Throwable.asTrackableException() = this

/** Android implementation of [PlatformTracker]; forwards to [Bugsnag]. */
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