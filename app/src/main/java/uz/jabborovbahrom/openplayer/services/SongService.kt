package uz.jabborovbahrom.openplayer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import jabborovbahrom.openplayer.R
import uz.jabborovbahrom.openplayer.MainActivity
import uz.jabborovbahrom.openplayer.entity.Song
import uz.jabborovbahrom.openplayer.utils.Utils
import java.lang.Exception
import kotlin.random.Random

class SongService : Service(),MediaPlayer.OnCompletionListener {

    companion object {
        private var mediaPlayer: MediaPlayer? = null
        var isPaused = false
        var currentPos = 0
        var songs: ArrayList<Song>? = null
        var mPlaybackInfoListener: PlaybackInfoListener? = null
        var mPlaybackInfoListener2: PlaybackInfoListener2? = null
        const val ACTION_PLAY_POS = "com.yourapp.ACTION_PLAY_POS"
        const val ACTION_PLAY = "com.yourapp.ACTION_PLAY"
        const val ACTION_PLAY_PAUSE = "com.yourapp.ACTION_PLAY_PAUSE"
        const val ACTION_PAUSE = "com.yourapp.ACTION_PAUSE"
        const val ACTION_NEXT = "com.yourapp.ACTION_NEXT"
        const val ACTION_PREV = "com.yourapp.ACTION_PREV"
        fun getMediaPlayerCurrentTime(): Int {
            return mediaPlayer!!.currentPosition
        }

        fun setMediaPlayerCurrentTime(currentTime: Int) {
            return mediaPlayer!!.seekTo(currentTime)
        }

        fun getCurrentSong(): Song? {
            return if (songs == null)
                null
            else
                songs!![currentPos]
        }
    }

