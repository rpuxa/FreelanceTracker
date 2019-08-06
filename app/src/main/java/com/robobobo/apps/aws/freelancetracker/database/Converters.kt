package com.robobobo.apps.aws.freelancetracker.database

import androidx.room.TypeConverter
import com.robobobo.apps.aws.freelancetracker.FreelanceMarket

@Suppress("unused")
class Converters {

    @TypeConverter
    fun Int.toFreelanceMarket() = FreelanceMarket.values().first { it.id == this }

    @TypeConverter
    fun FreelanceMarket.toInt() = id
}