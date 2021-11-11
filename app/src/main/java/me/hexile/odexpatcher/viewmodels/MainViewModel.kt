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
import me.hexile.odexpatcher.art.oat.OatFile
import me.hexile.odexpatcher.art.vdex.VdexFile
import me.hexile.odexpatcher.core.App
import me.hexile.odexpatcher.core.Const
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
        /*data class ShowSnackBarShare(val text: String, val data: String): Event()
        data class ShowSnackBarShareUri(val text: String, val data: Uri): Event()
        data class ShowSnackBar(val text: String): Event()
        data class ShowToast(val text: String): Event()*/
        /*object NavigateToSettings: Event()
        data class ShowSnackBar(val text: String): Event()
        data class ShowSnackBarAction(val text: String, val actionText: String, val action: (View) -> Unit): Event()
        data class ShowToast(val text: String): Event()*/
    }

    private val eventChannel = Channel<Event>(BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    /*init {
        viewModelScope.launch {
            eventChannel.send(Event.ShowToast("Toast"))
        }
    }*/

    /*fun settingsButtonClicked() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateToSettings)
        }
    }*/

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
                "OdexPatcher"
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

        // FIXME: Move to utils
        if (!Shell.rootAccess() && false) { // FIXME: false hardcoded
            //viewModel.addLog("[E] ERROR: No root access! This app won't work without it.")
            //viewModel.status.postValue("ERROR: " + getString(R.string.error_no_root_access))
            //viewModel.state.postValue(true)
            eventChannel.send(Event.SnackBarStringRes(R.string.error_no_root_access))
            return@launch
        }


        val targetApk = context.getPackageBaseApk(targetPackage.value!!)
        val baseFolder = File(targetApk).parentFile!!.absolutePath
        val dexFile = targetApk.extractFilename()
        val baseApk = context.getFileInFilesDir(dexFile)

        // Copy input file to private app folder
        contentResolver.openInputStream(inputFileUri.value!!)?.let {
            baseApk.copyInputStreamToFile(it)
        }

        // TODO(2.1.0): Add support for single dex files
        // Check if input file is zip
        try {
            ZipFile(baseApk)
        } catch (e: ZipException) {
            //viewModel.addLog("[E] ERROR: " + getString(R.string.error_file_not_zip))
            //viewModel.state.postValue(true)
            eventChannel.send(Event.SnackBarStringRes(R.string.error_file_not_zip))
            return@launch
        }

        // Extract dex files checksums
        val sourceClasses = extractClassesDex(baseApk)
        val targetClasses = extractClassesDex(targetApk)
        /*val targetOatChecksums = OatFile(File(Art.getOatFile(targetApk))).checksums
        val targetClasses = ArrayList<ByteArray>()
        targetOatChecksums.asSequence().iterator().forEach {
            targetClasses.add(it.second)
        }*/

        println("$baseApk = ${sourceClasses.toHexString()}")
        println("$targetApk = ${targetClasses.toHexString()}")

        // TODO(2.1.0): Auto generate dummy classes.dex to match
        // Check if same number of classes.dex in APK
        /*if (sourceClasses.size != targetClasses.size) {
            //viewModel.addLog("[E] ERROR: " + getString(R.string.error_different_dex_count))
            //viewModel.state.postValue(true)
            eventChannel.send(Event.SnackBarStringRes(R.string.error_different_dex_count))
            return@launch
        }*/

        // Run dex2oat on source APK
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                // Android 4.4 - 5.1 workflow
                //  - copy from /data/app/packagename.apk to /data/data/me.hexile.odexpatcher/files/backup.apk
                //  - copy from /data/data/me.hexile.odexpatcher/files/base.apk to /data/app/packagename.apk
                //  - dex2oat /data/app/packagename.apk
                //  - copy from /data/data/me.hexile.odexpatcher/files/backup.apk to /data/app/packagename.apk
                val backupApk = context.getFileInFilesDir("backup.apk").absolutePath

                //viewModel.addLog("[I] Backing up target apk…")
                var shellResult = Shell.su("cp $targetApk $backupApk").exec()
                if (!shellResult.isSuccess) {
                    //viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
                    //viewModel.state.postValue(true)
                    eventChannel.send(Event.SnackBarString("ERROR: cp exit code was ${shellResult.code}."))
                    return@launch
                }
                //viewModel.addLog(" Done!", false)

                //viewModel.addLog("[I] Copying over input file…")
                shellResult = Shell.su("cp ${baseApk.absolutePath} $targetApk").exec()
                if (!shellResult.isSuccess) {
                    //viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
                    //viewModel.state.postValue(true)
                    eventChannel.send(Event.SnackBarString("ERROR: cp exit code was ${shellResult.code}."))
                    return@launch
                }
                //viewModel.addLog(" Done!", false)

                //viewModel.addLog("[I] Running dex2oat…")
                shellResult = Shell.sh(
                    "dex2oat --dex-file=$targetApk --oat-file=${
                        context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
                    }"
                ).exec()
                if (!shellResult.isSuccess) {
                    //viewModel.addLog("[E] ERROR: dex2oat exit code was ${shellResult.code}.")
                    //viewModel.state.postValue(true)
                    eventChannel.send(Event.SnackBarString("ERROR: dex2oat exit code was ${shellResult.code}."))
                    return@launch
                }
                //viewModel.addLog(" Done!", false)

                //viewModel.addLog("[I] Restoring backup…")
                shellResult = Shell.su("cp $backupApk $targetApk").exec()
                if (!shellResult.isSuccess) {
                    //viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
                    //viewModel.state.postValue(true)
                    eventChannel.send(Event.SnackBarString("ERROR: cp exit code was ${shellResult.code}."))
                    return@launch
                }
                //viewModel.addLog(" Done!", false)
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                // Android 6.0 - 9.0 workflow
                //  - cd /data/data/me.hexile.odexpatcher/files/
                //  - dex2oat base.apk
                //viewModel.addLog("[I] Running dex2oat…")
                val shellResult = Shell.sh(
                    "cd ${context.getFileInFilesDir("").absolutePath} && dex2oat --dex-file=${dexFile} --dex-location=base.apk --oat-file=${
                        context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
                    }"
                ).exec()

                // Fix permissions
                val uid = context.packageManager.getApplicationInfo(context.packageName, 0).uid
                val gid = uid //(uid % 100000) - 10000 + 50000
                Shell.su(
                    "chown ${uid}:${gid} ${context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath} && chmod 600 ${
                        context.getFileInFilesDir(
                            Const.BASE_ODEX_FILE_NAME
                        ).absolutePath
                    }"
                ).exec()

                // SELinux
                Shell.su("restorecon -Rv ${context.getFileInFilesDir("").absolutePath}").exec()

                if (!shellResult.isSuccess) {
                    //viewModel.addLog("[E] ERROR: dex2oat exit code was ${shellResult.code}.")
                    //viewModel.state.postValue(true)
                    eventChannel.send(Event.SnackBarString("ERROR: dex2oat exit code was ${shellResult.code}."))
                    return@launch
                }
                //viewModel.addLog(" Done!", false)

                Shell.sh(
                    "cp ${context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath} ${
                        context.getFileInFilesDir(
                            Const.BASE_ODEX_FILE_NAME
                        ).absolutePath
                    }.bak"
                ).exec()
            }
            else -> {
                // Android 10+ workflow
                //  - cd /data/data/me.hexile.odexpatcher/files/
                //  - su dex2oat base.apk
                //viewModel.addLog("[I] Running dex2oat…")
                println(
                    "dex2oat --dex-file=${baseApk.absolutePath} --dex-location=base.apk --oat-file=${
                        context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
                    }"
                )
                val shellResult = Shell.su(
                    /*"cd ${App.context.getFileInFilesDir("").absolutePath} && dex2oat64 --dex-file=${Const.BASE_APK_FILE_NAME} --oat-file=${
                        App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
                    }"*/
                    "dex2oat --dex-file=${baseApk.absolutePath} --dex-location=base.apk --oat-file=${
                        context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
                    }"
                ).exec()
                if (!shellResult.isSuccess) {
                    //viewModel.addLog("[E] ERROR: dex2oat exit code was ${shellResult.code}.")
                    //viewModel.state.postValue(true)
                    eventChannel.send(Event.SnackBarString("ERROR: LL dex2oat exit code was ${shellResult.code}."))
                    return@launch
                }
                // Fix permissions
                val uid = context.packageManager.getApplicationInfo(context.packageName, 0).uid
                val gid = uid //(uid % 100000) - 10000 + 50000
                Shell.su(
                    "chown ${uid}:${gid} ${context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath} && chmod 600 ${
                        context.getFileInFilesDir(
                            Const.BASE_ODEX_FILE_NAME
                        ).absolutePath
                    }"
                ).exec()
                Shell.su(
                    "chown ${uid}:${gid} ${context.getFileInFilesDir(Const.BASE_VDEX_FILE_NAME).absolutePath} && chmod 600 ${
                        context.getFileInFilesDir(
                            Const.BASE_VDEX_FILE_NAME
                        ).absolutePath
                    }"
                ).exec()

                // SELinux
                Shell.su("restorecon -Rv ${context.getFileInFilesDir("").absolutePath}").exec()
                //viewModel.addLog(" Done!", false)
            }
        }

        // Fix permissions
        /*val appUid = (App.context.packageManager.getApplicationInfo(App.context.packageName, 0).uid % 100000)
        Shell.su("chown $appUid:$appUid ${App.context.getFileInFilesDir("*")}").exec()
        Shell.su("chmod 600 ${App.context.getFileInFilesDir("*")}").exec()
        if (SELinux.isEnabled()) {
            Shell.su("chcon u:object_r:app_data_file:s0 ${App.context.getFileInFilesDir("*")}").exec()
        }*/

        println(context.openFileInput(Const.BASE_ODEX_FILE_NAME).readBytes().copyOfRange(0, 4))

        // Patch files
        //viewModel.addLog("[I] Patching oat file…")
        val oatFile = OatFile(context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME))
        println(oatFile)
        //oatFile.patch(targetClasses)

        val oatFile1 = OatFile(context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME))
        println(oatFile1)
        /*try {
            OatFile(App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME)).patch(
                targetClasses
            )
        } catch (e: Exhttps://developer.android.com/training/data-storage/app-specific?hl=el#kotlinception) {
            e.printStackTrace()
            viewModel.addLog("[E] ERROR: ${e.message}")
            viewModel.state.postValue(true)
            return@launch
        }*/
        //viewModel.addLog(" Done!", false)

        // DexLocationToOdexFilename: https://cs.android.com/android/platform/superproject/+/master:art/runtime/oat_file_assistant.cc;l=532

        // https://cs.android.com/android/platform/superproject/+/master:art/runtime/oat_file_assistant.cc;l=807
        // https://cs.android.com/android/platform/superproject/+/master:art/runtime/oat_file_assistant.cc;l=429
        // https://cs.android.com/android/platform/superproject/+/master:art/runtime/oat_file_assistant.cc;l=348
        // 2021-11-08 17:46:59.483 633-709/system_process E/system_server: Dex checksum does not match for dex: /data/app/me.hexile.sara.singletextview-fNTidlXzViTslDfBSLOZAA==/base.apk.Expected: 4175936928, actual: 1970187948

        if (isSdkGreaterThan(Build.VERSION_CODES.O)) {
            val vdexFile = VdexFile(context.getFileInFilesDir(Const.BASE_VDEX_FILE_NAME))
            println(vdexFile)
            //vdexFile.patch(targetClasses)
            //viewModel.addLog("[I] Patching vdex file…")
            /*try {
                VdexFile(App.context.getFileInFilesDir(Const.BASE_VDEX_FILE_NAME)).patch(
                    targetClasses
                )
            } catch (e: Exception) {
                viewModel.addLog("[E] ERROR: ${e.message}")
                viewModel.state.postValue(true)
                return@launch
            }*/
            //viewModel.addLog(" Done!", false)
        }

        // Create folder if non-existent
        if (!File(Art.getOatFolder(targetApk)).isDirectory && isSdkGreaterThan(Build.VERSION_CODES.M)) {
            // https://android.googlesource.com/platform/frameworks/native/+/refs/tags/android-12.0.0_r1/cmds/installd/InstalldNativeService.cpp#2573
            /*
            if (fs_prepare_dir(oat_dir, S_IRWXU | S_IRWXG | S_IXOTH, AID_SYSTEM, AID_INSTALL)) {
                return error("Failed to prepare " + oatDir);
            }
            */
            Shell.su("mkdir -p ${Art.getOatFolder(targetApk)} && chown -R system:install ${baseFolder}/oat && chmod -R 771 ${baseFolder}/oat")
                .exec()
        }

        // Replace original files with patched ones
        //viewModel.addLog("[I] Replacing odex file…")
        println(
            "cp ${context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath} ${
                Art.getOatFile(targetApk)
            }"
        )
        var shellResult = Shell.su(
            "cp ${context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath} ${
                Art.getOatFile(targetApk)
            }"
        ).exec()
        if (!shellResult.isSuccess) {
            //viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
            //viewModel.state.postValue(true)
            eventChannel.send(Event.SnackBarString("ERROR: PATCH cp exit code was ${shellResult.code}."))
            return@launch
        }
        //viewModel.addLog(" Done!", false)

        if (isSdkGreaterThan(Build.VERSION_CODES.O)) {
            //viewModel.addLog("[I] Replacing vdex file…")
            shellResult =
                Shell.su(
                    "cp ${context.getFileInFilesDir(Const.BASE_VDEX_FILE_NAME).absolutePath} ${
                        Art.getVdexFile(
                            targetApk
                        )
                    }"
                ).exec()
            if (!shellResult.isSuccess) {
                //viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
                //viewModel.state.postValue(true)
                eventChannel.send(Event.SnackBarString("ERROR: cp exit code was ${shellResult.code}."))
                return@launch
            }
            //viewModel.addLog(" Done!", false)
        }

        eventChannel.send(Event.SnackBarString("DONE!"))

        // Fix permissions
        /*if (isSdkGreaterThan(Build.VERSION_CODES.M)) {
            val appUid = App.context.packageManager.getApplicationInfo(targetPackage.value!!, 0).uid
            // https://cs.android.com/android/platform/superproject/+/master:system/core/libcutils/include/private/android_filesystem_config.h
            // AID_USER_OFFSET      = 100000
            // AID_APP_START        = 10000
            // AID_SHARED_GID_START = 50000
            val gid = (appUid % 100000) - 10000 + 50000
            // https://cs.android.com/android/platform/superproject/+/master:frameworks/native/cmds/installd/dexopt.cpp;l=816
            //Shell.su("chown system:${appUid} ${Art.getOatFolder(targetApk)}* && chmod 644 ${Art.getOatFolder(targetApk)}*").exec()
        }*/

        /*try {
            val bytes = requireContext().contentResolver.openInputStream(viewModel.inputFileUri.value!!)?.readBytes()
            if (bytes != null) {
                println(bytes.copyOfRange(0, 4).toHexString())
            }
        } catch (fnfe: FileNotFoundException) {
            // ContentResolver.openInputStream() throws FileNotFoundException
            binding.root.showSnackbar("Cannot read input file!", Snackbar.LENGTH_SHORT)
            viewModel.inputFile.value = null
            viewModel.inputFileUri.value = null
        }*/
    }

