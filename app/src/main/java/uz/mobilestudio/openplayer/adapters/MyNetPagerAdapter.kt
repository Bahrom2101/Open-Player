package uz.mobilestudio.openplayer.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uz.mobilestudio.openplayer.netFragments.NetPagerFragment

class MyNetPagerAdapter(
    fragment: Fragment
) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return NetPagerFragment.newInstance(position + 1)
    }

}