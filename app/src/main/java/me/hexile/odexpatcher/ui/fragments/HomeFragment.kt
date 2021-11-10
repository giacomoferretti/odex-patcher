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

package me.hexile.odexpatcher.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.hexile.odexpatcher.R
import me.hexile.odexpatcher.core.BaseFragment
import me.hexile.odexpatcher.core.Const
import me.hexile.odexpatcher.core.openAppSettings
import me.hexile.odexpatcher.core.shouldShowRequestPermissionRationaleCompat
import me.hexile.odexpatcher.databinding.FragmentHomeBinding
import me.hexile.odexpatcher.utils.showSnackbar
import me.hexile.odexpatcher.viewmodels.MainViewModel

class HomeFragment : BaseFragment() {
    companion object {
        const val TAG = "HomeFragment"
    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>

    private val viewModel: MainViewModel by activityViewModels()

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                // Get display name
                requireContext().contentResolver.query(
                    uri,
                    arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                    null,
                    null,
                    null,
                    null
                )
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            viewModel.inputFile.value =
                                cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                        OpenableColumns.DISPLAY_NAME
                                    )
                                )
                        }
                    }

                viewModel.inputFileUri.value = uri
            }
        }

    // Android 12 - READ_EXTERNAL_STORAGE
    // check for permission
    //   false -> ask for permission
    //     allow -> isGranted true
    //     ignore -> isGranted false -> shouldShowRequestPermissionRationaleCompat false
    //   true -> SUCCESS

    // Android 12 - ACTION_GET_CONTENT
    // exit -> ActivityResult{resultCode=RESULT_CANCELED, data=null}
    // from Photos -> I/System.out: ActivityResult{resultCode=RESULT_OK, data=Intent { dat=content://com.google.android.apps.photos.contentprovider/-1/1/content://media/external/images/media/61/ORIGINAL/NONE/image/png/1776639384 flg=0x1 clip={text/uri-list {U(content)}} }}

    // Android 12 - ACTION_OPEN_DOCUMENT
    // exit -> does nothing
    // correct -> content://com.android.externalstorage.documents/document/primary%3Amultidex-release-1000001-modded.apk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    //startFileChooser()
                    binding.root.showSnackbar(
                        "isGranted = true",
                        Snackbar.LENGTH_SHORT
                    )
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // First time user denied permission
                        /*binding.root.showSnackbar(
                            "This app won't work without this permission.",
                            Snackbar.LENGTH_SHORT
                        )*/
                        binding.root.showSnackbar(
                            "shouldShowRequestPermissionRationaleCompat = true",
                            Snackbar.LENGTH_SHORT
                        )
                    } else {
                        // Never ask again
                        /*binding.root.showSnackbar(
                            "This app won't work without this permission.",
                            Snackbar.LENGTH_LONG,
                            "Settings"
                        ) {
                            openAppSettings(0)
                        }*/
                        binding.root.showSnackbar(
                            "shouldShowRequestPermissionRationaleCompat = false // never ask again",
                            Snackbar.LENGTH_SHORT
                        )
                    }
                }
            }

        fileChooserLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->


                println(result)
                /*if (result.resultCode == Activity.RESULT_OK) {
                    if (result.data == null || result.data!!.data == null) {
                        binding.root.showSnackbar(
                            "There was an error. Please retry.",
                            Snackbar.LENGTH_SHORT
                        )
                        return@registerForActivityResult
                    }

                    result.data?.data?.let { uri ->
                        viewModel.baseFile.value = uri.toString()
                        App.context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null, null)
                            ?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    viewModel.baseFile.value =
                                        cursor.getString(
                                            cursor.getColumnIndexOrThrow(
                                                OpenableColumns.DISPLAY_NAME
                                            )
                                        )
                                }
                            }

                        viewModel.baseFileUri.value = uri

                        val bytes = App.context.contentResolver.openInputStream(viewModel.baseFileUri.value!!)?.readBytes()
                    }
                }*/
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        viewModel.inputFile.observe(viewLifecycleOwner, { inputFile ->
            binding.inputFileTextView.text = inputFile ?: getString(R.string.choose_file)
        })

        viewModel.targetPackage.observe(viewLifecycleOwner, { targetPackage ->
            binding.targetAppTextView.text = targetPackage ?: getString(R.string.choose_app)
        })

        binding.chooseFileButton.setOnClickListener {
            openDocumentLauncher.launch(
                arrayOf(
                    "application/zip",
                    "application/vnd.android.package-archive",
                    "application/octet-stream",
                    "application/x-binary"
                )
            )
        }

        binding.chooseAppButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_appSelectorFragment)
        }

        binding.patchButton.setOnClickListener {
            viewModel.patch()
        }

        viewModel.eventsFlow
            .flowWithLifecycle(
                lifecycle = viewLifecycleOwner.lifecycle,
                minActiveState = Lifecycle.State.STARTED
            )
            .onEach { event ->
                when (event) {
                    is MainViewModel.Event.SaveLogEvent -> {
                        binding.root.showSnackbar(event.path, Snackbar.LENGTH_LONG, "Open") {
                            /*
                            // Code for sharing instead of opening
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, event.data)
                                type = "text/plain"
                            }

                            val shareIntent = Intent.createChooser(sendIntent, null)
                            startActivity(shareIntent)
                            */

                            val openIntent: Intent = Intent().apply {
                                action = Intent.ACTION_VIEW
                                setDataAndType(event.uri, "text/plain")
                            }

                            openIntent.resolveActivity(requireContext().packageManager)?.let {
                                startActivity(openIntent)
                            } ?: Toast.makeText(
                                requireContext(),
                                "No apps available.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is MainViewModel.Event.SnackBarStringRes -> {
                        binding.root.showSnackbar(getString(event.stringId), Snackbar.LENGTH_SHORT)
                    }
                    is MainViewModel.Event.SnackBarString -> {
                        binding.root.showSnackbar(event.string, Snackbar.LENGTH_SHORT)
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

//            val intent = Intent()
//                .setType("*/*")
//                .putExtra(
//                    Intent.EXTRA_MIME_TYPES,
//                    arrayOf(
//                        "application/zip",
//                        "application/vnd.android.package-archive",
//                        "application/octet-stream",
//                        "application/x-binary"
//                    )
//                )
//                .setAction(Intent.ACTION_GET_CONTENT)
//                .addCategory(Intent.CATEGORY_OPENABLE)
//                .putExtra(Intent.EXTRA_LOCAL_ONLY, true)

        // Check for storage permission
        /*if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            fileChooserLauncher.launch(Intent.createChooser(intent, "Select a file"))
        } else {
            // Request permission
            println("BRUH")
            requestStoragePermission()
        }*/
        /*when {
            checkSelfPermissionCompat(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                // You can use the API that requires the permission.
                fileChooserLauncher.launch(Intent.createChooser(intent, "Select a file"))
                binding.root.showSnackbar(
                    "Bruh ho i permessi",
                    Snackbar.LENGTH_SHORT
                )
            }
            shouldShowRequestPermissionRationaleCompat(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                // First time user denied permission
                binding.root.showSnackbar(
                    "shouldShowRequestPermissionRationaleCompat = true",
                    Snackbar.LENGTH_SHORT
                )
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                binding.root.showSnackbar(
                    "requestPermissionLauncher",
                    Snackbar.LENGTH_SHORT
                )
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }*/

        /*startActivityForResult(
            Intent.createChooser(intent, "Select a file"),
            Const.FILE_REQUEST_CODE
        )*/
        //}

        //binding.chooseAppButton.setOnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "*/*"
//                putExtra(
//                    Intent.EXTRA_MIME_TYPES,
//                    arrayOf(
//                        "application/zip",
//                        "application/vnd.android.package-archive",
//                        "application/octet-stream",
//                        "application/x-binary"
//                    )
//                )
//
//                // Optionally, specify a URI for the file that should appear in the
//                // system file picker when it loads.
//                //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
//            }
//
//            startActivityForResult(intent, 2)
        //openDocumentLauncher.launch(arrayOf("application/zip", "application/vnd.android.package-archive", "application/octet-stream", "application/x-binary"))
        //}

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reportAction -> {
                viewModel.saveLog()
                true
            }
            R.id.licensesAction -> {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun requestStoragePermission() {
        /*requestPermissions(
            arrayOf(),
            Const.Permission.STORAGE
        )*/
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun startFileChooser() {
        val intent = Intent()
            .setType("*/*")
            .putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "application/zip",
                    "application/vnd.android.package-archive",
                    "application/octet-stream",
                    "application/x-binary"
                )
            )
            .setAction(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_LOCAL_ONLY, true)

        startActivityForResult(
            Intent.createChooser(intent, "Select a file"),
            Const.FILE_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Request is for storage
        if (requestCode == Const.Permission.STORAGE) {

            // Check if permission was granted
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startFileChooser()
            } else {
                if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // First time user denied permission
                    binding.root.showSnackbar(
                        "This app won't work without this permission.",
                        Snackbar.LENGTH_SHORT
                    )
                } else {
                    // Never ask again
                    binding.root.showSnackbar(
                        "This app won't work without this permission.",
                        Snackbar.LENGTH_LONG,
                        "Settings"
                    ) {
                        openAppSettings(0)
                    }
                }
            }
        }
    }

    /*private fun initObserve() {
        viewModel.baseFile.observe(viewLifecycleOwner, {
            binding.inputFileTextView.text = it
        })

        viewModel.targetPackage.observe(viewLifecycleOwner, {
            binding.targetAppTextView.text = it
        })

        viewModel.state.observe(viewLifecycleOwner, {
            binding.patchButton.text = if (it) {
                "Patch"
            } else {
                "Patching"
            }
            binding.patchButton.isEnabled = it
        })

        viewModel.logs.observe(viewLifecycleOwner, {
            binding.patchLogs.text = it
        })

        viewModel.status.observe(viewLifecycleOwner, {
            binding.textView.text = it
        })
    }

    suspend fun updateLog(string: String) {
        // TODO: Integrate this function with ViewModel
        binding.patchLogs.newLineOnMainThread(string)
        binding.patchLogsScroll.scrollToBottom()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        initObserve()

        viewModel.logs.postValue("[I] Ready!") // FIXME: Use string resource
        viewModel.status.postValue(getString(R.string.ready))

        binding.chooseFileButton.setOnClickListener {
            // Check for storage permission
            if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                startFileChooser()
            } else {
                // Request permission
                requestStoragePermission()
            }
        }

        binding.chooseAppButton.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.right_enter,
                    R.anim.right_exit,
                    R.anim.right_pop_enter,
                    R.anim.right_pop_exit
                )
                .replace(R.id.container, AppSelectorFragment(), AppSelectorFragment.TAG)
                .addToBackStack(null)
                .commit()
        }

        binding.patchButton.setOnClickListener {
            if (viewModel.baseFileUri.value == null) {
                binding.root.showSnackbar("You need to select a base file", Snackbar.LENGTH_SHORT)
                return@setOnClickListener
            }

            if (viewModel.targetPackage.value == null) {
                binding.root.showSnackbar("You need to select a target app", Snackbar.LENGTH_SHORT)
                return@setOnClickListener
            }

            // TODO: This code sucks, it's unreadable
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                viewModel.state.postValue(false)

                Thread.sleep(5000)

                // Check for root access
                viewModel.addLog("[I] Checking for root access…")
                viewModel.status.postValue(getString(R.string.checking_root_access))
                if (!Shell.rootAccess()) {
                    viewModel.addLog("[E] ERROR: No root access! This app won't work without it.")
                    viewModel.status.postValue("ERROR: " + getString(R.string.error_no_root_access))
                    viewModel.state.postValue(true)
                    return@launch
                }
                viewModel.addLog(getString(R.string.done), false)

                val targetApk = App.context.getPackageBaseApk(viewModel.targetPackage.value!!)
                val baseFolder = File(targetApk).parentFile!!.absolutePath
                val baseApk = App.context.getFileInFilesDir(Const.BASE_APK_FILE_NAME)

                // Copy file
                viewModel.addLog("[I] Reading input file… ")
                App.context.contentResolver.openInputStream(viewModel.baseFileUri.value!!)
                    ?.let {
                        baseApk.copyInputStreamToFile(it)
                    }
                viewModel.addLog(getString(R.string.done), false)

                // TODO: Add support for single dex files
                // Check if input file is zip
                try {
                    ZipFile(baseApk)
                } catch (e: ZipException) {
                    viewModel.addLog("[E] ERROR: " + getString(R.string.error_file_not_zip))
                    viewModel.state.postValue(true)
                    return@launch
                }

                // Extract dex files checksums
                val sourceClasses = extractClassesDex(baseApk)
                val targetClasses = extractClassesDex(targetApk)

                // Check if same number of classes.dex in APK
                if (sourceClasses.size != targetClasses.size) {
                    viewModel.addLog("[E] ERROR: " + getString(R.string.error_different_dex_count))
                    viewModel.state.postValue(true)
                    return@launch
                }

                for (i in sourceClasses.toList().indices) {
                    logd("OP_classes", "${sourceClasses.toList()[i].second.toHexString()} => ${sourceClasses.toList()[i].second.toHexString()}")
                }

                // Run dex2oat on source APK
                when {
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                        // Android 4.4 - 5.1 workflow
                        //  - copy from /data/app/packagename.apk to /data/data/me.hexile.odexpatcher/files/backup.apk
                        //  - copy from /data/data/me.hexile.odexpatcher/files/base.apk to /data/app/packagename.apk
                        //  - dex2oat /data/app/packagename.apk
                        //  - copy from /data/data/me.hexile.odexpatcher/files/backup.apk to /data/app/packagename.apk
                        val backupApk = App.context.getFileInFilesDir("backup.apk").absolutePath

                        viewModel.addLog("[I] Backing up target apk…")
                        var shellResult = Shell.su("cp $targetApk $backupApk").exec()
                        if (!shellResult.isSuccess) {
                            viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
                            viewModel.state.postValue(true)
                            return@launch
                        }
                        viewModel.addLog(" Done!", false)

                        viewModel.addLog("[I] Copying over input file…")
                        shellResult =
                            Shell.su("cp ${App.context.getFileInFilesDir(Const.BASE_APK_FILE_NAME).absolutePath} $targetApk")
                                .exec()
                        if (!shellResult.isSuccess) {
                            viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
                            viewModel.state.postValue(true)
                            return@launch
                        }
                        viewModel.addLog(" Done!", false)

                        viewModel.addLog("[I] Running dex2oat…")
                        shellResult = Shell.sh(
                            "dex2oat --dex-file=$targetApk --oat-file=${
                                App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
                            }"
                        ).exec()
                        if (!shellResult.isSuccess) {
                            viewModel.addLog("[E] ERROR: dex2oat exit code was ${shellResult.code}.")
                            viewModel.state.postValue(true)
                            return@launch
                        }
                        viewModel.addLog(" Done!", false)

                        viewModel.addLog("[I] Restoring backup…")
                        shellResult = Shell.su("cp $backupApk $targetApk").exec()
                        if (!shellResult.isSuccess) {
                            viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
                            viewModel.state.postValue(true)
                            return@launch
                        }
                        viewModel.addLog(" Done!", false)
                    }
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                        // Android 6.0 - 9.0 workflow
                        //  - cd /data/data/me.hexile.odexpatcher/files/
                        //  - dex2oat base.apk
                        viewModel.addLog("[I] Running dex2oat…")
                        val shellResult = Shell.sh(
                            "cd ${App.context.getFileInFilesDir("").absolutePath} && dex2oat --dex-file=${Const.BASE_APK_FILE_NAME} --oat-file=${
                                App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
                            }"
                        ).exec()
                        if (!shellResult.isSuccess) {
                            viewModel.addLog("[E] ERROR: dex2oat exit code was ${shellResult.code}.")
                            viewModel.state.postValue(true)
                            return@launch
                        }
                        viewModel.addLog(" Done!", false)
                    }
                    else -> {
                        // Android 10+ workflow
                        //  - cd /data/data/me.hexile.odexpatcher/files/
                        //  - su dex2oat base.apk
                        viewModel.addLog("[I] Running dex2oat…")
                        val shellResult = Shell.su(
                            "cd ${App.context.getFileInFilesDir("").absolutePath} && dex2oat64 --dex-file=${Const.BASE_APK_FILE_NAME} --oat-file=${
                                App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
                            }"
                        ).exec()
                        if (!shellResult.isSuccess) {
                            viewModel.addLog("[E] ERROR: dex2oat exit code was ${shellResult.code}.")
                            viewModel.state.postValue(true)
                            return@launch
                        }
                        viewModel.addLog(" Done!", false)
                    }
                }

                // Fix permissions
                val appUid = (App.context.packageManager.getApplicationInfo(App.context.packageName, 0).uid % 100000)
                Shell.su("chown $appUid:$appUid ${App.context.getFileInFilesDir("*")}").exec()
                Shell.su("chmod 600 ${App.context.getFileInFilesDir("*")}").exec()
                if (SELinux.isEnabled()) {
                    Shell.su("chcon u:object_r:app_data_file:s0 ${App.context.getFileInFilesDir("*")}").exec()
                }

                // Patch files
                viewModel.addLog("[I] Patching oat file…")
                try {
                    OatFile(App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME)).patch(
                        targetClasses
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModel.addLog("[E] ERROR: ${e.message}")
                    viewModel.state.postValue(true)
                    return@launch
                }
                viewModel.addLog(" Done!", false)

                if (isSdkGreaterThan(Build.VERSION_CODES.O)) {
                    viewModel.addLog("[I] Patching vdex file…")
                    try {
                        VdexFile(App.context.getFileInFilesDir(Const.BASE_VDEX_FILE_NAME)).patch(
                            targetClasses
                        )
                    } catch (e: Exception) {
                        viewModel.addLog("[E] ERROR: ${e.message}")
                        viewModel.state.postValue(true)
                        return@launch
                    }
                    viewModel.addLog(" Done!", false)
                }

                // Create folder if non-existent
                if (!File(Art.getOatFolder(targetApk)).isDirectory && isSdkGreaterThan(Build.VERSION_CODES.M)) {
                    // https://cs.android.com/android/platform/superproject/+/master:frameworks/native/cmds/installd/InstalldNativeService.cpp;l=2591
                    Shell.su("mkdir -p ${Art.getOatFolder(targetApk)} && chown -R system:install ${baseFolder}/oat && chmod -R 771 ${baseFolder}/oat").exec()
                }

                // Replace original files with patched ones
                viewModel.addLog("[I] Replacing odex file…")
                var shellResult = Shell.su(
                    "cp ${App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath} ${
                        Art.getOatFile(targetApk)
                    }"
                ).exec()
                if (!shellResult.isSuccess) {
                    viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
                    viewModel.state.postValue(true)
                    return@launch
                }
                viewModel.addLog(" Done!", false)

                if (isSdkGreaterThan(Build.VERSION_CODES.O)) {
                    viewModel.addLog("[I] Replacing vdex file…")
                    shellResult =
                        Shell.su(
                            "cp ${App.context.getFileInFilesDir(Const.BASE_VDEX_FILE_NAME).absolutePath} ${
                                Art.getVdexFile(
                                    targetApk
                                )
                            }"
                        ).exec()
                    if (!shellResult.isSuccess) {
                        viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
                        viewModel.state.postValue(true)
                        return@launch
                    }
                    viewModel.addLog(" Done!", false)
                }

                // Fix permissions
                if (isSdkGreaterThan(Build.VERSION_CODES.M)) {
                    val appUid = App.context.packageManager.getApplicationInfo(viewModel.targetPackage.value!!, 0).uid
                    // https://cs.android.com/android/platform/superproject/+/master:system/core/libcutils/include/private/android_filesystem_config.h
                    // AID_USER_OFFSET      = 100000
                    // AID_APP_START        = 10000
                    // AID_SHARED_GID_START = 50000
                    val gid = (appUid % 100000) - 10000 + 50000
                    // https://cs.android.com/android/platform/superproject/+/master:frameworks/native/cmds/installd/dexopt.cpp;l=816
                    //Shell.su("chown system:${appUid} ${Art.getOatFolder(targetApk)}* && chmod 644 ${Art.getOatFolder(targetApk)}*").exec()
                }

                viewModel.addLog("[I] Done!")
                viewModel.state.postValue(true)
            }
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Const.FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                binding.root.showSnackbar(
                    "There was an error. Please retry.",
                    Snackbar.LENGTH_SHORT
                )
                return
            }

            data.data?.let { uri ->
                viewModel.baseFile.value = uri.toString()
                App.context.contentResolver.query(uri, null, null, null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            viewModel.baseFile.value =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        }
                    }

                viewModel.baseFileUri.value = uri
            }
        }
    }*/


}