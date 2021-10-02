package uz.jabborovbahrom.openplayer.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import jabborovbahrom.openplayer.R
import jabborovbahrom.openplayer.databinding.ActivityReportInfoBinding
import jabborovbahrom.openplayer.databinding.CustomPlayBinding
import uz.jabborovbahrom.openplayer.models.Report
import uz.jabborovbahrom.openplayer.models.SongFirebase
import uz.jabborovbahrom.openplayer.utils.NetworkHelper

class ReportInfoActivity : AppCompatActivity() {

    lateinit var binding: ActivityReportInfoBinding
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference: StorageReference
    lateinit var songFirebase: SongFirebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val bundle = intent.getBundleExtra("bundle")
        val report = bundle?.getSerializable("report") as Report

        binding.type.text = report.type
        binding.isChecked.text = if (report.isChecked!!) {
            "true"
        } else {
            "false"
        }
        binding.description.text = report.description

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("audios")

        firebaseFirestore.collection("audios")
            .document(report.uid!!)
            .get()
            .addOnSuccessListener {
                songFirebase = it.toObject(SongFirebase::class.java)!!
                binding.title.text = songFirebase.title
                binding.artist.text = songFirebase.artist
                val duration = songFirebase.duration
                val seconds = duration!! % 60
                val minutes = duration / 60

                if (seconds <= 9) {
                    binding.duration.text = "$minutes:0$seconds"
                } else {
                    binding.duration.text = "$minutes:$seconds"
                }

                val size = songFirebase.size!! / 100000
                binding.size.text = "${size / 10}.${size % 10} MB"

            }

        binding.playPause.setOnClickListener {
            playFirebase(
                songFirebase.title!!,
                songFirebase.downloadUrl!!,
                songFirebase.duration!!
            )
        }
    }

    private fun playFirebase(title: String, downloadUrl: String, duration: Int) {
        if (NetworkHelper(this).isNetworkConnected()) {
            val handler = Handler(Looper.getMainLooper())
            val playBinding = CustomPlayBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(this).create()
            dialog.setView(playBinding.root)
            val seconds = duration % 60
            val minutes = duration / 60
            if (seconds <= 9)
                playBinding.duration.text = "$minutes:0$seconds"
            else
                playBinding.duration.text = "$minutes:$seconds"
            playBinding.title.text = title

            val mediaPlayer = MediaPlayer()

            dialog.setOnShowListener {
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer.setDataSource(downloadUrl)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    mediaPlayer.start()
                    playBinding.progressBar.visibility = View.GONE
                }

                mediaPlayer.setOnCompletionListener {
                    playBinding.playPause.setImageResource(R.drawable.ic_play)
                }

                playBinding.seekBar.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }
                })
                playBinding.seekBar.max = duration * 1000
                val runnable = object : Runnable {
                    @SuppressLint("SetTextI18n")
                    override fun run() {
                        try {
                            playBinding.seekBar.progress =
                                mediaPlayer.currentPosition
                            val min =
                                mediaPlayer.currentPosition / 1000 / 60
                            val sek =
                                mediaPlayer.currentPosition / 1000 % 60
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
                            playBinding.currentTime.text = time
                            handler.postDelayed(this, 100)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                handler.postDelayed(runnable, 100)
                playBinding.playPause.setOnClickListener {
                    if (mediaPlayer.isPlaying) {
                        playBinding.playPause.setImageResource(R.drawable.ic_play)
                        mediaPlayer.pause()
                    } else {
                        playBinding.playPause.setImageResource(R.drawable.ic_pause)
                        mediaPlayer.start()
                    }
                }
            }

            dialog.setOnDismissListener {
                handler.removeCallbacksAndMessages(null)
                mediaPlayer.reset()
                mediaPlayer.stop()
            }
            dialog.show()

        } else {
            Toast.makeText(
                this,
                getString(R.string.internet_is_not_connected),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

}