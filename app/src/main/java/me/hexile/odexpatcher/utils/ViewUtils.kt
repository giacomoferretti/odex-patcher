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

fun View.showSnackbar(message: String, length: Int) {
    showSnackbar(message, length, null) {}
}

fun View.showSnackbar(message: String, length: Int, actionMessage: CharSequence?, action: (View) -> Unit) {
    val snackbar = Snackbar.make(this, message, length)

    actionMessage?.let {
        snackbar.setAction(it) {
            action(this)
        }
    }

    snackbar.show()
}