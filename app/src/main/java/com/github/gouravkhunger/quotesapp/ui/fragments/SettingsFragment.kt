
package com.github.gouravkhunger.quotesapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.github.gouravkhunger.quotesapp.R
import com.github.gouravkhunger.quotesapp.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : DialogFragment() {
    companion object {
        const val TAG = "SettingsFragment"
    }

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val inflater = requireActivity().layoutInflater
            binding = FragmentSettingsBinding.inflate(inflater)

            val builder = MaterialAlertDialogBuilder(it, R.style.MaterialAlertDialog_Rounded)
            builder.setView(binding.root)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
