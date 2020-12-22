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

    // https://cs.android.com/android/platform/superproject/+/master:bionic/libc/include/android/api-level.h

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
        logd("OP_OatPatching", "             file: ${file.absolutePath}")
        logd("OP_OatPatching", "         fileSize: $fileSize")

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
            logd("OP_OatPatching", "       oatVersion: $versionString")
            logd("OP_OatPatching", "     dexFileCount: $dexFileCount")
            logd(
                "OP_OatPatching",
                "oatDexFilesOffset: $oatDexFilesOffset [${Integer.toHexString(oatDexFilesOffset)}]"
            )

            offset = when (versionString) {
                // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4.2_r1/runtime/oat.h#84
                // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4.3_r1/runtime/oat.h#84
                Const.OAT_VERSION_KITKAT,
                Const.OAT_VERSION_KITKAT_MR2 -> {
                    Const.OAT_OFFSET + 16 * 4
                }

                // https://android.googlesource.com/platform/art/+/refs/tags/android-5.0.0_r1/runtime/oat.h#117
                // https://android.googlesource.com/platform/art/+/refs/tags/android-5.1.0_r1/runtime/oat.h#119
                Const.OAT_VERSION_LOLLIPOP,
                Const.OAT_VERSION_LOLLIPOP_MR1 -> {
                    Const.OAT_OFFSET + 21 * 4
                }

                // https://android.googlesource.com/platform/art/+/refs/tags/android-6.0.0_r1/runtime/oat.h#121
                // https://android.googlesource.com/platform/art/+/refs/tags/android-7.0.0_r1/runtime/oat.h#131
                // https://android.googlesource.com/platform/art/+/refs/tags/android-7.1.1_r1/runtime/oat.h#131
                // https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/runtime/oat.h#131
                Const.OAT_VERSION_MARSHMALLOW,
                Const.OAT_VERSION_NOUGAT,
                Const.OAT_VERSION_NOUGAT_MR1,
                Const.OAT_VERSION_OREO -> {
                    Const.OAT_OFFSET + 18 * 4
                }

                // https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/runtime/oat.h#134
                // https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/runtime/oat.h#135
                Const.OAT_VERSION_OREO_MR1,
                Const.OAT_VERSION_PIE -> {
                    Const.OAT_OFFSET + 19 * 4
                }

                // https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/runtime/oat.h#116
                Const.OAT_VERSION_ANDROID_10 -> {
                    Const.OAT_OFFSET + 14 * 4
                }

                // https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/runtime/oat.h#119
                Const.OAT_VERSION_ANDROID_11 -> {
                    Const.OAT_OFFSET + 15 * 4
                }

                else -> {
                    throw Exception(String.format("Unknown oat version %s", versionString))
                }
            }

            offset += it.read(offset - 4, 4).toInt()
            logd("OP_OatPatching", "           offset: $offset [${Integer.toHexString(offset)}]")
        }
    }

    fun patch(checksums: Map<String, ByteArray>) {
        RandomAccessFile(file, "rw").use {
            var cursor = when (versionString) {
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

                else -> {
                    // https://android.googlesource.com/platform/art/+/oreo-mr1-release/compiler/oat_writer.h
                    // https://android.googlesource.com/platform/art/+/pie-release/dex2oat/linker/oat_writer.h
                    // https://android.googlesource.com/platform/art/+/android10-release/dex2oat/linker/oat_writer.h
                    // https://android.googlesource.com/platform/art/+/android11-release/dex2oat/linker/oat_writer.h
                    oatDexFilesOffset + Const.OAT_OFFSET
                }
            }
            logd("OP_OatPatching", "           cursor: $cursor [${Integer.toHexString(cursor)}]")

            for (i in 0 until dexFileCount) {
                // Read name
                val nameLength = it.read(cursor, 4).toInt()
                val checksumOffset = cursor + 4 + nameLength
                logd("OP_OatPatching", "       nameLength: $nameLength")
                logd(
                    "OP_OatPatching",
                    "   checksumOffset: $checksumOffset [${Integer.toHexString(checksumOffset)}]"
                )

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
                        cursor = checksumOffset + 8 + (it.readIntLittleEndian() * 4)
                    }
                    Const.OAT_VERSION_NOUGAT,
                    Const.OAT_VERSION_NOUGAT_MR1,
                    Const.OAT_VERSION_OREO -> {
                        // https://android.googlesource.com/platform/art/+/nougat-release/compiler/oat_writer.cc#242
                        // https://android.googlesource.com/platform/art/+/nougat-mr1-release/compiler/oat_writer.cc#242
                        // https://android.googlesource.com/platform/art/+/oreo-release/compiler/oat_writer.cc#254
                        cursor = checksumOffset + 4 * 4
                    }
                    Const.OAT_VERSION_OREO_MR1 -> {
                        // https://android.googlesource.com/platform/art/+/oreo-mr1-release/compiler/oat_writer.cc#287
                        cursor = checksumOffset + 6 * 4
                    }
                    else -> {
                        // https://android.googlesource.com/platform/art/+/pie-release/dex2oat/linker/oat_writer.cc#315
                        // https://android.googlesource.com/platform/art/+/android10-release/dex2oat/linker/oat_writer.cc#325
                        // https://android.googlesource.com/platform/art/+/android11-release/dex2oat/linker/oat_writer.cc#326
                        cursor = checksumOffset + 8 * 4
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
