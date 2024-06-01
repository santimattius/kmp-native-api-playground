package com.santimattius.kmp.playground

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform