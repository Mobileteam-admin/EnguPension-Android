package com.enugu.pension.ui.fragment.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentProfileBinding
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.ProfileViewModel
import com.enugu.pension.viewmodel.TokenRefreshViewModel2


class ProfileFragment : BaseFragment() {
    private lateinit var binding:FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
//        initCall()
        observeLiveData()
    }

    private fun initViews() {
        binding.imgProfileBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
        profileViewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(ProfileViewModel::class.java)
    }

    private fun observeLiveData() {
    }
}