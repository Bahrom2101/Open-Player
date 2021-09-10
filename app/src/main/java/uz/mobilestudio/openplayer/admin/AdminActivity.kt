package uz.mobilestudio.openplayer.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import uz.mobilestudio.openplayer.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    lateinit var binding: ActivityAdminBinding
    lateinit var pagerAdapter: PagerAdapter
    lateinit var fragments: ArrayList<Fragment>
    lateinit var titles: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        titles = ArrayList()
        titles.add("All")
        titles.add("Report")
        titles.add("Permission")
        fragments = ArrayList()
        fragments.add(AllFragment())
        fragments.add(ReportFragment())
        fragments.add(PermissionFragment())

        pagerAdapter = PagerAdapter(fragments, titles, supportFragmentManager)
        binding.viewPager.adapter = pagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}