package com.example.chuckalarm

import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.chuckalarm.databinding.FragmentSpecificAlarmBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*


class SpecificAlarmFragment : Fragment() {
    val APP_PREFERENCES = "prefCalendar"
    val PREFERENCES_Key_AlarmTimeCalendarDateSD = "AlarmTimeCalendarDateSD"
    val PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD = "AlarmTimeCalendarTimeInMillisSD"
    val PREFERENCES_Key_AlarmTimeCalendarTimeSD = "AlarmTimeCalendarTimeSD"
    val PREF_KEY_MINUT_SD="PREF_KEY_MINUT_SD"
    val PREF_KEY_HOUR_SD="PREF_KEY_HOUR_SD"
    val PREFERENCES_Key_IsAlarmActualSD = "IsAlarmActualSD"



    var prefCalendar: SharedPreferences? = null
    var format = SimpleDateFormat("HH:mm")
    var formatDate = SimpleDateFormat("yyyy.MM.dd")
lateinit var  binding: FragmentSpecificAlarmBinding
val vm:MainViewModel by activityViewModels()
val vmSD:ViewModelSD by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        // Inflate the layout for this fragment
        binding= FragmentSpecificAlarmBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefCalendar =activity?.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        val editor: SharedPreferences.Editor = prefCalendar!!.edit()
        binding.tvAlarmSD.setText(format.format(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,System.currentTimeMillis())))
        Log.d("IsFormat",formatDate.format(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,0)))
        binding.textView3.setText(formatDate.format(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,System.currentTimeMillis()+24*3600*1000)))
        vmSD.liveDataIsTpOk.observe(viewLifecycleOwner){
            if(it){
                binding.tvAlarmSD.setText(format.format(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,System.currentTimeMillis())))
                binding.textView3.setText(formatDate.format(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,0)))
                binding.switchSD.isChecked=true
                binding.tvIsActualeIntervalSD.setText("?????????????????? ??????????")
            }
        }
        var alarmManagerMain = activity?.getSystemService(AppCompatActivity.ALARM_SERVICE)
        alarmManagerMain as AlarmManager
        binding.switchSD.setOnClickListener{
            if (!binding.switchSD.isChecked) {
                activity?.let { it1 -> vmSD.mAlarmOff(alarmManagerMain, editor, it1?.applicationContext)
                    Log.d("IsStarted","????????????????")
                    binding.tvIsActualeIntervalSD.setText("???? ??????????????")
                }

            } else {
                vmSD.mAlarmShouldBeTomorrowCheck(editor,
                    PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,
                    prefCalendar!!)
                activity?.applicationContext?.let { it1 ->
                    vmSD.mAlarmOn(alarmManagerMain, prefCalendar!!, editor,
                        it1)
                    Log.d("IsStarted","??????????????")
                    binding.tvAlarmSD.setText(prefCalendar!!.getString(PREFERENCES_Key_AlarmTimeCalendarTimeSD,"00:00"))
                    binding.tvIsActualeIntervalSD.setText("?????????????????? ??????????")
                }
            }
        }


        binding.imBascetSD.setOnClickListener{
            activity?.let { it1 -> vmSD.mAlarmOff(alarmManagerMain, editor, it1?.applicationContext)
                Log.d("IsStarted","????????????????")
            }
            parentFragmentManager.beginTransaction().remove(this).commit()
         vm.liveDataBulSD.value=true
            vmSD.ldIsActulFrSD.value=false
        }

        binding.root.setOnClickListener{
            activity?.applicationContext?.let { it1 ->
                vmSD.alarmBuild(parentFragmentManager,editor,prefCalendar!!,alarmManagerMain,
                    it1)
            }
            Toast.makeText(activity?.applicationContext,"?? ?????????? ???? ??????",Toast.LENGTH_LONG).show()
        }
        binding.lLayCalendar.setOnClickListener{
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build()
            datePicker.addOnPositiveButtonClickListener {
             var calendar=Calendar.getInstance()
                calendar.timeInMillis=datePicker.selection!!.toLong()
                calendar.set(Calendar.HOUR_OF_DAY,prefCalendar!!.getLong(PREF_KEY_HOUR_SD,0).toInt())
                calendar.set(Calendar.MINUTE,prefCalendar!!.getLong(PREF_KEY_MINUT_SD,0).toInt())
                if (calendar.timeInMillis<System.currentTimeMillis()){
                    Toast.makeText(requireActivity().applicationContext,"?????? ?????????? ?????? ????????????, ???????????????? ???????????? ????????",Toast.LENGTH_LONG).show()
                }else{
                    editor.putLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,calendar.timeInMillis)

                    activity?.applicationContext?.let { it1 ->
                        vmSD.mAlarmOn(alarmManagerMain, prefCalendar!!, editor,
                            it1)

                    }
                    binding.tvAlarmSD.setText(format.format(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,System.currentTimeMillis())))
                    binding.textView3.setText(formatDate.format(prefCalendar!!.getLong(PREFERENCES_Key_AlarmTimeCalendarTimeInMillisSD,0)))
                    binding.switchSD.isChecked=true
                    binding.tvIsActualeIntervalSD.setText("?????????????????? ??????????")
                    Log.d("IsStarted","?????????????? ${calendar.time}")
                    Log.d("IsStarted","?????????????? ${prefCalendar!!.getString(PREFERENCES_Key_AlarmTimeCalendarTimeSD,"vjhgkfhlsgjksgfsfshbjhgj")}")
                }





                Log.d("DataPicker",vmSD.calendar.time.toString())
            }
            datePicker.show(parentFragmentManager, "tag");
        }
    }

    override fun onResume() {
        super.onResume()
        binding.switchSD.isChecked =
            prefCalendar!!.getBoolean(PREFERENCES_Key_IsAlarmActualSD, false)
        if (binding.switchSD.isChecked){
            Toast.makeText(activity?.applicationContext, "???????? ???????????????????? ??????????????????", Toast.LENGTH_LONG).show()
        binding.tvIsActualeIntervalSD.setText("?????????????????? ??????????")
        }else{
            binding.tvIsActualeIntervalSD.setText("???? ??????????????")
        }
        vmSD.ldIsActulFrSD.value=true
    }


    companion object {

        @JvmStatic
        fun newInstance(): SpecificAlarmFragment {
            return SpecificAlarmFragment()
        }
    }
}