package com.example.chuckalarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var liveDataBulED = MutableLiveData<Boolean>()
    var liveDataBulSD = MutableLiveData<Boolean>()
    var liveDataIsTpOk = MutableLiveData<Boolean>()
    var ldInterval = MutableLiveData<Long>()


    val PREFERENCES_Key_AlarmTimeCalendarTimeInMillis = "AlarmTimeCalendarTimeInMillis"
    val PREFERENCES_Key_AlarmTimeCalendarTime = "AlarmTimeCalendarTime"
    val PREFERENCES_Key_IsAlarmActual = "IsAlarmActual"
    val PREFERENCES_Key_IsUserClick = "IsUserClick"


    var format = SimpleDateFormat("HH:mm")
    lateinit var countDownTimer: CountDownTimer
    var calendar = Calendar.getInstance()
    var materialTimepicker: MaterialTimePicker? = null

    fun materialTimepickerBuild() {
        materialTimepicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(0)
            .setMinute(0)
            .setTitleText("Выберите время для будильника")
            .build()
    }

    fun setCalendarFromTP() {
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.MINUTE, materialTimepicker!!.minute)
        calendar.set(Calendar.HOUR_OF_DAY, materialTimepicker!!.hour)
    }

    fun alarmOn(
        alarmManagerMain: AlarmManager,
        prefKeyTimeInMillis: String,
        prefCalendar: SharedPreferences,
        editor: SharedPreferences.Editor,
        alarmActionPendingIntent: PendingIntent,
        alarmInfoPendingIntent: PendingIntent,
        alarmActionRepeatPendingIntent: PendingIntent,
        prefKeyIsActual: String,
    ) {
        var alarmClockInfo =
            AlarmManager.AlarmClockInfo(prefCalendar.getLong(prefKeyTimeInMillis, 0),
                alarmInfoPendingIntent)
        alarmManagerMain.setAlarmClock(alarmClockInfo, alarmActionPendingIntent)
        alarmManagerMain.setRepeating(AlarmManager.RTC_WAKEUP,
            prefCalendar.getLong(prefKeyTimeInMillis, 0) + 5 * 60 * 1000,
            5 * 60 * 1000,
            alarmActionRepeatPendingIntent)
        editor.putBoolean(prefKeyIsActual, true)
        //editor.putBoolean(PREFERENCES_Key_IsAlarmActualRep, true)
        editor.apply()
    }

    fun mAlarmOn(
        alarmManagerMain: AlarmManager,
        prefCalendar: SharedPreferences,
        editor: SharedPreferences.Editor,
        context: Context,
    ) {

        alarmOn(alarmManagerMain,
            PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
            prefCalendar,
            editor,
            getAlarmActionPendingIntent(context),
            getAlarmInfoPendingIntent(context),
            getAlarmActionRepeatPendingIntent(context),
            PREFERENCES_Key_IsAlarmActual)

        startTimer(((prefCalendar.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
            0) - System.currentTimeMillis())))
        Log.d("stopTimer", "mAlarmOn IsAlarmAktuale= ${prefCalendar.getBoolean(PREFERENCES_Key_IsAlarmActual,false)}   ")
    }

    fun alarmOff(
        alarmManagerMain: AlarmManager,
        editor: SharedPreferences.Editor,
        alarmActionPendingIntent: PendingIntent,
        alarmInfoPendingIntent: PendingIntent,
        alarmActionRepeatPendingIntent: PendingIntent,
        prefKeyIsActual: String,
    ) {
        alarmManagerMain.cancel(alarmActionPendingIntent)
        // alarmManagerMain.cancel(getAlarmActionReceiverPendingIntent())
        alarmManagerMain.cancel(alarmActionRepeatPendingIntent)
        editor.putBoolean(prefKeyIsActual, false)
        // editor.putBoolean(PREFERENCES_Key_IsAlarmActualRep, false)
        editor.apply()
    }

    fun mAlarmOff(
        alarmManagerMain: AlarmManager,
        editor: SharedPreferences.Editor,
        context: Context, prefCalendar: SharedPreferences,
    ) {
        stopTimer(prefCalendar)
        alarmOff(alarmManagerMain,
            editor,
            getAlarmActionPendingIntent(context),
            getAlarmInfoPendingIntent(context),
            getAlarmActionRepeatPendingIntent(context),
            PREFERENCES_Key_IsAlarmActual)
Log.d("stopTimer","mAlarmOff IsAlarmAktuale= ${prefCalendar.getBoolean(PREFERENCES_Key_IsAlarmActual,false)}   ")
    }

    fun mAlarmShouldBeTomorrowCheck(
        editor: SharedPreferences.Editor,
        prefKeyTimeInMillis: String,
        prefCalendar: SharedPreferences,
    ) {
        if (prefCalendar.getLong(prefKeyTimeInMillis, 0) < System.currentTimeMillis()) {
            calendar.add(Calendar.MILLISECOND, 24 * 3600 * 1000)
            editor.putLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
                calendar.timeInMillis)
            editor.putString(PREFERENCES_Key_AlarmTimeCalendarTime,
                format.format(calendar.time))
            editor.apply()
        }
    }

    fun editPrefFromCalendar(editor: SharedPreferences.Editor, calendar: Calendar) {
        editor.putLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis, calendar.timeInMillis)
        editor.putString(PREFERENCES_Key_AlarmTimeCalendarTime,
            format.format(calendar.time))
        editor.apply()
    }
/////////////// Главная фигня

    fun alarmBuild(
        fragmentManager: FragmentManager,

        editor: SharedPreferences.Editor,
        prefCalendar: SharedPreferences,
        alarmManagerMain: AlarmManager, context: Context,
    ) {

        liveDataIsTpOk.value = false
        materialTimepickerBuild()
        materialTimepicker?.addOnPositiveButtonClickListener {
            stopTimer(prefCalendar)
            setCalendarFromTP()
            editPrefFromCalendar(editor, calendar)
            mAlarmShouldBeTomorrowCheck(editor,
                PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
                prefCalendar!!)
            mAlarmOn(alarmManagerMain, prefCalendar, editor, context) //вкл.основной будильник

            editor.putBoolean(PREFERENCES_Key_IsUserClick, false)
            editor.apply()
            liveDataIsTpOk.value = true
        }
        materialTimepicker?.show(fragmentManager, "tag_picker")

    }

    fun getAlarmActionPendingIntent(context: Context): PendingIntent {
        var intent = Intent(context, AlarmActivity::class.java)     //
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    }

    fun getAlarmActionRepeatPendingIntent(context: Context): PendingIntent {
        var intent = Intent(context, AlarmActivity::class.java)     //
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun getAlarmInfoPendingIntent(context: Context): PendingIntent {
        var alarmInfoIntent = Intent(context, MainActivity::class.java)
        alarmInfoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        alarmInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(context,
            0,
            alarmInfoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun startTimer(timeSecond: Long) {
        countDownTimer = object : CountDownTimer(timeSecond, 1) {

            override fun onTick(millisUntilFinished: Long) {
                ldInterval.value = (millisUntilFinished)
            }

            override fun onFinish() {}
        }.start()
    }

    fun stopTimer(prefCalendar: SharedPreferences) {
        if (prefCalendar.getBoolean(PREFERENCES_Key_IsAlarmActual, false)) {
            countDownTimer.cancel()
            Log.d("stopTimer", "stopTimer. ")
        }
    }

}