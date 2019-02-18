package com.devtau.flickrclient

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView

class ImagesViewHolder(val root: View) : RecyclerView.ViewHolder(root) {

    val context: Context get() = root.context
    val image: ImageView = root.findViewById(R.id.image)
}