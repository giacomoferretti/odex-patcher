package com.giacomoferretti.odexpatcher.library

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.giacomoferretti.odexpatcher.library.ktx.filename
import java.io.File

// https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/SystemProperties.java
@SuppressLint("PrivateApi")
fun getProperty(key: String, def: String): String {
    val c = Class.forName("android.os.SystemProperties")
    val method = c.getMethod("get", String::class.java, String::class.java)
    return method.invoke(c, key, def) as String
}

object Art {
    val CPU_ABI: String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        Build.CPU_ABI
    } else {
        Build.SUPPORTED_ABIS[0]
    }

    val ISA: String = when (CPU_ABI) {
        // Reference: https://android.googlesource.com/platform/art/+/refs/tags/android-13.0.0_r1/libartbase/arch/instruction_set.cc#40
        "armeabi-v7a", "armeabi" -> "arm"
        "arm64-v8a" -> "arm64"
        "x86" -> "x86"
        "x86_64" -> "x86_64"
        else -> "none"
    }

    val ISA_VARIANT = getProperty("dalvik.vm.isa.$ISA.variant", ISA)
    val ISA_FEATURES = getProperty("dalvik.vm.isa.$ISA.features", "default")

    fun getOatFolder(baseApk: String): String {
        return when {
            Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT -> {
                "/data/dalvik-cache/"
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                "/data/dalvik-cache/$ISA/"
            }
            else -> {
                File(baseApk).parent!! + "/oat/$ISA/"
            }
        }
    }

    fun getOatFile(baseApk: String): String {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                // https://android.googlesource.com/platform/art/+/refs/tags/android-13.0.0_r1/libartbase/base/file_utils.cc#426
                getOatFolder(baseApk) + baseApk.substring(1).replace("/", "@") + "@classes.dex"
            }
            else -> {
                getOatFolder(baseApk) + baseApk.filename().replace(".apk", ".odex")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getVdexFile(baseApk: String): String {
        return getOatFolder(baseApk) + baseApk.filename().replace(".apk", ".vdex")
    }

    fun isOptimized(baseApk: String): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            File(getOatFolder(baseApk)).exists() && File(getOatFile(baseApk)).exists()
        } else {
            File(getOatFolder(baseApk)).exists() && File(getOatFile(baseApk)).exists() &&
                    File(getVdexFile(baseApk)).exists()
        }
    }

    fun isRuntimeArt(): Boolean {
        val vmVersion = System.getProperty("java.vm.version")
        return vmVersion != null && vmVersion.startsWith("2")
    }
}

//class ArtException : Exception {
//    constructor() : super()
//    constructor(message: String) : super(message)
//    constructor(message: String, cause: Throwable) : super(message, cause)
//    constructor(cause: Throwable) : super(cause)
//    constructor(
//        message: String,
//        expected: ByteArray,
//        got: ByteArray
//    ) : super("$message Expected: ${expected.toHexString()}, got ${got.toHexString()}")
//}

