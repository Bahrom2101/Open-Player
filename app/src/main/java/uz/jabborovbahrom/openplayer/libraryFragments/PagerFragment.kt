package uz.jabborovbahrom.openplayer.libraryFragments

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import jabborovbahrom.openplayer.R
import jabborovbahrom.openplayer.databinding.FragmentPagerBinding
import uz.jabborovbahrom.openplayer.adapters.AlbumAdapter
import uz.jabborovbahrom.openplayer.adapters.ArtistAdapter
import uz.jabborovbahrom.openplayer.adapters.SongAdapter
import uz.jabborovbahrom.openplayer.db.AppDatabase
import uz.jabborovbahrom.openplayer.entity.Song
import uz.jabborovbahrom.openplayer.utils.Utils
import java.io.ByteArrayOutputStream

private const val ARG_PARAM1 = "param1"

class PagerFragment : Fragment() {
    private var param1: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }
    }

    lateinit var binding: FragmentPagerBinding
    lateinit var albumAdapter: AlbumAdapter
    lateinit var artistAdapter: ArtistAdapter
    lateinit var songAdapter: SongAdapter
    lateinit var appDatabase: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPagerBinding.inflate(layoutInflater)
        appDatabase = AppDatabase.getInstance(requireContext())

        when (param1) {
            1 -> {
                val allSong = appDatabase.songDao().getAllSong()
                songAdapter = SongAdapter(allSong, object : SongAdapter.OnClickListener {
                    override fun onViewClick(song: Song, position: Int) {
                        val bundle = Bundle()
                        bundle.putInt("pos", position)
                        val songs = ArrayList<Parcelable>()
                        songs.addAll(allSong)
                        bundle.putParcelableArrayList("songs", songs)
                        findNavController().navigate(R.id.playMusicFragment, bundle)
                    }
                })
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rv.layoutManager = layoutManager
                binding.rv.adapter = songAdapter
            }
            2 -> {
                val allArtistName = appDatabase.songDao().getAllArtistsName()
                artistAdapter =
                    ArtistAdapter(allArtistName, object : ArtistAdapter.OnClickListener {
                        override fun onViewClick(artist: String) {
                            val bundle = Bundle()
                            bundle.putString("artist", artist)
                            findNavController().navigate(R.id.albumFragment, bundle)
                        }
                    })
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rv.layoutManager = layoutManager
                binding.rv.adapter = artistAdapter
            }
            3 -> {
                val bmList = ArrayList<Bitmap>()
                val allAlbumsName = appDatabase.songDao().getAllAlbumsName()
                for (s in allAlbumsName) {
                    val albumId = appDatabase.songDao().getAlbumIdByAlbumName(s)
                    val bm = Utils.getAlbumArt(requireContext(), albumId)
                    bmList.add(bm!!)
                }

                albumAdapter =
                    AlbumAdapter(allAlbumsName, bmList, object : AlbumAdapter.OnClickListener {
                        override fun onViewClick(album: String, bm: Bitmap) {
                            val bundle = Bundle()
                            bundle.putString("album", album)
                            val stream = ByteArrayOutputStream()
                            bm.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            val byteArray: ByteArray = stream.toByteArray()
                            bundle.putByteArray("image", byteArray)
                            findNavController().navigate(R.id.albumFragment, bundle)
                        }
                    })

                val layoutManager =
                    GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
                binding.rv.layoutManager = layoutManager
                binding.rv.adapter = albumAdapter
            }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
            PagerFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}