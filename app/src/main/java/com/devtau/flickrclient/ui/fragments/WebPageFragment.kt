package com.devtau.flickrclient.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.devtau.flickrclient.R
import com.devtau.rest.model.RequestSignature
import com.devtau.rest.util.NetUtils
import com.devtau.rest.util.Logger

class WebPageFragment: Fragment() {

    private var page: String? = null
    private var url: String? = null
    private var webView: WebView? = null
    private var listener: Listener? = null


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is Listener) listener = context
        else throw RuntimeException(context?.toString() + " must implement WebPageFragment Listener")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(URL)
        page = arguments?.getString(PAGE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_web_view, container, false)
        setupWebView(root)
        changeContent(url, page)
        return root
    }


    fun changeContent(url: String?, page: String?) {
        if (url != null && page != null) throw RuntimeException("url OR page. not both")
        if (url == null && page == null) throw RuntimeException("url OR page. one needed")
        this.url = url
        this.page = page
        if (url != null) webView?.loadUrl(url)
        else if (page != null) webView?.loadData(page, "text/html; charset=UTF-8", null)
    }


    private fun setupWebView(root: View) {
        webView = root.findViewById(R.id.webView)
        val progressBar = root.findViewById<ProgressBar>(R.id.progressBar)
        webView?.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                Logger.d(LOG_TAG, "onPageFinished. url=$url")
                val urlArr = url.split('?')
                val endpoint = urlArr[0]
                if (TextUtils.equals(RequestSignature.MOCK_CALLBACK_PAGE, endpoint)) {
                    val query = urlArr[1]
                    val queryArr = query.split('&')
                    val token = NetUtils.parseQuerySegment(queryArr[0], "oauth_token")
                    val verifier = NetUtils.parseQuerySegment(queryArr[1], "oauth_verifier")
                    if (token == null || verifier == null) {
                        Logger.e(LOG_TAG, "error in url parsing. token or verifier is null")
                        return
                    }
                    listener?.processUserRegistered(token, verifier)
                    activity?.onBackPressed()
                }
            }
        }
        webView?.webChromeClient = object: WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
            }
        }
        webView?.settings?.javaScriptEnabled = true
    }


    interface Listener {
        fun processUserRegistered(token: String, verifier: String)
    }


    companion object {
        const val FRAGMENT_TAG = "com.devtau.flickrclient.ui.fragments.WebPageFragment"
        private const val PAGE = "page"
        private const val URL = "url"
        private const val LOG_TAG = "WebPageFragment"

        //url OR page
        fun newInstance(url: String?, page: String?): WebPageFragment {
            val fragment = WebPageFragment()
            val args = Bundle()
            args.putString(URL, url)
            args.putString(PAGE, page)
            fragment.arguments = args
            return fragment
        }
    }
}