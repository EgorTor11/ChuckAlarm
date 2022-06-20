package com.example.chuckalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class ViewModelSD: ViewModel() {
    val APP_PREFERENCES = "prefCalendar"

    var liveDataBulED = MutableLiveData<Boolean>()
    var liveDataBulSD = MutableLiveData<Boolean>()

    var ldIsActulFrSD= MutableLiveData<Boolean>()
    var liveDataIsTpOk = MutableLiveData<Boolean>()

    val PREFERENCES_Key_AlarmTimeCalendarDateSD = "AlarmTimeCalendarDateSD"
    val PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD = "AlarmTimeCalendarTimeInMillisSD"
    val PREFERENCES_Key_AlarmTimeCalendarTimeSD = "AlarmTimeCalendarTimeSD"

    val PREF_KEY_MINUT_SD="PREF_KEY_MINUT_SD"
    val PREF_KEY_HOUR_SD="PREF_KEY_HOUR_SD"
    val PREFERENCES_Key_IsAlarmActualSD = "IsAlarmActualSD"

    var format = SimpleDateFormat("HH:mm")

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

    fun setCalendarFromTP(prefCalendar: SharedPreferences,editor: SharedPreferences.Editor) {
        calendar.timeInMillis=prefCalendar.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,System.currentTimeMillis())
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.MINUTE, materialTimepicker!!.minute)
        calendar.set(Calendar.HOUR_OF_DAY, materialTimepicker!!.hour)
        editor.putString(PREFERENCES_Key_AlarmTimeCalendarTimeSD,format.format(calendar.time))
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
            PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,
            prefCalendar,
            editor,
            getAlarmActionPendingIntentSD(context),
            getAlarmInfoPendingIntentSD(context),
            getAlarmActionRepeatPendingIntentSD(context),
            PREFERENCES_Key_IsAlarmActualSD)
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
        context: Context,
    ) {
        alarmOff(alarmManagerMain,
            editor,
            getAlarmActionPendingIntentSD(context),
            getAlarmInfoPendingIntentSD(context),
            getAlarmActionRepeatPendingIntentSD(context),
            PREFERENCES_Key_IsAlarmActualSD)
    }

    fun mAlarmShouldBeTomorrowCheck(
        editor: SharedPreferences.Editor,
        prefKeyTimeInMillis: String,
        prefCalendar: SharedPreferences,
    ) {
        if (prefCalendar.getLong(prefKeyTimeInMillis, 0) < System.currentTimeMillis()) {
            calendar.add(Calendar.MILLISECOND, 24 * 3600 * 1000)
            editor.putLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,
                calendar.timeInMillis)
            editor.putString(PREFERENCES_Key_AlarmTimeCalendarTimeSD,
                format.format(calendar.time))
            editor.apply()
        }
    }

    fun editPrefFromCalendar(editor: SharedPreferences.Editor, calendar: Calendar) {
        editor.putLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD, calendar.timeInMillis)
        editor.putString(PREFERENCES_Key_AlarmTimeCalendarTimeSD,
            format.format(calendar.time))
        editor.apply()
    }
/////////////// Главная фигня

    fun alarmBuild(
        fragmentManager: FragmentManager,

        editor: SharedPreferences.Editor,
        prefCalendar: SharedPreferences,
        alarmManagerMain: AlarmManager, context: Context
    ) {
        liveDataIsTpOk.value=false
        materialTimepickerBuild()
        materialTimepicker?.addOnPositiveButtonClickListener {
            setCalendarFromTP(prefCalendar,editor)
            editPrefFromCalendar(editor, calendar)
editor.putLong(PREF_KEY_HOUR_SD, materialTimepicker?.hour?.toLong()!!)
editor.putLong(PREF_KEY_MINUT_SD, materialTimepicker?.minute?.toLong()!!)
editor.putString(PREFERENCES_Key_AlarmTimeCalendarTimeSD, format.format(calendar.time))

            // binding.tvAlarmTime.setText(mTime())
            mAlarmShouldBeTomorrowCheck(editor, PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,
                prefCalendar!!)
            alarmOn(alarmManagerMain,
                PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,
                prefCalendar!!,
                editor,
                getAlarmActionPendingIntentSD(context),
                getAlarmInfoPendingIntentSD(context),
                getAlarmActionRepeatPendingIntentSD(context),
                PREFERENCES_Key_IsAlarmActualSD) //вкл.основной будильник
            //  binding.switchOFOnAlarm.isChecked = true
            liveDataIsTpOk.value=true

        }
        materialTimepicker?.show(fragmentManager, "tag_picker")

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