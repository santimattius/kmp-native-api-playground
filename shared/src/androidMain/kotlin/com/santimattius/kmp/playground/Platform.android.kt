package com.santimattius.kmp.playground

import android.os.Build

/** Android [Platform] implementation; [name] is "Android &lt;SDK_INT&gt;". */
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

/** Android actual: returns [AndroidPlatform]. */
actual fun getPlatform(): Platform = AndroidPlatform()