    lateinit var notificationManager: NotificationManager
    lateinit var notificationBuilder: NotificationCompat.Builder
    private val REQUEST_CODE = 100
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionManager: MediaSessionManager? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null
    private var mNotificationActionsReceiver: NotificationReceiver? = null
    private val TAG = "SongService"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        registerNotificationActionsReceiver(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (!TextUtils.isEmpty(action)) {
                when (action) {
                    ACTION_PLAY_PAUSE -> {
                        playPauseMediaPlayer()
                    }
                    ACTION_PLAY -> {
                        playMediaPlayer(intent)
                    }
                    ACTION_PAUSE -> {
                        pause()
                    }
                    ACTION_NEXT -> {
                        nextMediaPlayer()
                    }
                    ACTION_PREV -> {
                        prevMediaPlayer()
                    }
                    ACTION_PLAY_POS -> {
                        playPosMediaPlayer(intent)
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun pause() {
        try {
            isPaused = true
            mediaPlayer!!.pause()
            showControllerNotification(
                songs!![currentPos].title,
                songs!![currentPos].artist,
                songs!![currentPos].albumId,
                true
            )
            setStatus(PlaybackInfoListener.State.PAUSED)
            setStatus2(PlaybackInfoListener2.State.PAUSED)
        } catch (e: Exception) {
        }
    }

    private fun playPosMediaPlayer(intent: Intent) {
        isPaused = false
        currentPos = intent.getIntExtra("pos", -1)
        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(this, Uri.parse(songs!![currentPos].contentUri))
        mediaPlayer?.prepare()
        mediaPlayer?.start()

        showControllerNotification(
            songs!![currentPos].title,
            songs!![currentPos].artist,
            songs!![currentPos].albumId,
            false
        )
        setPosition(currentPos)
        setPosition2(currentPos)
        Utils.setLastSongId(songs!![currentPos].mediaStoreId, this)
        mediaPlayer!!.setOnCompletionListener(this)
    }

    private fun playMediaPlayer(intent: Intent) {
        isPaused = false
        val bundle = intent.extras?.getBundle("bundle")
        currentPos = bundle?.getInt("pos", -1)!!
        songs = bundle.getSerializable("songs") as ArrayList<Song>
        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(this, Uri.parse(songs!![currentPos].contentUri))
        mediaPlayer?.prepare()
        mediaPlayer?.start()

        showControllerNotification(
            songs!![currentPos].title,
            songs!![currentPos].artist,
            songs!![currentPos].albumId,
            false
        )
        setPosition(currentPos)
        setPosition2(currentPos)
        Utils.setLastSongId(songs!![currentPos].mediaStoreId, this)
        mediaPlayer!!.setOnCompletionListener(this)
    }

    private fun nextMediaPlayer() {
        if (currentPos == songs?.size!! - 1)
            currentPos = 0
        else
            currentPos++
        if (Utils.getIsShuffle(this))
            currentPos = Random.nextInt(0, songs!!.size)
        isPaused = false
        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(this, Uri.parse(songs!![currentPos].contentUri))
        mediaPlayer?.prepare()
        mediaPlayer?.start()
        showControllerNotification(
            songs!![currentPos].title,
            songs!![currentPos].artist,
            songs!![currentPos].albumId,
            false
        )
        setPosition(currentPos)
        setPosition2(currentPos)
        Utils.setLastSongId(songs!![currentPos].mediaStoreId, this)
    }

    private fun prevMediaPlayer() {
        if (currentPos == 0)
            currentPos = songs!!.size - 1
        else
            currentPos--
        if (Utils.getIsShuffle(this))
            currentPos = Random.nextInt(0, songs!!.size)
        isPaused = false
        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(this, Uri.parse(songs!![currentPos].contentUri))
        mediaPlayer?.prepare()
        mediaPlayer?.start()
        showControllerNotification(
            songs!![currentPos].title,
            songs!![currentPos].artist,
            songs!![currentPos].albumId,
            false
        )
        setPosition(currentPos)
        setPosition2(currentPos)
        Utils.setLastSongId(songs!![currentPos].mediaStoreId, this)
    }

    private fun playPauseMediaPlayer() {
        try {
            if (isPaused) {
                isPaused = false
                mediaPlayer!!.start()
                showControllerNotification(
                    songs!![currentPos].title,
                    songs!![currentPos].artist,
                    songs!![currentPos].albumId,
                    false
                )
                setStatus(PlaybackInfoListener.State.PLAYING)
                setStatus2(PlaybackInfoListener2.State.PLAYING)
            } else {
                isPaused = true
                mediaPlayer!!.pause()
                showControllerNotification(
                    songs!![currentPos].title,
                    songs!![currentPos].artist,
                    songs!![currentPos].albumId,
                    true
                )
                setStatus(PlaybackInfoListener.State.PAUSED)
                setStatus2(PlaybackInfoListener2.State.PAUSED)
            }
        } catch (e: Exception) {
        }
    }

    private fun setStatus(@PlaybackInfoListener.State state: Int) {
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener!!.onStateChanged(state)
        }

    }

    private fun setStatus2(@PlaybackInfoListener2.State state: Int) {
        if (mPlaybackInfoListener2 != null) {
            mPlaybackInfoListener2!!.onStateChanged(state)
        }

    }

    private fun setPosition(position: Int) {
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener!!.onPositionChanged(position)
        }
    }

    private fun setPosition2(position: Int) {
        if (mPlaybackInfoListener2 != null) {
            mPlaybackInfoListener2!!.onPositionChanged(position)
        }
    }

    private fun showControllerNotification(
        title: String,
        artist: String,
        albumId: Long,
        removableNotification: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel name"
            val descriptionText = "Channel description"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("1", name, importance).apply {
                description = descriptionText
            }

            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setShowBadge(false)

            // Register the channel with the system
            notificationManager.areNotificationsEnabled()
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, 0)

        initMediaSession(songs!![currentPos])

        notificationBuilder = NotificationCompat.Builder(this, "1")
            .setShowWhen(false)
            .setSmallIcon(R.drawable.ic_musical_note)
            .setLargeIcon(Utils.getAlbumArt(this, albumId))
            .setColor(ContextCompat.getColor(this, R.color.main_color))
            .setContentTitle(title)
            .setContentText(artist)
            .setContentIntent(pendingIntent)
            .addAction(notificationAction(ACTION_PREV))
            .addAction(notificationAction(ACTION_PLAY_PAUSE))
            .addAction(notificationAction(ACTION_NEXT))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession!!.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )

        if (removableNotification) {
            notificationManager.notify(1, notificationBuilder.build())
            stopForeground(false)
        } else {
            startForeground(1, notificationBuilder.build())
        }
    }

