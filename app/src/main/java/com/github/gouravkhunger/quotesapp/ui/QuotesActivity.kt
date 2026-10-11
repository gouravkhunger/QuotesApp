/*
 * MIT License
 *
 * Copyright (c) 2021 Gourav Khunger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.gouravkhunger.quotesapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import com.github.gouravkhunger.quotesapp.R
import com.github.gouravkhunger.quotesapp.databinding.ActivityQuotesBinding
import com.github.gouravkhunger.quotesapp.store.Preference
import com.github.gouravkhunger.quotesapp.ui.fragments.QuoteFragmentDirections
import com.github.gouravkhunger.quotesapp.ui.fragments.SettingsFragment
import com.github.gouravkhunger.quotesapp.viewmodels.QuoteViewModel
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.AppUpdaterUtils.UpdateListener
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.Update
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuotesActivity : AppCompatActivity() {

    // variables
    var atHome = true
    private lateinit var binding: ActivityQuotesBinding
    private val viewModel by viewModels<QuoteViewModel>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_QuotesApp)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityQuotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(bars.left, bars.top, bars.right, bars.bottom)
            windowInsets
        }

        val navController = (
            supportFragmentManager.findFragmentById(R.id.quotesNavHostFragment) as NavHostFragment
        ).navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            atHome = destination.id == R.id.quoteFragment
            binding.settingsBtn.visibility = if (atHome) View.VISIBLE else View.GONE
            binding.myBookmarksImgBtn.visibility = if (atHome) View.VISIBLE else View.GONE
            binding.backToQuotePage.visibility = if (atHome) View.GONE else View.VISIBLE
            binding.activityTitle.setText(if (atHome) R.string.app_name else R.string.myBookMarks)
        }

        binding.myBookmarksImgBtn.setOnClickListener {
            navController.navigate(
                QuoteFragmentDirections.actionQuoteFragmentToBookmarkFragment()
            )
        }

        binding.backToQuotePage.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.settingsBtn.setOnClickListener {
            SettingsFragment().show(supportFragmentManager, SettingsFragment.TAG)
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            viewModel.saveSetting(Preference.ASK_NOTIF_PERM, false)

            val message = if (isGranted) "Notifications set up successfully!"
                else  "Daily quotes won't work! Please set up notifications in settings."
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        }

        Log.d("QuotesActivity", "onCreate: ${viewModel.getSetting(Preference.ASK_NOTIF_PERM)}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && viewModel.getSetting(Preference.ASK_NOTIF_PERM)) {
            if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (!viewModel.getSetting(Preference.CHECK_FOR_UPDATES)) return

        val appUpdaterUtils = AppUpdaterUtils(this)
            .setUpdateFrom(UpdateFrom.GITHUB)
            .setGitHubUserAndRepo("gouravkhunger", "QuotesApp")
            .withListener(object : UpdateListener {
                override fun onSuccess(update: Update, isUpdateAvailable: Boolean) {
                    if (!isUpdateAvailable) return

                    Snackbar.make(
                        findViewById(R.id.flFragment),
                        getString(R.string.update_text, update.latestVersion),
                        Snackbar.LENGTH_LONG
                    ).setAction(R.string.download) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.download_url))
                            )
                        )
                    }.show()
                }

                override fun onFailed(error: AppUpdaterError) {
                    // ignore
                }
            })
        appUpdaterUtils.start()
    }
}
