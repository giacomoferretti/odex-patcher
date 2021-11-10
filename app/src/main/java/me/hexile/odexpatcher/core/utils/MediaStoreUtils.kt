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

package me.hexile.odexpatcher.core.utils

import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import me.hexile.odexpatcher.core.App
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

object MediaStoreUtils {

    private val cr get() = App.getContext().contentResolver

    @get:RequiresApi(Build.VERSION_CODES.Q)
    private val tableUri
        get() = MediaStore.Downloads.EXTERNAL_CONTENT_URI

    fun Uri.inputStream() = cr.openInputStream(this)
        ?: throw FileNotFoundException("Resource does not exist: $this")

    fun Uri.outputStream() = cr.openOutputStream(this, "rwt")
        ?: throw FileNotFoundException("Resource does not exist: $this")

    private fun insertFile(displayName: String): MediaStoreFile {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        }

        val fileUri = cr.insert(tableUri, contentValues)
            ?: throw IOException("Failed to insert $displayName.")


        cr.query(
            fileUri,
            arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA),
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(idIndex)
                val data = cursor.getString(dataColumn)
                return MediaStoreFile(id, data)
            }
        }

        throw IOException("Failed to insert $displayName.")
    }

    fun getFile(displayName: String): UriFile {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            LegacyUriFile(
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    displayName
                )
            )
        } else {
            insertFile(displayName)
        }

    }

    interface UriFile {
        val uri: Uri
        fun delete(): Boolean
    }

    private class LegacyUriFile(private val file: File) : UriFile {
        override val uri = file.toUri()
        override fun delete() = file.delete()
        override fun toString() = file.toString()
    }

    private class MediaStoreFile(private val id: Long, private val data: String) : UriFile {
        override val uri = ContentUris.withAppendedId(tableUri, id)
        override fun toString() = data
        override fun delete(): Boolean {
            val selection = "${MediaStore.MediaColumns._ID} == ?"
            val selectionArgs = arrayOf(id.toString())
            return cr.delete(uri, selection, selectionArgs) == 1
        }
    }
}