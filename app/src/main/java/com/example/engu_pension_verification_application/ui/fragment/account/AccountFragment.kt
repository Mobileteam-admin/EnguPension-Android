package com.example.engu_pension_verification_application.ui.fragment.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.R
import kotlinx.android.synthetic.main.fragment_account.*


class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClicked()
    }

    private fun onClicked() {
        img_account_back.setOnClickListener {
            activity?.onBackPressed()
        }
        txt_statement.setOnClickListener {
            findNavController().navigate(R.id.action_account_to_accountstatement)
        }
        txt_kinprofile.setOnClickListener {
            findNavController().navigate(R.id.action_account_to_kinprofile)
        }
    }

}