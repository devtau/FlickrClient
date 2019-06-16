package com.devtau.flickrclient.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.crashlytics.android.Crashlytics
import com.devtau.flickrclient.BuildConfig
import com.devtau.flickrclient.DependencyRegistry
import com.devtau.flickrclient.R
import com.devtau.flickrclient.rest.model.Image
import com.devtau.flickrclient.ui.fragments.WebPageFragment
import com.devtau.flickrclient.ui.fragments.listFragment.ListFragment
import io.fabric.sdk.android.Fabric

class MainActivity: AppCompatActivity(),
    ListFragment.Listener,
    WebPageFragment.Listener {

    private var presenter: MainPresenter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_main)
        DependencyRegistry(this).inject(this, intent.extras)
        presenter?.showListFragment()
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

    override fun showDetails(selectedImage: Image, imageView: View) = presenter?.showDetails(selectedImage, imageView)
    override fun search(searchQuery: String) = presenter?.search(searchQuery)
    override fun authorize() = presenter?.authorize()
    override fun processUserRegistered(token: String, verifier: String) = presenter?.processUserRegistered(token, verifier)


    fun configureWith(presenter: MainPresenter) {
        this.presenter = presenter
    }


    companion object {
        private const val LOG_TAG = "MainActivity"
    }
}