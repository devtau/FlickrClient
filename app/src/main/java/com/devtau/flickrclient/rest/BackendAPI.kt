package com.devtau.flickrclient.rest

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
/**
 * Простой интерфейс для работы с сервером. Здесь перечисляем все используемые API методы
 * Simple interface for work with server. All API methods are enlisted here
 */
interface BackendAPI {

    @GET(REQUEST_TOKEN_ENDPOINT)
    fun requestToken(
        @Query("oauth_nonce") nonce: String,
        @Query("oauth_timestamp") timestamp: Long,
        @Query("oauth_consumer_key") consumerKey: String?,
        @Query("oauth_signature_method") signatureMethod: String,
        @Query("oauth_version") version: String,
        @Query("oauth_callback") callback: String,
        @Query("oauth_signature") signature: String
    ): Call<String>

    @GET(REQUEST_ACCESS_TOKEN_ENDPOINT)
    fun requestAccessToken(
        @Query("oauth_nonce") nonce: String,
        @Query("oauth_timestamp") timestamp: Long,
        @Query("oauth_verifier") verifier: String,
        @Query("oauth_consumer_key") consumerKey: String?,
        @Query("oauth_signature_method") signatureMethod: String,
        @Query("oauth_version") version: String,
        @Query("oauth_token") token: String,
        @Query("oauth_signature") signature: String
    ): Call<String>

    @GET(REST)
    fun search(
        @Query("nojsoncallback") noJsonCallback: Int,
        @Query("format") format: String,
        @Query("oauth_nonce") nonce: String,
        @Query("oauth_timestamp") timestamp: Long,
        @Query("oauth_consumer_key") consumerKey: String,
        @Query("oauth_signature_method") signatureMethod: String,
        @Query("oauth_version") version: String,
        @Query("oauth_token") token: String,
        @Query("oauth_signature") signature: String,
        @Query("text") query: String,
        @Query("per_page") perPage: Int,
        @Query("method") method: String = SEARCH_PHOTOS
    ): Call<SearchResponse>


    companion object {
        const val BASE_SERVER_URL = "https://api.flickr.com"

        const val REQUEST_TOKEN_ENDPOINT = "/services/oauth/request_token"
        const val AUTHORIZE_ENDPOINT = "/services/oauth/authorize"
        const val REQUEST_ACCESS_TOKEN_ENDPOINT = "/services/oauth/access_token"
        const val REST = "/services/rest"
        const val SEARCH_PHOTOS = "flickr.photos.search"
    }
}