package uz.jabborovbahrom.openplayer.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uz.jabborovbahrom.openplayer.libraryFragments.PagerFragment

class MyPagerAdapter(
    fragment: Fragment
) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return PagerFragment.newInstance(position + 1)
    }
}