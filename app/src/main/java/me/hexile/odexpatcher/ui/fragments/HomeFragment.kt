package me.hexile.odexpatcher.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.*
import me.hexile.odexpatcher.BuildConfig
import me.hexile.odexpatcher.R
import me.hexile.odexpatcher.art.Art
import me.hexile.odexpatcher.art.OatFile
import me.hexile.odexpatcher.art.VdexFile
import me.hexile.odexpatcher.core.App
import me.hexile.odexpatcher.core.BaseFragment
import me.hexile.odexpatcher.core.Const
import me.hexile.odexpatcher.databinding.FragmentHomeBinding
import me.hexile.odexpatcher.utils.*
import me.hexile.odexpatcher.viewmodels.MainActivityViewModel
import java.io.File
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class HomeFragment : BaseFragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    private lateinit var binding: FragmentHomeBinding

    private val viewModel by activityViewModels<MainActivityViewModel>()

    private fun requestStoragePermission() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            Const.Permission.STORAGE
        )
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
                        Snackbar.LENGTH_SHORT,
                        "Settings"
                    ) {
                        openAppSettings(0)
                    }
                }
            }
        }
    }

    private fun initObserve() {
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
    }

    suspend fun updateLog(string: String) {
        // TODO: Integrate this function with ViewModel
        binding.patchLogs.newLineOnMainThread(string)
        binding.patchLogsScroll.scrollToBottom()
    }

    override fun onStart() {
        super.onStart()
        activity.setTitle(R.string.app_name)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        initObserve()

        viewModel.logs.postValue("[I] Ready!") // FIXME: Use string resource

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

                // FIXME: Maybe chown with app's uid is better
                //  This is a temp fix because Shell.sh runs as root and idk what to do instead
                //  (https://github.com/topjohnwu/libsu/issues/42)
                App.context.getFileInFilesDir("backup.apk").createNewFile()
                App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).createNewFile()
                App.context.getFileInFilesDir(Const.BASE_VDEX_FILE_NAME).createNewFile()

                // Check for root access
                viewModel.addLog("[I] Checking for root access…")
                if (!Shell.rootAccess()) {
                    viewModel.addLog("[E] ERROR: No root access! This app won't work without it.")
                    viewModel.state.postValue(true)
                    return@launch
                }
                viewModel.addLog("Done!", false)

                val targetApk = App.context.getPackageBaseApk(viewModel.targetPackage.value!!)
                val baseFolder = File(targetApk).parentFile!!.absolutePath
                val baseApk = App.context.getFileInFilesDir(Const.BASE_APK_FILE_NAME)

                // Copy file
                viewModel.addLog("[I] Reading input file… ")
                App.context.contentResolver.openInputStream(viewModel.baseFileUri.value!!)
                    ?.let {
                        baseApk.copyInputStreamToFile(it)
                    }
                viewModel.addLog("Done!", false)

                // TODO: Add support for single dex files
                // Check if input file is zip
                try {
                    ZipFile(baseApk)
                } catch (e: ZipException) {
                    viewModel.addLog("[E] ERROR: Input file is not a zip.")
                    viewModel.state.postValue(true)
                    return@launch
                }

                // Extract dex files checksums
                val sourceClasses = extractClassesDex(baseApk)
                val targetClasses = extractClassesDex(targetApk)

                // Check if same number of classes.dex in APK
                if (sourceClasses.size != targetClasses.size) {
                    viewModel.addLog("[E] ERROR: Input file has a different amount of classes.dex files.")
                    viewModel.state.postValue(true)
                    return@launch
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
                            "cd ${App.context.getFileInFilesDir(".").absolutePath} && dex2oat --dex-file=${Const.BASE_APK_FILE_NAME} --oat-file=${
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
                            "cd ${App.context.getFileInFilesDir(".").absolutePath} && dex2oat --dex-file=${Const.BASE_APK_FILE_NAME} --oat-file=${
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
                var shellResult = Shell.su("stat -c '%u' ${App.context.getFileInFilesDir(Const.BASE_APK_FILE_NAME)}").exec()
                val uid = shellResult.out[0]
                Shell.su("chown $uid:$uid ${App.context.getFileInFilesDir("*")}").exec()

                // Patch files
                viewModel.addLog("[I] Patching oat file…")
                try {
                    OatFile(App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME)).patch(
                        targetClasses
                    )
                } catch (e: Exception) {
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

                // Replace original files with patched ones
                viewModel.addLog("[I] Replacing odex file…")
                shellResult = Shell.su(
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
    }
}