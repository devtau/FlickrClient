package com.devtau.rest

import com.devtau.rest.model.RequestSignature
import org.junit.Test
import org.junit.Assert.*
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun sha_isCorrect() {
        val consumerKey = "4ead60d4df777c9cde59b829c70b5469"
        val callback = "4ead60d4df777c9cde59b829c70b5469"
        val token = ""
        val tokenSecret = ""
        val searchQuery = "раз два"
        val perPage = 99
        val verifier = ""

        val signature = RequestSignature(consumerKey, BackendAPI.REQUEST_TOKEN_ENDPOINT, tokenSecret, token,
            searchQuery, perPage, verifier, callback)
        assertEquals("0fhNGlzpFNAsTme/hDfUb5HPB5U=", signature.encodedSignature)
    }
}
