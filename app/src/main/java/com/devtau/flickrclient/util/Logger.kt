package com.devtau.flickrclient.util

import android.util.Log
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.devtau.flickrclient.BuildConfig

object Logger {

    private const val LOGS_NEEDED = true

    fun v(tag: String, msg: String?) { if (LOGS_NEEDED && BuildConfig.DEBUG) Log.v(tag, msg) }
    fun d(tag: String, msg: String?) { if (LOGS_NEEDED && BuildConfig.DEBUG) Log.d(tag, msg) }
    fun i(tag: String, msg: String?) { if (LOGS_NEEDED && BuildConfig.DEBUG) Log.i(tag, msg) }
    fun w(tag: String, msg: String?) { if (LOGS_NEEDED && BuildConfig.DEBUG) Log.w(tag, msg) }
    fun e(tag: String, msg: String?) { Log.e(tag, msg); logFabric(tag, msg) }

    //у нас всего 100 символов на строку StackTrace
    //we have only 100 chars for StackTrace string
    private fun logFabric(tag: String, errorMsg: String?) {
        if (LOGS_NEEDED && !BuildConfig.DEBUG) {
            val builder = StringBuilder()
            val depth = if (Thread.currentThread().stackTrace.size > 7) 7 else Thread.currentThread().stackTrace.size
            try {
                var divider = ""
                for (i in 3 until depth) {
                    builder.append(divider)
                    divider = " - "
                    val fullName = Thread.currentThread().stackTrace[i].toString()
                    val bracketIndex = fullName.indexOf('(')
                    val fileLineNumber = fullName.substring(bracketIndex + 1, fullName.length - 1)

                    val dotIndex = fileLineNumber.indexOf('.')
                    val colonIndex = fileLineNumber.indexOf(':')
                    val className = fileLineNumber.substring(0, dotIndex)
                    val lineNumber = fileLineNumber.substring(colonIndex + 1)

                    builder.append(className)
                    builder.append('.')
                    builder.append(lineNumber)
                }

                Answers.getInstance().logCustom(CustomEvent(tag)
                        .putCustomAttribute("ErrorMsg", errorMsg)
                        .putCustomAttribute("StackTrace", builder.toString()))
                Log.e(tag, builder.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}