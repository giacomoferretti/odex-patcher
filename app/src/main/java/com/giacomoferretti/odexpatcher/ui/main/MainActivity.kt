package com.giacomoferretti.odexpatcher.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.giacomoferretti.odexpatcher.databinding.ActivityMainBinding
import com.giacomoferretti.odexpatcher.utils.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        lifecycleScope.launch(Dispatchers.Default) {
            val normalApp = AppInfo.fromPackageName(
                packageManager,
                "com.giacomoferretti.odexpatcher.example.nativelib.normal"
            )
            val patchedApp = AppInfo.fromPackageName(
                packageManager,
                "com.giacomoferretti.odexpatcher.example.nativelib.patched"
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
        }
    }
}