package com.dial.superplinko.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.dial.superplinko.databinding.ActivityWebViewBinding


@Suppress("DEPRECATION")
class WebViewActivity : AppCompatActivity() {

    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private lateinit var binding: ActivityWebViewBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        if(savedInstanceState != null){
            savedInstanceState.getString("link")?.let { webViewSetup(it) }
            savedInstanceState.getBundle("webViewState")?.let { (binding.webView).restoreState(it) }
        } else {
            intent.getStringExtra("LINK")?.let { webViewSetup(it) }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewSetup(link: String) {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.allowContentAccess = true
            settings.allowFileAccess = true
            settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            settings.loadWithOverviewMode = true
            settings.allowUniversalAccessFromFileURLs = true

            binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            loadUrl(link)
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {

            // при входе в полноэкранный режим
            override fun onShowCustomView(view: View?, callback: CustomViewCallback) {
                binding.frameWeb.visibility = View.VISIBLE
                binding.frameWeb.addView(view)
                binding.webView.visibility = View.GONE
            }

            override fun onHideCustomView() {
                binding.frameWeb.visibility = View.GONE
                binding.frameWeb.removeAllViews()
                binding.webView.visibility = View.VISIBLE
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                uploadMessageAboveL = filePathCallback
                openImageChooserActivity()
                return true
            }
        }
    }

    override fun onBackPressed() {
        if(binding.webView.canGoBack()) binding.webView.goBack() else  super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val bundle = Bundle()
        binding.webView.saveState(bundle)
        outState.putBundle("webViewState", bundle)
        outState.putString("link", intent.getStringExtra("LINK"))
    }

    private fun openImageChooserActivity() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return
            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data)
            } else if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(result)
                uploadMessage = null
            }
        }
    }

    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = Array(clipData.itemCount){
                            i -> clipData.getItemAt(i).uri
                    }
                }
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        uploadMessageAboveL!!.onReceiveValue(results)
        uploadMessageAboveL = null
    }

    companion object {
        private const val FILE_CHOOSER_RESULT_CODE = 10000
    }
}