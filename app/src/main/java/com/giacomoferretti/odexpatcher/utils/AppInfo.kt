package com.giacomoferretti.odexpatcher.utils

import android.content.ContentResolver
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.WorkerThread
import com.giacomoferretti.odexpatcher.library.Art
import com.giacomoferretti.odexpatcher.library.InstructionSet
import com.giacomoferretti.odexpatcher.library.Utils

interface IAppInfo {
    val name: String
    val packageName: String
    val iconUri: Uri
    val dexCount: Int
    val abi: String
    val isOptimized: Boolean
}

data class AppInfo(
    override val name: String,
    override val packageName: String,
    override val iconUri: Uri,
    override val dexCount: Int,
    override val abi: String,
    override val isOptimized: Boolean,
) : IAppInfo {
    companion object {
        @WorkerThread
        fun fromPackageName(packageManager: PackageManager, packageName: String): AppInfo {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)

            val iconUri = if (appInfo.icon != 0) {
                Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(packageName)
                    .path(appInfo.icon.toString())
                    .build()
            } else {
                Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority("android")
                    .appendPath("drawable")
                    .appendPath("sym_def_app_icon")
                    .build()
            }

            val abi = InstructionSet.fromPackageName(packageManager, packageName).value
            val isOptimized = Art.isOptimized(appInfo.sourceDir, abi)

            return AppInfo(
                name = appInfo.loadLabel(packageManager).toString(),
                packageName = appInfo.packageName,
                iconUri = iconUri,
                dexCount = Utils.countClassesDex(appInfo.sourceDir),
                abi = abi,
                isOptimized = isOptimized
            )
        }
    }
}