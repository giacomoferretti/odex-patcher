package me.hexile.odexpatcher.utils

import android.content.Context
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

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