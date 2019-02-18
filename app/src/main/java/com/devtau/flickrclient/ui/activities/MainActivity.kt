package com.devtau.flickrclient.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.crashlytics.android.Crashlytics
import com.devtau.database.DataSource
import com.devtau.database.DataSourceImpl
import com.devtau.flickrclient.BuildConfig
import com.devtau.flickrclient.FragmentsCoordinator
import com.devtau.flickrclient.FragmentsCoordinatorImpl
import com.devtau.flickrclient.R
import com.devtau.flickrclient.ui.RESTClientViewMain
import com.devtau.flickrclient.ui.fragments.ListFragment
import com.devtau.flickrclient.ui.fragments.WebPageFragment
import com.devtau.flickrclient.util.AppUtils
import com.devtau.flickrclient.util.PreferencesManager
import com.devtau.rest.RESTClient
import com.devtau.rest.RESTClientImpl
import com.devtau.rest.model.Image
import com.devtau.rest.util.Logger
import io.fabric.sdk.android.Fabric
/**
 * MainActivity выступает хабом, координирующим сетевой слой со слоем бд и слоем вью.
 * Все слои представлены интерфейсами. Хаб лишь вызывает их методы по мере необходимости.
 * Представленная в этом проекте модульная архитектура позволяет нескольким проектам совместно использовать
 * слои сетевого взаимодействия и базы данных, а также более жестко разделяет обязанности классов и частей проекта
 * в соответствии с концепцией SOLID
 */
class MainActivity: AppCompatActivity(),
    ListFragment.Listener,
    WebPageFragment.Listener {

    private var fragmentsCoordinator: FragmentsCoordinator? = null
    private var prefs: PreferencesManager? = null
    private var restClientView: RESTClientViewMain? = null
    private var restClient: RESTClient? = null
    private var dataSource: DataSource? = null
    private var tempTokenSecret: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_main)
        fragmentsCoordinator = FragmentsCoordinatorImpl(supportFragmentManager)
        fragmentsCoordinator?.showListFragment()
        prefs = PreferencesManager.getInstance(this)
        dataSource = DataSourceImpl(this, AppUtils.DATABASE_NAME)
        restClientView = RESTClientViewMain(this, fragmentsCoordinator, prefs, dataSource, LOG_TAG, object: RESTClientViewMain.Listener {
            override fun updateTempTokenSecret(tempTokenSecret: String) {
                this@MainActivity.tempTokenSecret = tempTokenSecret
            }
        })
        restClient = RESTClientImpl(restClientView)
    }

    override fun showDetails(selectedImage: Image, imageView: View) {
        fragmentsCoordinator?.showImageFragment(selectedImage, imageView, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if (BuildConfig.DEBUG) {
            menuInflater.inflate(R.menu.debug_menu, menu)
            true
        } else super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == R.id.openDB) {
            DBViewerActivity.newInstance(this)
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun search(searchQuery: String) {
        val token = prefs?.token
        val tokenSecret = prefs?.tokenSecret
        if (token == null || tokenSecret == null) {
            restClient?.requestToken("")
        } else {
            restClient?.search(token, tokenSecret, searchQuery, PAGE_SIZE)
        }
    }

    override fun processUserRegistered(token: String, verifier: String) {
        val tokenSecret = tempTokenSecret
        if (tokenSecret == null) {
            Logger.e(LOG_TAG, "tokenSecret is null upon call of processUserRegistered")
            return
        }
        restClient?.requestAccessToken(token, tokenSecret, verifier)
    }


    companion object {
        private const val LOG_TAG = "MainActivity"
        private const val PAGE_SIZE = 99
    }
}