package com.robobobo.apps.aws.freelancetracker

import com.robobobo.apps.aws.freelancetracker.database.Offer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.io.Serializable

enum class FreelanceMarket(val id: Int, val designation: String) : Serializable {
    WEBLANCER(0, "Weblancer") {
        private val baseUrl = "https://www.weblancer.net"

        override fun loadOffers(): List<Offer> {
            return try {
                Jsoup.connect("$baseUrl/jobs/mobilynye-prilozheniya-28/")
                    .data()
                    .get()
                    .select("div")
                    .first { it.className() == "cols_table" }
                    .children()
                    .map {
                        val row = it.child(0)
                        val title = row.child(0).child(0)
                        val link = baseUrl + title.attr("href")
                        val name = title.text()
                        val description = row.child(1)
                        val suggestionsString = it.child(1).child(1)
                            .let { element ->
                                if (element.children().isEmpty()) element.text() else element.child(
                                    0
                                ).text()
                            }
                        val suggestions =
                            if (suggestionsString.startsWith("нет")) {
                                0
                            } else {
                                suggestionsString.substringBefore(' ').toIntOrNull() ?: -1
                            }
                        Offer(
                            WEBLANCER,
                            name,
                            link,
                            description.text(),
                            suggestions,
                            true,
                            System.currentTimeMillis()
                        )
                    }
            } catch (e: IOException) {
                @Suppress("RemoveExplicitTypeArguments")
                emptyList<Offer>()
            }
        }

        override suspend fun loadFullOffer(offer: Offer): FullOffer {
            return withContext(Dispatchers.IO) {
                val doc =
                    Jsoup.connect(offer.link)
                        .get()
                        .select("div")

                val fullText = doc.first { it.className() == "col-12 text_field" }
                    .child(1)
                    .text()

                FullOffer(offer, fullText, offer.link)
            }
        }
    },

    KWORK(1, "Kwork") {
        override fun loadOffers(): List<Offer> {
            return try {
                Jsoup.connect("https://kwork.ru/projects?c=39&")
                    .get()
                    .select("div")
                    .first { it.className() == "project-list js-project-list" }
                    .children()
                    .select("div.card")
                    .map {
                        val row = it.child(0).child(0)
                        val title = row.child(0).child(0).child(0)
                        val description = row.child(1).child(0).text()
                        val suggestionsString = it.child(0).child(2).child(0).text()
                        val suggestions =
                            suggestionsString.substring(suggestionsString.lastIndexOf(' ') + 1)
                                .toInt()
                        Offer(
                            KWORK,
                            title.text(),
                            title.attr("href"),
                            description,
                            suggestions,
                            true,
                            System.currentTimeMillis()
                        )
                    }
            } catch (e: IOException) {
                @Suppress("RemoveExplicitTypeArguments")
                emptyList<Offer>()
            }
        }

        override suspend fun loadFullOffer(offer: Offer): FullOffer {
            return withContext(Dispatchers.IO) {
                val doc = Jsoup.connect(offer.link)
                    .get()
                    .select("div")

                val fullText =
                    doc.first { it.className() == "wish_name f14 first-letter br-with-lh break-word lh22" }
                        .child(0)
                        .text()

                val projectLink =
                    doc.first { it.className() == "color-gray project_card--informers project_card--informers-justify" }
                        .child(1)
                        .child(0)
                        .baseUri()
                val projectId = projectLink.substring(projectLink.lastIndexOf('/') + 1)
                FullOffer(offer, fullText, "https://kwork.ru/new_offer?project=$projectId")
            }
        }
    },
/*
    FL(2, "FL") {
         private val ARGS =
            "action=postfilter&kind=5&pf_category=&pf_subcategory=&comboe_columns[1]=0&comboe_columns[0]=0&comboe_column_id=0&comboe_db_id=0&comboe=Все+специализации&pf_categofy[1][225]=1&location_columns[1]=0&location_columns[0]=0&location_column_id=0&location_db_id=0&location=Все+страны&pf_cost_from=&pf_cost_to=&pf_keywords=&u_token_key=5a72cb8488fe5673019423e44f0a79ce"
                        .split('&')
                        .associate {
                            val (first, second) = it.split('=')
                            first to second
                        }

        override fun loadOffers(): List<Offer> {
            val result = Jsoup.connect("https://www.fl.ru/projects/")
                .data(ARGS)
                .post()
                .select("div")
                .first { it.id() == "projects-list" }

        }

        override suspend fun loadFullOffer(offer: Offer): FullOffer {
        }
    }*/
    ;

    protected abstract fun loadOffers(): List<Offer>

    abstract suspend fun loadFullOffer(offer: Offer): FullOffer

    companion object {
        suspend fun loadAllOffers(): List<Offer> = coroutineScope {
            val list = ArrayList<Offer>()
            values()
                .map {
                    async(Dispatchers.IO) { it.loadOffers() }
                }
                .forEach {
                    list += it.await()
                }
            list
        }
    }
}