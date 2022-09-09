package com.giacomoferretti.odexpatcher.adapters

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.isGone
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageUri", "placeholderUri")
fun bindImageFromUri(view: ImageView, uri: Uri?, @DrawableRes placeholderImage: Int) {
    // TODO: This gets called multiple times whenever the state changes
    if (uri != null && uri != Uri.EMPTY) {
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