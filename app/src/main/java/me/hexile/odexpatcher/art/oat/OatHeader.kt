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

package me.hexile.odexpatcher.art.oat

import me.hexile.odexpatcher.art.ArtInfo
import me.hexile.odexpatcher.ktx.readBytes
import me.hexile.odexpatcher.ktx.toInt
import java.io.RandomAccessFile

sealed class OatHeader(private val raf: RandomAccessFile) : ArtInfo {
    abstract fun nextChecksum(pos: Long): Long

    override fun parseChecksum(pos: Long, checksums: ArrayList<Pair<Long, ByteArray>>): Long {
        var cursor = pos

        // Read name
        cursor += 4 + raf.readBytes(cursor, 4).toInt()

        // Read checksum
        checksums.add(Pair(cursor, raf.readBytes(cursor, 4)))

        return nextChecksum(cursor)
    }

    /*protected fun calculateHeaderSize(): Long {
        return headerFields * 4 + raf.readBytes(Const.HEADER_OFFSET + (headerFields - 1) * 4, 4).toInt().toLong()
    }*/

    override fun toString(): String {
        return "OatHeader(version='$version', dexFileCount=$dexFileCount, dexChecksumOffset=$dexChecksumOffset, headerSize=$headerSize)"
    }

    object Const {
        val MAGIC = "oat\n".toByteArray()
        const val HEADER_OFFSET = 4096
        const val ANDROID_4_4 = "007"
        const val ANDROID_4_4_3 = "008"
        const val ANDROID_5 = "039"
        const val ANDROID_5_1 = "045"
        const val ANDROID_6 = "064"
        const val ANDROID_7 = "079"
        const val ANDROID_7_1_1 = "088"
        const val ANDROID_8 = "124"
        const val ANDROID_8_1 = "131"
        const val ANDROID_9 = "138"
        const val ANDROID_10 = "170"
        const val ANDROID_11 = "183"
        const val ANDROID_12 = "195"
        const val ANDROID_DEV = "199"
    }

    object Parser {
        fun parse(raf: RandomAccessFile): OatHeader {
            String(raf.readBytes(Const.HEADER_OFFSET + 4, 4)).substring(0, 3).let {
                return when (it) {
                    Const.ANDROID_4_4 -> {
                        OatHeader007(raf)
                    }
                    Const.ANDROID_4_4_3 -> {
                        OatHeader008(raf)
                    }
                    Const.ANDROID_5 -> {
                        OatHeader039(raf)
                    }
                    Const.ANDROID_5_1 -> {
                        OatHeader045(raf)
                    }
                    Const.ANDROID_6 -> {
                        OatHeader064(raf)
                    }
                    Const.ANDROID_7 -> {
                        OatHeader079(raf)
                    }
                    Const.ANDROID_7_1_1 -> {
                        OatHeader088(raf)
                    }
                    Const.ANDROID_8 -> {
                        OatHeader124(raf)
                    }
                    Const.ANDROID_8_1 -> {
                        OatHeader131(raf)
                    }
                    Const.ANDROID_9 -> {
                        OatHeader138(raf)
                    }
                    Const.ANDROID_10 -> {
                        OatHeader170(raf)
                    }
                    Const.ANDROID_11 -> {
                        OatHeader183(raf)
                    }
                    Const.ANDROID_12 -> {
                        OatHeader195(raf)
                    }
                    Const.ANDROID_DEV -> {
                        OatHeader199(raf)
                    }
                    else -> {
                        throw Exception("Cannot parse OAT version $it")
                    }
                }
            }
        }
    }
}

