package me.hexile.odexpatcher.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo

fun isUserApp(ai: ApplicationInfo): Boolean {
    return (ai.flags and ApplicationInfo.FLAG_SYSTEM) == 0
}

fun isUserApp(pi: PackageInfo): Boolean {
    return isUserApp(pi.applicationInfo)
}