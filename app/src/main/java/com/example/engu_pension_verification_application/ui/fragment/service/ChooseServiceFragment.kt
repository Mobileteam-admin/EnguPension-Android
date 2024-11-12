package com.example.engu_pension_verification_application.ui.fragment.service

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.R
import kotlinx.android.synthetic.main.fragment_choose_service.*


class ChooseServiceFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClicked()
    }

    private fun onClicked() {
        cv_activeservice.setOnClickListener {
            findNavController().navigate(R.id.action_chooseservice_to_ActiveService)
        }
        cv_retiree.setOnClickListener { findNavController().navigate(R.id.action_chooseservice_to_Retiree) }
    }

}