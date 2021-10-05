package com.github.gouravkhunger.quotesapp.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ShareUtil {
    fun share(
        view: View,
        context: Context,
        onShared: () -> Unit
        ) {

        val bitmap: Bitmap = view.drawToBitmap()
        var imageUri: Uri? = null
        val file: File?
        var fos1: FileOutputStream? = null
        try {
            val folder =
                File(context.cacheDir.toString() + File.separator.toString() + "My Temp Files")
            val filename = "img.jpg"
            file = File(folder.path, filename)
            fos1 = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos1)
            imageUri = FileProvider.getUriForFile(
                context,
                context.packageName.toString() + ".com.github.gouravkhunger.quotesapp.provider",
                file
            )
        } catch (ex: Exception) {
        } finally {
            try {
                fos1?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Intent.EXTRA_STREAM, imageUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/jpg"
        context.startActivity(Intent.createChooser(intent, "Send Quote Using"))
        onShared()
    }

}