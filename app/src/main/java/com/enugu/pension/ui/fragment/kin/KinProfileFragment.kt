package com.enugu.pension.ui.fragment.kin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.enugu.pension.databinding.FragmentKinProfileBinding
import com.enugu.pension.ui.fragment.base.BaseFragment

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