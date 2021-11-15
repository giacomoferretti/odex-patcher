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

package me.hexile.odexpatcher.ktx

import android.content.Context
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

fun String.filename(): String {
    return this.substring(this.lastIndexOf("/") + 1)
}

fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}

fun Context.getPackageBaseApk(packageName: String): String {
    return this.packageManager.getPackageInfo(packageName, 0).applicationInfo.sourceDir
}

fun Context.getFileInFilesDir(filename: String): File {
    return File(this.filesDir.absolutePath, filename)
}

fun RandomAccessFile.readIntLittleEndian(): Int {
    val data = ByteArray(4)
    this.read(data)
    return data.toInt()
}

fun RandomAccessFile.seekStart() {
    this.seek(0)
}

fun RandomAccessFile.seekInt(pos: Int) {
    this.seek(pos.toLong())
}

fun RandomAccessFile.readBytes(amount: Int): ByteArray {
    val data = ByteArray(amount)
    this.read(data)
    return data
}

fun RandomAccessFile.readBytes(offset: Long, amount: Int): ByteArray {
    val data = ByteArray(amount)
    this.seek(offset)
    this.read(data)
    return data
}

fun RandomAccessFile.readBytes(offset: Int, amount: Int): ByteArray {
    return this.readBytes(offset.toLong(), amount)
}