package com.example.engu_pension_verification_application.ui.fragment.service

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentRetireeBinding
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeBankFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeBasicDetailsFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeDocumentsFragment
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.RetireeServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import com.google.android.material.tabs.TabLayout


class RetireeServiceFragment : BaseFragment() {
    private lateinit var binding:FragmentRetireeBinding
    private val retireeServiceViewModel by activityViewModels<RetireeServiceViewModel>()
    val prefs = SharedPref
//    var BankdetailsList = ArrayList<ListBanksItem?>()
//    var AccountTypeList = ArrayList<AccountTypeItem?>()
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    val tabIcons = intArrayOf(
        R.drawable.ic_basicdetails_white, R.drawable.ic_documents_active, R.drawable.ic_bank_white
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRetireeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // toolbar_retiree.setTitle("Retiree")
        initViewModel()
//        retireeServicePresenter = RetireeServicePresenter(this)


        setupViewPager(binding.tabRetireeViewpager)
        binding.tabTablayoutRetiree!!.setupWithViewPager(binding.tabRetireeViewpager)


//        initCall()
        onClicked()
        observeLiveData()
    }

    private fun observeLiveData() {
        retireeServiceViewModel.onMoveToNextTab.observe(viewLifecycleOwner) {
            binding.tabRetireeViewpager.setCurrentItem(getItem(+1), true)
        }
        retireeServiceViewModel.enableDocTab.observe(viewLifecycleOwner) {
            binding.tabTablayoutRetiree.getTabAt(RetireeDocumentsFragment.TAB_POSITION)?.view?.isEnabled = it
        }
        retireeServiceViewModel.enableBankTab.observe(viewLifecycleOwner) {
            binding.tabTablayoutRetiree.getTabAt(RetireeBankFragment.TAB_POSITION)?.view?.isEnabled = it
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
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), 
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun onClicked() {
        binding.imgBackRetiree.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setupViewPager(viewpager: ViewPager?) {
        val retiree_adapter = ViewPagerAdapter(childFragmentManager)

        retiree_adapter.addFragment(RetireeBasicDetailsFragment(), "Basic Details")
        retiree_adapter.addFragment(RetireeDocumentsFragment(), "Documents")
        retiree_adapter.addFragment(RetireeBankFragment(), "Bank Information")


        viewpager?.adapter = retiree_adapter
        retiree_adapter.notifyDataSetChanged()

        viewpager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Log.d("VIEW PAGER POSITION", "onPageScrolled: " + position)
            }

            override fun onPageSelected(position: Int) {
                retireeServiceViewModel.currentTabPos.value= position
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })


        binding.tabTablayoutRetiree.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {/*val view = tab?.customView
                if (view is AppCompatTextView) {
                    view.setTypeface(view.typeface, Typeface.BOLD)
                }*/

                val position = tab!!.position
                when (position) {
                    0 -> {
                        binding.tabTablayoutRetiree.getTabAt(0)?.setIcon(R.drawable.ic_basicdetails_white)
                        binding.tabTablayoutRetiree.getTabAt(1)?.setIcon(R.drawable.ic_documents_inactive)
                        binding.tabTablayoutRetiree.getTabAt(2)?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    1 -> {
                        binding.tabTablayoutRetiree.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        binding.tabTablayoutRetiree.getTabAt(1)?.setIcon(R.drawable.ic_documents_active)
                        binding.tabTablayoutRetiree.getTabAt(2)?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    2 -> {
                        binding.tabTablayoutRetiree.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        binding.tabTablayoutRetiree.getTabAt(1)?.setIcon(R.drawable.ic_documents_inactive)
                        binding.tabTablayoutRetiree.getTabAt(2)?.setIcon(R.drawable.ic_bank_white)
                    }
                }
            }


            override fun onTabUnselected(tab: TabLayout.Tab?) {/*val view = tab?.customView
                if (view is AppCompatTextView) {
                    view.typeface = mUnselectedTypeFace
                }*/
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        retireeServiceViewModel.refreshTabsState()
        viewpager?.currentItem = when (prefs.onboardingStage){
            OnboardingStage.RETIREE_DOCUMENTS -> RetireeDocumentsFragment.TAB_POSITION
            OnboardingStage.RETIREE_BANK_INFO -> RetireeBankFragment.TAB_POSITION
            else -> RetireeBasicDetailsFragment.TAB_POSITION
        }
    }
    // Function to enable or disable tabs
    /*fun enableDisableTabs(tabLayout: TabLayout, enableTab0: Boolean, enableTab1: Boolean, enableTab2: Boolean) {
        tabLayout.getTabAt(0)?.view?.isEnabled = enableTab0
        tabLayout.getTabAt(1)?.view?.isEnabled = enableTab1
        tabLayout.getTabAt(2)?.view?.isEnabled = enableTab2
    }*/




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
        return binding.tabRetireeViewpager.currentItem + i
    }




    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }
}