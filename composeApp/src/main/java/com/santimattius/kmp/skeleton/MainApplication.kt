package com.santimattius.kmp.skeleton

import android.app.Application
import com.santimattius.kmp.playground.Configuration
import com.santimattius.kmp.playground.CrashTracker

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashTracker.initialize(config = Configuration.load(this))
    }
}