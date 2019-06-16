package com.devtau.flickrclient.db.dao

import androidx.room.*
import com.devtau.flickrclient.rest.model.SearchItem
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface SearchItemDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg searchItems: SearchItem): Completable

    @Query("SELECT * FROM SearchItems WHERE _id = :id")
    fun getById(id: String): Flowable<SearchItem>

    @Query("SELECT _id, `query`, MAX(lastUsed) as lastUsed FROM SearchItems")
    fun getLastSearched(): Flowable<SearchItem>

    @Query("SELECT * FROM SearchItems")
    fun getList(): Flowable<List<SearchItem>>

    @Delete
    fun delete(vararg searchItems: SearchItem): Completable

    @Query("DELETE FROM SearchItems")
    fun delete()
}