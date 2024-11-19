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
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeBankFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeBasicDetailsFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeDocumentsFragment
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.RetireeServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_retiree.*


class RetireeServiceFragment : Fragment() {

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_retiree, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // toolbar_retiree.setTitle("Retiree")
        initViewModel()
//        retireeServicePresenter = RetireeServicePresenter(this)


        setupViewPager(tab_retiree_viewpager)
        tab_tablayout_retiree!!.setupWithViewPager(tab_retiree_viewpager)


//        initCall()
        onClicked()
        observeLiveData()
    }

    private fun observeLiveData() {
        retireeServiceViewModel.onMoveToNextTab.observe(viewLifecycleOwner) {
            tab_retiree_viewpager.setCurrentItem(getItem(+1), true)
        }
        retireeServiceViewModel.enableTab0.observe(viewLifecycleOwner) {
            tab_tablayout_retiree.getTabAt(0)?.view?.isEnabled = it
        }
        retireeServiceViewModel.enableTab1.observe(viewLifecycleOwner) {
            tab_tablayout_retiree.getTabAt(1)?.view?.isEnabled = it
        }
        retireeServiceViewModel.enableTab2.observe(viewLifecycleOwner) {
            tab_tablayout_retiree.getTabAt(2)?.view?.isEnabled = it
        }

        tokenRefreshViewModel2.tokenRefreshError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Loader.hideLoader()
                if (error.isNotEmpty()) Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                SharedPref.logout()
                val intent = Intent(context, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

    }
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), // use `this` if the ViewModel want to tie with fragment's lifecycle
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun onClicked() {
        img_back_retiree.setOnClickListener {
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


        tab_tablayout_retiree.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {/*val view = tab?.customView
                if (view is AppCompatTextView) {
                    view.setTypeface(view.typeface, Typeface.BOLD)
                }*/

                val position = tab!!.position

                /*if (prefs.isRBasicSubmit == true)
                {
                    enableDisableTabs(tab_tablayout_retiree, true, true, false)
                    if (prefs.isRDocSubmit == true){
                        enableDisableTabs(tab_tablayout_retiree, true, true, true)
                    }
                }else{
                    enableDisableTabs(tab_tablayout_retiree, true, false, false)

                }*/

               /* Log.d("pref", "pref isRBasicSubmit on tab selectd ${prefs.isRBasicSubmit} ")
                when(prefs.isRBasicSubmit){
                    true->{
                        Log.d("pref", "pref isRBasicSubmit in isRBasic pref ${prefs.isRBasicSubmit} ")
                        enableDisableTabs(tab_tablayout_retiree, true, true, false)
                    }
                    false->{
                        enableDisableTabs(tab_tablayout_retiree, true, false, false)
                    }
                }
                when(prefs.isRDocSubmit){
                    true->{
                        Log.d("pref", "pref isRBasicSubmit in isRDoc pref ${prefs.isRBasicSubmit} ")
                        enableDisableTabs(tab_tablayout_retiree, true, true, true)

                    }
                    false->{

                        if (prefs.isRBasicSubmit == false){
                            enableDisableTabs(tab_tablayout_retiree, true, false, false)
                        }else {
                            enableDisableTabs(tab_tablayout_retiree, true, true, false)
                        }
                    }
                }*/


                Log.d("position", "onTabSelected: " + position)
                when (position) {
                    0 -> {
                        tab_tablayout_retiree.getTabAt(0)?.setIcon(R.drawable.ic_basicdetails_white)
                        tab_tablayout_retiree.getTabAt(1)?.setIcon(R.drawable.ic_documents_inactive)
                        tab_tablayout_retiree.getTabAt(2)?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    1 -> {
                        tab_tablayout_retiree.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        tab_tablayout_retiree.getTabAt(1)?.setIcon(R.drawable.ic_documents_active)
                        tab_tablayout_retiree.getTabAt(2)?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    2 -> {
                        tab_tablayout_retiree.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        tab_tablayout_retiree.getTabAt(1)?.setIcon(R.drawable.ic_documents_inactive)
                        tab_tablayout_retiree.getTabAt(2)?.setIcon(R.drawable.ic_bank_white)
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
        return tab_retiree_viewpager.currentItem + i
    }




    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }
}