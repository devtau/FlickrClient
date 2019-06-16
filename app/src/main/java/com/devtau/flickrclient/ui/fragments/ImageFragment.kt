package com.devtau.flickrclient.ui.fragments

import android.os.Build
import android.os.Bundle
import androidx.transition.Fade
import androidx.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.devtau.flickrclient.*
import com.devtau.flickrclient.rest.model.Image

class ImageFragment: Fragment() {

    private var image: Image? = null
    private var imageView: ImageView? = null
    private var idView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        image = arguments?.getParcelable(IMAGE)
        if (Build.VERSION.SDK_INT >= 21) {
            val changeTransform = TransitionInflater.from(context).inflateTransition(R.transition.transition)
            sharedElementEnterTransition = changeTransform
            enterTransition = Fade()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_details, container, false)
        imageView = root.findViewById(R.id.image)
        idView = root.findViewById(R.id.id)
        changeContent(image)
        return root
    }


    fun changeContent(image: Image?) {
        this.image = image
        Image.processLargeImage(image, imageView, true)
        idView?.text = image?.id?.toString()
    }


    companion object {
        const val FRAGMENT_TAG = "com.devtau.flickrclient.ui.fragments.ImageFragment"
        private const val IMAGE = "image"

        fun newInstance(image: Image?): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putParcelable(IMAGE, image)
            fragment.arguments = args
            return fragment
        }
    }
}