package uz.mobilestudio.openplayer.netFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.android.material.tabs.TabLayoutMediator
import uz.mobilestudio.openplayer.R
import uz.mobilestudio.openplayer.adapters.MyNetPagerAdapter
import uz.mobilestudio.openplayer.databinding.FragmentNetMusicBinding

class NetMusicFragment : Fragment() {

    lateinit var binding: FragmentNetMusicBinding
    lateinit var myNetPagerAdapter: MyNetPagerAdapter
    lateinit var titles: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNetMusicBinding.inflate(layoutInflater)

        titles = ArrayList()
        titles.add(getString(R.string.explore))
        titles.add(getString(R.string.downloaded))
        titles.add(getString(R.string.saved))
        myNetPagerAdapter = MyNetPagerAdapter(this)
        binding.viewPager.adapter = myNetPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.global)
    }
}