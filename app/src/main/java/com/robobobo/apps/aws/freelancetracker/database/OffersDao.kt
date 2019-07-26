package com.robobobo.apps.aws.freelancetracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robobobo.apps.aws.freelancetracker.MONTH_IN_MILLIS

@Dao
abstract class OffersDao {

    @Query("SELECT * FROM offers")
    abstract suspend fun getAll(): List<Offer>

    @Query("DELETE FROM offers")
    abstract suspend fun clear()

    @Query("DELETE FROM offers WHERE :time > time + $MONTH_IN_MILLIS ")
    abstract suspend fun clearOld(time: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(offers: List<Offer>)

    @Query("UPDATE offers SET isNew = 0")
    abstract suspend fun markAllAsNotNew()

    @Query("UPDATE offers SET isNew = 0 WHERE id == :offerId")
    abstract suspend fun markAsNotNew(offerId: Int)

    suspend fun markAsNotNew(offer: Offer) = markAsNotNew(offer.id)

    suspend fun markAsNotNew(offers: List<Offer>) = offers.forEach { offer -> markAsNotNew(offer.id) }

    suspend fun addNew(offers: List<Offer>): List<Offer> {
        val all = getAll()
        var id = (all.maxBy { it.id }?.id ?: 0) + 1
        val new = ArrayList<Offer>()
        val toInsert = ArrayList<Offer>()
        for (offer in offers) {
            val offerInDB = all.find { it.similar(offer) }

            if (offerInDB == null) {
                offer.id = id++
                new.add(offer)
                toInsert.add(offer)
            } else if (!offerInDB.same(offer)) {
                offer.id = offerInDB.id
                toInsert.add(offer)
            }
        }

        insert(toInsert)

        return new
    }
}