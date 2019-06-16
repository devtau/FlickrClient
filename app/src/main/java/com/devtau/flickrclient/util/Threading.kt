package com.devtau.flickrclient.util

import android.os.Handler
import android.os.Looper
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

object Threading {

    fun dispatchMain(block: Action) {
        Handler(Looper.getMainLooper()).post {
            try {
                block.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun <T> async(task: Callable<T>): Disposable = async(task, null, null, Schedulers.io())
    fun <T> async(task: Callable<T>, finished: Consumer<T>): Disposable = async(task, finished, null, Schedulers.io())
    fun <T> async(task: Callable<T>, finished: Consumer<T>, onError: Consumer<Throwable>): Disposable =
        async(task, finished, onError, Schedulers.io())

    fun <T> async(task: Callable<T>, finished: Consumer<T>?, onError: Consumer<Throwable>?, scheduler: Scheduler): Disposable =
        Single.fromCallable(task)
            .subscribeOn(scheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(finished ?: Consumer { /*NOP*/ }, onError ?: Consumer { /*NOP*/ })
}