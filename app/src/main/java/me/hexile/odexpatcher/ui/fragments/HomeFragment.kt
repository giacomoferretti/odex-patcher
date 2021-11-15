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
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.*
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

    private val saveLogLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.saveLog()
            } else {
                if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // First time user denied permission
                    binding.root.showSnackbar(
                        getString(R.string.notice_app_wont_work_permission),
                        Snackbar.LENGTH_SHORT
                    )
                } else {
                    // Never ask again
                    binding.root.showSnackbar(
                        getString(R.string.notice_app_wont_work_permission),
                        Snackbar.LENGTH_SHORT,
                        getString(R.string.settings)
                    ) {
                        openAppSettings(0)
                    }
                }
            }
        }

    private val chooseFileLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openDocumentLauncher.launch(
                    arrayOf(
                        "application/zip",
                        "application/vnd.android.package-archive",
                        "application/octet-stream",
                        "application/x-binary"
                    )
                )
            } else {
                if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // First time user denied permission
                    binding.root.showSnackbar(
                        getString(R.string.notice_app_wont_work_permission),
                        Snackbar.LENGTH_SHORT
                    )
                } else {
                    // Never ask again
                    binding.root.showSnackbar(
                        getString(R.string.notice_app_wont_work_permission),
                        Snackbar.LENGTH_SHORT,
                        getString(R.string.settings)
                    ) {
                        openAppSettings(0)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                chooseFileLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                openDocumentLauncher.launch(
                    arrayOf(
                        "application/zip",
                        "application/vnd.android.package-archive",
                        "application/octet-stream",
                        "application/x-binary"
                    )
                )
            }
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
                        binding.root.showSnackbar(
                            event.path,
                            Snackbar.LENGTH_LONG,
                            getString(R.string.open)
                        ) {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, event.data)
                                type = "text/plain"
                            }

                            val shareIntent = Intent.createChooser(
                                sendIntent,
                                getString(R.string.chooser_title_share_log)
                            )
                            startActivity(shareIntent)
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

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reportAction -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    saveLogLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    viewModel.saveLog()
                }
                true
            }
            R.id.licensesAction -> {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}