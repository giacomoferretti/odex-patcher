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

package me.hexile.odexpatcher.viewmodels

import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.hexile.odexpatcher.BuildConfig
import me.hexile.odexpatcher.R
import me.hexile.odexpatcher.art.Art
import me.hexile.odexpatcher.art.Dex2Oat
import me.hexile.odexpatcher.art.oat.OatFile
import me.hexile.odexpatcher.art.vdex.VdexFile
import me.hexile.odexpatcher.core.App
import me.hexile.odexpatcher.core.SELinux
import me.hexile.odexpatcher.core.utils.MediaStoreUtils
import me.hexile.odexpatcher.core.utils.MediaStoreUtils.inputStream
import me.hexile.odexpatcher.core.utils.MediaStoreUtils.outputStream
import me.hexile.odexpatcher.data.AppInfo
import me.hexile.odexpatcher.ktx.*
import me.hexile.odexpatcher.utils.*
import java.io.File
import java.util.zip.ZipException
import java.util.zip.ZipFile

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val inputFile = MutableLiveData<String>()
    val inputFileUri = MutableLiveData<Uri>()
    val targetPackage = MutableLiveData<String>()
    val installedApps = MutableLiveData<MutableList<AppInfo>>()

    private val context by lazy {
        getApplication<App>().applicationContext
    }

    private val contentResolver by lazy {
        getApplication<App>().contentResolver
    }

    private val packageManager by lazy {
        getApplication<App>().packageManager
    }

    init {
        refreshInstalledApps()
    }

    fun addToInstalledApps(appInfo: AppInfo) {
        installedApps.value?.add(appInfo)
        installedApps.value = installedApps.value
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            forLoop()
            println("Done in ${System.currentTimeMillis() - startTime}")
        }
    }

    private fun forLoop() {
        val apps: ArrayList<AppInfo> = ArrayList()

        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        //println(packages.size)

        for (packageInfo in packages) {
            // Skip same package
            if (packageInfo.packageName == context.packageName) {
                continue
            }

            // TODO: Add filter to show system apps
            // TODO: Add support for system apps
            if (/*!isUserApp(packageInfo) || */isUserApp(packageInfo)) {
                apps.add(
                    AppInfo(
                        name = packageManager.getApplicationLabel(packageInfo.applicationInfo)
                            .toString(),
                        packageName = packageInfo.packageName,
                        versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
                        versionName = packageInfo.versionName,
                        icon = packageManager.getApplicationIcon(packageInfo.applicationInfo)
                    )
                )
            }
        }

        installedApps.value = apps
    }

    sealed class Event {
        data class SaveLogEvent(val path: String, val data: String) : Event()
        data class SnackBarStringRes(@StringRes val stringId: Int) : Event()
        data class SnackBarString(val string: String) : Event()
    }

    private val eventChannel = Channel<Event>(BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun saveLog() = viewModelScope.launch(Dispatchers.IO) {
        eventChannel.send(Event.SnackBarString("Collecting data..."))

        val filename = "odexpatcher_%s.txt".format(now.toTime(timeFormatStandard))
        val logFile = MediaStoreUtils.getFile(filename)

        logFile.uri.outputStream().bufferedWriter().use { file ->
            file.write("---[ App Info ]---\n")
            file.write("${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) - ${BuildConfig.BUILD_TYPE} \n")

            file.write("\n---[ Device Info ]---\n")
            file.write("SDK_INT = ${Build.VERSION.SDK_INT}\n")
            file.write("RELEASE = ${Build.VERSION.RELEASE}\n")
            file.write("CPU_ABI = ${Art.CPU_ABI}\n")
            file.write("ISA = ${Art.ISA}\n")

            file.write("\n---[ SELinux ]---\n")
            file.write("isEnabled = ${SELinux.isEnabled()}\n")
            file.write("isEnforced = ${SELinux.isEnforced()}\n")
            file.write("getContext = ${SELinux.getContext()}\n")
            ProcessBuilder("logcat", "-d").start()
                .inputStream.bufferedReader().lineSequence()
                .filter { it.contains("avc") }
                .filter { it.contains(context.packageName) }
                .forEach { file.write("$it\n") }

            file.write("\n---[ Logcat ]---\n")
            ProcessBuilder(
                "logcat",
                "-d",
                "--pid=${android.os.Process.myPid()}",
                "-s",
                "OdexPatcher",
                "-s",
                "SHELL_IN",
                "-s",
                "SHELLOUT",
                "-s",
                "SHELLIMPL"
            ).start()
                .inputStream.reader().use { it.copyTo(file) }

            file.write("\n---[ System Properties ]---\n")
            ProcessBuilder("getprop").start()
                .inputStream.reader().use { it.copyTo(file) }
        }

        val data = logFile.uri.inputStream().bufferedReader().use { file -> file.readText() }

        eventChannel.send(Event.SaveLogEvent(logFile.toString(), data))
    }

    fun patch() = viewModelScope.launch(Dispatchers.IO) {
        inputFileUri.value ?: run {
            eventChannel.send(Event.SnackBarStringRes(R.string.error_select_input_file))
            return@launch
        }

        targetPackage.value ?: run {
            eventChannel.send(Event.SnackBarStringRes(R.string.error_select_target_app))
            return@launch
        }

        // TODO: Move all of this stuff to a use case?

        // TODO: Step 1 - check root access
        if (!Shell.rootAccess()) {
            loge("patch", "No root access!")
            eventChannel.send(Event.SnackBarStringRes(R.string.error_no_root_access))
            return@launch
        }

        val targetApk = context.getPackageApk(targetPackage.value!!)
        val baseFolder = targetApk.parentFile!!.absolutePath
        val dexFile = targetApk.name
        val baseApk = context.getFileInCacheDir(dexFile)

        // TODO: Step 2 - copy to cache folder
        // Copy input file to private app folder
        contentResolver.openInputStream(inputFileUri.value!!)?.let {
            baseApk.copyInputStreamToFile(it)
        }

        // TODO: Step 3 - check if zip / check if dex
        // TODO(2.1.0): Add support for single dex files
        // Check if input file is zip
        try {
            ZipFile(baseApk)
        } catch (e: ZipException) {
            loge("patch", "Input file is not a zip.")
            eventChannel.send(Event.SnackBarStringRes(R.string.error_file_not_zip))
            return@launch
        }

        // TODO: Step 4 - extract original checksums
        // Extract dex files checksums
        val sourceClasses = extractChecksums(baseApk)
        val targetClasses = extractChecksums(targetApk)
        logd("patch", "--- Checksums ---")
        logd("patch", "source: ${sourceClasses.toHexString()}")
        logd("patch", "target: ${targetClasses.toHexString()}")

        // TODO: Step 5 - check same amount of checksums
        // TODO(2.1.0): Auto generate dummy classes.dex to match
        // TODO: System apps sometimes are stripped of classes.dex, so this won't work
        //   Maybe parse the .odex and .vdex files instead.
        // Check if same number of classes.dex in APK
        if (sourceClasses.size != targetClasses.size) {
            loge("patch", "source.size != target.size")
            eventChannel.send(Event.SnackBarStringRes(R.string.error_different_dex_count))
            return@launch
        }

        // TODO: Step 6 - run dex2oat on input file
        // Run dex2oat on input file
        val outputOatFile = context.getFileInCacheDir("base.odex")
        val outputVdexFile = context.getFileInCacheDir("base.vdex")
        Dex2Oat.run(baseApk.absolutePath, dexFile)

        // TODO: Step 7 - fix permissions
        fixCacheFolderPermission(outputOatFile)
        fixCacheFolderPermission(outputVdexFile)

        // TODO: Step 8.1 - patch oat checksums
        // Patch oat
        val oatFile = OatFile(outputOatFile)
        logd("patch", oatFile.toString())
        oatFile.patch(targetClasses)

        // TODO: Step 8.2 - patch vdex checksums (optional)
        // Patch vdex
        if (isSdkGreaterThan(Build.VERSION_CODES.O)) {
            val vdexFile = VdexFile(outputVdexFile)
            logd("patch", vdexFile.toString())
            vdexFile.patch(targetClasses)
        }

        // TODO: Step 9.1 - create missing folder (optional)
        // Create folder if non-existent
        if (!File(Art.getOatFolder(targetApk.absolutePath)).isDirectory && isSdkGreaterThan(Build.VERSION_CODES.M)) {
            Shell.sh("mkdir -p ${Art.getOatFolder(targetApk.absolutePath)}").exec()
        }

        // TODO: Step 9.2 - replace original oat file
        Shell.sh("cp $outputOatFile ${Art.getOatFile(targetApk.absolutePath)}").exec()

        // TODO: Step 9.3 - replace original vdex file (optional)
        if (isSdkGreaterThan(Build.VERSION_CODES.O)) {
            Shell.sh("cp $outputVdexFile ${Art.getVdexFile(targetApk.absolutePath)}").exec()
        }

        // TODO: Step 10 - fix permissions
        chown("$baseFolder/oat", 1000, 1012, true)
        chmod("$baseFolder/oat", "771", true)
        chown(Art.getOatFile(targetApk.absolutePath), 1000, multiuserSharedAppGid(targetPackage.value!!))
        chmod(Art.getOatFile(targetApk.absolutePath), "644")
        chown(Art.getVdexFile(targetApk.absolutePath), 1000, multiuserSharedAppGid(targetPackage.value!!))
        chmod(Art.getVdexFile(targetApk.absolutePath), "644")
        restorecon("$baseFolder/oat", true)
        restorecon("$baseFolder/oat", true)

        eventChannel.send(Event.SnackBarString("DONE!"))
    }
}