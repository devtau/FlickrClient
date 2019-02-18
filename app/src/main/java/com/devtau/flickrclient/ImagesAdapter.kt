package com.devtau.flickrclient

import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devtau.rest.model.Image

class ImagesAdapter(
    private var images: List<Image>?,
    private val listener: Listener
): RecyclerView.Adapter<ImagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_image, parent, false)
        return ImagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val current = images?.get(position) ?: return
        holder.root.setOnClickListener { listener.onImageSelected(current, holder.image) }
        Image.processMiddleImage(current, holder.image, true)
        ViewCompat.setTransitionName(holder.image, current.id.toString() + "_image")
    }

    override fun getItemCount(): Int = images?.size ?: 0


    fun updateList(newList: List<Image>?) {
        if (listChanged(newList)) {
            images = newList
            notifyDataSetChanged()
        }
    }

    private fun listChanged(newList: List<Image>?): Boolean {
        val oldList = images
        if (newList == null || oldList == null) return true
        if (newList.size != oldList.size) return true
        for (i in newList.indices) if (newList[i] != oldList[i]) return true
        return false
    }


    interface Listener {
        fun onImageSelected(image: Image, imageView: View)
    }
}