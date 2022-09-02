package com.giacomoferretti.odexpatcher.library.ktx

internal fun String.filename(): String {
    return this.substring(this.lastIndexOf("/") + 1)
}