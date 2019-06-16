package com.devtau.flickrclient.rest

import com.devtau.flickrclient.rest.model.Image
import com.devtau.flickrclient.rest.response.AccessTokenResponse
import com.devtau.flickrclient.rest.response.TokenResponse
import io.reactivex.functions.Consumer

interface NetworkLayer {
    fun requestToken(tokenSecret: String, listener: Consumer<TokenResponse?>)
    fun requestAccessToken(token: String, tokenSecret: String, verifier: String, listener: Consumer<AccessTokenResponse?>)
    fun search(token: String, tokenSecret: String, query: String, perPage: Int, listener: Consumer<List<Image>?>)
}