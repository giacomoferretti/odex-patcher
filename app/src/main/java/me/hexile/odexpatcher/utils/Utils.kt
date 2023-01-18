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

import android.os.Build
import androidx.lifecycle.MutableLiveData
import me.hexile.odexpatcher.ktx.toByteArray
import java.io.File
import java.util.zip.ZipFile

fun isSdkGreaterThan(version: Int): Boolean {
    return Build.VERSION.SDK_INT >= version
}

fun isSdkEqual(version: Int): Boolean {
    return Build.VERSION.SDK_INT == version
}

operator fun <T> MutableLiveData<ArrayList<T>>.plusAssign(values: List<T>) {
    val value = this.value ?: arrayListOf()
    value.addAll(values)
    this.value = value
}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

fun extractChecksums(file: File): HashMap<String, ByteArray> {
    return extractClassesDex(file.absolutePath)
}

fun extractClassesDex(path: String): HashMap<String, ByteArray> {
    val checksums = hashMapOf<String, ByteArray>()
    ZipFile(path).use { zip ->
        zip
            .entries()
            .asSequence()
            .forEach {
                if (it.name.startsWith("classes") && it.name.endsWith(".dex")) {
                    // Add classes.dex as classes1.dex for ordering purpose while patching.
                    val dexName = if (it.name == "classes.dex") "classes1.dex" else it.name
                    checksums[dexName] = it.crc.toInt().toByteArray()
                }
            }
    }
    return checksums
}