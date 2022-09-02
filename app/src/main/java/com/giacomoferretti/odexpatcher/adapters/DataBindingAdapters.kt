package com.giacomoferretti.odexpatcher.adapters

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.isGone
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter(value = ["imageUri", "placeholderUri"])
fun bindImageFromUri(view: ImageView, uri: Uri?, @DrawableRes placeholderImage: Int) {
    Log.d("BINDING", uri.toString())
    if (uri != null && uri != Uri.EMPTY) {
//        view.load(uri) {
//            placeholder(placeholderI)
//            crossfade(true)
//        }
        Glide
            .with(view.context)
            .load(uri)
            .placeholder(placeholderImage)
            .into(view)
    }
}

@BindingAdapter("gone")
fun setGone(view: View, gone: Boolean) {
    view.isGone = gone
}