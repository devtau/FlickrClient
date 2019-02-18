package com.devtau.database.tables

import com.devtau.rest.model.Image
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteColumn
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteCreator
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteType

@StorIOSQLiteType(table = ImageStored.TABLE_NAME)
data class ImageStored @StorIOSQLiteCreator constructor (
    @StorIOSQLiteColumn(name = "_id", key = true) var id: Long?,
    @StorIOSQLiteColumn(name = "owner") var owner: String?,
    @StorIOSQLiteColumn(name = "secret") var secret: String?,
    @StorIOSQLiteColumn(name = "server") var server: String?,
    @StorIOSQLiteColumn(name = "farm") var farm: String?,
    @StorIOSQLiteColumn(name = "title") var title: String?,
    @StorIOSQLiteColumn(name = "isPublic") var isPublic: Boolean?,
    @StorIOSQLiteColumn(name = "isFriend") var isFriend: Boolean?,
    @StorIOSQLiteColumn(name = "isFamily") var isFamily: Boolean?
) {
    private fun convertToImage(): Image = Image(id, owner, secret, server, farm, title,
        if (isPublic == true) 1 else 0, if (isFriend == true) 1 else 0, if (isFamily == true) 1 else 0)


    companion object {
        const val TABLE_NAME = "Images"

        fun convertListToImages(imagesStored: List<ImageStored>): List<Image> {
            val images = ArrayList<Image>()
            for (next in imagesStored) images.add(next.convertToImage())
            return images
        }

        fun convertListToStored(images: List<Image>): List<ImageStored> {
            val imagesStored = ArrayList<ImageStored>()
            for (next in images) imagesStored.add(convertFromImage(next))
            return imagesStored
        }

        private fun convertFromImage(image: Image): ImageStored = ImageStored(
            image.id, image.owner, image.secret, image.server, image.farm, image.title, image.isPublic == 1,
            image.isFriend == 1, image.isFamily == 1)
    }
}