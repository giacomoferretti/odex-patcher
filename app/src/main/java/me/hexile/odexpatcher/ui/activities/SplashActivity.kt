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
        // TODO: Add support for force dark mode
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