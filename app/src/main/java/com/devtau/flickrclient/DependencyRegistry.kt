package com.devtau.flickrclient

import android.content.Context
import android.os.Bundle
import com.devtau.flickrclient.db.DataLayerImpl
import com.devtau.flickrclient.rest.NetworkLayerImpl
import com.devtau.flickrclient.ui.activities.MainActivity
import com.devtau.flickrclient.ui.activities.MainPresenter
import com.devtau.flickrclient.util.Constants

class DependencyRegistry(val context: Context) {

    fun inject(activity: MainActivity, bundle: Bundle?) {
        val networkLayer = NetworkLayerImpl(context)
        val dataLayer = DataLayerImpl(context)
        val fragmentsCoordinator = FragmentsCoordinatorImpl(activity.supportFragmentManager)
        val presenter = MainPresenter(activity, networkLayer, dataLayer, fragmentsCoordinator)
        activity.configureWith(presenter)
    }

    private fun idFromBundle(bundle: Bundle?): Int? {
        if (bundle == null) return null
        val spyId = bundle.getInt(Constants.ID_EXTRA)
        if (spyId == 0) return null
        return spyId
    }
}