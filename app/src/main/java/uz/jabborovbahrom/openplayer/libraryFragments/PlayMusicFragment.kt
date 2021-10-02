package uz.jabborovbahrom.openplayer.libraryFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import jabborovbahrom.openplayer.R
import jabborovbahrom.openplayer.databinding.FragmentPlayMusicBinding
import uz.jabborovbahrom.openplayer.entity.Song
import uz.jabborovbahrom.openplayer.services.PlaybackInfoListener
import uz.jabborovbahrom.openplayer.services.SongService
import uz.jabborovbahrom.openplayer.services.SongService.Companion.ACTION_NEXT
import uz.jabborovbahrom.openplayer.services.SongService.Companion.ACTION_PLAY
import uz.jabborovbahrom.openplayer.services.SongService.Companion.ACTION_PLAY_PAUSE
import uz.jabborovbahrom.openplayer.services.SongService.Companion.ACTION_PLAY_POS
import uz.jabborovbahrom.openplayer.services.SongService.Companion.ACTION_PREV
import uz.jabborovbahrom.openplayer.services.SongService.Companion.currentPos
import uz.jabborovbahrom.openplayer.services.SongService.Companion.getMediaPlayerCurrentTime
import uz.jabborovbahrom.openplayer.services.SongService.Companion.isPaused
import uz.jabborovbahrom.openplayer.services.SongService.Companion.mPlaybackInfoListener
import uz.jabborovbahrom.openplayer.services.SongService.Companion.setMediaPlayerCurrentTime
import uz.jabborovbahrom.openplayer.utils.Utils
import kotlin.random.Random

class PlayMusicFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    lateinit var binding: FragmentPlayMusicBinding
    private var playbackListener: PlaybackListener? = null
    lateinit var songs: ArrayList<Song>
    lateinit var handler: Handler
    var currentPosition: Int = -1
    var isDestroy = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayMusicBinding.inflate(layoutInflater)

        initUI()

        playbackListener = PlaybackListener()

        mPlaybackInfoListener = playbackListener

        binding.playPause.setOnClickListener {
            if (isPaused) {
                binding.playPause.setImageResource(R.drawable.ic_pause_red)
            } else {
                binding.playPause.setImageResource(R.drawable.ic_play_red)
            }
            onPlayPauseClick()
        }

        binding.next.setOnClickListener {
            binding.playPause.setImageResource(R.drawable.ic_pause_red)
            if (currentPosition == songs.size - 1)
                currentPosition = 0
            else
                currentPosition++
            onNextClick()
            setPlaySong()
        }

        binding.skip.setOnClickListener {
            binding.playPause.setImageResource(R.drawable.ic_pause_red)
            if (currentPosition == 0)
                currentPosition = songs.size - 1
            else
                currentPosition--
            onPrevClick()
            setPlaySong()
        }

        binding.shuffle.setOnClickListener {
            if (Utils.getIsShuffle(requireContext())) {
                Utils.setIsShuffle(false, requireContext())
                binding.shuffle.setImageResource(R.drawable.ic_shuffle)
            } else {
                Utils.setIsShuffle(true, requireContext())
                binding.shuffle.setImageResource(R.drawable.ic_shuffle_red)
            }
        }
        binding.repeat.setOnClickListener {
            if (Utils.getIsRepeat(requireContext())) {
                Utils.setIsRepeat(false, requireContext())
                binding.repeat.setImageResource(R.drawable.ic_repeat)
            } else {
                Utils.setIsRepeat(true, requireContext())
                binding.repeat.setImageResource(R.drawable.ic_repeat_red)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setMediaPlayerCurrentTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        return binding.root
    }

    private fun playRand() {
        val intent = Intent(requireActivity().baseContext, SongService::class.java)
        intent.action = ACTION_PLAY_POS
        currentPosition = Random.nextInt(0, songs.size)
        intent.putExtra("pos", currentPosition)
        requireActivity().startService(intent)
    }

    private fun onPlayPauseClick() {
        val intent = Intent(requireActivity().baseContext, SongService::class.java)
        intent.action = ACTION_PLAY_PAUSE
        requireActivity().startService(intent)
    }

    private fun onPrevClick() {
        if (!Utils.getIsShuffle(requireContext())) {
            val intent = Intent(requireActivity().baseContext, SongService::class.java)
            intent.action = ACTION_PREV
            requireActivity().startService(intent)
        } else {
            playRand()
        }
    }

    private fun onNextClick() {
        if (!Utils.getIsShuffle(requireContext())) {
            val intent = Intent(requireActivity().baseContext, SongService::class.java)
            intent.action = ACTION_NEXT
            requireActivity().startService(intent)
        } else {
            playRand()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {
        val currentTrackBar =
            requireActivity().findViewById<View>(R.id.view)
        val bottomView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        currentTrackBar.visibility = View.GONE
        bottomView.visibility = View.GONE

        if (isPaused)
            binding.playPause.setImageResource(R.drawable.ic_play_red)
        else
            binding.playPause.setImageResource(R.drawable.ic_pause_red)

        if (!Utils.getIsShuffle(requireContext()))
            binding.shuffle.setImageResource(R.drawable.ic_shuffle)
        else
            binding.shuffle.setImageResource(R.drawable.ic_shuffle_red)

        if (!Utils.getIsRepeat(requireContext()))
            binding.repeat.setImageResource(R.drawable.ic_repeat)
        else
            binding.repeat.setImageResource(R.drawable.ic_repeat_red)

        val clickTrack = arguments?.getBoolean("clickTrackBar", false)

        if (clickTrack == true) {
            try {
                songs = SongService.songs!!
                val song = arguments?.getSerializable("song") as Song

                if (isPaused)
                    binding.playPause.setImageResource(R.drawable.ic_play_red)
                else
                    binding.playPause.setImageResource(R.drawable.ic_pause_red)

                if (!Utils.getIsShuffle(requireContext()))
                    binding.shuffle.setImageResource(R.drawable.ic_shuffle)
                else
                    binding.shuffle.setImageResource(R.drawable.ic_shuffle_red)

                if (!Utils.getIsRepeat(requireContext()))
                    binding.repeat.setImageResource(R.drawable.ic_repeat)
                else
                    binding.repeat.setImageResource(R.drawable.ic_repeat_red)

                setPlaySongTrack(song)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            currentPosition = arguments?.getInt("pos", -1)!!
            songs = arguments?.getSerializable("songs") as ArrayList<Song>
            binding.currentCount.text = "${currentPosition + 1}/${songs.size}"
            binding.title.text = songs[currentPosition].title
            binding.artist.text = songs[currentPosition].artist
            val bm = Utils.getAlbumArt(requireContext(), songs[currentPosition].albumId)
            binding.image.setImageBitmap(bm)
            val duration = songs[currentPosition].duration
            val seconds = duration % 60
            val minutes = duration / 60
            if (seconds <= 9)
                binding.duration.text = "$minutes:0$seconds"
            else
                binding.duration.text = "$minutes:$seconds"

            if (songs[currentPosition].mediaStoreId != Utils.getLastSongId(requireContext())) {
                val intent = Intent(requireActivity(), SongService::class.java)
                val bundle = Bundle()
                bundle.putInt("pos", currentPosition)
                bundle.putParcelableArrayList("songs", songs)
                intent.putExtra("bundle", bundle)
                intent.action = ACTION_PLAY
                requireActivity().startService(intent)

                handler = Handler(Looper.getMainLooper())
                binding.seekBar.max = duration * 1000
                handler.postDelayed(runnable, 100)
            } else if (songs[currentPosition].mediaStoreId == Utils.getLastSongId(requireContext()) && !isPaused) {
                val intent = Intent(requireActivity(), SongService::class.java)
                val bundle = Bundle()
                bundle.putInt("pos", currentPosition)
                bundle.putParcelableArrayList("songs", songs)
                intent.putExtra("bundle", bundle)
                intent.action = ACTION_PLAY
                requireActivity().startService(intent)

                handler = Handler(Looper.getMainLooper())
                binding.seekBar.max = duration * 1000
                handler.postDelayed(runnable, 100)
            } else {
                setPlaySong()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPlaySong() {
        binding.currentCount.text = "${currentPosition + 1}/${songs.size}"
        binding.title.text = songs[currentPosition].title
        binding.artist.text = songs[currentPosition].artist
        val bm = Utils.getAlbumArt(context?.applicationContext!!, songs[currentPosition].albumId)
        binding.image.setImageBitmap(bm)
        val duration = songs[currentPosition].duration
        val seconds = duration % 60
        val minutes = duration / 60
        if (seconds <= 9)
            binding.duration.text = "$minutes:0$seconds"
        else
            binding.duration.text = "$minutes:$seconds"

        handler = Handler(Looper.getMainLooper())
        binding.seekBar.max = duration * 1000
        handler.postDelayed(runnable, 100)
    }

    @SuppressLint("SetTextI18n")
    private fun setPlaySongTrack(song: Song) {
        binding.currentCount.text = "${currentPos + 1}/${songs.size}"
        binding.title.text = song.title
        binding.artist.text = song.artist
        val bm = Utils.getAlbumArt(context?.applicationContext!!, song.albumId)
        binding.image.setImageBitmap(bm)
        val duration = song.duration
        val seconds = duration % 60
        val minutes = duration / 60
        if (seconds <= 9)
            binding.duration.text = "$minutes:0$seconds"
        else
            binding.duration.text = "$minutes:$seconds"

        handler = Handler(Looper.getMainLooper())
        binding.seekBar.max = duration * 1000
        handler.postDelayed(runnable, 100)
    }

    private var runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            try {
                binding.seekBar.progress = getMediaPlayerCurrentTime()
                val min = getMediaPlayerCurrentTime() / 1000 / 60
                val sek = getMediaPlayerCurrentTime() / 1000 % 60
                var time = ""
                time += if (min < 10)
                    "0$min"
                else
                    "$min"
                time += ":"
                time += if (sek < 10)
                    "0$sek"
                else
                    "$sek"
                binding.currentTime.text = time
                handler.postDelayed(this, 100)
            } catch (e: Exception) {
            }
        }
    }

    internal inner class PlaybackListener : PlaybackInfoListener() {

        override fun onPositionChanged(position: Int) {
            currentPosition = position
            if (!isDestroy)
                setPlaySong()
        }

        override fun onStateChanged(@State state: Int) {
            if (state == 2) {
                if (!isPaused)
                    binding.playPause.setImageResource(R.drawable.ic_pause_red)
                else
                    binding.playPause.setImageResource(R.drawable.ic_play_red)

            }
        }

        override fun onPlaybackCompleted() {
            //After playback is complete
        }
    }

    override fun onDestroy() {
        isDestroy = true
        val currentTrackBar =
            requireActivity().findViewById<View>(R.id.view)
        val bottomView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        currentTrackBar.visibility = View.VISIBLE
        bottomView.visibility = View.VISIBLE
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                val bundle = Bundle()
                bundle.putString("state", "Lib")
                findNavController().navigate(R.id.searchFragment, bundle)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}