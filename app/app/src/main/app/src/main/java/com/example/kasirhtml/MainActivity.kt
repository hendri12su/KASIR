package com.example.kasirhtml

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                KasirWebView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onShareReceipt = { receiptText -> shareText(receiptText) }
                )
            }
        }
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Bagikan Struk / Laporan"))
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KasirWebView(
    modifier: Modifier = Modifier,
    onShareReceipt: (String) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()

                addJavascriptInterface(
                    WebAppInterface(
                        context = context,
                        onShareReceipt = onShareReceipt
                    ),
                    "AndroidBridge"
                )

                loadUrl("file:///android_asset/index.html")
            }
        }
    )
}

class WebAppInterface(
    private val context: Context,
    private val onShareReceipt: (String) -> Unit
) {
    private val prefs = context.getSharedPreferences("kasir_prefs", Context.MODE_PRIVATE)
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun saveState(jsonPayload: String) {
        prefs.edit().putString("state_json", jsonPayload).apply()
    }

    @JavascriptInterface
    fun loadState(): String {
        return prefs.getString("state_json", "") ?: ""
    }

    @JavascriptInterface
    fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun shareReceipt(receiptText: String) {
        mainHandler.post {
            onShareReceipt(receiptText)
        }
    }
}
