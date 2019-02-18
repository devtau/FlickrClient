package com.devtau.database.listeners

interface SearchHistoryListener {
    fun processQueries(list: List<String>?)
}