// KITKAT (4.4 - 4.4.2)
open class OatHeader007(private val raf: RandomAccessFile) : OatHeader(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4_r1/runtime/oat.cc#25
    override val version: String = Const.ANDROID_4_4

    override val dexChecksumOffset: Long
        get() = Const.HEADER_OFFSET + headerSize
    override val headerSize: Long
        get() = mHeaderSize
    override val dexFileCount: Int
        get() = mDexFileCount
    // override val headerFields: Int = 16

    // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4_r1/runtime/oat.h#84
    // Missing instruction_set_features_
    // Missing oat_dex_files_offset_
    private val mHeaderSize: Long =
        16 * 4 + raf.readBytes(Const.HEADER_OFFSET + (16 - 1) * 4, 4).toInt().toLong()
    private val mDexFileCount: Int = raf.readBytes(Const.HEADER_OFFSET + 4 * 4, 4).toInt()

    override fun nextChecksum(pos: Long): Long {
        // Get dex file offset
        raf.seek(pos + 4)
        val dexFileOffset = raf.readBytes(4).toInt()

        // Read dex header data
        // 4096 = oat header offset
        //   96 = class_defs_size offset
        raf.seek(dexFileOffset.toLong() + Const.HEADER_OFFSET + 96)
        return pos + 8 + (raf.readBytes(4).toInt() * 4)
    }
}

// KITKAT (4.4.3 - 4.4.4)
open class OatHeader008(raf: RandomAccessFile) : OatHeader007(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4.3_r1/runtime/oat.cc#25
    override val version: String = Const.ANDROID_4_4_3

    // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4.3_r1/runtime/oat.h#84
    // Missing instruction_set_features_
    // Missing oat_dex_files_offset_
}

// LOLLIPOP (5.0 - 5.0.2)
open class OatHeader039(raf: RandomAccessFile) : OatHeader008(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-5.0.0_r1/runtime/oat.cc#26
    override val version: String = Const.ANDROID_5
    override val dexChecksumOffset: Long
        get() = Const.HEADER_OFFSET + mHeaderSize
    override val dexFileCount: Int
        get() = mDexFileCount
    override val headerSize: Long
        get() = mHeaderSize
    // override val headerFields: Int = 21

    // https://android.googlesource.com/platform/art/+/refs/tags/android-5.0.0_r1/runtime/oat.h#117
    // Missing oat_dex_files_offset_
    private val mHeaderSize: Long =
        21 * 4 + raf.readBytes(Const.HEADER_OFFSET + (21 - 1) * 4, 4).toInt().toLong()
    private val mDexFileCount: Int = raf.readBytes(Const.HEADER_OFFSET + 5 * 4, 4).toInt()
}

// LOLLIPOP MR1 (5.1 - 5.1.1)
open class OatHeader045(raf: RandomAccessFile) : OatHeader039(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-5.1.0_r1/runtime/oat.cc#26
    override val version: String = Const.ANDROID_5_1

    // https://android.googlesource.com/platform/art/+/refs/tags/android-5.1.0_r1/runtime/oat.h#119
    // Missing oat_dex_files_offset_
}

// MARSHMALLOW (6.0 - 6.0.1)
open class OatHeader064(raf: RandomAccessFile) : OatHeader045(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-6.0.0_r1/runtime/oat.h#35
    override val version: String = Const.ANDROID_6
    override val dexChecksumOffset: Long
        get() = Const.HEADER_OFFSET + mHeaderSize
    override val headerSize: Long
        get() = mHeaderSize
    // override val headerFields: Int = 18

    // https://android.googlesource.com/platform/art/+/refs/tags/android-6.0.0_r1/runtime/oat.h#121
    // Missing oat_dex_files_offset_
    private val mHeaderSize: Long =
        18 * 4 + raf.readBytes(Const.HEADER_OFFSET + (18 - 1) * 4, 4).toInt().toLong()
}

// NOUGAT (7.0 - 7.1)
open class OatHeader079(raf: RandomAccessFile) : OatHeader064(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-7.0.0_r1/runtime/oat.h#35
    override val version: String = Const.ANDROID_7

    // https://android.googlesource.com/platform/art/+/refs/tags/android-7.0.0_r1/runtime/oat.h#131
    // Missing oat_dex_files_offset_

    // https://android.googlesource.com/platform/art/+/refs/tags/android-7.0.0_r1/compiler/oat_writer.cc#242
    override fun nextChecksum(pos: Long): Long = pos + 4 * 4
}