    private fun initMediaSession(song: Song) {
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        mediaSession = MediaSessionCompat(this, "AudioPlayer")
        transportControls = mediaSession!!.controller.transportControls
        mediaSession!!.isActive = true
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        updateMetaData(song)
    }

    private fun updateMetaData(song: Song) {
        mediaSession!!.setMetadata(
            MediaMetadataCompat.Builder()
                .putBitmap(
                    MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    Utils.getAlbumArt(this, song.albumId)
                )
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .build()
        )
    }

    private fun notificationAction(action: String): NotificationCompat.Action {
        val icon = when (action) {
            ACTION_PREV -> R.drawable.ic_skip
            ACTION_PLAY_PAUSE ->
                if (isPaused)
                    R.drawable.ic_play
                else
                    R.drawable.ic_pause
            ACTION_NEXT -> R.drawable.ic_next
            else -> R.drawable.ic_skip
        }
        return NotificationCompat.Action.Builder(icon, action, playerAction(action)).build()
    }

    private fun playerAction(action: String): PendingIntent {
        val pauseIntent = Intent()
        pauseIntent.action = action
        return PendingIntent.getBroadcast(
            this,
            REQUEST_CODE,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun registerActionsReceiver() {
        mNotificationActionsReceiver = NotificationReceiver()
        val intentFilter = IntentFilter()

        intentFilter.addAction(ACTION_PREV)
        intentFilter.addAction(ACTION_PLAY_PAUSE)
        intentFilter.addAction(ACTION_NEXT)
//        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
//        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
//        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
//        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        registerReceiver(mNotificationActionsReceiver, intentFilter)
    }

    private fun unregisterActionsReceiver() {
        if (mNotificationActionsReceiver != null) {
            try {
                unregisterReceiver(mNotificationActionsReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    private fun registerNotificationActionsReceiver(isReceiver: Boolean) {
        if (isReceiver) {
            registerActionsReceiver()
        } else {
            unregisterActionsReceiver()
        }
    }

    private inner class NotificationReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            // TODO Auto-generated method stub
            val action = intent.action

            if (action != null) {

                when (action) {
                    ACTION_PREV -> {
                        prevMediaPlayer()
                    }
                    ACTION_PLAY_PAUSE -> {
                        playPauseMediaPlayer()
                    }
                    ACTION_NEXT -> {
                        nextMediaPlayer()
                    }

//                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> if (songs!![currentPos] != null) {
//                        playPauseMediaPlayer()
//                    }
//                    BluetoothDevice.ACTION_ACL_CONNECTED -> if (songs!![currentPos] != null && isPaused) {
//                        playPauseMediaPlayer()
//                    }
//                    Intent.ACTION_HEADSET_PLUG -> if (songs!![currentPos] != null) {
//                        when (intent.getIntExtra("state", -1)) {
//                            //0 means disconnected
//                            0 -> playPauseMediaPlayer()
//                            //1 means connected
//                            1 -> if (isPaused) {
//                                playPauseMediaPlayer()
//                            }
//                        }
//                    }
//                    AudioManager.ACTION_AUDIO_BECOMING_NOISY -> if (isPlaying()) {
//                        pauseMediaPlayer()
//                    }
                }
            }
        }
    }

    override fun onDestroy() {
        registerNotificationActionsReceiver(false)
        super.onDestroy()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (mPlaybackInfoListener != null) {
            when {
                currentPos != songs?.size!! - 1 -> nextMediaPlayer()
                Utils.getIsRepeat(this) -> nextMediaPlayer()
                else -> isPaused = true
            }

            mPlaybackInfoListener!!.onStateChanged(PlaybackInfoListener.State.COMPLETED)
            mPlaybackInfoListener!!.onPlaybackCompleted()
        }
        if (mPlaybackInfoListener2 != null) {
            mPlaybackInfoListener2!!.onStateChanged(PlaybackInfoListener2.State.COMPLETED)
            mPlaybackInfoListener2!!.onPlaybackCompleted()
        }
    }
}