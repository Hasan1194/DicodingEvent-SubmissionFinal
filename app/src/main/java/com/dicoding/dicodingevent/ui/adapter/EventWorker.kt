@file:Suppress("unused", "RedundantSuppression")

package com.dicoding.dicodingevent.ui.adapter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.dicodingevent.BuildConfig
import com.dicoding.dicodingevent.R
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.SyncHttpClient
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.util.concurrent.CountDownLatch

class EventWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        private val TAG = EventWorker::class.java.simpleName
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "channel_01"
        const val CHANNEL_NAME = "dicoding channel"
    }

    private var resultStatus: Result? = null

    override fun doWork(): Result {
        return getCurrentEvent()
    }

    private fun getCurrentEvent(): Result {
        Looper.prepare()
        val client = SyncHttpClient()
        val baseUrl = BuildConfig.BASE_URL
        val url = "${baseUrl}events?active=1"
        Log.d(TAG, "getCurrentEvent: $url")

        val latch = CountDownLatch(1)
        var resultStatus: Result = Result.failure()

        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header?>?,
                responseBody: ByteArray
            ) {
                val result = String(responseBody)
                Log.d(TAG, result)
                try {
                    val responseObject = JSONObject(result)
                    val eventsArray = responseObject.getJSONArray("listEvents")

                    val futureEvents = mutableListOf<JSONObject>()
                    for (i in 0 until eventsArray.length()) {
                        val event = eventsArray.getJSONObject(i)
                        val date = event.getString("beginTime")
                        val eventDateMillis = parseDateToMillis(date)

                        if (eventDateMillis > System.currentTimeMillis()) {
                            futureEvents.add(event)
                        }
                    }

                    val closestEvent = futureEvents
                        .sortedBy { parseDateToMillis(it.getString("beginTime")) }
                        .take(1)
                        .firstOrNull()

                    if (closestEvent != null) {
                        val name: String = closestEvent.getString("name")
                        val date: String = closestEvent.getString("beginTime")
                        val title = "Upcoming Event: $name"
                        val time = "Held on: $date"
                        showNotification(title, time)
                        Log.d(TAG, "onSuccess: Found closest event.....")
                        resultStatus = Result.success()
                    } else {
                        showNotification(
                            "No Upcoming Events",
                            "There are no upcoming events at this time."
                        )
                        resultStatus = Result.failure()
                    }
                } catch (e: Exception) {
                    showNotification("Get Current Event Not Success", e.message)
                    Log.d(TAG, "onSuccess: Failed.....")
                    resultStatus = Result.failure()
                }
                latch.countDown()
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header?>?,
                responseBody: ByteArray?,
                error: Throwable
            ) {
                Log.d(TAG, "onFailure: Failed.....")
                showNotification("Get Current Event Failed", error.message)
                resultStatus = Result.failure()
                latch.countDown()
            }
        })

        latch.await()
        return resultStatus
    }


    private fun parseDateToMillis(dateString: String): Long {
        val dateFormat =
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateString)
            date?.time ?: Long.MAX_VALUE
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    private fun showNotification(title: String, summary: String?) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle(title)
                .setContentText(summary)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(summary)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notification.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }
}
