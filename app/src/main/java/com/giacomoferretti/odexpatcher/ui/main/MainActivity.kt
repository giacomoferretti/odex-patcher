package com.giacomoferretti.odexpatcher.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.giacomoferretti.odexpatcher.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel.debugInit(
            packageManager,
            "com.giacomoferretti.odexpatcher.example.simple.normal",
            "com.giacomoferretti.odexpatcher.example.simple.patched"
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.content.uiState = state

                    // Manage messages
                    state.userMessages.also {
                        if (it.isNotEmpty()) {
                            val message = it[0]

                            Snackbar.make(binding.root, message.message, Snackbar.LENGTH_SHORT)
                                .show()
                            viewModel.setMessageShown(message.id)
                        }
                    }
                }
            }
        }

        binding.content.patchButton.setOnClickListener {
            viewModel.patch()
            viewModel.showRandomMessage()
        }


        /*lifecycleScope.launch(Dispatchers.Default) {
            val normalApp = AppInfo.fromPackageName(
                packageManager,
                "com.giacomoferretti.odexpatcher.example.simple.normal"
            )
            val patchedApp = AppInfo.fromPackageName(
                packageManager,
                "com.giacomoferretti.odexpatcher.example.simple.patched"
            )

            withContext(Dispatchers.Main) {
                binding.content.inputApp.appInfo = normalApp
                binding.content.outputApp.appInfo = patchedApp
            }
        }

        binding.content.patchButton.setOnClickListener { button ->
            button.isEnabled = false
            binding.content.progressBar.isIndeterminate = true
            binding.content.progressText.text = "Patching..."

            lifecycleScope.launch {
                delay(1000)

                withContext(Dispatchers.Main) {
                    button.isEnabled = true
                    binding.content.progressBar.isIndeterminate = false
                    binding.content.progressBar.progress = 100
                    binding.content.progressText.text = "Patched!"
                }
            }
        }*/
    }
}