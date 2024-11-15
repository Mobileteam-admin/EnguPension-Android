package com.example.engu_pension_verification_application.ui.fragment.service


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBankFragment
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBasicDetailsFragment
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveDocumentsFragment
import com.example.engu_pension_verification_application.utils.SharedPref
import com.example.engu_pension_verification_application.view_models.ActiveServiceViewModel
import com.example.engu_pension_verification_application.view_models.TokenRefreshViewModel2
import com.example.engu_pension_verification_application.view_models.TokenRefreshViewModel2Factory
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_active_service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ActiveServiceFragment : Fragment() {
        private val activeServiceViewModel by activityViewModels<ActiveServiceViewModel>()
        private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2

    val prefs = SharedPref

    val tabIcons = intArrayOf(
        R.drawable.ic_basicdetails_white, R.drawable.ic_documents_active, R.drawable.ic_bank_white
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_service, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_activeservice.setTitle(null)
        initViewModels()


        setupViewPager(tab_activeservice_viewpager)
        tab_tablayout_activeservice!!.setupWithViewPager(tab_activeservice_viewpager)

        //enableDisableTabs(enableTab0 = true, enableTab1 = false, enableTab2 = false)


        observeLiveData()
        onClicked()
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), // use `this` if the ViewModel want to tie with fragment's lifecycle
            TokenRefreshViewModel2Factory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun observeLiveData() {
        activeServiceViewModel.onMoveToNextTab.observe(viewLifecycleOwner) {
            tab_activeservice_viewpager.setCurrentItem(getItem(+1), true)
        }
        activeServiceViewModel.enableTab0.observe(viewLifecycleOwner) {
                tab_tablayout_activeservice.getTabAt(0)?.view?.isEnabled = it
        }
        activeServiceViewModel.enableTab1.observe(viewLifecycleOwner) {
                tab_tablayout_activeservice.getTabAt(1)?.view?.isEnabled = it
        }
        activeServiceViewModel.enableTab2.observe(viewLifecycleOwner) {
                tab_tablayout_activeservice.getTabAt(2)?.view?.isEnabled = it
        }

        tokenRefreshViewModel2.tokenRefreshError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Loader.hideLoader()
                if (error.isNotEmpty()) Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                clearLogin()
                val intent = Intent(context, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

    }

    private fun onClicked() {
        img_back_activeservice.setOnClickListener {
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
                activeServiceViewModel.currentTabPos.value= position
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


        tab_tablayout_activeservice.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                val position = tab!!.position
                //enableDisableTabs(enableTab0 = true, enableTab1 = false, enableTab2 = false)
                //enableDisableTabs(tab_tablayout_activeservice, true, false, false)

                /*val tabLayout: TabLayout = tab_tablayout_activeservice

                // Initially disable all tabs except the first one
                tabLayout.getTabAt(0)?.select()
                for (i in 1 until tabLayout.tabCount) {
                    tabLayout.getTabAt(i)?.view?.isEnabled = false
                }*/

                //prefs.isActiveBasicSubmit = true
                //prefs.isActiveDocSubmit = false
                Log.d("pref", "pref isActiveBasicSubmit on tab selectd ${prefs.isActiveBasicSubmit} ")
                /*if (prefs.isActiveBasicSubmit){

                    Log.d("pref", "pref isActiveBasicSubmit true  ${prefs.isActiveBasicSubmit} ")


                    if (prefs.isActiveDocSubmit){
                        enableDisableTabs(tab_tablayout_activeservice, true, true, true)

                    } else if (!prefs.isActiveDocSubmit){
                        enableDisableTabs(tab_tablayout_activeservice, true, true, false)

                    }

                    enableDisableTabs(tab_tablayout_activeservice, true, true, false)

                }
                else{
                    enableDisableTabs(tab_tablayout_activeservice, true, false, false)

                }*/


                /*when {
                    prefs.isActiveBasicSubmit -> {
                    // Enable tabs 0 and 1, disable tab 2
                        enableDisableTabs(tab_tablayout_activeservice, true, true, false)
                    }

                    !prefs.isActiveBasicSubmit -> {
                        enableDisableTabs(tab_tablayout_activeservice, true, false, false)
                    }


                    prefs.isActiveDocSubmit -> {
                    // Enable all tabs 0, 1, and 2
                        enableDisableTabs(tab_tablayout_activeservice, true, true, true)
                    }

                    !prefs.isActiveDocSubmit -> {
                        enableDisableTabs(tab_tablayout_activeservice, true, true, false)
                    }
                   *//* else -> {
                    // Disable tabs 1 and 2, enable tab 0
                        enableDisableTabs(tab_tablayout_activeservice, true, false, false)
                    }*//*
                }*/

                /*
                if (prefs.isActiveBasicSubmit == true)
                {
                    //enableDisableTabs(tab_tablayout_activeservice, true, true, false)

                    if (prefs.isActiveDocSubmit == true){
                        //enableDisableTabs(tab_tablayout_activeservice, true, true, true)
                        enableDisableTabs(enableTab0 = true, enableTab1 = true, enableTab2 = true)
                    }else{
                        enableDisableTabs(enableTab0 = true, enableTab1 = true, enableTab2 = false)
                    }
                }else{
                    //enableDisableTabs(tab_tablayout_activeservice, true, false, false)
                    enableDisableTabs(enableTab0 = true, enableTab1 = false, enableTab2 = false)
                }
                */

                /*
                            false-> {when(prefs.isActiveBasicSubmit){
                    true->{
                        Log.d("pref", "pref isActiveBasicSubmit in isActiveBasic pref ${prefs.isActiveBasicSubmit} ")
                        enableDisableTabs(tab_tablayout_activeservice, true, true, false)
                        when(prefs.isActiveDocSubmit){
                            true->{
                                Log.d("pref", "pref isActiveBasicSubmit in isActiveDoc pref ${prefs.isActiveBasicSubmit} ")
                                enableDisableTabs(tab_tablayout_activeservice, true, true, true)

                            }
                                enableDisableTabs(tab_tablayout_activeservice, true, true, false)
                            }

                        }
                    }
                    false->{
                        enableDisableTabs(tab_tablayout_activeservice, true, false, false)

                    }
                }
                */
                /*when(prefs.isActiveDocSubmit){
                    true->{
                        Log.d("pref", "pref isActiveBasicSubmit in isActiveDoc pref ${prefs.isActiveBasicSubmit} ")
                        enableDisableTabs(tab_tablayout_activeservice, true, true, true)

                    }
                    false->{

                        if (prefs.isActiveBasicSubmit == false){
                            enableDisableTabs(tab_tablayout_activeservice, true, false, false)
                        }else {
                            enableDisableTabs(tab_tablayout_activeservice, true, true, false)
                        }
                    }
                }*/



                Log.d("position", "onTabSelected: " + position)
                when (position) {
                    0 -> {
                        tab_tablayout_activeservice.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basicdetails_white)
                        tab_tablayout_activeservice.getTabAt(1)
                            ?.setIcon(R.drawable.ic_documents_inactive)
                        tab_tablayout_activeservice.getTabAt(2)
                            ?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    1 -> {

                        tab_tablayout_activeservice.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        tab_tablayout_activeservice.getTabAt(1)
                            ?.setIcon(R.drawable.ic_documents_active)
                        tab_tablayout_activeservice.getTabAt(2)
                            ?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    2 -> {

                        tab_tablayout_activeservice.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        tab_tablayout_activeservice.getTabAt(1)
                            ?.setIcon(R.drawable.ic_documents_inactive)
                        tab_tablayout_activeservice.getTabAt(2)?.setIcon(R.drawable.ic_bank_white)
                    }
                }
            }


            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }


    /*fun enableDisableTabs(tabLayout: TabLayout, enableTab0: Boolean, enableTab1: Boolean, enableTab2: Boolean) {
        tabLayout.getTabAt(0)?.view?.isEnabled = enableTab0
        tabLayout.getTabAt(1)?.view?.isEnabled = enableTab1
        tabLayout.getTabAt(2)?.view?.isEnabled = enableTab2
    }*/


    //tab_tablayout_activeservice



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
        return tab_activeservice_viewpager.currentItem + i
    }

    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }


    fun onTokenRefreshFailure(response: ResponseRefreshToken) {
        Loader.hideLoader()
        Toast.makeText(
            context, response.token_detail?.message, Toast.LENGTH_LONG
        ).show()

        clearLogin()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    private fun clearLogin() {
        prefs.isLogin = false
        prefs.user_id = ""
        prefs.user_name = ""
        prefs.email = ""
        prefs.access_token = ""
        prefs.refresh_token = ""
    }

}


