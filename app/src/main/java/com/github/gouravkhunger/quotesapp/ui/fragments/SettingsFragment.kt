
package com.github.gouravkhunger.quotesapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
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

            val builder = MaterialAlertDialogBuilder(it, R.style.MaterialAlertDialog_Rounded)
            builder.setView(binding.root)

            builder.setPositiveButton("Save") { dialog, _ ->
                viewModel.saveSetting(Preference.CHECK_FOR_UPDATES, binding.checkForUpdates.isChecked)
                dialog.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
