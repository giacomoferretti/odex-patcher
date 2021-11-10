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

import android.util.Log
import me.hexile.odexpatcher.BuildConfig

const val TAG = "OdexPatcher"

fun logi(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.i(TAG, "[$tag] $message")
    }
}

fun loge(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(TAG, "[$tag] $message")
    }
}

fun logd(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TAG, "[$tag] $message")
    }
}

fun logw(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.w(TAG, "[$tag] $message")
    }
}

fun logv(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.v(TAG, "[$tag] $message")
    }
}

fun logwtf(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.wtf(TAG, "[$tag] $message")
    }
}