package com.santimattius.kmp.playground

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * Example repository that triggers exceptions and reports them via [Bugsnag].
 * Demonstrates using the shared [Bugsnag.track] and [Throwable.asTrackableException] API
 * from common code with a [CoroutineExceptionHandler].
 */
class CrashRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    /**
     * Launches coroutines that throw; uncaught exceptions are reported to Bugsnag
     * via the installed [CoroutineExceptionHandler].
     * For demonstration only; do not use as a pattern for production error handling.
     */
    suspend fun crash() {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception")
            Bugsnag.track(exception.asTrackableException())
        }
        val job = coroutineScope.launch(handler) { // root coroutine, running in GlobalScope
            throw AssertionError()
        }
        val deferred = coroutineScope.async(handler) { // also root, but async instead of launch
            throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
        }
        joinAll(job, deferred)
    }
}