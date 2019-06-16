package com.devtau.flickrclient.ui.fragments.listFragment

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.devtau.flickrclient.R

class ImagesViewHolder(val root: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(root) {

    val context: Context get() = root.context
    val image: ImageView = root.findViewById(R.id.image)
}