//    fun saveLog() = withExternalRW {
//        viewModelScope.launch(Dispatchers.IO) {
//            val filename = "odexpatcher_%s.txt".format(now.toTime(timeFormatStandard))
//            logd("OP_saveLog", filename)
//            println(cr)
//
//            //val displayName = filename
//
//            //val values = ContentValues()
//            //values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
//            //values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
//
//            //val uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: throw IOException("Failed to insert $displayName.")
//
//            /*val uri: Uri = getContentResolver().insert(
//                MediaStore.Files.getContentUri("external"),
//                values
//            ) //important!*/
//
//            MediaStoreUtils.getFile(filename).outputStream().bufferedWriter().use { file ->
//                file.write("---System Properties---\n\n")
//                ProcessBuilder("getprop").start()
//                    .inputStream.reader().use { it.copyTo(file) }
//
//                file.write("\n---Logcat---\n")
//                file.write("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n")
//                ProcessBuilder("logcat", "-d", "--pid=${android.os.Process.myPid()}", "-s", "OdexPatcher").start()
//                    .inputStream.reader().use { it.copyTo(file) }
//            }
//
//            /*val cursor = cr.query(uri, null, null, null, null)
//            DatabaseUtils.dumpCursor(cursor)*/
//
//            //val logfile = getFile(filename, true)
//            /*val logFile = MediaStoreUtils.getFile(filename, true)
//            logFile.uri.outputStream().bufferedWriter().use { file ->
//                file.write("---Detected Device Info---\n\n")
//                file.write("isAB=${Info.isAB}\n")
//                file.write("isSAR=${Info.isSAR}\n")
//                file.write("ramdisk=${Info.ramdisk}\n")
//
//                file.write("\n\n---System Properties---\n\n")
//                ProcessBuilder("getprop").start()
//                    .inputStream.reader().use { it.copyTo(file) }
//
//                file.write("\n\n---System MountInfo---\n\n")
//                FileInputStream("/proc/self/mountinfo").reader().use { it.copyTo(file) }
//
//                file.write("\n---Magisk Logs---\n")
//                file.write("${Info.env.magiskVersionString} (${Info.env.magiskVersionCode})\n\n")
//                file.write(consoleText)
//
//                file.write("\n---Manager Logs---\n")
//                file.write("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n")
//                ProcessBuilder("logcat", "-d").start()
//                    .inputStream.reader().use { it.copyTo(file) }
//            }
//            SnackbarEvent(logFile.toString()).publish()*/
//        }
//    }

    /*val baseFile = MutableLiveData<String>()
    val baseFileUri = MutableLiveData<Uri>()
    val targetPackage = MutableLiveData<String>()
    val state = MutableLiveData<Boolean>()
    val installedApps = MutableLiveData<MutableList<AppInfo>>()
    val logs = MutableLiveData<String>()
    val status = MutableLiveData<String>()*/

    /*fun setBaseFile(file: String) {
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
    }*/

}