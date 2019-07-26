package com.robobobo.apps.aws.freelancetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.robobobo.apps.aws.freelancetracker.database.MyDataBase
import com.robobobo.apps.aws.freelancetracker.database.Offer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private var offersList = emptyList<Offer>()
    private val adapter = OfferAdapter()
    private val dao by lazy { MyDataBase.create(this).offersDao }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        offers.layoutManager = LinearLayoutManager(this)
        offers.adapter = adapter
        offers.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        track.setOnClickListener {
            track.isChecked = if (RequestBroadcast.isRunning(this)) {
                RequestBroadcast.stop(this)
                false
            } else {
                RequestBroadcast.start(this)
                true
            }
        }

        read_all.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                updateOfferAdapter(
                    offersList.map {
                        Offer(
                            it.site,
                            it.title,
                            it.link,
                            it.description,
                            it.suggestions,
                            false,
                            it.time
                        )
                    }
                )
                dao.markAllAsNotNew()
            }
        }

        notify.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                sendBroadcast(RequestBroadcast.intent(this@MainActivity))
            }
        }

        update.setOnClickListener {
            loadOffers()
        }

        loadOffers()
    }

    override fun onResume() {
        super.onResume()
        track.isChecked = RequestBroadcast.isRunning(this)
    }

    private fun updateOfferAdapter(list: List<Offer>) {
        offersList = list
        adapter.submitList(list)
    }

    private fun loadOffers() =
        lifecycleScope.launch(Dispatchers.Main) {
            progress_bar.isVisible = true
            read_all.isVisible = false
            val result = sortOffers(
                FreelanceMarket.loadAllOffers()
            )
            updateOfferAdapter(result)
            progress_bar.isVisible = false
            read_all.isVisible = true
        }

    private suspend fun sortOffers(offers: List<Offer>): List<Offer> {
        val all = dao.getAll()
        val toSort = ArrayList<Offer>()
        offers.forEach { offer ->
            toSort.add(all.find { it.same(offer) } ?: offer)
        }
        toSort.sortBy { -it.time }

        return toSort
    }
}