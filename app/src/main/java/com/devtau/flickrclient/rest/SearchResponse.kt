package com.devtau.flickrclient.rest

import com.devtau.flickrclient.rest.model.Image

class SearchResponse {

    val images: List<Image>? get() = photos?.photo
    private val photos: Photo? = null


    private class Photo(
        val page: Int,
        val pages: Int,
        val perpage: Int,
        val total: Int,
        val photo: List<Image>
    )
}