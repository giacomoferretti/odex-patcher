package me.hexile.odexpatcher.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.hexile.odexpatcher.R
import me.hexile.odexpatcher.art.Art

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)

        // Check if device is running with ART
        if (!Art.isRuntimeArt()) {
            val changeToArtDialog = MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.change_to_art))
                .setView(R.layout.dialog_art_development_settings)
                .setNegativeButton(getString(R.string.exit)) { _, _ ->
                    finish()
                }
                .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                    startActivity(intent)
                    finish()
                }

            MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.art_warning_body))
                .setNegativeButton(getString(R.string.exit)) { _, _ ->
                    finish()
                }
                .setPositiveButton(getString(R.string.change_to_art)) { _, _ ->
                    changeToArtDialog.show()
                }
                .show()
        } else {
            startMainActivity()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}