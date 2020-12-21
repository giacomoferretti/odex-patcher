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
import me.hexile.odexpatcher.utils.*
import java.io.File
import java.io.RandomAccessFile

class OatFile(private val file: File) {

    object Const {
        val OAT_HEADER = "oat\n".toByteArray()
        const val OAT_OFFSET = 4096
        const val OAT_VERSION_KITKAT = "007"
        const val OAT_VERSION_KITKAT_MR2 = "008"
        const val OAT_VERSION_LOLLIPOP = "039"
        const val OAT_VERSION_LOLLIPOP_MR1 = "045"
        const val OAT_VERSION_MARSHMALLOW = "064"
        const val OAT_VERSION_NOUGAT = "079"
        const val OAT_VERSION_NOUGAT_MR1 = "088"
        const val OAT_VERSION_OREO = "124"
        const val OAT_VERSION_OREO_MR1 = "131"
        const val OAT_VERSION_PIE = "138"
        const val OAT_VERSION_ANDROID_10 = "170"
        const val OAT_VERSION_ANDROID_11 = "183"
    }

    var oatVersion: ByteArray
    var dexFileCount: Int
    var oatDexFilesOffset: Int = 0

    private var offset = 0

    private var fileSize: Long = 0

    init {
        fileSize = file.length()

        RandomAccessFile(file, "r").use {
            it.seek(Const.OAT_OFFSET)

            // Check correct header
            if (!it.read(4).contentEquals(Const.OAT_HEADER)) {
                throw Exception("OAT doesn't contain correct magic header.")
            }

            // Parse data
            oatVersion = it.read(Const.OAT_OFFSET + 4, 4)
            dexFileCount = if (isSdkEqual(Build.VERSION_CODES.KITKAT)) {
                it.read(Const.OAT_OFFSET + 16, 4).toInt()
            } else {
                it.read(Const.OAT_OFFSET + 20, 4).toInt()
            }

            if (isSdkGreaterThan(Build.VERSION_CODES.O_MR1)) {
                oatDexFilesOffset = it.read(Const.OAT_OFFSET + 24, 4).toInt()
            }

            // https://cs.android.com/android/platform/superproject/+/master:bionic/libc/include/android/api-level.h

            offset = when (versionString) {
                // Android 4.4 - 4.4.2 (007)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4.2_r1/runtime/oat.h#84
                // Android 4.4.3 - 4.4.4 (008)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4.3_r1/runtime/oat.h#84
                Const.OAT_VERSION_KITKAT,
                Const.OAT_VERSION_KITKAT_MR2 -> {
                    Const.OAT_OFFSET + 16 * 4
                }

                // Android 5.0.0 - 5.0.2 (039)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-5.0.0_r1/runtime/oat.h#117
                // Android 5.1.0 - 5.1.1 (045)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-5.1.0_r1/runtime/oat.h#119
                Const.OAT_VERSION_LOLLIPOP,
                Const.OAT_VERSION_LOLLIPOP_MR1 -> {
                    Const.OAT_OFFSET + 21 * 4
                }

                // Android 6.0.0 - 6.0.1 (064)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-6.0.0_r1/runtime/oat.h#121
                // Android 7.0 - 7.1 (079)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-7.0.0_r1/runtime/oat.h#131
                // Android 7.1.1 - 7.1.2 (088)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-7.1.1_r1/runtime/oat.h#131
                // Android 8.0.0 (124)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/runtime/oat.h#131
                Const.OAT_VERSION_MARSHMALLOW,
                Const.OAT_VERSION_NOUGAT,
                Const.OAT_VERSION_NOUGAT_MR1,
                Const.OAT_VERSION_OREO -> {
                    Const.OAT_OFFSET + 18 * 4
                }

                // Android 8.1.0 (131)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/runtime/oat.h#134
                // Android 9.0.0 (138)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/runtime/oat.h#135
                Const.OAT_VERSION_OREO_MR1,
                Const.OAT_VERSION_PIE -> {
                    Const.OAT_OFFSET + 19 * 4
                }

                // Android 10 (170)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/runtime/oat.h#116
                Const.OAT_VERSION_ANDROID_10 -> {
                    Const.OAT_OFFSET + 14 * 4
                }

                // Android 11 (183)
                // https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/runtime/oat.h#119
                Const.OAT_VERSION_ANDROID_11 -> {
                    Const.OAT_OFFSET + 15 * 4
                }

                else -> {
                    throw Exception(String.format("Unknown oat version %s", versionString))
                }
            }

            offset += it.read(offset - 4, 4).toInt()
        }
    }

