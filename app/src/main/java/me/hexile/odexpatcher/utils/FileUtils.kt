package me.hexile.odexpatcher.utils

import android.content.Context
import java.io.File
import java.io.InputStream

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