package com.fcenesiz.notification_show_time

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class CustomNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        // Servici durdur
        context?.startService(
            Intent(
                context,
                CustomNotificationService::class.java)
                    .setAction(
                        CustomNotificationService.ACTION_STOP
                    )
        )
    }


}