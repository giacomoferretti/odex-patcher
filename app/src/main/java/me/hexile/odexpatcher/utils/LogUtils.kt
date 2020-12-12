package me.hexile.odexpatcher.utils

import android.util.Log
import me.hexile.odexpatcher.BuildConfig

fun logi(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.i(tag, message)
    }
}

fun loge(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message)
    }
}

fun logd(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, message)
    }
}

fun logw(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.w(tag, message)
    }
}

fun logv(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.v(tag, message)
    }
}

fun logwtf(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.wtf(tag, message)
    }
}