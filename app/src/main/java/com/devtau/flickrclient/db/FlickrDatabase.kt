package com.devtau.flickrclient.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.devtau.flickrclient.BuildConfig
import com.devtau.flickrclient.db.dao.ImageDAO
import com.devtau.flickrclient.db.dao.SearchItemDAO
import com.devtau.flickrclient.rest.model.Image
import com.devtau.flickrclient.rest.model.SearchItem

@Database(entities = [Image::class, SearchItem::class], version = 1)
abstract class FlickrDatabase: RoomDatabase() {

    abstract fun imageDao(): ImageDAO
    abstract fun searchItemDao(): SearchItemDAO


    companion object {
        @Volatile private var INSTANCE: FlickrDatabase? = null

        fun getInstance(context: Context): FlickrDatabase =
            INSTANCE ?: synchronized(this) { INSTANCE ?: buildDatabase(context).also { INSTANCE = it } }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, FlickrDatabase::class.java, BuildConfig.DATABASE_NAME)
                .fallbackToDestructiveMigration().build()
    }
}