// NOUGAT MR1 (7.1.1 - 7.1.2)
open class OatHeader088(raf: RandomAccessFile) : OatHeader079(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-7.1.1_r1/runtime/oat.h#35
    override val version: String = Const.ANDROID_7_1_1

    // https://android.googlesource.com/platform/art/+/refs/tags/android-7.1.1_r1/runtime/oat.h#131
    // Missing oat_dex_files_offset_

    // https://android.googlesource.com/platform/art/+/refs/tags/android-7.1.1_r1/compiler/oat_writer.cc#242
}

// OREO (8.0)
open class OatHeader124(raf: RandomAccessFile) : OatHeader088(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/runtime/oat.h#35
    override val version: String = Const.ANDROID_8

    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/runtime/oat.h#131
    // Missing oat_dex_files_offset_

    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/compiler/oat_writer.cc#254
}

// OREO MR1 (8.1)
open class OatHeader131(raf: RandomAccessFile) : OatHeader124(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/runtime/oat.h#36
    override val version: String = Const.ANDROID_8_1
    override val dexChecksumOffset: Long
        get() = mDexChecksumOffset
    override val headerSize: Long
        get() = mHeaderSize
    // override val headerFields: Int = 19

    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/runtime/oat.h#134
    private val mHeaderSize: Long =
        19 * 4 + raf.readBytes(Const.HEADER_OFFSET + (19 - 1) * 4, 4).toInt().toLong()
    private val mDexChecksumOffset: Long =
        Const.HEADER_OFFSET + raf.readBytes(Const.HEADER_OFFSET + 6 * 4, 4).toInt().toLong()

    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/compiler/oat_writer.cc#287
    override fun nextChecksum(pos: Long): Long = pos + 6 * 4
}

// PIE (9.0)
open class OatHeader138(raf: RandomAccessFile) : OatHeader131(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/runtime/oat.h#36
    override val version: String = Const.ANDROID_9

    // https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/runtime/oat.h#135

    // https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/dex2oat/linker/oat_writer.cc#315
    override fun nextChecksum(pos: Long): Long = pos + 8 * 4
}

// Q (10)
open class OatHeader170(raf: RandomAccessFile) : OatHeader138(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/runtime/oat.h#36
    override val version: String = Const.ANDROID_10

    // https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/runtime/oat.h#116
    override val headerSize: Long
        get() = mHeaderSize
    // override val headerFields: Int = 14

    private val mHeaderSize: Long =
        14 * 4 + raf.readBytes(Const.HEADER_OFFSET + (14 - 1) * 4, 4).toInt().toLong()

    // https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/dex2oat/linker/oat_writer.cc#325
}

// R (11)
open class OatHeader183(raf: RandomAccessFile) : OatHeader170(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/runtime/oat.h#36
    override val version: String = Const.ANDROID_11

    // https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/runtime/oat.h#119
    override val headerSize: Long
        get() = mHeaderSize
    // override val headerFields: Int = 15

    private val mHeaderSize: Long =
        15 * 4 + raf.readBytes(Const.HEADER_OFFSET + (15 - 1) * 4, 4).toInt().toLong()

    // https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/dex2oat/linker/oat_writer.cc#326
}

// S (12)
open class OatHeader195(raf: RandomAccessFile) : OatHeader183(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-12.0.0_r1/runtime/oat.h#36
    override val version: String = Const.ANDROID_12

    // https://android.googlesource.com/platform/art/+/refs/tags/android-12.0.0_r1/runtime/oat.h#125
    override val headerSize: Long
        get() = mHeaderSize
    // override val headerFields: Int = 16

    private val mHeaderSize: Long =
        16 * 4 + raf.readBytes(Const.HEADER_OFFSET + (16 - 1) * 4, 4).toInt().toLong()

    // https://android.googlesource.com/platform/art/+/refs/tags/android-12.0.0_r1/dex2oat/linker/oat_writer.cc#326
    override fun nextChecksum(pos: Long): Long = pos + 10 * 4
}

// Sv2 (???)
class OatHeader199(raf: RandomAccessFile) : OatHeader195(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-s-v2-preview-1/runtime/oat.h#36
    override val version: String = Const.ANDROID_DEV
    // https://android.googlesource.com/platform/art/+/refs/tags/android-s-v2-preview-1/runtime/oat.h#125
}