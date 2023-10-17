package com.dial.superplinko.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.dial.superplinko.Constant.ANSWER_NO
import com.dial.superplinko.Constant.ANSWER_NO_PUSH
import com.dial.superplinko.Constant.IMG_BACKGROUND
import com.dial.superplinko.Constant.IMG_LOGO
import com.dial.superplinko.Constant.PATH_GAME
import com.dial.superplinko.api.ApiInterface
import com.dial.superplinko.api.ApiUtilities
import com.dial.superplinko.databinding.ActivitySplashBinding
import com.dial.superplinko.repository.InfoUrlRepository
import com.dial.superplinko.viewmodel.InfoUrlViewModel
import com.dial.superplinko.viewmodel.InfoUrlViewModelFactory
import com.onesignal.OneSignal
import java.util.Locale

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var infoUrlViewModel: InfoUrlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.imgLogoApp.load(IMG_LOGO)
        binding.imgBackground.load(IMG_BACKGROUND)

        Handler(Looper.getMainLooper()).postDelayed({

            val apiInterface = ApiUtilities.getInstance().create(ApiInterface::class.java)

            val infoIPRepository = InfoUrlRepository(apiInterface)

            infoUrlViewModel = ViewModelProvider(this, InfoUrlViewModelFactory(infoIPRepository,
                getSystemDetail(), Locale.getDefault().language, Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
            )[InfoUrlViewModel::class.java]

            infoUrlViewModel.info.observe(this) { it1 ->
                val intent = Intent(this@SplashActivity, WebViewActivity::class.java)

                when(it1.url) {
                    ANSWER_NO -> {
                        intent.putExtra("LINK", PATH_GAME)
                    }
                    ANSWER_NO_PUSH -> {
                        OneSignal.disablePush(true)
                        intent.putExtra("LINK", PATH_GAME)
                    } else -> {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        OneSignal.promptForPushNotifications(false) {
                            intent.putExtra("LINK", it1.url)
                        }
                    } else {
                        intent.putExtra("LINK", it1.url)
                    }
                }
                }
                startActivity(intent)
                finish()
            }
        }, 3000)
    }

    @SuppressLint("HardwareIds")
    private fun getSystemDetail(): String {
        return Build.BRAND + " " + Build.MODEL
    }
}