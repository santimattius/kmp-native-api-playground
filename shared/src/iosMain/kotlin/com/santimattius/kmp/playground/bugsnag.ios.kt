package com.santimattius.kmp.playground

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSException
import swiftPMImport.kmp.native.api.playground.shared.BugsnagConfiguration

/** iOS actual: [Configuration] is CocoaPods Bugsnag [BugsnagConfiguration]. */
@OptIn(ExperimentalForeignApi::class)
actual typealias Configuration = BugsnagConfiguration

/** iOS actual: [TrackableException] is [NSException]. */
actual typealias TrackableException = NSException

/** iOS actual: builds an [NSException] from this [Throwable] for [Bugsnag.track]. */
actual fun Throwable.asTrackableException() = NSException.exceptionWithName(
    name = this::class.simpleName,
    reason = message ?: toString(),
    userInfo = null
)

/** iOS implementation of [PlatformTracker]; forwards to CocoaPods Bugsnag. */
@OptIn(ExperimentalForeignApi::class)
internal actual class PlatformTracker {
    actual fun initialize(config: Configuration) {
        Bugsnag.initialize(config)
    }

    actual fun track(exception: TrackableException) {
        Bugsnag.track(exception)
    }
}
