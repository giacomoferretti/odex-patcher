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

import me.hexile.odexpatcher.art.ArtException
import me.hexile.odexpatcher.art.ArtPatcher
import me.hexile.odexpatcher.utils.readBytes
import me.hexile.odexpatcher.utils.seekStart
import java.io.File
import java.io.RandomAccessFile

class VdexFile(private val file: File) : ArtPatcher(file) {
    override fun checkIfValid(raf: RandomAccessFile) {
        // Check VDEX magic
        raf.readBytes(4).let {
            if (!it.contentEquals(VdexHeader.Const.MAGIC)) {
                throw ArtException(
                    "VDEX doesn't contain correct magic header.",
                    VdexHeader.Const.MAGIC,
                    it
                )
            }
        }

        // Go back to start
        raf.seekStart()
    }

    override fun parseHeader(raf: RandomAccessFile): VdexHeader {
        return VdexHeader.Parser.parse(raf)
    }

    override fun toString(): String {
        return "VdexFile(${toStringContents()})"
    }
}