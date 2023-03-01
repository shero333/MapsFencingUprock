package com.example.mapsfencing.notification_services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mapsfencing.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import java.util.*

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {


    @SuppressLint("ServiceCast", "DiscouragedApi")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.i("onMessageReceived: ", "called")

        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val ringtone: Ringtone = RingtoneManager.getRingtone(this, notification)
//        ringtone.play()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.isLooping = false
        }

        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(100, 300, 300, 300)
        vibrator.vibrate(pattern, -1)

        val resourceImage = resources.getIdentifier(Objects.requireNonNull(message.notification)!!.icon, "drawable", packageCodePath)

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, "com.example.mapsfencing")
        builder.setSmallIcon(R.drawable.user_location_icon)
        builder.setContentTitle(message.notification?.title)
        builder.setContentText(message.notification?.body)

        val mnotificationManager: NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelId = "com.example.mapsfencing"
            val channel = NotificationChannel(channelId, "User location changed", NotificationManager.IMPORTANCE_HIGH)
            mnotificationManager.createNotificationChannel(channel)
            builder.setChannelId(channelId)

        }
        mnotificationManager.notify(100, builder.build())
    }
}