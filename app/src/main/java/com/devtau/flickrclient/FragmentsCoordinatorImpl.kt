package com.devtau.flickrclient

import android.os.Build
import android.support.v4.app.FragmentManager
import android.view.View
import com.devtau.flickrclient.ui.fragments.ImageFragment
import com.devtau.flickrclient.ui.fragments.ListFragment
import com.devtau.flickrclient.ui.fragments.WebPageFragment
import com.devtau.rest.model.Image
import com.devtau.rest.util.Logger

class FragmentsCoordinatorImpl(private val fragmentManager: FragmentManager): FragmentsCoordinator {

    override fun showListFragment() {
        var listFragment = fragmentManager.findFragmentByTag(ListFragment.FRAGMENT_TAG) as ListFragment?
        if (listFragment != null) {
            listFragment.changeContent()
        } else {
            val transaction = fragmentManager.beginTransaction()
            try {
                listFragment = ListFragment.newInstance()
                transaction.replace(R.id.mainFrame, listFragment, ListFragment.FRAGMENT_TAG)
                    .commit()
            } catch (e: IllegalStateException) {
                Logger.e(LOG_TAG, e.localizedMessage)
            }
        }
    }

    override fun showImageFragment(image: Image, imageView: View?, animateTransition: Boolean) {
        var imageFragment = fragmentManager.findFragmentByTag(ImageFragment.FRAGMENT_TAG) as ImageFragment?
        if (imageFragment != null) {
            imageFragment.changeContent(image)
        } else {
            val transaction = fragmentManager.beginTransaction()
            if (animateTransition && Build.VERSION.SDK_INT >= 21 && imageView != null) {
                transaction.addSharedElement(imageView, "imageTransition")
            }
            try {
                imageFragment = ImageFragment.newInstance(image)
                transaction.replace(R.id.mainFrame, imageFragment, ImageFragment.FRAGMENT_TAG)
                    .addToBackStack(ImageFragment.FRAGMENT_TAG)
                    .commit()
            } catch (e: IllegalStateException) {
                Logger.e(LOG_TAG, e.localizedMessage)
            }
        }
    }

    override fun showWebPageFragmentWithUrl(url: String?) {
        var webPageFragment = fragmentManager.findFragmentByTag(WebPageFragment.FRAGMENT_TAG) as WebPageFragment?
        if (webPageFragment != null) {
            webPageFragment.changeContent(url, null)
        } else {
            val transaction = fragmentManager.beginTransaction()
            try {
                webPageFragment = WebPageFragment.newInstance(url, null)
                transaction.replace(R.id.mainFrame, webPageFragment, WebPageFragment.FRAGMENT_TAG)
                    .addToBackStack(WebPageFragment.FRAGMENT_TAG)
                    .commit()
            } catch (e: IllegalStateException) {
                Logger.e(LOG_TAG, e.localizedMessage)
            }
        }
    }

    override fun showWebPageFragmentWithPage(page: String?) {
        var webPageFragment = fragmentManager.findFragmentByTag(WebPageFragment.FRAGMENT_TAG) as WebPageFragment?
        if (webPageFragment != null) {
            webPageFragment.changeContent(null, page)
        } else {
            val transaction = fragmentManager.beginTransaction()
            try {
                webPageFragment = WebPageFragment.newInstance(null, page)
                transaction.replace(R.id.mainFrame, webPageFragment, WebPageFragment.FRAGMENT_TAG)
                    .addToBackStack(WebPageFragment.FRAGMENT_TAG)
                    .commit()
            } catch (e: IllegalStateException) {
                Logger.e(LOG_TAG, e.localizedMessage)
            }
        }
    }


    companion object {
        private const val LOG_TAG = "FragmentsCoordinatorImpl"
    }
}