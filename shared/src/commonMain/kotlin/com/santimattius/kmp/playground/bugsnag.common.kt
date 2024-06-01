package com.santimattius.kmp.playground

expect class Configuration
expect class TrackableException

object Bugsnag {

    private val provider: PlatformTracker = PlatformTracker()

    fun initialize(config: Configuration) {
        provider.initialize(config)
    }

    fun track(exception: TrackableException) {
        provider.track(exception)
    }
}

internal expect class PlatformTracker(){
    fun initialize(config: Configuration)
    fun track(exception: TrackableException)
}