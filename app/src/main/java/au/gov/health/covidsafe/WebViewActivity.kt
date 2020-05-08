package au.gov.health.covidsafe

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity
import au.gov.health.covidsafe.logging.CentralLog

class WebViewActivity : FragmentActivity() {

    companion object {
        val URL_ARG = "URL_ARG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        val webView = findViewById<WebView>(R.id.webview)
        webView.webViewClient = WebViewClient()
        if (intent.getStringExtra(URL_ARG).isNullOrBlank()) {
            webView.loadUrl("https://www.australia.gov.au")
        } else {
            webView.loadUrl(intent.getStringExtra(URL_ARG))
        }

        val wbc: WebChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(w: WebView) {
                CentralLog.d("WebViewActivity", "Window trying to close")
            }
        }

        webView.webChromeClient = wbc
    }
}
