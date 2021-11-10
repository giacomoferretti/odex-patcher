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

package me.hexile.odexpatcher.art.vdex

import me.hexile.odexpatcher.art.ArtInfo
import me.hexile.odexpatcher.ktx.readBytes
import me.hexile.odexpatcher.ktx.toInt
import java.io.RandomAccessFile

/*enum class VdexSection(val value: Int) {
    CHECKSUM(0),
    DEX_FILE(1),
    VERIFIER_DEPS(2),
    TYPE_LOOKUP_TABLE(3),
    NUMBER_OF_SECTIONS(4);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}*/

sealed class VdexHeader(private val raf: RandomAccessFile) : ArtInfo {
    override fun parseChecksum(pos: Long, checksums: ArrayList<Pair<Long, ByteArray>>): Long {
        checksums.add(Pair(pos, raf.readBytes(pos, 4)))
        return pos + 4
    }

    override fun toString(): String {
        return "VdexHeader(version='$version', dexFileCount=$dexFileCount, dexChecksumOffset=$dexChecksumOffset)" //, headerSize=$headerSize)"
    }

    object Const {
        val MAGIC = "vdex".toByteArray()
        const val ANDROID_8 = "006"
        const val ANDROID_8_1 = "010"
        const val ANDROID_9 = "019"
        const val ANDROID_10 = "021"
        const val ANDROID_11 = ANDROID_10
        const val ANDROID_12 = "027"
    }

    object Parser {
        fun parse(raf: RandomAccessFile): VdexHeader {
            String(raf.readBytes(4, 4)).substring(0, 3).let {
                return when (it) {
                    Const.ANDROID_8 -> {
                        VdexHeader006(raf)
                    }
                    Const.ANDROID_8_1 -> {
                        VdexHeader010(raf)
                    }
                    Const.ANDROID_9 -> {
                        VdexHeader019(raf)
                    }
                    Const.ANDROID_10 -> {
                        VdexHeader021(raf)
                    }
                    Const.ANDROID_12 -> {
                        VdexHeader027(raf)
                    }
                    else -> {
                        throw Exception("Cannot parse VDEX version $it")
                    }
                }
            }
        }
    }
}

// OREO (8.0)
open class VdexHeader006(raf: RandomAccessFile) : VdexHeader(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/runtime/vdex_file.h#69
    override val version: String = Const.ANDROID_8

    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/runtime/vdex_file.h#71
    override val dexChecksumOffset: Long = 24 // 6 fields
    override val headerSize: Long
        get() = TODO("Not yet implemented")
    override val dexFileCount: Int
        get() = mDexFileCount


    private val mDexFileCount: Int = raf.readBytes(8, 4).toInt()
}

// OREO MR1 (8.1)
open class VdexHeader010(raf: RandomAccessFile) : VdexHeader006(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/runtime/vdex_file.h#76
    override val version: String = Const.ANDROID_8_1

    // https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/runtime/vdex_file.h#78
}

// PIE (9.0)
open class VdexHeader019(raf: RandomAccessFile) : VdexHeader010(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/runtime/vdex_file.h#96
    override val version = Const.ANDROID_9

    // https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/runtime/vdex_file.h#107
    override val dexChecksumOffset: Long = 20 // 5 fields
    override val dexFileCount: Int
        get() = mDexFileCount

    private val mDexFileCount: Int = raf.readBytes(12, 4).toInt()
}

// Q - R (10 - 11)
open class VdexHeader021(raf: RandomAccessFile) : VdexHeader019(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/runtime/vdex_file.h#118
    // https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/runtime/vdex_file.h#118
    override val version = Const.ANDROID_10

    // https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/runtime/vdex_file.h#129
    // https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/runtime/vdex_file.h#129
    override val dexChecksumOffset: Long = 28 // 7 fields
}

// S (12)
class VdexHeader027(raf: RandomAccessFile) : VdexHeader021(raf) {
    // https://android.googlesource.com/platform/art/+/refs/tags/android-12.0.0_r1/runtime/vdex_file.h#127
    override val version = Const.ANDROID_12
    override val dexFileCount: Int
        get() = mDexFileCount
    override val dexChecksumOffset: Long
        get() = mDexChecksumOffset

    private val mHeaderSize: Int = 12
    private val mDexFileCount: Int
    private val mDexChecksumOffset: Long
    private val mNumberOfSections: Int

    init {
        raf.seek(8)

        mNumberOfSections = raf.readBytes(4).toInt()

        /*for (i in 0 until mNumberOfSections) {
            VdexSection.fromInt(raf.readBytes(4).toInt())
        }*/

        // https://android.googlesource.com/platform/art/+/refs/tags/android-12.0.0_r1/runtime/vdex_file.h#168
        mDexFileCount = raf.readBytes(mHeaderSize + 8, 4).toInt() / 4

        // https://android.googlesource.com/platform/art/+/refs/tags/android-12.0.0_r1/runtime/vdex_file.h#144
        mDexChecksumOffset = mHeaderSize.toLong() + mNumberOfSections.toLong() * 12
    }
}