package com.devtau.rest

import android.content.Context
import android.net.ConnectivityManager
import android.text.TextUtils
import com.devtau.rest.model.RequestSignature
import com.devtau.rest.util.NetUtils
import com.devtau.rest.util.ErrorConstants
import com.devtau.rest.util.Logger
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.UnsupportedEncodingException
import java.util.concurrent.TimeUnit

class RESTClientImpl(private val view: RESTClientView?): RESTClient {

    private var httpClientLogging: OkHttpClient? = null
    private var httpClientNotLogging: OkHttpClient? = null


    override fun requestToken(tokenSecret: String) {
        if (!checkConnection(view?.getContext())) return
        val callback = RequestSignature.MOCK_CALLBACK_PAGE
        var signature: RequestSignature? = null
        try {
            signature = RequestSignature(view?.getApiKey(), BackendAPI.REQUEST_TOKEN_ENDPOINT, tokenSecret, null, null, null, null, callback)
        } catch (e: UnsupportedEncodingException) {
            view?.showDialog(R.string.encoding_not_supported)
        }
        signature ?: return

        getBackendAPIClient(true).requestToken(
            signature.nonce, signature.timestamp, signature.consumerKey, RequestSignature.OAUTH_SIGNATURE_METHOD,
            RequestSignature.OAUTH_VERSION, callback, signature.encodedSignature)
            .enqueue(object: BaseCallback<String?>() {
                override fun processBody(responseBody: String?) {
                    val arr = responseBody?.split('&') ?: return
                    val callbackConfirmed = NetUtils.parseQuerySegment(arr[0], "oauth_callback_confirmed")
                    val tempToken = NetUtils.parseQuerySegment(arr[1], "oauth_token")
                    val tempTokenSecret = NetUtils.parseQuerySegment(arr[2], "oauth_token_secret")
                    if (callbackConfirmed == null || tempToken == null || tempTokenSecret == null) {
                        Logger.e(LOG_TAG, "error in requestToken parsing")
                        return
                    }
                    view?.processToken(TextUtils.equals(callbackConfirmed, "true"), tempToken, tempTokenSecret)
                }
            })
    }

    override fun requestAccessToken(token: String, tokenSecret: String, verifier: String) {
        if (!checkConnection(view?.getContext())) return
        var signature: RequestSignature? = null
        try {
            signature = RequestSignature(view?.getApiKey(), BackendAPI.REQUEST_ACCESS_TOKEN_ENDPOINT, tokenSecret, token, null, null, verifier)
        } catch (e: UnsupportedEncodingException) {
            view?.showDialog(R.string.encoding_not_supported)
        }
        signature ?: return

        getBackendAPIClient(true).requestAccessToken(
            signature.nonce, signature.timestamp, verifier, signature.consumerKey, RequestSignature.OAUTH_SIGNATURE_METHOD,
            RequestSignature.OAUTH_VERSION, token, signature.encodedSignature)
            .enqueue(object: BaseCallback<String?>() {
                override fun processBody(responseBody: String?) {
                    val arr = responseBody?.split('&') ?: return
                    try {
                        val fullName = NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[0], "fullname"))
                        val receivedToken = NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[1], "oauth_token"))
                        val receivedTokenSecret = NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[2], "oauth_token_secret"))
                        val userNsid = NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[3], "user_nsid"))
                        val username = NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[4], "username"))
                        view?.processAccessToken(fullName, receivedToken, receivedTokenSecret, userNsid, username)
                    } catch (e: UnsupportedEncodingException) {
                        view?.showDialog(R.string.encoding_not_supported)
                    }
                }
            })
    }

    override fun search(token: String, tokenSecret: String, query: String, perPage: Int) {
        if (!checkConnection(view?.getContext())) return
        var signature: RequestSignature? = null
        try {
            signature = RequestSignature(view?.getApiKey(), BackendAPI.REST, tokenSecret, token, query, perPage)
        } catch (e: UnsupportedEncodingException) {
            view?.showDialog(R.string.encoding_not_supported)
        }
        signature ?: return
        val consumerKey = view?.getApiKey() ?: return

        getBackendAPIClient(true).search(
            1, "json", signature.nonce, signature.timestamp, consumerKey, RequestSignature.OAUTH_SIGNATURE_METHOD,
            RequestSignature.OAUTH_VERSION, token, signature.encodedSignature, query, perPage)
            .enqueue(object: BaseCallback<SearchResponse?>() {
                override fun processBody(responseBody: SearchResponse?) = view.processSearchResult(responseBody?.images)
            })
    }


    private fun getBackendAPIClient(loggerNeeded: Boolean): BackendAPI {
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BackendAPI.BASE_SERVER_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(getClient(loggerNeeded))
            .build()
        return retrofit.create(BackendAPI::class.java)
    }

    private fun getClient(loggerNeeded: Boolean): OkHttpClient {
        fun buildClient(loggerNeeded: Boolean): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG && loggerNeeded) {
                builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
            return builder.build()
        }

        var client: OkHttpClient? = if (loggerNeeded) httpClientLogging else httpClientNotLogging
        if (client == null) {
            synchronized(RESTClientImpl::class.java) {
                client = if (loggerNeeded) httpClientLogging else httpClientNotLogging
                if (client == null) {
                    if (loggerNeeded) {
                        httpClientLogging = buildClient(true)
                        client = httpClientLogging
                    } else {
                        httpClientNotLogging = buildClient(false)
                        client = httpClientNotLogging
                    }
                }
            }
        }
        return client!!
    }

    private fun checkConnection(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        if (!networkInfo.isConnectedOrConnecting) view?.showDialog(R.string.you_seem_to_be_offline)
        return networkInfo.isConnectedOrConnecting
    }


    private abstract inner class BaseCallback<T>: Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            val baseResponseBody = response.body()
            if (response.isSuccessful) {
                Logger.d(LOG_TAG, "retrofit response isSuccessful")
                processBody(baseResponseBody)
            } else {
                handleError(response.code(), response.errorBody(), response.body())
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            Logger.e(LOG_TAG, "retrofit failure: " + t.localizedMessage)
            if (t.localizedMessage != null) view?.showToast(t.localizedMessage)
        }


        private fun handleError(errorCode: Int, errorBody: ResponseBody?, responseBody: T?) {
            val errorMsg = "retrofit error code: " + errorCode.toString()
            when (errorCode) {
                ErrorConstants.INTERNAL_SERVER_ERROR -> view?.showDialog(R.string.internal_server_error)
                ErrorConstants.FLICKR_API_UNAVAILABLE -> view?.showDialog(R.string.flickr_api_unavailable)
                ErrorConstants.SERVICE_CURRENTLY_UNAVAILABLE -> view?.showDialog(R.string.flickr_api_unavailable)
                ErrorConstants.SIGNATURE_INVALID -> view?.showDialog(R.string.signature_invalid)
            }
            Logger.e(LOG_TAG, errorMsg)
        }

        abstract fun processBody(responseBody: T?)
    }


    companion object {
        private const val LOG_TAG = "RESTClient"
        private const val TIMEOUT_CONNECT = 10L
        private const val TIMEOUT_READ = 60L
        private const val TIMEOUT_WRITE = 120L
    }
}