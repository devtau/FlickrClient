package com.devtau.database

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class DBSubscriberActivity: AppCompatActivity() {

    var dataSource: DataSource? = null
    private val compositeDisposableForOnStop = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataSource = DataSourceImpl(applicationContext, provideDBName())
    }

    @SuppressLint("CheckResult")
    override fun onStart() {
        super.onStart()
        Observable.just("").observeOn(Schedulers.io()).subscribe { restartLoaders() }
    }

    public override fun onStop() {
        compositeDisposableForOnStop.clear()
        super.onStop()
    }


    fun disposeOnStop(disposable: Disposable?) {
        if (disposable != null) compositeDisposableForOnStop.add(disposable)
    }

    abstract fun provideDBName(): String
    abstract fun restartLoaders()


    companion object {
        private const val LOG_TAG = "DBSubscriberActivity"
    }
}