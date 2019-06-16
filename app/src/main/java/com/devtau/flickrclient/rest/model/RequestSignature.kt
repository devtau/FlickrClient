package com.devtau.flickrclient.rest.model

import android.os.Build
import android.util.Base64
import com.devtau.flickrclient.rest.BackendAPI
import com.devtau.flickrclient.util.Logger
import com.devtau.flickrclient.util.NetUtils
import com.devtau.flickrclient.util.RandomString
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class RequestSignature constructor(
    val consumerKey: String?,
    private val endpoint: String,
    private val tokenSecret: String,
    private val token: String? = null,
    private val query: String? = null,
    private val perPage: Int? = null,
    private val verifier: String? = null,
    private val callback: String? = null
) {

    val nonce: String = if (Build.VERSION.SDK_INT >= 19) RandomString().nextString() else System.currentTimeMillis().toString()
    val timestamp: Long = getTime() / 1000
    val encodedSignature: String

    init {
        val queryBuilder = StringBuilder()
        if (callback != null) queryBuilder.append("oauth_callback=" + NetUtils.urlEncode(callback) + '&')
        queryBuilder.append("oauth_consumer_key=$consumerKey")
        queryBuilder.append("&oauth_nonce=$nonce")
        queryBuilder.append("&oauth_signature_method=$OAUTH_SIGNATURE_METHOD")
        queryBuilder.append("&oauth_timestamp=$timestamp")
        if (token != null) queryBuilder.append("&oauth_token=$token")
        if (verifier != null) queryBuilder.append("&oauth_verifier=$verifier")
        queryBuilder.append("&oauth_version=$OAUTH_VERSION")
        if (perPage != null) queryBuilder.append("&per_page=$perPage")
        if (query != null) queryBuilder.append("&text=$query")

        val urlToEncode = "GET&" + NetUtils.urlEncode(BackendAPI.BASE_SERVER_URL + endpoint) + '&' + NetUtils.urlEncode(queryBuilder.toString())

        Logger.d(LOG_TAG, "urlToEncode before encode=$urlToEncode")
        encodedSignature = computeSignature(urlToEncode, "$CONSUMER_SECRET&$tokenSecret")
        Logger.d(LOG_TAG, "hash length=" + encodedSignature.length + ", " + toString())
    }


    override fun toString(): String = "RequestSignature{" +
            ", encodedSignature='" + encodedSignature + '\''.toString() +
            ", endpoint='" + endpoint + '\''.toString() +
            ", tokenSecret='" + tokenSecret + '\''.toString() +
            ", token='" + token + '\''.toString() +
            ", verifier='" + verifier + '\''.toString() +
            ", callback='" + callback + '\''.toString() +
            ", consumerKey='" + consumerKey + '\''.toString() +
            ", nonce='" + nonce + '\''.toString() +
            ", perPage='" + perPage + '\''.toString() +
            ", query='" + query + '\''.toString() +
            '}'.toString()

    private fun computeSignature(baseString: String, key: String): String {
        val algorithm = "HmacSHA1"
        val mac = Mac.getInstance(algorithm)
        val spec = SecretKeySpec(key.toByteArray(), algorithm)
        mac.init(spec)
        val byteHMAC = mac.doFinal(baseString.toByteArray(charset("UTF-8")))
        return String(Base64.encode(byteHMAC, Base64.NO_WRAP))
    }

    private fun getTime(): Long {
        val dateFormatGmt = SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.getDefault())
        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
        val dateFormatLocal = SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.getDefault())
        return dateFormatLocal.parse(dateFormatGmt.format(Date())).time
    }


    companion object {
        const val OAUTH_SIGNATURE_METHOD = "HMAC-SHA1"
        const val OAUTH_VERSION = "1.0"
        const val MOCK_CALLBACK_PAGE = "http://www.example.com/"
        const val FLICKR_PAGE = "https://www.flickr.com/"
        const val FLICKR_LOGIN_PAGE = "https://identity.flickr.com/login"

        private const val LOG_TAG = "RequestSignature"
        private const val CONSUMER_SECRET = "094564723c29a5d4"
    }
}