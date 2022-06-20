package com.example.chuckalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.view.Window
import android.widget.Toast
import com.example.chuckalarm.databinding.ActivityAlarmBinding
import com.example.chuckalarm.databinding.ActivityMainBinding
import okhttp3.*
import java.io.IOException
import java.util.*
import android.view.WindowManager
import androidx.annotation.RequiresApi


class AlarmActivity : AppCompatActivity() {
    val APP_PREFERENCES = "prefCalendar"
    var prefCalendar: SharedPreferences? = null
    val PREFERENCES_Key_IsAlarmActual = "IsAlarmActual"
    val PREFERENCES_Key_IsAlarmActualSD = "IsAlarmActualSD"
    val PREFERENCES_Key_IsAlarmActualRep = "IsAlarmActualRep"
    val PREFERENCES_Key_IsAlarmActual2 = "IsAlarmActual2"
    private val enginePackageName = "com.google.android.tts"  //"com.svox.pico"//"iSpeech"

    lateinit var binding: ActivityAlarmBinding
    lateinit var tts: TextToSpeech
    lateinit var ringtone: Ringtone

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefCalendar = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        val editor: SharedPreferences.Editor = prefCalendar!!.edit()
        editor.putBoolean(PREFERENCES_Key_IsAlarmActual,true)
        editor.apply()
        setShowWhenLocked(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        var alarmManagerCancele = getSystemService(Context.ALARM_SERVICE)
        alarmManagerCancele as AlarmManager
        binding.toggleButton.setOnClickListener {
            editor.putBoolean(PREFERENCES_Key_IsAlarmActual,false)
            editor.putBoolean(PREFERENCES_Key_IsAlarmActualSD,false)
           // editor.putBoolean(PREFERENCES_Key_IsAlarmActualRep,false)
            editor.apply()
            alarmManagerCancele.cancel(getAlarmActionRepPendingIntent())
            alarmManagerCancele.cancel(getAlarmActionPendingIntent())
            alarmManagerCancele.cancel(getAlarmActionPendingIntentSD(this))
            alarmManagerCancele.cancel(getAlarmActionRepeatPendingIntentSD(this))

            finish()
        }

        Thread {
            val request = Request.Builder()
                .url("https://api.chucknorris.io/jokes/random")
                .get().build()
            val client = OkHttpClient.Builder().build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body().use { responseBody ->
                        var test = responseBody!!.string()
                        var txt = test.substringAfter("value")
                        var txt1 = txt.substringAfter(":\"")
                        var txt2 = txt1.substringBefore("\"}")
                        Log.d("MyLog",
                            test)
                        runOnUiThread() {
                            Toast.makeText(this@AlarmActivity, txt2, Toast.LENGTH_SHORT).show()
                            tts = TextToSpeech(this@AlarmActivity) { status ->
                                if (status == TextToSpeech.SUCCESS) {
                                    tts.setEngineByPackageName(enginePackageName)
                                    // tts.language = Locale.UK
                                    Log.d("msg", (tts.voices.toString()).substringAfter("ru")

                                    )
                                    speak("Давай вставай собака сутулая")//txt2
                                }
                            }
                        }

                    }
                }
            })
        }.start()


        var notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(this, notificationUri)

        if (ringtone == null) {
            notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(this, notificationUri)
        }
        if (ringtone != null) {
            ringtone.play()
        }
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone.stop()
        tts.stop()
        tts.shutdown()
    }

    private fun speak(SAMPLE_TEXT: String) {
        val localeRu = Locale("ru")
        tts.setLanguage(localeRu)

        //tts.voice="en-us-x-sfg#male_1-local"
        val a: MutableSet<String> = HashSet()
        a.add("male")
        // val v = Voice("en-us-x-sfg#male_2-local", Locale("en", "US"), 400, 200, true, a)
        //val vv = Voice("en-us-x-sfg#male_3-local", Locale("en", "US"), 400, 200, true, a)
     //   tts.setVoice(vv)
        tts.setPitch(0.8f)
        tts.setSpeechRate(0.8f)

        tts.speak(SAMPLE_TEXT, TextToSpeech.QUEUE_FLUSH, null)
    }

    fun getAlarmActionRepPendingIntent(): PendingIntent {
        var intent = Intent(this, AlarmActivity::class.java)     //
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun getAlarmInfoRepPendingIntent(): PendingIntent {
        var alarmInfoIntent = Intent(this, MainActivity::class.java)
        alarmInfoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        alarmInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(this,
            11,
            alarmInfoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }
    fun getAlarmActionPendingIntent(): PendingIntent {
        var intent = Intent(this, AlarmActivity::class.java)     //
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    }
    fun getAlarmActionPendingIntentSD(context: Context): PendingIntent {
        var intent = Intent(context, AlarmActivity::class.java)     //
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(context, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    }

    fun getAlarmActionRepeatPendingIntentSD(context: Context): PendingIntent {
        var intent = Intent(context, AlarmActivity::class.java)     //
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return PendingIntent.getActivity(context, 12, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun getAlarmInfoPendingIntentSD(context: Context): PendingIntent {
        var alarmInfoIntent = Intent(context, MainActivity::class.java)
        alarmInfoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        alarmInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(context,
            10,
            alarmInfoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }
}