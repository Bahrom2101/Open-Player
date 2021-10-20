package uz.jabborovbahrom.openplayer

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import io.grpc.okhttp.internal.Util
import jabborovbahrom.openplayer.R
import jabborovbahrom.openplayer.databinding.ActivityMainBinding
import uz.jabborovbahrom.openplayer.db.AppDatabase
import uz.jabborovbahrom.openplayer.entity.Song
import uz.jabborovbahrom.openplayer.services.SongService
import uz.jabborovbahrom.openplayer.services.SongService.Companion.getCurrentSong
import uz.jabborovbahrom.openplayer.services.SongService.Companion.mPlaybackInfoListener2
import uz.jabborovbahrom.openplayer.services.UploadService
import uz.jabborovbahrom.openplayer.utils.Utils
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var appDatabase: AppDatabase
    private var playbackListener2: PlaybackInfoListener2? = null
    var isDestroy = false
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val directory =
            File(
                Environment.getExternalStorageDirectory(),
                File.separator.toString() + Environment.DIRECTORY_MUSIC + "/Open Player"
            )
        if (!directory.exists())
            directory.mkdirs()

        setSupportActionBar(binding.toolbar)

        when (Utils.getLanguage(this)) {
            "uz" -> {
                setApplicationLocale("uz")
            }
            "en" -> {
                setApplicationLocale("en")
            }
        }

        binding.view.root.setOnClickListener {
            if (getCurrentSong() != null) {
                val bundle = Bundle()
                val song = getCurrentSong()
                bundle.putBoolean("clickTrackBar", true)
                bundle.putSerializable("song", song)
                navController.navigate(R.id.playMusicFragment, bundle)
            }
        }

        binding.view.playPause.setOnClickListener {
            try {
                if (SongService.isPaused) {
                    binding.view.playPause.setImageResource(R.drawable.ic_pause)
                } else {
                    binding.view.playPause.setImageResource(R.drawable.ic_play)
                }
                if (getCurrentSong() != null) {
                    onPlayPauseClick()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setApplicationLocale(locale: String) {
        val resources = resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(Locale(locale.lowercase(Locale.getDefault())))
        } else {
            config.locale = Locale(locale.lowercase(Locale.getDefault()))
        }
        resources.updateConfiguration(config, dm)
    }

    private fun onPlayPauseClick() {
        val intent = Intent(this.baseContext, SongService::class.java)
        intent.action = SongService.ACTION_PLAY_PAUSE
        startService(intent)
    }

    override fun onStart() {
        super.onStart()
        if (!permission()) {
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
        } else {
            when (Utils.getLanguage(this)) {
                "uz" -> {
                    setApplicationLocale("uz")
                }
                "en" -> {
                    setApplicationLocale("en")
                }
            }

            appDatabase = AppDatabase.getInstance(this)
            appDatabase.songDao().deleteAllSong()
            writeDatabase()

            playbackListener2 = PlaybackInfoListener2()

            mPlaybackInfoListener2 = playbackListener2

            navController = findNavController(R.id.my_nav_host_fragment)

            binding.bottomNavigation.setupWithNavController(navController)

            binding.bottomNavigation.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.library -> {
                        navController.popBackStack()
                        navController.navigate(R.id.homeFragment)
                        true
                    }
                    R.id.network -> {
                        navController.popBackStack()
                        navController.navigate(R.id.netMusicFragment)
                        true
                    }
                    else -> false
                }
            }
            setTrackBar()
        }
    }

    private fun permission(): Boolean {
        return checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun setTrackBar() {
        if (getCurrentSong() != null) {
            val song = getCurrentSong()
            binding.view.image.setImageBitmap(Utils.getAlbumArt(this, song?.albumId))
            binding.view.title.text = song?.title
            binding.view.artist.text = song?.artist
            if (SongService.isPaused) {
                binding.view.playPause.setImageResource(R.drawable.ic_play)
            } else {
                binding.view.playPause.setImageResource(R.drawable.ic_pause)
            }
        }
    }

    internal inner class PlaybackInfoListener2 :
        uz.jabborovbahrom.openplayer.services.PlaybackInfoListener2() {

        override fun onPositionChanged(position: Int) {
            setTrackBar()
        }

        override fun onStateChanged(@State state: Int) {
            setTrackBar()
        }

        override fun onPlaybackCompleted() {
            //After playback is complete
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        if (!Utils.getPermission(this))
            if (!Utils.getWork(this)) {
                val workRequest: WorkRequest =
                    PeriodicWorkRequestBuilder<UploadService>(7, TimeUnit.DAYS)
                        .setConstraints(
                            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .setInitialDelay(1, TimeUnit.SECONDS)
                        .build()
                WorkManager.getInstance(this)
                    .enqueue(workRequest)
            }
        isDestroy = true
        super.onDestroy()
    }

    private fun writeDatabase() {
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE
        )

// Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(1, TimeUnit.MILLISECONDS).toString()
        )

// Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = applicationContext.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        query?.use { cursor ->
            // Cache column indices.
            val mediaStoreIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val pathColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                // Get values of columns for a given audio.
                val mediaStoreId = cursor.getLong(mediaStoreIdColumn)
                val displayName = cursor.getString(displayNameColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val path = cursor.getString(pathColumn)
                val duration = cursor.getInt(durationColumn) / 1000
                val album = cursor.getString(albumColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val size = cursor.getInt(sizeColumn)
                val coverArt = ContentUris.withAppendedId(artworkUri, albumId).toString()

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mediaStoreId
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                val songUploadedById =
                    appDatabase.songUploadedDao().getSongUploadedById(mediaStoreId)
                var isUploaded = 0
                if (songUploadedById != null) {
                    isUploaded = 1
                }
                val song = Song(
                    mediaStoreId,
                    contentUri.toString(),
                    displayName,
                    title,
                    artist,
                    path,
                    duration,
                    album,
                    albumId,
                    size,
                    coverArt,
                    isUploaded
                )
                val songById = appDatabase.songDao().getSongById(song.mediaStoreId)
                if (songById == null) {
                    appDatabase.songDao().addSong(song)
                }
            }
        }
    }

}