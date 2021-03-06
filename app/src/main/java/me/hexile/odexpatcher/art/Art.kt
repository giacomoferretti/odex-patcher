/*
 * Copyright 2020 Giacomo Ferretti
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
import me.hexile.odexpatcher.core.Const
import java.io.File

object Art {
    val CPU_ABI: String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        Build.CPU_ABI
    } else {
        Build.SUPPORTED_ABIS[0]
    }

    val ISA_FOLDER: String = when(CPU_ABI) {
        // Reference: https://cs.android.com/android/platform/superproject/+/master:art/libartbase/arch/instruction_set.cc;l=40
        "armeabi-v7a", "armeabi" -> "arm"
        "arm64-v8a" -> "arm64"
        "x86" -> "x86"
        "x86_64" -> "x86_64"
        else -> "none"
    }

    fun getOatFolder(baseApk: String): String {
        return when {
            Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT -> {
                "/data/dalvik-cache/"
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                "/data/dalvik-cache/$ISA_FOLDER/"
            }
            else -> {
                File(baseApk).parent!! + "/oat/$ISA_FOLDER/"
            }
        }
    }

    fun getOatFile(baseApk: String): String {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                getOatFolder(baseApk) + baseApk.substring(1).replace("/", "@") + "@classes.dex"
            }
            else -> {
                getOatFolder(baseApk) + Const.BASE_ODEX_FILE_NAME
            }
        }
    }

    fun getVdexFile(baseApk: String): String {
        return getOatFolder(baseApk) + Const.BASE_VDEX_FILE_NAME
    }

    fun isRuntimeArt(): Boolean {
        val vmVersion = System.getProperty("java.vm.version")
        return vmVersion != null && vmVersion.startsWith("2")
    }
}