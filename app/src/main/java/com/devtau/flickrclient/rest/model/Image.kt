package com.devtau.flickrclient.rest.model

import android.os.Parcelable
import android.text.TextUtils
import android.widget.ImageView
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.devtau.flickrclient.R
import com.devtau.flickrclient.util.Logger
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "Images")
data class Image(
    @PrimaryKey @ColumnInfo(name = "_id") @SerializedName("id") var id: Long?,
    @SerializedName("owner") var owner: String?,
    @SerializedName("secret") var secret: String?,
    @SerializedName("server") var server: String?,
    @SerializedName("farm") var farm: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("ispublic") var isPublic: Int?,
    @SerializedName("isfriend") var isFriend: Int?,
    @SerializedName("isfamily") var isFamily: Int?
): Parcelable {

    fun deepEquals(other: Image?): Boolean = when {
        other == null -> false
        id != other.id || owner != other.owner || secret != other.secret || server != other.server
                || farm != other.farm || title != other.title || isPublic != other.isPublic
                || isFriend != other.isFriend || isFamily != other.isFamily -> false
        else -> super.equals(other)
    }

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
                val builder = Glide.with(imageView.context).load(imageUrl).apply(createOptions())
                if (withCrossFade) builder.transition(DrawableTransitionOptions.withCrossFade())
                builder.into(imageView)
                Logger.v(LOG_TAG, "processRegularImage id: " + image.id + ", secret: " + image.secret + ", imageUrl: " + imageUrl)
            } else {
                Glide.with(imageView.context).load(R.drawable.no_image_placeholder).apply(createOptions()).into(imageView)
                Logger.v(LOG_TAG, "processRegularImage id: " + image.id + ", secret: " + image.secret + ", image not found")
            }
        }

        private fun createOptions(): RequestOptions = RequestOptions()
            .error(R.drawable.no_image_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.NORMAL)
    }
}