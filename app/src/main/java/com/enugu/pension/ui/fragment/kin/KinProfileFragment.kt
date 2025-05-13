package com.enugu.pension.ui.fragment.kin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.enugu.pension.R
import com.enugu.pension.databinding.FragmentKinProfileBinding
import com.enugu.pension.model.dto.KeyValue
import com.enugu.pension.ui.adapter.KinProfileAdapter
import com.enugu.pension.ui.adapter.ProfileAdapter
import com.enugu.pension.ui.fragment.base.BaseFragment

class KinProfileFragment : BaseFragment() {
    companion object {
        const val ARG_NAME = "name"
        const val ARG_PHONE_NUMBER = "phone_number"
        const val ARG_EMAIL = "email"
        const val ARG_ADDRESS = "address"
        const val ARG_PIN_CODE = "pin_code"
    }
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
        setListeners()
        populate()
    }

    private fun setListeners() {
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
    }
    private fun populate() {
        binding.tvKinName.text = arguments?.getString(ARG_NAME) ?: ""
        binding.tvRelation.isGone = true
        val items = listOf(
            KeyValue(getString(R.string.email_id), arguments?.getString(ARG_EMAIL)?:""),
            KeyValue(getString(R.string.phone), arguments?.getString(ARG_PHONE_NUMBER)?:""),
            KeyValue(getString(R.string.address), arguments?.getString(ARG_ADDRESS)?:""),
//            KeyValue(getString(R.string.pincode), arguments?.getString(ARG_PIN_CODE)?:""),
        )
        binding.rvDetails.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = KinProfileAdapter(items)
        }
    }

}