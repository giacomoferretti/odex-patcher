package me.hexile.odexpatcher.utils

import android.os.Build
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.util.*
import java.util.zip.ZipFile

fun isSdkGreaterThan(version: Int): Boolean {
    return Build.VERSION.SDK_INT >= version
}

operator fun <T> MutableLiveData<ArrayList<T>>.plusAssign(values: List<T>) {
    val value = this.value ?: arrayListOf()
    value.addAll(values)
    this.value = value
}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

fun extractClassesDex(file: File): Map<String, ByteArray> {
    return extractClassesDex(file.absolutePath)
}

fun extractClassesDex(path: String): Map<String, ByteArray> {
    val classesDex = LinkedHashMap<String, ByteArray>()
    ZipFile(path).use { zip ->
        zip
            .entries()
            .asSequence()
            .forEach {
                if (it.name.startsWith("classes") && it.name.endsWith(".dex")) {
                    classesDex[it.name] = it.crc.toInt().toByteArray()
                }
            }
    }
    return classesDex
}