    fun patch(checksums: Map<String, ByteArray>) {
        RandomAccessFile(file, "rw").use {
            for (i in 0 until dexFileCount) {
                // TODO: Better parsing to find checksums
                val baseOffset = when (versionString) {
                    Const.OAT_VERSION_KITKAT,
                    Const.OAT_VERSION_KITKAT_MR2,
                    Const.OAT_VERSION_LOLLIPOP,
                    Const.OAT_VERSION_LOLLIPOP_MR1,
                    Const.OAT_VERSION_MARSHMALLOW,
                    Const.OAT_VERSION_NOUGAT,
                    Const.OAT_VERSION_NOUGAT_MR1,
                    Const.OAT_VERSION_OREO -> {
                        // https://android.googlesource.com/platform/art/+/kitkat-release/compiler/oat_writer.h
                        // https://android.googlesource.com/platform/art/+/lollipop-release/compiler/oat_writer.h
                        // https://android.googlesource.com/platform/art/+/lollipop-mr1-release/compiler/oat_writer.h
                        // https://android.googlesource.com/platform/art/+/marshmallow-release/compiler/oat_writer.h
                        // https://android.googlesource.com/platform/art/+/nougat-release/compiler/oat_writer.h
                        // https://android.googlesource.com/platform/art/+/nougat-mr1-release/compiler/oat_writer.h
                        // https://android.googlesource.com/platform/art/+/oreo-release/compiler/oat_writer.h
                        offset
                    }

                    // https://android.googlesource.com/platform/art/+/oreo-mr1-release/compiler/oat_writer.h
                    // https://android.googlesource.com/platform/art/+/pie-release/dex2oat/linker/oat_writer.h
                    // https://android.googlesource.com/platform/art/+/android10-release/dex2oat/linker/oat_writer.h
                    // https://android.googlesource.com/platform/art/+/android11-release/dex2oat/linker/oat_writer.h

                    else -> {
                        oatDexFilesOffset
                    }
                }

                // Read name
                val nameLength = it.read(baseOffset, 4).toInt()
                val checksumOffset = baseOffset + 4 + nameLength

                // TODO: Better error handling
                if (checksumOffset > fileSize) {
                    throw Exception("Offset outside of file. Please report it.")
                }

                // Update checksum
                it.seek(checksumOffset.toLong())
                it.write(checksums.entries.elementAt(i).value)

                when (versionString) {
                    Const.OAT_VERSION_KITKAT,
                    Const.OAT_VERSION_KITKAT_MR2,
                    Const.OAT_VERSION_LOLLIPOP,
                    Const.OAT_VERSION_LOLLIPOP_MR1,
                    Const.OAT_VERSION_MARSHMALLOW -> {
                        // Get dex file offset
                        it.seek(checksumOffset.toLong() + 4)
                        val dexFileOffset = it.readIntLittleEndian()

                        // Read header data
                        // 4096 = oat header offset
                        //   96 = class_defs_size offset
                        it.seek(dexFileOffset.toLong() + 4096 + 96)
                        offset = checksumOffset + 8 + (it.readIntLittleEndian() * 4)
                    }
                    else -> {
                        // https://android.googlesource.com/platform/art/+/nougat-release/compiler/oat_writer.cc#242
                        offset = checksumOffset + 4 * 4
                    }
                }
            }
        }
    }

    private val versionString: String
        get() {
            return String(oatVersion).substring(0, 3)
        }
}
