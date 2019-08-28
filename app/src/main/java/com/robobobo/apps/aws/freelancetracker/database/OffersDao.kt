package com.robobobo.apps.aws.freelancetracker.database

import androidx.room.*
import com.robobobo.apps.aws.freelancetracker.MONTH_IN_MILLIS

@Dao
abstract class OffersDao {

    @Query("SELECT * FROM offers")
    abstract suspend fun getAll(): List<Offer>

    @Query("DELETE FROM offers")
    abstract suspend fun clear()

    @Query("DELETE FROM offers WHERE :time > time + $MONTH_IN_MILLIS ")
    abstract suspend fun clearOld(time: Long = System.currentTimeMillis()): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(offers: List<Offer>)

    @Query("UPDATE offers SET isNew = 0")
    abstract suspend fun markAllAsNotNew()

    @Query("UPDATE offers SET isNew = 0 WHERE id = :offerId")
    abstract suspend fun markAsNotNew(offerId: Int)

    @Query("SELECT * FROM offers WHERE isNew = 1")
    abstract suspend fun getAllNew(): List<Offer>

    suspend fun newCount() = getAllNew().size

    suspend fun markAsNotNew(offer: Offer) = markAsNotNew(offer.id)

    @Transaction
    open suspend fun markAsNotNew(offers: Iterable<Offer>) = offers.forEach { markAsNotNew(it) }

    @Transaction
    open suspend fun markAsNotNew(offersIds: IntArray)= offersIds.forEach { markAsNotNew(it) }

    @Transaction
    open suspend fun addNew(offers: Iterable<Offer>): List<Offer> {
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