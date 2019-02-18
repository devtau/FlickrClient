package com.devtau.rest.model

import android.os.Parcelable
import android.text.TextUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.devtau.rest.BackendAPI
import com.devtau.rest.R
import com.devtau.rest.util.Logger
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Image(
    @SerializedName("id") var id: Long? = null,
    @SerializedName("owner") var owner: String? = null,
    @SerializedName("secret") var secret: String? = null,
    @SerializedName("server") var server: String? = null,
    @SerializedName("farm") var farm: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("ispublic") var isPublic: Int? = null,
    @SerializedName("isfriend") var isFriend: Int? = null,
    @SerializedName("isfamily") var isFamily: Int? = null
): Parcelable {


    companion object {
        private const val LOG_TAG = "Image"
        private const val SMALL_IMAGE_SIZE = 270
        private const val MIDDLE_IMAGE_SIZE = 540
        private const val LARGE_IMAGE_SIZE = 1080


        fun processMiddleImage(image: Image?, imageView: ImageView?, withCrossFade: Boolean) {
            image ?: return
            imageView ?: return
            processRegularImage(
                image,
                imageView,
                withCrossFade,
                MIDDLE_IMAGE_SIZE
            )
        }

        fun processLargeImage(image: Image?, imageView: ImageView?, withCrossFade: Boolean) {
            image ?: return
            imageView ?: return
            processRegularImage(
                image,
                imageView,
                withCrossFade,
                LARGE_IMAGE_SIZE
            )
        }


        private fun processRegularImage(image: Image, imageView: ImageView, withCrossFade: Boolean, size: Int) {
            if (!TextUtils.isEmpty(image.secret)) {
                val formatter = imageView.context.getString(R.string.image_url_path_formatter)
                val imageUrl = String.format(Locale.getDefault(), formatter, image.farm, image.server, image.id, image.secret)
                if (withCrossFade)
                    Glide.with(imageView.context)
                        .load(imageUrl)
                        .apply(createOptions())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView)
                else
                    Glide.with(imageView.context)
                        .load(imageUrl)
                        .apply(createOptions())
                        .into(imageView)
                Logger.v(LOG_TAG, "processRegularImage id: " + image.id + ", secret: " + image.secret + ", imageUrl: " + imageUrl)
            } else {
                Glide.with(imageView.context).load(R.drawable.no_image_placeholder).apply(createOptions()).into(imageView)
                Logger.d(LOG_TAG, "processRegularImage id: " + image.id + ", secret: " + image.secret + ", image not found")
            }
        }

        private fun createOptions(): RequestOptions = RequestOptions()
            .error(R.drawable.no_image_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.NORMAL)
    }
}