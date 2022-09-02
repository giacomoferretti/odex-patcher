package com.giacomoferretti.odexpatcher.library

import java.util.zip.ZipFile

object Utils {
    fun countClassesDex(path: String): Int {
        var count = 0

        ZipFile(path).use { zip ->
            zip
                .entries()
                .asSequence()
                .forEach {
                    if (it.name.startsWith("classes") && it.name.endsWith(".dex")) {
                        count++
                    }
                }
        }

        return count
    }
}