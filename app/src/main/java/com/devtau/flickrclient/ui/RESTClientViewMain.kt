package com.devtau.flickrclient.ui

import android.content.Context
import android.widget.Toast
import com.devtau.database.DataSource
import com.devtau.flickrclient.BuildConfig
import com.devtau.flickrclient.FragmentsCoordinator
import com.devtau.flickrclient.R
import com.devtau.flickrclient.util.PreferencesManager
import com.devtau.rest.BackendAPI
import com.devtau.rest.RESTClientViewImpl
import com.devtau.rest.model.Image
import com.devtau.rest.util.Logger
/**
 * Довольно часто проще зашить реализацию RESTClientViewImpl классом, вложенным в активность
 */
class RESTClientViewMain(
    private val context: Context,
    private val fragmentsCoordinator: FragmentsCoordinator?,
    private val prefs: PreferencesManager?,
    private val dataSource: DataSource?,
    private val logTag: String,
    private val listener: Listener?
): RESTClientViewImpl() {

    override fun getLogTag(): String = logTag
    override fun getContext(): Context = context
    override fun getApiKey(): String = BuildConfig.FLICKR_API_KEY


    override fun processToken(callbackConfirmed: Boolean?, tempToken: String?, tempTokenSecret: String?) {
        if (callbackConfirmed == true && tempToken != null && tempTokenSecret != null) {
            listener?.updateTempTokenSecret(tempTokenSecret)
            val url = BackendAPI.BASE_SERVER_URL + BackendAPI.AUTHORIZE_ENDPOINT + "?oauth_token=$tempToken"
            fragmentsCoordinator?.showWebPageFragmentWithUrl(url)
        }
    }

    override fun showWebPage(page: String?) {
        if (page != null) fragmentsCoordinator?.showWebPageFragmentWithPage(page)
    }

    override fun processAccessToken(fullName: String?, receivedToken: String?, receivedTokenSecret: String?,
                                    userNsid: String?, username: String?) {
        if (fullName == null || receivedToken == null || receivedTokenSecret == null || userNsid == null || username == null) {
            Logger.e(logTag, "in processAccessToken. received some unexpected null data")
            return
        }
        prefs?.token = receivedToken
        prefs?.tokenSecret = receivedTokenSecret
        prefs?.userName = username
        prefs?.fullName = fullName
        prefs?.userNsid = userNsid
        showDialog(R.string.now_you_are_authorized)
        fragmentsCoordinator?.updateSearchVisibility()
    }

    override fun processSearchResult(images: List<Image>?) {
        if (images == null || images.isEmpty()) showDialog(R.string.no_images_found)
        if (images != null) dataSource?.saveImages(images)
    }


    interface Listener {
        fun updateTempTokenSecret(tempTokenSecret: String)
    }
}