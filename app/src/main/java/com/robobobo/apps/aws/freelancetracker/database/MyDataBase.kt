package com.robobobo.apps.aws.freelancetracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Offer::class],
    version = 1
)
abstract class MyDataBase : RoomDatabase() {

    companion object {
        fun create(context: Context) =
            Room.databaseBuilder(context, MyDataBase::class.java, "my.db")
                .build()
    }

    abstract val offersDao: OffersDao
}