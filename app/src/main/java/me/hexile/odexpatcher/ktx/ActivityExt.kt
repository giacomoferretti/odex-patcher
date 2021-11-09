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

package me.hexile.odexpatcher.ktx

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Activity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun Activity.checkSelfPermissionCompat(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Activity.launchAndFinish(targetPackage: String) {
    startActivity(packageManager.getLaunchIntentForPackage(targetPackage))
    finish()
}