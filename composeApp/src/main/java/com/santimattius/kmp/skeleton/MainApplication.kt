package com.santimattius.kmp.skeleton

import android.app.Application
import com.santimattius.kmp.playground.Configuration
import com.santimattius.kmp.playground.Bugsnag

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Bugsnag.initialize(config = Configuration.load(this))
    }
}