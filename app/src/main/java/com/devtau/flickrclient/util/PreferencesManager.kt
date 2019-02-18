package com.devtau.flickrclient.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager private constructor(context: Context?) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    var token: String?
        get() = prefs?.getString(TOKEN, null)
        set(value) {
            val editor = prefs?.edit()
            editor?.putString(TOKEN, value)
            editor?.apply()
        }

    var tokenSecret: String?
        get() = prefs?.getString(TOKEN_SECRET, null)
        set(value) {
            val editor = prefs?.edit()
            editor?.putString(TOKEN_SECRET, value)
            editor?.apply()
        }

    var userName: String?
        get() = prefs?.getString(USER_NAME, null)
        set(value) {
            val editor = prefs?.edit()
            editor?.putString(USER_NAME, value)
            editor?.apply()
        }

    var fullName: String?
        get() = prefs?.getString(FULL_NAME, null)
        set(value) {
            val editor = prefs?.edit()
            editor?.putString(FULL_NAME, value)
            editor?.apply()
        }

    var userNsid: String?
        get() = prefs?.getString(USER_NSID, null)
        set(value) {
            val editor = prefs?.edit()
            editor?.putString(USER_NSID, value)
            editor?.apply()
        }

    fun clear() = prefs?.edit()?.clear()?.apply()


    companion object {
        private const val PREFS_NAME = "flickrclient"
        private const val TOKEN = "token"
        private const val TOKEN_SECRET = "tokenSecret"
        private const val USER_NAME = "userName"
        private const val FULL_NAME = "fullName"
        private const val USER_NSID = "userNsid"
        private var self: PreferencesManager? = null

        @Synchronized
        fun getInstance(context: Context?): PreferencesManager? {
            context ?: return null
            synchronized(PreferencesManager::class.java) {
                if (self == null) self = PreferencesManager(context)
            }
            return self
        }
    }
}