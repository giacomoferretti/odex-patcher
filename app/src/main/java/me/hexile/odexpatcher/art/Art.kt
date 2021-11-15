/*
 * Copyright 2020-2021 Giacomo Ferretti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.hexile.odexpatcher.art

import android.os.Build
import me.hexile.odexpatcher.ktx.extractFilename
import me.hexile.odexpatcher.ktx.getProperty
import me.hexile.odexpatcher.ktx.toHexString
import java.io.File

object Art {
    val CPU_ABI: String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        Build.CPU_ABI
    } else {
        Build.SUPPORTED_ABIS[0]
    }

    val ISA: String = when (CPU_ABI) {
        // Reference: https://cs.android.com/android/platform/superproject/+/master:art/libartbase/arch/instruction_set.cc;l=40
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
                // https://android.googlesource.com/platform/art/+/refs/tags/android-12.0.0_r1/libartbase/base/file_utils.cc#406
                getOatFolder(baseApk) + baseApk.substring(1).replace("/", "@") + "@classes.dex"
            }
            else -> {
                getOatFolder(baseApk) + baseApk.extractFilename().replace(".apk", ".odex")
            }
        }
    }

    fun getVdexFile(baseApk: String): String {
        return getOatFolder(baseApk) + baseApk.extractFilename().replace(".apk", ".vdex")
    }

    fun isRuntimeArt(): Boolean {
        val vmVersion = System.getProperty("java.vm.version")
        return vmVersion != null && vmVersion.startsWith("2")
    }
}

class ArtException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
    constructor(
        message: String,
        expected: ByteArray,
        got: ByteArray
    ) : super("$message Expected: ${expected.toHexString()}, got ${got.toHexString()}")
}