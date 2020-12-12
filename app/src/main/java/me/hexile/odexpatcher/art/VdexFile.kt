package me.hexile.odexpatcher.art

import me.hexile.odexpatcher.utils.toInt
import java.io.File
import java.io.RandomAccessFile

class VdexFile(private val file: File) {

    companion object {
        val VDEX_HEADER = "vdex".toByteArray()
        const val VDEX_READ_BYTES = 64
    }

    private val data = ByteArray(VDEX_READ_BYTES)

    var vdexVerifierDepsVersion: ByteArray
    var vdexDexSectionVersion: ByteArray
    var vdexNumberOfDexFiles: ByteArray

    private var offset = 0

    init {
        // TODO: Instead of reading data into a ByteArray,
        //  read only necessary data usingRandomAccessFile
        // Read data
        file.inputStream().use {
            it.read(data)
        }

        // Check correct header
        if (!data.copyOfRange(0, 4).contentEquals(VDEX_HEADER)) {
            throw Exception("VDEX doesn't contain correct magic header.")
        }

        vdexVerifierDepsVersion = data.copyOfRange(4, 8)
        vdexDexSectionVersion = data.copyOfRange(8, 12)
        vdexNumberOfDexFiles = data.copyOfRange(12, 16)

        offset = when (getVersionString()) {
            // Android 8.0.0 - 8.1.0
            // https://cs.android.com/android/platform/superproject/+/android-8.0.0_r1:art/runtime/vdex_file.h;l=71
            // https://cs.android.com/android/platform/superproject/+/android-8.1.0_r1:art/runtime/vdex_file.h;l=78
            "006", "010" -> { 24 }

            // Android 9.0.0
            // https://cs.android.com/android/platform/superproject/+/android-9.0.0_r1:art/runtime/vdex_file.h;l=107
            "019" -> { 20 }

            // Android 10 - Android 11
            // https://cs.android.com/android/platform/superproject/+/android-10.0.0_r30:art/runtime/vdex_file.h;l=129
            // https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:art/runtime/vdex_file.h;l=129
            "021" -> { 28 }

            else -> {
                throw Exception(String.format("Unknown vdex version %s", getVersionString()))
            }
        }
    }

    fun patch(checksums: Map<String, ByteArray>) {
        for (i in 0 until vdexNumberOfDexFiles.toInt()) {
            val tmpOffset = offset + (i * 4)

            RandomAccessFile(file, "rw").use {
                it.seek(tmpOffset.toLong())
                it.write(checksums.entries.elementAt(i).value)
            }
        }
    }

    fun getVersionString(): String {
        return String(vdexVerifierDepsVersion).substring(0, 3)
    }

    fun toByteArray(): ByteArray {
        return data
    }
}
