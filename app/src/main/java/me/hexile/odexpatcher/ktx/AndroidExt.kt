package me.hexile.odexpatcher.ktx

import android.annotation.SuppressLint
import me.hexile.odexpatcher.core.App

// https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/SystemProperties.java
@SuppressLint("PrivateApi")
fun getProperty(key: String, def: String): String {
    val c = Class.forName("android.os.SystemProperties")
    val method = c.getMethod("get", String::class.java, String::class.java)
    return method.invoke(c, key, def) as String
}

fun appUid(packageName: String): Int {
    return App.getContext().packageManager.getApplicationInfo(packageName, 0).uid
}

fun selfAppUid(): Int {
    return appUid(App.getContext().packageName)
}

fun multiuserSharedAppGid(packageName: String): Int {
    return multiuserSharedAppGid(appUid(packageName))
}

fun multiuserSharedAppGid(appUid: Int): Int {
    // Definition: https://android.googlesource.com/platform/system/core/+/refs/tags/android-12.0.0_r1/libcutils/include/private/android_filesystem_config.h#198
    // Used by: https://android.googlesource.com/platform/system/core/+/refs/tags/android-12.0.0_r1/libcutils/multiuser.cpp#56
    //  -> https://android.googlesource.com/platform/frameworks/native/+/refs/tags/android-12.0.0_r1/cmds/installd/InstalldNativeService.cpp#380
    return (appUid % 100000) - 10000 + 50000
}

fun cacheAppGid(packageName: String): Int {
    return cacheAppGid(appUid(packageName))
}

fun cacheAppGid(appUid: Int): Int {
    return appUid - 10000 + 20000
}