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

import android.annotation.SuppressLint
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("SetTextI18n")
fun TextView.newLine(string: String) {
    this.text = this.text.toString() + "\n$string"
}

suspend fun TextView.newLineOnMainThread(string: String) {
    val textView = this

    withContext(Dispatchers.Main) {
        textView.newLine(string)
    }
}

fun ScrollView.scrollToBottom() {
    val lastChild = getChildAt(childCount - 1)
    val bottom = lastChild.bottom + lastChild.paddingBottom + paddingBottom
    val delta = bottom - (scrollY + height)
    smoothScrollBy(0, delta)
}

fun View.showSnackbar(message: String, length: Int = Snackbar.LENGTH_SHORT) {
    showSnackbar(message, length, null) {}
}

fun View.showSnackbar(
    message: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackbar = Snackbar.make(this, message, length)

    actionMessage?.let {
        snackbar.setAction(it) {
            action(this)
        }
    }

    snackbar.show()
}