package com.devtau.flickrclient

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.devtau.rest.util.Logger
/**
 * https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
 */
class CustomGridLayoutManager(context: Context?, spanCount: Int): GridLayoutManager(context, spanCount) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
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