package com.github.gouravkhunger.quotesapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.drawToBitmap
import com.github.gouravkhunger.quotesapp.R
import java.lang.Exception

object ShareUtils {
    fun share(view: ViewGroup, context: Context) {
        view.setBackgroundResource(R.drawable.activity_background)

        val bmp = view.drawToBitmap()
        val bmpPath = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bmp,
            "quote",
            null
        )
        val uri = Uri.parse(bmpPath)

        // Sharing
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpg"

        try {
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "Shared from QuotesApp built by Gourav Khunger.\n\n" +
                    "Download the app now: " +
                    context.getString(R.string.download_url)
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to share!\nPlease try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
        context.startActivity(Intent.createChooser(intent, "Share via:"))

        view.background = null
        bmp.recycle()
    }
}
