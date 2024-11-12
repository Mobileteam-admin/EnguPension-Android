package com.example.engu_pension_verification_application.ui.fragment.service

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBankFragment
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBasicDetailsFragment
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveDocumentsFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeBankFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeBasicDetailsFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeDocumentsFragment
import com.example.engu_pension_verification_application.utils.ViewPageCallBack
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_active_service.*
import kotlinx.android.synthetic.main.fragment_retiree.*

/*

class RetireeFragment : Fragment(), ViewPageCallBack {
    val tabIcons = intArrayOf(
        R.drawable.ic_basicdetails_white,
        R.drawable.ic_documents_active,
        R.drawable.ic_bank_white
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_retiree, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // toolbar_retiree.setTitle("Retiree")

        setupViewPager(tab_retiree_viewpager)
        tab_tablayout_retiree!!.setupWithViewPager(tab_retiree_viewpager)
        createTabIcons()

        onClicked()
    }

    private fun onClicked() {
        img_back_retiree.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun createTabIcons() {

       */
/* tab_tablayout_retiree.getTabAt(0)?.setIcon(tabIcons[0])
        tab_tablayout_retiree.getTabAt(1)?.setIcon(tabIcons[1])
        tab_tablayout_retiree.getTabAt(2)?.setIcon(tabIcons[2])*//*

        */
/* val tabone = LayoutInflater.from(context).inflate(R.layout.custom_tab, null) as TextView
         tabone.text = "Basic Details"
         tabone.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_basicdetails_white, 0, 0)
         tab_tablayout_activeservice.getTabAt(0)?.setCustomView(tabone)

         val tabtwo = LayoutInflater.from(context).inflate(R.layout.custom_tab, null) as TextView
         tabtwo.text = "Documents"
         tabtwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_documents_active, 0, 0)
         tab_tablayout_activeservice.getTabAt(0)?.setCustomView(tabtwo)

         val tabthree = LayoutInflater.from(context).inflate(R.layout.custom_tab, null) as TextView
         tabthree.text = "Bank Information"
         tabthree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bank_white, 0, 0)
         tab_tablayout_activeservice.getTabAt(0)?.setCustomView(tabthree)
 *//*

    }
    private fun setupViewPager(viewpager: ViewPager?) {
        val retiree_adapter = ViewPagerAdapter(childFragmentManager)

        retiree_adapter.addFragment(RetireeBasicDetailsFragment(this), "Basic Details")
        retiree_adapter.addFragment(RetireeDocumentsFragment(this), "Documents")
        retiree_adapter.addFragment(RetireeBankFragment(), "Bank Information")


        viewpager?.adapter = retiree_adapter
        retiree_adapter.notifyDataSetChanged()

        */
/*viewpager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Log.d("VIEW PAGER POSITION", "onPageScrolled: " + position)
            }

            override fun onPageSelected(position: Int) {
                retiree_adapter.notifyDataSetChanged()
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })*//*

        tab_tablayout_retiree.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                */
/*val view = tab?.customView
                if (view is AppCompatTextView) {
                    view.setTypeface(view.typeface, Typeface.BOLD)
                }*//*


                val position = tab!!.position

                Log.d("position", "onTabSelected: " + position)
                when (position) {
                    0 -> {
                        tab_tablayout_retiree.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basicdetails_white)
                        tab_tablayout_retiree.getTabAt(1)
                            ?.setIcon(R.drawable.ic_documents_inactive)
                        tab_tablayout_retiree.getTabAt(2)
                            ?.setIcon(R.drawable.ic_bank_inactive)
                    }
                    1 -> {
                        tab_tablayout_retiree.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        tab_tablayout_retiree.getTabAt(1)
                            ?.setIcon(R.drawable.ic_documents_active)
                        tab_tablayout_retiree.getTabAt(2)
                            ?.setIcon(R.drawable.ic_bank_inactive)
                    }
                    2 -> {
                        tab_tablayout_retiree.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        tab_tablayout_retiree.getTabAt(1)
                            ?.setIcon(R.drawable.ic_documents_inactive)
                        tab_tablayout_retiree.getTabAt(2)
                            ?.setIcon(R.drawable.ic_bank_white)
                    }
                }
            }


            override fun onTabUnselected(tab: TabLayout.Tab?) {
                */
/*val view = tab?.customView
                if (view is AppCompatTextView) {
                    view.typeface = mUnselectedTypeFace
                }*//*

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }



    class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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

    override fun onViewMoveNext() {
        tab_retiree_viewpager.setCurrentItem(getItem(+1),true)
    }

    private fun getItem(i:Int): Int{
        return tab_retiree_viewpager.currentItem + i
    }
}*/
