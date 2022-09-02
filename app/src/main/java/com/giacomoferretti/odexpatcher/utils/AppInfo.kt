package com.giacomoferretti.odexpatcher.utils

import android.content.ContentResolver
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.WorkerThread
// import com.giacomoferretti.odexpatcher.library.Art
// import com.giacomoferretti.odexpatcher.library.InstructionSet
// import com.giacomoferretti.odexpatcher.library.Utils

interface IAppInfo {
    val name: String
    val packageName: String
    val iconUri: Uri
    val dexCount: Int
    val abi: String
}

data class AppInfo(
    override val name: String,
    override val packageName: String,
    override val iconUri: Uri,
    override val dexCount: Int,
    override val abi: String,
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

            val a = ApplicationInfo::class.java.getDeclaredField("primaryCpuAbi").get(packageManager.getPackageInfo(packageName, 0).applicationInfo)

            // var abi = Art.ISA
            // if (a != null) {
            //     abi = InstructionSet.fromAbi(a as String).value
            // }

            return AppInfo(
                name = appInfo.loadLabel(packageManager).toString(),
                packageName = appInfo.packageName,
                iconUri = iconUri,
                dexCount = 0, // Utils.countClassesDex(appInfo.sourceDir),
                abi = "null" // abi
            )
        }
    }
}