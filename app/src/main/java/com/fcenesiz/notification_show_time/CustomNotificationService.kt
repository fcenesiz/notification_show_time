package com.fcenesiz.notification_show_time

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.SyncStateContract.Constants
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CustomNotificationService : Service() {


    companion object {
        const val CHANNEL_ID = "time_channel"
        const val CLICK_ACTION = "click_action"
        const val ACTION_START = "action.start"
        const val ACTION_STOP = "action.stop"
    }

    private var running = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START){
            running = true
            loop()
        }else if (intent?.action == ACTION_STOP){
            running = false
           stopSelf()
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // 45 saniyede bir güncelle
    @OptIn(DelicateCoroutinesApi::class)
    private fun loop(){
        // servis main thread'i kullanır
        // ANR oluşturma ihtimalinden dolayı kullanıldı
        GlobalScope.launch(Dispatchers.IO) {
            while (running){
                val sdf =  SimpleDateFormat("hh:mm", Locale.US)
                val currentDate = sdf.format(Date())
                startOrUpdateForeground(currentDate)

                delay(45000)
            }
        }
    }

    private fun startOrUpdateForeground(time: String) {

        // bildirime tıklandığında açılacak activity
        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // kişiselleştirilmiş görünüm

        val stopIntent = Intent(this, CustomNotificationReceiver::class.java)
        val pendingStopIntent = PendingIntent.getBroadcast(
            this@CustomNotificationService,
            2,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationLayout = RemoteViews(packageName, R.layout.custom_notification_view)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.custom_notification_view)

        notificationLayout.setTextViewText(R.id.tvClockTime, time)
        notificationLayout.setOnClickPendingIntent(R.id.btnStopService, pendingStopIntent)
        notificationLayoutExpanded.setTextViewText(R.id.tvClockTime, time)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.btnStopService, pendingStopIntent)


        // bildirim
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true) // her cihaz için geçerli değil
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.baseline_access_time_24)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .setContentIntent(activityPendingIntent)
            .build()

        // bildirimi başlat veya güncelle
        startForeground(1, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

}