package uz.jabborovbahrom.openplayer.libraryFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.android.material.tabs.TabLayoutMediator
import jabborovbahrom.openplayer.R
import jabborovbahrom.openplayer.databinding.FragmentHomeBinding
import uz.jabborovbahrom.openplayer.adapters.MyPagerAdapter

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var myPagerAdapter: MyPagerAdapter
    lateinit var titles: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titles = ArrayList()
        titles.add(getString(R.string.song))
        titles.add(getString(R.string.artist))
        titles.add(getString(R.string.album))

        myPagerAdapter = MyPagerAdapter(this)
        binding.viewPager.adapter = myPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.library)
    }
}