package me.hexile.odexpatcher.ktx

fun Pair<Long, ByteArray>.toHexString(): String {
    return "(${this.first}, ${this.second.toHexString()})"
}

@JvmName("pairLongByteArrayToHexString")
fun List<Pair<Long, ByteArray>>.toHexString(): String {
    return this.iterator().let {
        if (!it.hasNext()) {
            "[]"
        } else {
            var result = "["
            while (true) {
                val entry = it.next()
                result += entry.toHexString()
                if (!it.hasNext()) break
                result += ", "
            }
            "$result]"
        }
    }
}

@JvmName("byteArrayToHexString")
fun List<ByteArray>.toHexString(): String {
    return this.iterator().let {
        if (!it.hasNext()) {
            "[]"
        } else {
            var result = "["
            while (true) {
                val entry = it.next()
                result += entry.toHexString()
                if (!it.hasNext()) break
                result += ", "
            }
            "$result]"
        }
    }
}

fun Map<Long, ByteArray>.toHexString(): String {
    return this.iterator().let {
        if (!it.hasNext()) {
            "{}"
        } else {
            var result = "{"
            while (true) {
                val entry = it.next()
                result += "${entry.key}=${entry.value.toHexString()}"
                if (!it.hasNext()) break
                result += ", "
            }
            "$result}"
        }
    }
}