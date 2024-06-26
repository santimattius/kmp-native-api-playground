package com.santimattius.kmp.playground

import cocoapods.Bugsnag.Bugsnag
import cocoapods.Bugsnag.BugsnagConfiguration
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSException

@OptIn(ExperimentalForeignApi::class)
actual typealias Configuration = BugsnagConfiguration
actual typealias TrackableException = NSException

actual fun Throwable.asTrackableException() = NSException.exceptionWithName(
    name = this::class.simpleName,
    reason = message ?: toString(),
    userInfo = null
)

@OptIn(ExperimentalForeignApi::class)
internal actual class PlatformTracker {
    actual fun initialize(config: Configuration) {
        Bugsnag.startWithConfiguration(config)
    }

    actual fun track(exception: TrackableException) {
        Bugsnag.notify(exception)
    }
}
