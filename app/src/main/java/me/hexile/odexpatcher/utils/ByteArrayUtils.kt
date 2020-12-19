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

package me.hexile.odexpatcher.utils

import java.util.*

const val HEX_CHARS = "0123456789ABCDEF"

fun ByteArray.toHexString(separator: String = ""): String {
    val result = StringBuilder()

    this.forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }

    return result.toString()
}

fun String.hexToByteArray(): ByteArray {
    val uppercase = this.toUpperCase(Locale.getDefault())
    val len = uppercase.length
    val result = ByteArray(len / 2)
    (0 until len step 2).forEach { i ->
        result[i.shr(1)] = HEX_CHARS.indexOf(uppercase[i]).shl(4).or(HEX_CHARS.indexOf(uppercase[i + 1])).toByte()
    }
    return result
}

fun ByteArray.toInt(littleEndian: Boolean = true): Int {
    if (this.size != 4) {
        throw IllegalArgumentException("ByteArray size must be 4")
    }

    var result = 0
    var shift = if (littleEndian) 0 else 24
    val step = if (littleEndian) 8 else -8
    for (byte in this) {
        result += byte.toInt() and 0xFF shl shift
        shift += step
    }
    return result
}

fun Int.toByteArray(littleEndian: Boolean = true): ByteArray {
    val result = ByteArray(4)

    if (littleEndian) {
        result[0] = (this and 0xFF).toByte()
        result[1] = ((this ushr 8) and 0xFF).toByte()
        result[2] = ((this ushr 16) and 0xFF).toByte()
        result[3] = ((this ushr 24) and 0xFF).toByte()
    } else {
        result[3] = (this and 0xFF).toByte()
        result[2] = ((this ushr 8) and 0xFF).toByte()
        result[1] = ((this ushr 16) and 0xFF).toByte()
        result[0] = ((this ushr 24) and 0xFF).toByte()
    }

    return result
}

fun ByteArray.findFirst(sequence: ByteArray, offset: Int = 0): Int {
    if (sequence.isEmpty()) {
        throw IllegalArgumentException("sequence must be a non-empty byte array")
    }

    if (offset < 0) {
        throw IllegalArgumentException("offset must be a non-negative number")
    }

    var x = offset
    var y = 0
    while (x < this.size) {
        if (this[x] == sequence[y]) {
            y++

            if (y == sequence.size) {
                return x - (sequence.size - 1)
            }
        } else {
            y = 0
        }

        x++
    }

    return -1
}