package com.devtau.flickrclient.rest

import android.content.Context
import com.devtau.flickrclient.BuildConfig
import com.devtau.flickrclient.R
import com.devtau.flickrclient.rest.model.Image
import com.devtau.flickrclient.rest.model.RequestSignature
import com.devtau.flickrclient.rest.response.AccessTokenResponse
import com.devtau.flickrclient.rest.response.TokenResponse
import com.devtau.flickrclient.util.ErrorConstants
import com.devtau.flickrclient.util.NetUtils
import com.devtau.flickrclient.util.AppUtils
import com.devtau.flickrclient.util.Logger
import com.google.gson.GsonBuilder
import io.reactivex.functions.Consumer
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

class NetworkLayerImpl(private val context: Context?): NetworkLayer {

    private var httpClientLogging: OkHttpClient? = null
    private var httpClientNotLogging: OkHttpClient? = null


    override fun requestToken(tokenSecret: String, listener: Consumer<TokenResponse?>) {
        if (!AppUtils.checkConnection(context)) return
        val callback = RequestSignature.MOCK_CALLBACK_PAGE
        var signature: RequestSignature? = null
        try {
            signature = RequestSignature(BuildConfig.FLICKR_API_KEY, BackendAPI.REQUEST_TOKEN_ENDPOINT, tokenSecret,
                null, null, null, null, callback)
        } catch (e: UnsupportedEncodingException) {
            AppUtils.showDialog(context, R.string.encoding_not_supported)
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
                    listener.accept(TokenResponse("true" == callbackConfirmed, tempToken, tempTokenSecret))
                }
            })
    }

    override fun requestAccessToken(token: String, tokenSecret: String, verifier: String, listener: Consumer<AccessTokenResponse?>) {
        if (!AppUtils.checkConnection(context)) return
        var signature: RequestSignature? = null
        try {
            signature = RequestSignature(BuildConfig.FLICKR_API_KEY, BackendAPI.REQUEST_ACCESS_TOKEN_ENDPOINT, tokenSecret, token, null, null, verifier)
        } catch (e: UnsupportedEncodingException) {
            AppUtils.showDialog(context, R.string.encoding_not_supported)
        }
        signature ?: return

        getBackendAPIClient(true).requestAccessToken(
            signature.nonce, signature.timestamp, verifier, signature.consumerKey, RequestSignature.OAUTH_SIGNATURE_METHOD,
            RequestSignature.OAUTH_VERSION, token, signature.encodedSignature)
            .enqueue(object: BaseCallback<String?>() {
                override fun processBody(responseBody: String?) {
                    val arr = responseBody?.split('&') ?: return
                    try {
                        listener.accept(AccessTokenResponse(
                            NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[0], "fullname")),
                            NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[1], "oauth_token")),
                            NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[2], "oauth_token_secret")),
                            NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[3], "user_nsid")),
                            NetUtils.urlDecode(NetUtils.parseQuerySegment(arr[4], "username"))))
                    } catch (e: UnsupportedEncodingException) {
                        AppUtils.showDialog(context, R.string.encoding_not_supported)
                    }
                }
            })
    }

    override fun search(token: String, tokenSecret: String, query: String, perPage: Int, listener: Consumer<List<Image>?>) {
        if (!AppUtils.checkConnection(context)) return
        var signature: RequestSignature? = null
        try {
            signature = RequestSignature(BuildConfig.FLICKR_API_KEY, BackendAPI.REST, tokenSecret, token, query, perPage)
        } catch (e: UnsupportedEncodingException) {
            AppUtils.showDialog(context, R.string.encoding_not_supported)
        }
        signature ?: return
        getBackendAPIClient(true).search(
            1, "json", signature.nonce, signature.timestamp, BuildConfig.FLICKR_API_KEY, RequestSignature.OAUTH_SIGNATURE_METHOD,
            RequestSignature.OAUTH_VERSION, token, signature.encodedSignature, query, perPage)
            .enqueue(object: BaseCallback<SearchResponse?>() {
                override fun processBody(responseBody: SearchResponse?) = listener.accept(responseBody?.images)
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
            synchronized(NetworkLayerImpl::class.java) {
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
            if (t.localizedMessage != null) AppUtils.showToast(context, t.localizedMessage)
        }


        private fun handleError(errorCode: Int, errorBody: ResponseBody?, responseBody: T?) {
            val errorMsg = "retrofit error code: $errorCode"
            when (errorCode) {
                ErrorConstants.INTERNAL_SERVER_ERROR -> AppUtils.showDialog(context, R.string.internal_server_error)
                ErrorConstants.FLICKR_API_UNAVAILABLE -> AppUtils.showDialog(context, R.string.flickr_api_unavailable)
                ErrorConstants.SERVICE_CURRENTLY_UNAVAILABLE -> AppUtils.showDialog(context, R.string.flickr_api_unavailable)
                ErrorConstants.SIGNATURE_INVALID -> AppUtils.showDialog(context, R.string.signature_invalid)
            }
            Logger.e(LOG_TAG, errorMsg)
        }

        abstract fun processBody(responseBody: T?)
    }


    companion object {
        private const val LOG_TAG = "NetworkLayer"
        private const val TIMEOUT_CONNECT = 10L
        private const val TIMEOUT_READ = 60L
        private const val TIMEOUT_WRITE = 120L
    }
}