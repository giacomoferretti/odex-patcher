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

import me.hexile.odexpatcher.utils.read
import me.hexile.odexpatcher.utils.toInt
import java.io.File
import java.io.RandomAccessFile

class VdexFile(private val file: File) {

    object Const {
        val VDEX_HEADER = "oat\n".toByteArray()
        const val VDEX_VERSION_OREO = "006"
        const val VDEX_VERSION_OREO_MR1 = "010"
        const val VDEX_VERSION_PIE = "019"
        const val VDEX_VERSION_ANDROID_10 = "021"
    }

    var vdexVerifierDepsVersion: ByteArray
    var vdexDexSectionVersion: ByteArray
    var vdexNumberOfDexFiles: ByteArray

    private var offset = 0

    init {
        RandomAccessFile(file, "r").use {
            // Check correct header
            if (!it.read(4).contentEquals(Const.VDEX_HEADER)) {
                throw Exception("VDEX doesn't contain correct magic header.")
            }

            vdexVerifierDepsVersion = it.read(4, 4)
            vdexDexSectionVersion = it.read(8, 4)
            vdexNumberOfDexFiles = it.read(12, 4)

            offset = when (versionString) {
                // Android 8.0.0 - 8.1.0
                // https://cs.android.com/android/platform/superproject/+/android-8.0.0_r1:art/runtime/vdex_file.h;l=71
                // https://cs.android.com/android/platform/superproject/+/android-8.1.0_r1:art/runtime/vdex_file.h;l=78
                Const.VDEX_VERSION_OREO,
                Const.VDEX_VERSION_OREO_MR1 -> {
                    24
                }

                // Android 9.0.0
                // https://cs.android.com/android/platform/superproject/+/android-9.0.0_r1:art/runtime/vdex_file.h;l=107
                Const.VDEX_VERSION_PIE -> {
                    20
                }

                // Android 10 - Android 11
                // https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:art/runtime/vdex_file.h;l=129
                // https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:art/runtime/vdex_file.h;l=129
                Const.VDEX_VERSION_ANDROID_10 -> {
                    28
                }

                else -> {
                    throw Exception(String.format("Unknown vdex version %s", versionString))
                }
            }
        }
    }

    fun patch(checksums: Map<String, ByteArray>) {
        RandomAccessFile(file, "rw").use {
            for (i in 0 until vdexNumberOfDexFiles.toInt()) {
                val checksumOffset = offset + (i * 4)
                it.seek(checksumOffset.toLong())
                it.write(checksums.entries.elementAt(i).value)
            }
        }
    }

    private val versionString: String
        get() {
            return String(vdexVerifierDepsVersion).substring(0, 3)
        }
}
