package com.devtau.flickrclient

import android.view.View
import com.devtau.rest.model.Image

interface FragmentsCoordinator {
    fun showListFragment()
    fun showImageFragment(image: Image, imageView: View?, animateTransition: Boolean)
    fun showWebPageFragmentWithUrl(url: String?)
    fun showWebPageFragmentWithPage(page: String?)
}