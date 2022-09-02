package com.giacomoferretti.odexpatcher.example.simple

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val linearLayout = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
        }

        val textView = TextView(this).apply {
            gravity = Gravity.CENTER
            text = BuildConfig.EXAMPLE_TEXT
            setTextSize(TypedValue.COMPLEX_UNIT_SP, BuildConfig.EXAMPLE_TEXT_SIZE)
            setTextColor(BuildConfig.EXAMPLE_TEXT_COLOR)
            setTypeface(typeface, BuildConfig.EXAMPLE_TEXT_STYLE)
        }
        linearLayout.addView(textView)

        val square = View(this).apply {
            setBackgroundColor(Color.parseColor(BuildConfig.EXAMPLE_SQUARE_COLOR))
            layoutParams = LinearLayout.LayoutParams(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    BuildConfig.EXAMPLE_SQUARE_SIZE,
                    resources.displayMetrics
                ).toInt(),
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    BuildConfig.EXAMPLE_SQUARE_SIZE,
                    resources.displayMetrics
                ).toInt()
            ).apply {
                setMargins(
                    0,
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16f,
                        resources.displayMetrics
                    ).toInt(),
                    0,
                    0
                )
            }
        }
        linearLayout.addView(square)

        setContentView(linearLayout)
    }
}