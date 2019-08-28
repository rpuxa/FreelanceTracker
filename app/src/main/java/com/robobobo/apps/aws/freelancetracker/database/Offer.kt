package com.robobobo.apps.aws.freelancetracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.robobobo.apps.aws.freelancetracker.FreelanceMarket
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "offers")
@TypeConverters(value = [Converters::class])
data class Offer(
    val site: FreelanceMarket,
    val title: String,
    val link: String,
    val description: String,
    val suggestions: Int,
    val isNew: Boolean,
    val time: Long
) : Serializable {

    val date: String get() = format.format(Date(time))

    @PrimaryKey
    var id: Int = 0

    fun similar(other: Offer): Boolean {
        return link == other.link && (
                description == other.description ||
                        title == other.title
                )
    }

    fun same(other: Offer) =
        link == other.link &&
                description == other.description &&
                title == other.title

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Offer

        if (site != other.site) return false
        if (title != other.title) return false
        if (link != other.link) return false
        if (description != other.description) return false
        if (suggestions != other.suggestions) return false
        if (isNew != other.isNew) return false
        if (time != other.time) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = site.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + suggestions
        result = 31 * result + isNew.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + id
        return result
    }

    companion object {
        val format = SimpleDateFormat("HH:mm MMMM dd", Locale.US)
    }
}

