package com.devtau.flickrclient.util

import android.text.TextUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

object NetUtils {
    fun parseQuerySegment(segment: String, propertyName: String): String? =
        if (TextUtils.equals(segment.split('=')[0], propertyName))
            segment.split('=')[1] else null

    @Throws(UnsupportedEncodingException::class)
    fun urlEncode(input: String?): String? {
        input ?: return null
        return URLEncoder.encode(input, "UTF-8")
    }

    @Throws(UnsupportedEncodingException::class)
    fun urlDecode(input: String?): String? {
        input ?: return null
        return URLDecoder.decode(input, "UTF-8")
    }
}
