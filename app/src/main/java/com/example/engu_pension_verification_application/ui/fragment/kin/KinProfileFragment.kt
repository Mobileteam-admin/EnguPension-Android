package com.example.engu_pension_verification_application.ui.fragment.kin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.FragmentKinProfileBinding
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment

class KinProfileFragment : BaseFragment() {
    private lateinit var binding:FragmentKinProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentKinProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClicked()
    }

    private fun onClicked() {
        binding.imgKinprofileBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

}