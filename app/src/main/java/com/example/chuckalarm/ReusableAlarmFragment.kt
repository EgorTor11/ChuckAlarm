package com.example.chuckalarm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.chuckalarm.databinding.FragmentReusableAlarmBinding
import com.example.chuckalarm.databinding.FragmentSpecificAlarmBinding


class ReusableAlarmFragment : Fragment() {
    lateinit var  binding: FragmentReusableAlarmBinding
    val vm:MainViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding= FragmentReusableAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imBascetED.setOnClickListener{
            parentFragmentManager.beginTransaction().remove(this).commit()
            vm.liveDataBulED.value=true
        }
        binding.cardAlarmED.setOnClickListener{

        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ReusableAlarmFragment {
            return ReusableAlarmFragment()
        }
    }
}