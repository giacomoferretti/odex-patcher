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

import me.hexile.odexpatcher.art.ArtException
import me.hexile.odexpatcher.art.ArtPatcher
import me.hexile.odexpatcher.ktx.readBytes
import me.hexile.odexpatcher.ktx.seekInt
import me.hexile.odexpatcher.ktx.seekStart
import java.io.File
import java.io.RandomAccessFile

class OatFile(file: File) : ArtPatcher(file) {
    object Const {
        val ELF_MAGIC = byteArrayOf(0x7F) + "ELF".toByteArray()
        const val OAT_HEADER_OFFSET = 4096
    }

    override fun checkIfValid(raf: RandomAccessFile) {
        // Check ELF magic
        raf.readBytes(4).let {
            if (!it.contentEquals(Const.ELF_MAGIC)) {
                throw ArtException(
                    "OAT doesn't contain correct magic header.",
                    Const.ELF_MAGIC,
                    it
                )
            }
        }

        // Check OAT magic
        raf.seekInt(Const.OAT_HEADER_OFFSET)
        raf.readBytes(4).let {
            if (!it.contentEquals(OatHeader.Const.MAGIC)) {
                throw ArtException(
                    "OAT doesn't contain correct magic header.",
                    OatHeader.Const.MAGIC,
                    it
                )
            }
        }

        // Go back to start
        raf.seekStart()
    }

    override fun parseHeader(raf: RandomAccessFile): OatHeader {
        return OatHeader.Parser.parse(raf)
    }

    override fun toString(): String {
        return "OatFile(${toStringContents()})"
    }
}
