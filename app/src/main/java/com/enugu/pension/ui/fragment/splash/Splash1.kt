package com.enugu.pension.ui.fragment.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enugu.pension.R
import com.enugu.pension.databinding.FragmentSplash1Binding
import com.enugu.pension.ui.fragment.base.BaseFragment


class Splash1 : BaseFragment() {
    private lateinit var binding:FragmentSplash1Binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplash1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClicked()
    }

    private fun onClicked() {
        binding.imgNext1.setOnClickListener {
            navigate(
                R.id.action_splash1_to_splash2,
                allowAnimation = false, popUpTo = R.id.navigation_splash1)
        }


    }
}