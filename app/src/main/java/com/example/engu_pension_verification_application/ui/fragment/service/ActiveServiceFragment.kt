package com.example.engu_pension_verification_application.ui.fragment.service


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentActiveServiceBinding
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBankFragment
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBasicDetailsFragment
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveDocumentsFragment
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.ActiveServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import com.google.android.material.tabs.TabLayout


class ActiveServiceFragment : BaseFragment() {
    private lateinit var binding:FragmentActiveServiceBinding
        private val activeServiceViewModel by activityViewModels<ActiveServiceViewModel>()
        private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2

    val prefs = SharedPref

    val tabIcons = intArrayOf(
        R.drawable.ic_basicdetails_white, R.drawable.ic_documents_active, R.drawable.ic_bank_white
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActiveServiceBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarActiveservice.setTitle(null)
        initViewModel()


        setupViewPager(binding.tabActiveserviceViewpager)
        binding.tabTablayoutActiveservice!!.setupWithViewPager(binding.tabActiveserviceViewpager)

        //enableDisableTabs(enableTab0 = true, enableTab1 = false, enableTab2 = false)


        observeLiveData()
        onClicked()
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), 
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun observeLiveData() {
        activeServiceViewModel.onMoveToNextTab.observe(viewLifecycleOwner) {
            binding.tabActiveserviceViewpager.setCurrentItem(getItem(+1), true)
        }
        activeServiceViewModel.enableDocTab.observe(viewLifecycleOwner) {
                binding.tabTablayoutActiveservice.getTabAt(ActiveDocumentsFragment.TAB_POSITION)?.view?.isEnabled = it
        }
        activeServiceViewModel.enableBankTab.observe(viewLifecycleOwner) {
                binding.tabTablayoutActiveservice.getTabAt(ActiveBankFragment.TAB_POSITION)?.view?.isEnabled = it
        }

        tokenRefreshViewModel2.tokenRefreshError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                dismissLoader()
                if (error.isNotEmpty()) Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                prefs.logout()
                val intent = Intent(context, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun onClicked() {
        binding.imgBackActiveservice.setOnClickListener {
            activity?.onBackPressed()
        }
    }


    private fun setupViewPager(viewpager: ViewPager?) {
        val activeservice_adapter = ViewPagerAdapter(childFragmentManager)

        activeservice_adapter.addFragment(ActiveBasicDetailsFragment(), "Basic Details")
        activeservice_adapter.addFragment(ActiveDocumentsFragment(), "Documents")
        activeservice_adapter.addFragment(ActiveBankFragment(), "Bank Information")


        viewpager?.adapter = activeservice_adapter
        activeservice_adapter.notifyDataSetChanged()
        viewpager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                activeServiceViewModel.currentTabPos.value = position
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })


        // Function to enable or disable tabs
        /*fun enableDisableTabs(tabLayout: TabLayout, enableTab0: Boolean, enableTab1: Boolean, enableTab2: Boolean) {
            tabLayout.getTabAt(0)?.view?.isEnabled = enableTab0
            tabLayout.getTabAt(1)?.view?.isEnabled = enableTab1
            tabLayout.getTabAt(2)?.view?.isEnabled = enableTab2
        }*/


        binding.tabTablayoutActiveservice.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                val position = tab!!.position
                Log.d("position", "onTabSelected: " + position)
                when (position) {
                    0 -> {
                        binding.tabTablayoutActiveservice.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basicdetails_white)
                        binding.tabTablayoutActiveservice.getTabAt(1)
                            ?.setIcon(R.drawable.ic_documents_inactive)
                        binding.tabTablayoutActiveservice.getTabAt(2)
                            ?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    1 -> {

                        binding.tabTablayoutActiveservice.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        binding.tabTablayoutActiveservice.getTabAt(1)
                            ?.setIcon(R.drawable.ic_documents_active)
                        binding.tabTablayoutActiveservice.getTabAt(2)
                            ?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    2 -> {

                        binding.tabTablayoutActiveservice.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        binding.tabTablayoutActiveservice.getTabAt(1)
                            ?.setIcon(R.drawable.ic_documents_inactive)
                        binding.tabTablayoutActiveservice.getTabAt(2)?.setIcon(R.drawable.ic_bank_white)
                    }
                }
            }


            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        activeServiceViewModel.refreshTabsState()
        viewpager?.currentItem = when (prefs.onboardingStage){
            OnboardingStage.ACTIVE_DOCUMENTS -> ActiveDocumentsFragment.TAB_POSITION
            OnboardingStage.ACTIVE_BANK_INFO -> ActiveBankFragment.TAB_POSITION
            else -> ActiveBasicDetailsFragment.TAB_POSITION
        }
    }


    /*fun enableDisableTabs(tabLayout: TabLayout, enableTab0: Boolean, enableTab1: Boolean, enableTab2: Boolean) {
        tabLayout.getTabAt(0)?.view?.isEnabled = enableTab0
        tabLayout.getTabAt(1)?.view?.isEnabled = enableTab1
        tabLayout.getTabAt(2)?.view?.isEnabled = enableTab2
    }*/


    //binding.tabTablayoutActiveservice



    private fun enableNextTab(tabLayout : TabLayout,currentTabIndex: Int) {
        if (currentTabIndex < tabLayout.tabCount - 1) {
            tabLayout.getTabAt(currentTabIndex + 1)?.view?.isEnabled = true
        }
    }


    private fun enableAllTabs(tabLayout: TabLayout) {
        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.view?.isEnabled = true
        }
    }


    class ViewPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getCount(): Int {
            return mFragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)

        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }

    }

    private fun getItem(i: Int): Int {
        return binding.tabActiveserviceViewpager.currentItem + i
    }

}


