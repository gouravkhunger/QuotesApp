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

package com.github.gouravkhunger.quotesapp

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.*
import androidx.work.ListenableWorker.Result
import com.github.gouravkhunger.quotesapp.workers.DailyQuoteWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

// Base application class
@HiltAndroidApp
class QuotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // create notification channels for SDK >= SDK 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                this.getString(R.string.daily_notif_id),
                this.getString(R.string.daily_notif_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.description = this.getString(R.string.daily_notif_desc)

            notificationManager.createNotificationChannel(channel)
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // setup work request for daily motivational quote
        val workRequest = PeriodicWorkRequest
            .Builder(DailyQuoteWorker::class.java, 1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        // enqueue unique periodic work so it doesn't get repeated
        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                getString(R.string.daily_notif_id),
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

}
