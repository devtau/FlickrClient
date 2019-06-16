package com.devtau.flickrclient.ui.activities

import android.content.Context
import android.view.View
import com.devtau.flickrclient.FragmentsCoordinator
import com.devtau.flickrclient.R
import com.devtau.flickrclient.db.DataLayer
import com.devtau.flickrclient.rest.BackendAPI
import com.devtau.flickrclient.rest.NetworkLayerImpl
import com.devtau.flickrclient.rest.model.Image
import com.devtau.flickrclient.rest.response.AccessTokenResponse
import com.devtau.flickrclient.rest.response.TokenResponse
import com.devtau.flickrclient.util.AppUtils
import com.devtau.flickrclient.util.Constants
import com.devtau.flickrclient.util.Logger
import com.devtau.flickrclient.util.PreferencesManager
import io.reactivex.functions.Consumer
/**
 * MainPresenter выступает хабом, координирующим сетевой слой со слоем бд и слоем вью.
 * Все слои представлены интерфейсами. Хаб лишь вызывает их методы по мере необходимости.
 * MainPresenter is currently a data hub, coordinating network layer with database layer and view layer
 * All layers are represented with interfaces. Hub is only calling their methods when needed.
 */
class MainPresenter(
    private val context: Context,
    private val networkLayer: NetworkLayerImpl,
    private val dataLayer: DataLayer,
    private val fragmentsCoordinator: FragmentsCoordinator
) {
    private var prefs = PreferencesManager.getInstance(context)
    private var tempTokenSecret: String? = null


    fun showListFragment() {
        fragmentsCoordinator.showListFragment()
    }

    fun search(searchQuery: String) {
        val token = prefs?.token
        val tokenSecret = prefs?.tokenSecret
        if (token == null || tokenSecret == null) {
            networkLayer.requestToken("", Consumer { response -> processToken(response) })
        } else {
            networkLayer.search(token, tokenSecret, searchQuery, Constants.PAGE_SIZE, Consumer { images ->
                if (images == null || images.isEmpty()) AppUtils.showDialog(context, R.string.no_images_found)
                if (images != null) dataLayer.saveImages(images)
            })
        }
    }

    fun authorize() = networkLayer.requestToken("", Consumer { response -> processToken(response) })

    fun processUserRegistered(token: String, verifier: String) {
        val tokenSecret = tempTokenSecret
        if (tokenSecret == null) {
            Logger.e(LOG_TAG, "processUserRegistered. tokenSecret is null. aborting")
            return
        }
        networkLayer.requestAccessToken(token, tokenSecret, verifier, Consumer { response -> processAccessToken(response) })
    }

    fun showDetails(selectedImage: Image, imageView: View) {
        fragmentsCoordinator.showImageFragment(selectedImage, imageView, true)
    }


    private fun processToken(response: TokenResponse?) {
        if (response?.callbackConfirmed != true || response.tempToken == null || response.tempTokenSecret == null) {
            Logger.e(LOG_TAG, "processToken. bad inputs. aborting")
            return
        }
        tempTokenSecret = response.tempTokenSecret
        val url = BackendAPI.BASE_SERVER_URL + BackendAPI.AUTHORIZE_ENDPOINT + "?oauth_token=${response.tempToken}"
        fragmentsCoordinator.showWebPageFragmentWithUrl(url)
    }

    private fun processAccessToken(response: AccessTokenResponse?) {
        if (response?.fullName == null || response.receivedToken == null || response.receivedTokenSecret == null
            || response.userNsid == null || response.username == null) {
            Logger.e(LOG_TAG, "processAccessToken. bad inputs. aborting")
            return
        }
        prefs?.token = response.receivedToken
        prefs?.tokenSecret = response.receivedTokenSecret
        prefs?.userName = response.username
        prefs?.fullName = response.fullName
        prefs?.userNsid = response.userNsid
        AppUtils.showDialog(context, R.string.now_you_are_authorized)
        fragmentsCoordinator.updateSearchVisibility()
    }


    companion object {
        private const val LOG_TAG = "MainPresenter"
    }
}