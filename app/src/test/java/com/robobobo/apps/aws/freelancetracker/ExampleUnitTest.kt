package com.robobobo.apps.aws.freelancetracker

import org.jsoup.Jsoup
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val ARGS =
        "action=postfilter&kind=5&pf_category=&pf_subcategory=&comboe_columns[1]=0&comboe_columns[0]=0&comboe_column_id=0&comboe_db_id=0&comboe=Все+специализации&pf_categofy[1][225]=1&location_columns[1]=0&location_columns[0]=0&location_column_id=0&location_db_id=0&location=Все+страны&pf_cost_from=&pf_cost_to=&pf_keywords=&u_token_key=5a72cb8488fe5673019423e44f0a79ce"
            .split('&')
            .associate {
                val (first, second) = it.split('=')
                first to second
            }

    @Test
    fun server() {
        val result = Jsoup.connect("https://www.fl.ru/projects/")
            .data(ARGS)
            .post()
            .select("div")
            .first { it.id() == "projects-list" }
            .children()
            .filter { it.className() == "b-post  b-post_padbot_15 b-post_margbot_20 b-post_bordbot_eee b-post_relative" }
            .map {
                it
            }
    }
}



fun Any.toConsole(): Unit = print("$this  ")

fun main() {
    val list = listOf(1, 2, 3)
    val array = IntArray(1)

    { 42 }.toConsole()
    list.toConsole()
    array.toConsole()
}

// 1) [I@3f99bd52  [1, 2, 3]  kotlin.Unit
// 2) 42  java.util.ArrayList@4481f34a  [I@448139f0
// 2) kotlin.Unit  [1, 2, 3]  [I@448139f0
// 4) Function0<java.lang.Integer>  java.util.ArrayList@4481f34a  [I@448139f0
// 5) Function0<java.lang.Integer>  [1, 2, 3]  [I@448139f0
