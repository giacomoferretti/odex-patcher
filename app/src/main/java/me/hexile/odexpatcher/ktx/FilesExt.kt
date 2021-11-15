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
import com.topjohnwu.superuser.Shell
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

fun Context.getPackageApk(packageName: String): File {
    return File(this.packageManager.getPackageInfo(packageName, 0).applicationInfo.sourceDir)
}

fun Context.getFileInFilesDir(filename: String): File {
    return File(this.filesDir.absolutePath, filename)
}

fun Context.getFileInCacheDir(filename: String): File {
    return File(this.cacheDir.absolutePath, filename)
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

fun chown(path: String, uid: Int, gid: Int, recursive: Boolean = false): Boolean {
    return Shell.sh("chown ${if (recursive) "-R" else ""} $uid:$gid $path").exec().isSuccess
}

fun chmod(path: String, chmod: String, recursive: Boolean = false): Boolean {
    return Shell.sh("chmod ${if (recursive) "-R" else ""} $chmod $path").exec().isSuccess
}

fun restorecon(path: String, recursive: Boolean = false): Boolean {
    return Shell.sh("restorecon ${if (recursive) "-R" else ""} $path").exec().isSuccess
}

fun fixCacheFolderPermission(fileInCacheDir: File, recursive: Boolean = false): Boolean {
    if (!chown(fileInCacheDir.absolutePath, selfAppUid(), cacheAppGid(selfAppUid()), recursive)) {
        return false
    }

    if (!chmod(fileInCacheDir.absolutePath, "600", recursive)) {
        return false
    }

    if (!restorecon(fileInCacheDir.absolutePath, recursive)) {
        return false
    }

    return true
}