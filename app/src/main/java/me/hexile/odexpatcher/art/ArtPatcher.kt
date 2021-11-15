package me.hexile.odexpatcher.art

import me.hexile.odexpatcher.ktx.readBytes
import me.hexile.odexpatcher.ktx.toHexString
import java.io.File
import java.io.RandomAccessFile

interface ArtInfo {
    val version: String
    val dexFileCount: Int
    val dexChecksumOffset: Long
    val headerSize: Long

    //val headerFields: Int
    fun parseChecksum(pos: Long, checksums: ArrayList<Pair<Long, ByteArray>>): Long
    //fun getHeaderSize(): Long
}

abstract class ArtPatcher(private val file: File) {
    protected abstract fun checkIfValid(raf: RandomAccessFile)
    protected abstract fun parseHeader(raf: RandomAccessFile): ArtInfo

    var header: ArtInfo

    private val _checksums: ArrayList<Pair<Long, ByteArray>>
    val checksums: List<Pair<Long, ByteArray>>
        get() = _checksums.toList()

    init {
        RandomAccessFile(file, "r").use { raf ->
            checkIfValid(raf)
            header = parseHeader(raf)
            _checksums = parseChecksums()
        }
    }

    fun patch(checksums: ArrayList<ByteArray>) {
        if (checksums.isEmpty()) {
            throw IllegalArgumentException("Cannot be empty")
        } else if (checksums.size != this._checksums.size) {
            throw IllegalArgumentException("Must be same size ${checksums.size} != ${this._checksums.size}")
        }

        RandomAccessFile(file, "rw").use { raf ->
            this._checksums.asSequence().withIndex().iterator().forEach {
                println("PATCHING ${it.index}")

                val source = raf.readBytes(it.value.first, 4)
                val sourceCache = it.value.second
                val new = checksums[it.index]

                println(" -> ${source.toHexString()} = ${new.toHexString()} [${sourceCache.toHexString()}]")

                raf.seek(it.value.first)
                raf.write(checksums[it.index]) // java.lang.IndexOutOfBoundsException: Invalid index 0, size is 0
            }
        }
    }

    private fun parseChecksums(): ArrayList<Pair<Long, ByteArray>> {
        val checksums = ArrayList<Pair<Long, ByteArray>>()

        var cursor = header.dexChecksumOffset
        for (i in 0 until header.dexFileCount) {
            cursor = header.parseChecksum(cursor, checksums)
        }

        return checksums
    }

    protected fun toStringContents(): String {
        val checksumsString = checksums.iterator().let {
            if (!it.hasNext()) {
                "{}"
            } else {
                var result = "{"
                while (true) {
                    val entry = it.next()
                    result += "${entry.first}=${entry.second.toHexString()}"
                    if (!it.hasNext()) break
                    result += ", "
                }
                "$result}"
            }
        }

        return "file=$file, header=$header, checksums=$checksumsString)"
    }

    override fun toString(): String {
        return "ArtPatcher(${toStringContents()})"
    }
}