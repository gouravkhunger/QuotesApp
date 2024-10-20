package com.github.gouravkhunger.quotesapp.workers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.gouravkhunger.quotesapp.R
import com.github.gouravkhunger.quotesapp.repository.QuoteRepository
import com.github.gouravkhunger.quotesapp.ui.QuotesActivity
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyQuoteWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Injector {
        fun getRepository(): QuoteRepository
    }

    private val injector = EntryPoints.get(context, Injector::class.java)

    // get the repository
    private val quoteRepository = injector.getRepository()

    override fun doWork(): Result {

        // check if notification permission is granted
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) CoroutineScope(Dispatchers.IO).launch {

            // get quote of the day from the api
            val result = quoteRepository.getQuoteOfTheDay()

            if (result.isSuccessful) {
                // generate a notification intent
                val notificationIntent = Intent(context, QuotesActivity::class.java)
                    .setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )

                // generate pending intent from the intent
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                // setup notification builder
                val builder = NotificationCompat.Builder(
                    context,
                    context.getString(R.string.daily_notif_id)
                )
                    .setSmallIcon(R.drawable.ic_motivation)
                    .setContentTitle(context.getString(R.string.notif_title))
                    .setContentText(
                        result.body()!![0].quote
                    )
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(
                                // add author info in the expanded notification
                                result.body()!![0].quote + "\n\n- ${result.body()!![0].author}"
                            )
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    )
                    .setAutoCancel(true)

                // send the notification
                val managerCompat = NotificationManagerCompat.from(context)
                managerCompat.notify(420, builder.build())
            }
        }

        // who cares even if the request failed, nvm
        return Result.success()
    }
}