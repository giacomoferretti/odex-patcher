package me.hexile.odexpatcher.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import me.hexile.odexpatcher.core.App
import me.hexile.odexpatcher.core.BaseFragment

fun Activity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun Activity.checkSelfPermissionCompat(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Activity.launchAndFinish(targetPackage: String) {
    startActivity(packageManager.getLaunchIntentForPackage(targetPackage))
    finish()
}

fun BaseFragment.shouldShowRequestPermissionRationaleCompat(permission: String) =
    activity.shouldShowRequestPermissionRationaleCompat(permission)

fun BaseFragment.checkSelfPermissionCompat(permission: String) =
    activity.checkSelfPermissionCompat(permission)

fun BaseFragment.openAppSettings(requestCode: Int) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", App.context.packageName, null)
    intent.data = uri
    startActivityForResult(intent, requestCode)
}