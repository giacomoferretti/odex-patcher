@file:JvmName("#") // Hide from JVM

package com.giacomoferretti.odexpatcher.library.internal

import android.annotation.SuppressLint

@JvmSynthetic
fun getProperty(key: String): String {
    return getProperty(key, "")
}

// https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-13.0.0_r1/core/java/android/os/SystemProperties.java#163
@JvmSynthetic
@SuppressLint("PrivateApi")
fun getProperty(key: String, def: String): String {
    val c = Class.forName("android.os.SystemProperties")
    val method = c.getMethod("get", String::class.java, String::class.java)
    return method.invoke(c, key, def) as String
}