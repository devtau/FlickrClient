package com.devtau.flickrclient.db

import android.os.Bundle
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
/**
 * Фрагмент, которому необходимо постоянное подключение к бд наследует DBSubscriberFragment.
 * Подписка оформляется переопределяя метод restartLoaders и вызывая методы интерфейса DataLayer,
 * обернутые в disposeOnStop
 * Fragment, that needs db connection extends DBSubscriberFragment.
 * Subscription is made through restartLoaders method by calling DataLayer interface methods,
 * wrapped in disposeOnStop
 */
abstract class DBSubscriberFragment: Fragment() {

    var dataLayer: DataLayer? = null
    private var disposable: CompositeDisposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = context?.applicationContext ?: return
        dataLayer = DataLayerImpl(context)
        disposable = CompositeDisposable()
    }

    override fun onStart() {
        super.onStart()
        restartLoaders()
    }

    override fun onStop() {
        disposable?.clear()
        super.onStop()
    }


    fun disposeOnStop(disposable: Disposable?) {
        if (disposable != null) this.disposable?.add(disposable)
    }

    abstract fun restartLoaders()
}