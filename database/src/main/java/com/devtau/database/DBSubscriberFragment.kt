package com.devtau.database

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class DBSubscriberFragment: Fragment() {

    var dataSource: DataSource? = null
    private var disposable: CompositeDisposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (context?.applicationContext != null) {
            dataSource = DataSourceImpl(context!!.applicationContext!!, provideDBName())
            disposable = CompositeDisposable()
        }
    }

    @SuppressLint("CheckResult")
    override fun onStart() {
        super.onStart()
        Observable.just("").observeOn(Schedulers.io()).subscribe { restartLoaders() }
    }

    override fun onStop() {
        disposable?.clear()
        super.onStop()
    }


    fun disposeOnStop(disposable: Disposable?) {
        if (disposable != null) this.disposable?.add(disposable)
    }

    abstract fun provideDBName(): String
    abstract fun restartLoaders()
}