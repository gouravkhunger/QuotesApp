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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // logic to switch between fragments
        binding.myBookmarksImgBtn.setOnClickListener {
            val action = QuoteFragmentDirections
                .actionQuoteFragmentToBookmarkFragment()
            val navController = Navigation.findNavController(binding.quotesNavHostFragment)

            navController.navigate(action)
            it.visibility = View.GONE

            with(binding) {
                settingsBtn.visibility = View.GONE
                backToQuotePage.visibility = View.VISIBLE
                activityTitle.text = resources.getText(R.string.myBookMarks)
            }

            atHome = false
        }

        binding.backToQuotePage.setOnClickListener {
            super.onBackPressed()
            it.visibility = View.GONE

            with(binding) {
                settingsBtn.visibility = View.VISIBLE
                myBookmarksImgBtn.visibility = View.VISIBLE
                activityTitle.text = resources.getText(R.string.app_name)
            }

            atHome = true
        }

        binding.settingsBtn.setOnClickListener {
            SettingsFragment().show(supportFragmentManager, SettingsFragment.TAG)
        }

        // Update theme once everything is set up
        setTheme(R.style.Theme_QuotesApp)

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

    // implementation to handle error cases regarding navigation icons
    // this function updates the icons and sets variables according
    // to how navigation was carried out
    override fun onBackPressed() {
        super.onBackPressed()
        if (!atHome) {
            with(binding) {
                settingsBtn.visibility = View.VISIBLE
                backToQuotePage.visibility = View.GONE
                myBookmarksImgBtn.visibility = View.VISIBLE
                activityTitle.text = resources.getText(R.string.app_name)
            }

            atHome = true
        }
    }
}
