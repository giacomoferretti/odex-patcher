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

package me.hexile.odexpatcher.core

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import me.hexile.odexpatcher.ktx.checkSelfPermissionCompat
import me.hexile.odexpatcher.ktx.shouldShowRequestPermissionRationaleCompat

open class BaseFragment : Fragment() {
    val activity get() = requireActivity() as AppCompatActivity
}

fun BaseFragment.shouldShowRequestPermissionRationaleCompat(permission: String) =
    activity.shouldShowRequestPermissionRationaleCompat(permission)

fun BaseFragment.checkSelfPermissionCompat(permission: String) =
    activity.checkSelfPermissionCompat(permission)

fun BaseFragment.openAppSettings(requestCode: Int) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", activity.packageName, null)
    intent.data = uri
    startActivityForResult(intent, requestCode)
}