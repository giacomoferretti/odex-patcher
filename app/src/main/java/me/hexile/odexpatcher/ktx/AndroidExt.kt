package me.hexile.odexpatcher.ktx

import android.annotation.SuppressLint

// https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/SystemProperties.java
@SuppressLint("PrivateApi")
fun getProperty(key: String, def: String): String {
    val c = Class.forName("android.os.SystemProperties")
    val method = c.getMethod("get", String::class.java, String::class.java)
    return method.invoke(c, key, def) as String
}