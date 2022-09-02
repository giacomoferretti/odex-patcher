package com.giacomoferretti.odexpatcher.library

import android.os.Build
import androidx.annotation.RequiresApi
import com.giacomoferretti.odexpatcher.library.internal.getProperty
import com.giacomoferretti.odexpatcher.library.internal.ktx.filename
import java.io.File

object Art {
    @JvmStatic
    fun getIsaVariant(isa: InstructionSet): String {
        return getIsaVariant(isa.value)
    }

    @JvmStatic
    fun getIsaVariant(isa: String): String {
        return getProperty("dalvik.vm.isa.$isa.variant")
    }

    @JvmStatic
    fun getIsaFeatures(isa: InstructionSet): String {
        return getIsaFeatures(isa.value)
    }

    @JvmStatic
    fun getIsaFeatures(isa: String): String {
        return getProperty("dalvik.vm.isa.$isa.features")
    }

    @JvmStatic
    fun getOatFolder(baseApk: String, isa: InstructionSet): String {
        return getOatFolder(baseApk, isa.value)
    }

    @JvmStatic
    fun getOatFolder(baseApk: String, isa: String): String {
        return when {
            Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT -> {
                "/data/dalvik-cache/"
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                "/data/dalvik-cache/$isa/"
            }
            else -> {
                File(baseApk).parent!! + "/oat/$isa/"
            }
        }
    }

    @JvmStatic
    fun getOatFile(baseApk: String, isa: InstructionSet): String {
        return getOatFile(baseApk, isa.value)
    }

    @JvmStatic
    fun getOatFile(baseApk: String, isa: String): String {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                // https://android.googlesource.com/platform/art/+/refs/tags/android-13.0.0_r1/libartbase/base/file_utils.cc#426
                getOatFolder(baseApk, isa) + baseApk.substring(1).replace("/", "@") + "@classes.dex"
            }
            else -> {
                getOatFolder(baseApk, isa) + baseApk.filename().replace(".apk", ".odex")
            }
        }
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.O)
    fun getVdexFile(baseApk: String, isa: InstructionSet): String {
        return getVdexFile(baseApk, isa.value)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.O)
    fun getVdexFile(baseApk: String, isa: String): String {
        return getOatFolder(baseApk, isa) + baseApk.filename().replace(".apk", ".vdex")
    }

    @JvmStatic
    fun isOptimized(baseApk: String, isa: InstructionSet): Boolean {
        return isOptimized(baseApk, isa.value)
    }

    @JvmStatic
    fun isOptimized(baseApk: String, isa: String): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            File(getOatFolder(baseApk, isa)).exists() && File(getOatFile(baseApk, isa)).exists()
        } else {
            File(getOatFolder(baseApk, isa)).exists() && File(getOatFile(baseApk, isa)).exists() &&
                    File(getVdexFile(baseApk, isa)).exists()
        }
    }

    @JvmStatic
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

