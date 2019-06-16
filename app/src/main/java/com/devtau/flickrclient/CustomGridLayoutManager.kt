package com.devtau.flickrclient

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devtau.flickrclient.util.Logger
/**
 * https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
 */
class CustomGridLayoutManager(context: Context?, spanCount: Int): androidx.recyclerview.widget.GridLayoutManager(context, spanCount) {

    override fun onLayoutChildren(recycler: androidx.recyclerview.widget.RecyclerView.Recycler?, state: androidx.recyclerview.widget.RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Logger.w(LOG_TAG, "meet a IOOBE in RecyclerView")
        }
    }


    companion object {
        private const val LOG_TAG = "CustomGridLayoutManager"
    }
}