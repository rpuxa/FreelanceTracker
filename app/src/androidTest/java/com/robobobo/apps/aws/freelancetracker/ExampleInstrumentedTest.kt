package com.robobobo.apps.aws.freelancetracker

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.robobobo.apps.aws.freelancetracker.database.MyDataBase
import com.robobobo.apps.aws.freelancetracker.database.Offer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DataBaseTest {

    @Test
    fun clearTest() = runBlocking(Dispatchers.IO) {
        val dataBase = MyDataBase.create(InstrumentationRegistry.getTargetContext()).offersDao

        fun createOffer(id: Int): Offer {
            return Offer(
                FreelanceMarket.KWORK,
                id.toString(),
                id.toString(),
                id.toString(),
                0,
                true,
                System.currentTimeMillis()
            ).apply { this.id = id }
        }

        dataBase.clear()
        dataBase.insert(List(10) { createOffer(it) })
        assertEquals(10, dataBase.newCount())
        dataBase.markAllAsNotNew()
        assertEquals(0, dataBase.newCount())

        assertEquals(0, dataBase.clearOld())
        val newOffers = listOf(createOffer(11).apply { id = 0 })
        val new = dataBase.addNew(newOffers)
        assertEquals(1, new.size)
        val id = new[0].id
        assertEquals(10, id)
        dataBase.markAsNotNew(intArrayOf(id))
        assertEquals(0, dataBase.newCount())
    }

    @Test
    fun f() {

    }
}
