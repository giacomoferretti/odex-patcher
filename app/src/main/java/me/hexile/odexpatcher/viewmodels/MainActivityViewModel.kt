package me.hexile.odexpatcher.viewmodels

import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.hexile.odexpatcher.core.App
import me.hexile.odexpatcher.data.AppInfo
import me.hexile.odexpatcher.utils.isUserApp
import me.hexile.odexpatcher.utils.newLine

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    val baseFile = MutableLiveData<String>()
    val baseFileUri = MutableLiveData<Uri>()
    val targetPackage = MutableLiveData<String>()
    val state = MutableLiveData<Boolean>()
    val installedApps = MutableLiveData<MutableList<AppInfo>>()
    val logs = MutableLiveData<String>()

    fun setBaseFile(file: String) {
        baseFile.value = file
    }

    fun getBaseFile(): LiveData<String> {
        return baseFile
    }

    fun addToInstalledApps(appInfo: AppInfo) {
        installedApps.value?.add(appInfo)
        installedApps.value = installedApps.value
    }

    fun refreshInstalledApps() {
        val apps: ArrayList<AppInfo> = ArrayList()

        val packageManager = App.context.packageManager
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        for (packageInfo in packages) {
            // Skip same package
            if (packageInfo.packageName == App.context.packageName) {
                continue
            }

            // TODO: Add filter to show system apps
            if (isUserApp(packageInfo)) {
                apps.add(
                    AppInfo(
                        name = packageManager.getApplicationLabel(packageInfo.applicationInfo)
                            .toString(),
                        packageName = packageInfo.packageName,
                        versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
                        versionName = packageInfo.versionName,
                        icon = App.context.packageManager.getApplicationIcon(packageInfo.applicationInfo)
                    )
                )
            }
        }

        installedApps.value = apps
    }

    fun addLog(line: String, newLine: Boolean = true) {
        val n = if (newLine) { "\n" } else { "" }
        logs.postValue(logs.value + "$n$line")
    }

    init {
        state.value = true

        installedApps.value = ArrayList()

        viewModelScope.launch {
            refreshInstalledApps()
        }
    }

}