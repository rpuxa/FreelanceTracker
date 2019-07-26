package com.robobobo.apps.aws.freelancetracker.database

import androidx.room.TypeConverter
import com.robobobo.apps.aws.freelancetracker.FreelanceMarket

class Converters {

    @TypeConverter
    fun toFreelanceMarket(id: Int) = FreelanceMarket.values().first { it.id == id }

    @TypeConverter
   fun fromFreelanceMarket(market: FreelanceMarket) = market.id
}