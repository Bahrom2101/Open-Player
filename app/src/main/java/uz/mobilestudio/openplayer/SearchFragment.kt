package uz.mobilestudio.openplayer

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import uz.mobilestudio.openplayer.adapters.SongDownloadedAdapter
import uz.mobilestudio.openplayer.databinding.FragmentSearchBinding
import uz.mobilestudio.openplayer.db.AppDatabase
import uz.mobilestudio.openplayer.entity.Song
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.GONE
    }

    lateinit var binding: FragmentSearchBinding
    lateinit var appDatabase: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        appDatabase = AppDatabase.getInstance(requireContext())

        binding.rv.setHasFixedSize(true)

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.cancel.setOnClickListener {
            binding.searchText.setText("")
        }

        when (arguments?.getString("state")) {
            "Lib" -> {
                val songDownloadedAdapter =
                    SongDownloadedAdapter(object : SongDownloadedAdapter.OnClickListener {
                        override fun onViewClick(song: Song, position: Int) {
                            val bundle = Bundle()
                            bundle.putInt("pos", 0)
                            val songs = ArrayList<Parcelable>()
                            songs.add(song)
                            bundle.putParcelableArrayList("songs", songs)
                            findNavController().navigate(R.id.playMusicFragment, bundle)
                        }
                    })
                binding.searchText.addTextChangedListener {
                    if (binding.searchText.text.toString().length > 0) {
                        val search = binding.searchText.text.toString()
                        val list = appDatabase.songDao()
                            .search("%${search.lowercase(Locale.getDefault())}%")
                        songDownloadedAdapter.submitList(list)
                        binding.progressBar.visibility = View.GONE
                    }
                }
                binding.rv.adapter = songDownloadedAdapter
            }
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.GONE
    }

    override fun onDetach() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.VISIBLE
        super.onDetach()
    }

    override fun onDestroy() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.VISIBLE
        super.onDestroy()
    }
}