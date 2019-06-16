package com.devtau.flickrclient.db.dao

import androidx.room.*
import com.devtau.flickrclient.rest.model.Image
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface ImageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg list: Image): Completable

    @Query("SELECT * FROM Images WHERE _id = :id")
    fun getById(id: String): Flowable<Image>

    @Query("SELECT * FROM Images")
    fun getList(): Flowable<List<Image>>

    @Delete
    fun delete(vararg list: Image): Completable

    @Query("DELETE FROM Images")
    fun delete()
}