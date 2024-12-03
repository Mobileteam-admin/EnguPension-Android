package com.example.engu_pension_verification_application.ui.fragment.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.FragmentAccountBinding
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment


class AccountFragment : BaseFragment() {
    private lateinit var binding:FragmentAccountBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClicked()
    }

    private fun onClicked() {
        binding.imgAccountBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.txtStatement.setOnClickListener {
            navigate(R.id.action_account_to_accountstatement)
        }
        binding.txtKinprofile.setOnClickListener {
            navigate(R.id.action_account_to_kinprofile)
        }
    }

}