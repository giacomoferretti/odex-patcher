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

package me.hexile.odexpatcher.utils

import androidx.annotation.IntRange
import me.hexile.odexpatcher.core.App
import me.hexile.odexpatcher.core.SELinux
import me.hexile.odexpatcher.ktx.getFileInFilesDir

class Log {
    private fun getRandomString(@IntRange(from = 1) length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    init {
        val testFile = App.getContext().getFileInFilesDir(".removeme_${getRandomString(20)}")
        logd("OP_testFile", "   absolutePath: ${testFile.absolutePath}")
        logd("OP_testFile", "  createNewFile: ${testFile.createNewFile()}")
        logd("OP_testFile", "     canExecute: ${testFile.canExecute()}")
        logd("OP_testFile", "        canRead: ${testFile.canRead()}")
        logd("OP_testFile", "       canWrite: ${testFile.canWrite()}")
        logd("OP_testFile", "SELinux context: ${SELinux.getFileContext(testFile.absolutePath)}")
        logd("OP_testFile", "    SELinux app: ${SELinux.getContext()}")
        logd("OP_testFile", "         delete: ${testFile.delete()}")

        //val content = assets.open("dummy.dex").readBytes()
        //println(content.toHexString())
    }
}