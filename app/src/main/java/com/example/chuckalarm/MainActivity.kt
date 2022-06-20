package com.example.chuckalarm

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.chuckalarm.databinding.ActivityMainBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import okhttp3.*
import java.util.*
import android.R
import android.app.*
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import android.content.SharedPreferences
import android.view.View
import androidx.activity.viewModels
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {
    val CHANNEL_ID = "CHANNEL_ID"
    lateinit var binding: ActivityMainBinding
    var calendar = Calendar.getInstance()
    val vm: MainViewModel by viewModels()
    val vmSD: ViewModelSD by viewModels()

    val APP_PREFERENCES = "prefCalendar"
    val PREFERENCES_Key_AlarmTimeCalendarTimeInMillis = "AlarmTimeCalendarTimeInMillis"
    val PREFERENCES_Key_AlarmTimeCalendarTime = "AlarmTimeCalendarTime"
    val PREFERENCES_Key_IsAlarmActual = "IsAlarmActual"
    val PREFERENCES_Key_IsAlarmActualSD = "IsAlarmActualSD"


    var prefCalendar: SharedPreferences? = null
    var format = SimpleDateFormat("HH:mm")
    var formatCDT = SimpleDateFormat("HH:mm:ss")

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefCalendar = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        val editor: SharedPreferences.Editor = prefCalendar!!.edit()
        if (prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
                Long.MIN_VALUE) == Long.MIN_VALUE
        ) {
            editor.putLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
                System.currentTimeMillis())
            editor.apply()
        }
//        if (prefCalendar!!.getBoolean(PREFERENCES_Key_IsAlarmActual, false)){
//            vm.startTimer(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
//                Long.MIN_VALUE) - System.currentTimeMillis())
//        }

        vm.ldInterval.observe(this) {
            binding.tvIsAktualeInterval.setText("сработает через ${
                (vm.ldInterval.value)?.div(3600000)
            } часов" +
                    " ${(vm.ldInterval.value)?.rem((3600000))?.div(60000)} минут" +
                    "${(vm.ldInterval.value)?.rem((60000))?.div(1000)} секунд")
        }

        binding.tvAlarmTime.setText(mTime(editor))

        vm.liveDataIsTpOk.observe(this) {
            if (it) {
                binding.tvAlarmTime.setText(mTime(editor))
                // vm.stopTimer()
                //  vm.startTimer(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,System.currentTimeMillis())-System.currentTimeMillis())
                binding.switchOFOnAlarm.isChecked = true
                //   binding.tvIsAktualeInterval.setText("сработает через ${}")
            }
        }


        createNotificationChannel()

        var alarmManagerMain = getSystemService(ALARM_SERVICE)
        alarmManagerMain as AlarmManager
        if (!Settings.canDrawOverlays(this)) {
            Log.d("MyLog", "нету настройки")
            Toast.makeText(this, "нету настройки", Toast.LENGTH_LONG).show()
            val alertDialog: AlertDialog = AlertDialog.Builder(this@MainActivity).create()

            // Указываем Title
            alertDialog.setTitle("Информационое сообщение")

            // Указываем текст сообщение
            alertDialog.setMessage("Welcome to devcolibri.com")

            // задаем иконку
            alertDialog.setIcon(R.drawable.alert_dark_frame)

            // Обработчик на нажатие OK
            alertDialog.setButton("OK") { dialog, which -> // Код который выполнится после закрытия окна
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.fromParts("package", packageName, null)))
            }
            // показываем Alert
            alertDialog.show()
        }


        binding.switchOFOnAlarm.setOnClickListener {
            if (!binding.switchOFOnAlarm.isChecked) {
                vm.mAlarmOff(alarmManagerMain, editor, this)
                // vm.stopTimer()
                binding.tvIsAktualeInterval.setText("не активен")
                // vm.liveDataIsTpOk.value=false
            } else {
                vm.mAlarmShouldBeTomorrowCheck(editor,
                    PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
                    prefCalendar!!)
                vm.mAlarmOn(alarmManagerMain, prefCalendar!!, editor, this)
                // vm.startTimer(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,0)-System.currentTimeMillis())
                // vm.liveDataIsTpOk.value=false
                //binding.tvIsAktualeInterval.setText("сработает через")
            }

        }

        binding.tvAlarmTime.setOnClickListener {
            vm.alarmBuild(supportFragmentManager, editor, prefCalendar!!, alarmManagerMain, this)

        }

        binding.imKar.setOnClickListener { }


//        binding.cardEveryDay.setOnClickListener {
//            supportFragmentManager.beginTransaction()
//                .replace(binding.frameLayoutContainerED.id, ReusableAlarmFragment.newInstance())
//                .commit();
//            it.setVisibility(View.GONE)
//        }

        binding.cardSD.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(binding.frContainerSD.id, SpecificAlarmFragment.newInstance())
                .commit();
            binding.cardSD.setVisibility(View.GONE)

        }
        vm.liveDataBulED.observe(this) {
            if (it) {
                binding.cardEveryDay.setVisibility(View.VISIBLE)
            }
        }
        vm.liveDataBulSD.observe(this) {
            if (it) {
                binding.cardSD.setVisibility(View.VISIBLE)
            }
        }
    }


    fun getAlarmActionReceiverPendingIntent(): PendingIntent {
        var intent = Intent(this, MyReceiver::class.java)     //
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        var pIntent = PendingIntent.getBroadcast(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return pIntent
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "getString"
            val descriptionText = "getString(R.string.channel_description)"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onPause() {
        super.onPause()
        vm.stopTimer()
    }
// для комита
    override fun onResume() {
        super.onResume()
        binding.switchOFOnAlarm.isChecked =
            prefCalendar!!.getBoolean(PREFERENCES_Key_IsAlarmActual, false)
        if (binding.switchOFOnAlarm.isChecked) {
            Toast.makeText(this, "Есть запущенный будильник", Toast.LENGTH_LONG).show()
            vm.startTimer(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
                Long.MIN_VALUE) - System.currentTimeMillis())

        } else {
            binding.tvIsAktualeInterval.setText("не активен")
        }
        if (prefCalendar!!.getBoolean(PREFERENCES_Key_IsAlarmActualSD, false)) {
            supportFragmentManager.beginTransaction()
                .replace(binding.frContainerSD.id, SpecificAlarmFragment.newInstance())
                .commit();
            binding.cardSD.setVisibility(View.GONE)
        }
        if (vmSD.ldIsActulFrSD.value == true) {
            binding.cardSD.setVisibility(View.GONE)
        }

    }


    fun mTime(editor: SharedPreferences.Editor): String {
        if (prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
                Long.MIN_VALUE) == Long.MIN_VALUE
        ) {
            Log.d("mNull", "Пусто")
            editor.putLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
                System.currentTimeMillis())
        }
        return format.format(prefCalendar?.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillis,
            System.currentTimeMillis())).toString()


    }


}