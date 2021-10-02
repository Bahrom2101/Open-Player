package uz.jabborovbahrom.openplayer

import android.Manifest
import android.app.AlertDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.florent37.runtimepermission.kotlin.askPermission
import jabborovbahrom.openplayer.R
import jabborovbahrom.openplayer.databinding.ActivitySplashBinding
import jabborovbahrom.openplayer.databinding.SelectLanguageBinding
import uz.jabborovbahrom.openplayer.utils.Utils
import java.util.*

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (Utils.getLanguage(this)) {
            "" -> {
                var lan = "uz"
                val selectDialog = SelectLanguageBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(this).create()
                dialog.setView(selectDialog.root)
                selectDialog.lan1.setOnClickListener {
                    lan = "uz"
                    selectDialog.image1.setImageResource(R.drawable.ic_tick)
                    selectDialog.image2.setImageDrawable(null)
                }
                selectDialog.lan2.setOnClickListener {
                    lan = "en"
                    selectDialog.image2.setImageResource(R.drawable.ic_tick)
                    selectDialog.image1.setImageDrawable(null)
                }
                selectDialog.ok.setOnClickListener {
                    Utils.setLanguage(lan, this)
                    if (lan == "uz") {
                        binding.tv.text =
                            "Iltimos tugmani bosing va qurilma xotirasiga ruxsat bering"
                        setApplicationLocale(lan)
                    }
                    dialog.dismiss()
                }
                dialog.setCancelable(false)
                dialog.show()
            }
            "uz" -> {
                setApplicationLocale("uz")
            }
            "en" -> {
                setApplicationLocale("en")
            }
        }
        binding.btn.setOnClickListener {
            askPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) {
                finish()
            }.onDeclined { e ->
                if (e.hasDenied()) {
                    //the list of denied permissions
                    AlertDialog.Builder(this)
                        .setMessage("Please accept our permissions")
                        .setPositiveButton("yes") { _, _ ->
                            e.askAgain();
                        } //ask again
                        .setNegativeButton("no") { dialog, _ ->
                            dialog.dismiss();
                        }
                        .show();
                }

                if (e.hasForeverDenied()) {
                    e.goToSettings();
                }
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

}