package com.devtau.flickrclient.db

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
/**
 * Активность, которой необходимо постоянное подключение к бд наследует DBSubscriberActivity.
 * Подписка оформляется переопределяя метод restartLoaders и вызывая методы интерфейса DataLayer,
 * обернутые в disposeOnStop
 * Activity, that needs db connection extends DBSubscriberActivity.
 * Subscription is made through restartLoaders method by calling DataLayer interface methods,
 * wrapped in disposeOnStop
 */
abstract class DBSubscriberActivity: AppCompatActivity() {

    var dataLayer: DataLayer? = null
    private val compositeDisposableForOnStop = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataLayer = DataLayerImpl(applicationContext)
    }

    override fun onStart() {
        super.onStart()
        restartLoaders()
    }

    public override fun onStop() {
        compositeDisposableForOnStop.clear()
        super.onStop()
    }


    fun disposeOnStop(disposable: Disposable?) {
        if (disposable != null) compositeDisposableForOnStop.add(disposable)
    }

    abstract fun restartLoaders()


    companion object {
        private const val LOG_TAG = "DBSubscriberActivity"
    }
}