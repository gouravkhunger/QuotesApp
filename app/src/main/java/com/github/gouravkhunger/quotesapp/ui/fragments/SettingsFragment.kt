/*
 * MIT License
 *
 * Copyright (c) 2024 Gourav Khunger
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

package com.github.gouravkhunger.quotesapp.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.github.gouravkhunger.quotesapp.R
import com.github.gouravkhunger.quotesapp.databinding.FragmentSettingsBinding
import com.github.gouravkhunger.quotesapp.store.Preference
import com.github.gouravkhunger.quotesapp.viewmodels.QuoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : DialogFragment() {
    companion object {
        const val TAG = "SettingsFragment"
    }

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel by activityViewModels<QuoteViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val inflater = requireActivity().layoutInflater
            binding = FragmentSettingsBinding.inflate(inflater)
            binding.checkForUpdates.isChecked = viewModel.getSetting(Preference.CHECK_FOR_UPDATES)

            binding.manageNotification.setOnClickListener {
                openNotificationSettings()
            }

            val builder = MaterialAlertDialogBuilder(it, R.style.MaterialAlertDialog_Rounded)
            builder.setView(binding.root)

            builder.setPositiveButton("Save") { dialog, _ ->
                viewModel.saveSetting(Preference.CHECK_FOR_UPDATES, binding.checkForUpdates.isChecked)
                dialog.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
            )
        } else {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.fromParts("package", requireContext().packageName, null)
            })
        }
    }
}

