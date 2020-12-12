package me.hexile.odexpatcher.art

import android.os.Build
import me.hexile.odexpatcher.utils.findFirst
import me.hexile.odexpatcher.utils.toInt
import java.io.File
import java.io.RandomAccessFile

class OatFile(private val file: File) {

    companion object {
        val OAT_HEADER = "oat\n".toByteArray()
        const val OAT_READ_BYTES = 4 * 1024 * 1024 // 4MB
    }

    private val data = ByteArray(OAT_READ_BYTES)

    var oatOffset: Int
    var oatVersion: ByteArray
    var dexFileCount: Int

    private var offset = 0

    private var fileSize: Long = 0

    init {
        // TODO: Instead of reading data into a ByteArray,
        //  read only necessary data usingRandomAccessFile
        // Read data
        fileSize = file.length()
        file.inputStream().use {
            it.read(data)
        }

        // Parse data
        oatOffset = data.findFirst(OAT_HEADER)
        oatVersion = data.copyOfRange(oatOffset + 4, oatOffset + 8)
        dexFileCount = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            data.copyOfRange(oatOffset + 16, oatOffset + 20).toInt()
        } else {
            data.copyOfRange(oatOffset + 20, oatOffset + 24).toInt()
        }

        offset = when (getVersionString()) {
            // Android 4.4 - 4.4.2 (007)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4.2_r1/runtime/oat.h#84
            // Android 4.4.3 - 4.4.4 (008)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-4.4.3_r1/runtime/oat.h#84
            "007", "008" -> {
                val headersOffset = oatOffset + 16 * 4
                headersOffset + data.copyOfRange(headersOffset - 4, headersOffset).toInt()
            }

            // Android 5.0.0 - 5.0.2 (039)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-5.0.0_r1/runtime/oat.h#117
            // Android 5.1.0 - 5.1.1 (045)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-5.1.0_r1/runtime/oat.h#119
            "039", "045" -> {
                val headersOffset = oatOffset + 21 * 4
                headersOffset + data.copyOfRange(headersOffset - 4, headersOffset).toInt()
            }

            // Android 6.0.0 - 6.0.1 (064)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-6.0.0_r1/runtime/oat.h#121
            // Android 7.0 - 7.1 (079)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-7.0.0_r1/runtime/oat.h#131
            // Android 7.1.1 - 7.1.2 (088)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-7.1.1_r1/runtime/oat.h#131
            // Android 8.0.0 (124)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/runtime/oat.h#131
            "064", "079", "088", "124" -> {
                val headersOffset = oatOffset + 18 * 4
                headersOffset + data.copyOfRange(headersOffset - 4, headersOffset).toInt()
            }

            // Android 8.1.0 (131)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/runtime/oat.h#134
            // Android 9.0.0 (138)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/runtime/oat.h#135
            "131", "138" -> {
                val headersOffset = oatOffset + 19 * 4
                headersOffset + data.copyOfRange(headersOffset - 4, headersOffset).toInt()
            }

            // Android 10 (170)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/runtime/oat.h#116
            "170" -> {
                val headersOffset = oatOffset + 14 * 4
                headersOffset + data.copyOfRange(headersOffset - 4, headersOffset).toInt()
            }

            // Android 11 (183)
            // https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/runtime/oat.h#119
            "183" -> {
                val headersOffset = oatOffset + 15 * 4
                headersOffset + data.copyOfRange(headersOffset - 4, headersOffset).toInt()
            }

            else -> {
                throw Exception(String.format("Unknown oat version %s", getVersionString()))
            }
        }
    }

    fun patch(checksums: Map<String, ByteArray>) {
        for (i in 0 until dexFileCount) {
            // TODO: Better parsing to find checksums
            val baseOffset = when (getVersionString()) {
                "007", "008", "039", "045", "064" -> {
                    // https://android.googlesource.com/platform/art/+/kitkat-release/compiler/oat_writer.h
                    offset + 4
                }
                else -> {
                    data.findFirst("base.apk".toByteArray(), offset)
                }
            }

            // Read name
            val nameLength = data.copyOfRange(baseOffset - 4, baseOffset).toInt()
            val checksumOffset = baseOffset + nameLength

            // TODO: Better error handling
            if (checksumOffset > fileSize) {
                throw Exception("Offset outside of file. Please report it.")
            }

            RandomAccessFile(file, "rw").use {
                it.seek(checksumOffset.toLong())
                it.write(checksums.entries.elementAt(i).value)
            }

            offset = checksumOffset
        }
    }

    fun getVersionString(): String {
        return String(oatVersion).substring(0, 3)
    }

    fun toByteArray(): ByteArray {
        return data
    }
}
