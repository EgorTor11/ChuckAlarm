package com.example.chuckalarm

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.parseIntent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import okhttp3.*
import java.io.IOException
import java.util.*
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.view.WindowManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity


class MyReceiver : BroadcastReceiver() {
    val CHANNEL_ID = "CHANNEL_ID"



    override fun onReceive(context: Context, intent: Intent) {

        var intent2 = Intent(context, TestActivity::class.java)
        Log.d("MyLog", "Intent Befor")
     //   startActivity(intent2)


        Toast.makeText(context, "Обнаружен новый сигнал", Toast.LENGTH_LONG).show()
        var intent1 = Intent(context, MainActivity::class.java)

        intent2.setFlags(FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_IMMUTABLE)
        var builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setWhen(System.currentTimeMillis())
            .setContentTitle("textTitle")
            .setContentText("textContent")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }






    }



    fun wakeDevice(context: Context) {
        //Создаём Power manager
        var powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        //Создаём WakeLock
        var myWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            "MyApp:NotificationWakelockTag")
        //Указываем длительность работы (В данном случае 5 секунд)
        // myWakeLock.acquire(5 * 1000L)
        //Запускаем WakeLock
        if (myWakeLock.isHeld())
            myWakeLock.release()
    }


}