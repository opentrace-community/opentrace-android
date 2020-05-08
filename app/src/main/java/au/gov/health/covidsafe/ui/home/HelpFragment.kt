package au.gov.health.covidsafe.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.atlassian.mobilekit.module.feedback.FeedbackModule
import kotlinx.android.synthetic.main.fragment_help.*
import kotlinx.android.synthetic.main.fragment_help.view.*
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.BaseFragment

class HelpFragment : BaseFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_help, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = view.helpWebView
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = createWebVieClient(view)
        webView.loadUrl(HELP_URL)
        reportAnIssue.setOnClickListener {
            FeedbackModule.showFeedbackScreen()
        }
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private fun createWebVieClient(view: View): WebViewClient =
            object : WebViewClient() {
                private var isRedirecting = false
                private var loadFinished = false

                override fun shouldOverrideUrlLoading(webView: WebView, request: WebResourceRequest): Boolean {
                    if (!loadFinished) isRedirecting = true
                    loadFinished = false
                    val urlString = request.url.toString()
                    if (urlString == HELP_URL) {
                        webView.loadUrl(request.url.toString())
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                        webView.context.startActivity(intent)
                    }
                    return true
                }

                override fun onPageStarted(webView: WebView, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(webView, url, favicon)
                    loadFinished = false
                    view.progress.isVisible = true
                }

                override fun onPageFinished(webView: WebView, url: String?) {
                    super.onPageFinished(webView, url)

                    if (!isRedirecting) loadFinished = true

                    if (loadFinished && !isRedirecting) {
                        view.progress.isVisible = false
                    } else {
                        isRedirecting = false
                    }
                }
            }
}

private const val HELP_URL = "https://www.covidsafe.gov.au/help-topics.html"
