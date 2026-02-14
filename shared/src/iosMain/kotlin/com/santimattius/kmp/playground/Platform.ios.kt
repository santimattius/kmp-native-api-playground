package com.santimattius.kmp.playground

import platform.UIKit.UIDevice

/** iOS [Platform] implementation; [name] is the device system name and version (e.g. "iOS 17.0"). */
class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

/** iOS actual: returns [IOSPlatform]. */
actual fun getPlatform(): Platform = IOSPlatform()
