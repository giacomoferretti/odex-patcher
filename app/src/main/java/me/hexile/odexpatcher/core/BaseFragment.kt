package me.hexile.odexpatcher.core

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    val activity get() = requireActivity() as AppCompatActivity
}