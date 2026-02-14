package com.santimattius.kmp.playground

/**
 * Platform-agnostic configuration for the crash reporting SDK.
 * Actual type is the native SDK configuration (e.g. Bugsnag Android/iOS config).
 */
expect class Configuration

/**
 * Platform-agnostic representation of an exception that can be reported to the crash reporting SDK.
 * On Android this is [Throwable]; on iOS it is the native NSException type.
 */
expect class TrackableException

/**
 * Converts this [Throwable] into a [TrackableException] suitable for [Bugsnag.track].
 * On Android returns this instance; on iOS creates an NSException with the same message and type name.
 */
expect fun Throwable.asTrackableException(): TrackableException

/**
 * Cross-platform facade for crash and error reporting.
 * Wraps the native Bugsnag SDK (or equivalent) behind a common API so shared code can report
 * exceptions without depending on platform-specific types.
 *
 * Must call [initialize] with a [Configuration] (e.g. from the app's Application or iOS App delegate)
 * before calling [track].
 */
object Bugsnag {

    private val provider: PlatformTracker = PlatformTracker()

    /**
     * Initializes the crash reporting SDK with the given [config].
     * Call once at app startup from the main application (Android Application or iOS App delegate).
     */
    fun initialize(config: Configuration) {
        provider.initialize(config)
    }

    /**
     * Reports [exception] to the crash reporting backend.
     * Use [Throwable.asTrackableException] to convert a [Throwable] from common code.
     */
    fun track(exception: TrackableException) {
        provider.track(exception)
    }
}

/**
 * Platform-specific implementation that forwards to the native Bugsnag SDK.
 * Internal API; use [Bugsnag] from shared code.
 */
internal expect class PlatformTracker() {
    fun initialize(config: Configuration)
    fun track(exception: TrackableException)
}