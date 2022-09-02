package com.giacomoferretti.odexpatcher.library.internal.ktx

@JvmSynthetic
internal fun String.filename(): String {
    return this.substring(this.lastIndexOf("/") + 1)
}