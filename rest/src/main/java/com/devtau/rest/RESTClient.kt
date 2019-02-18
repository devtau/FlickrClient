package com.devtau.rest
/**
 * Прокси интерфейс для работы с сетевым слоем. Здесь все, что умеет модуль rest
 */
interface RESTClient {
    fun requestToken(tokenSecret: String)
    fun requestAccessToken(token: String, tokenSecret: String, verifier: String)
    fun search(token: String, tokenSecret: String, query: String, perPage: Int)
}