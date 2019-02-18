package com.devtau.database.listeners

interface LastSearchQueryListener {
    fun processQuery(query: String?)
}