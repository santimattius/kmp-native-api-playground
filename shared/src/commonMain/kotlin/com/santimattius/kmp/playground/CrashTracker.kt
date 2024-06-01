package com.santimattius.kmp.playground

expect class Configuration
expect class TrackableException

object CrashTracker {

    private val provider: TrackerProvider by lazy {
        TrackerProvider()
    }

    fun initialize(config: Configuration) {
        provider.initialize(config)
    }

    fun track(exception: TrackableException) {
        provider.track(exception)
    }
}

expect class TrackerProvider(){
    fun initialize(config: Configuration)
    fun track(exception: TrackableException)
}