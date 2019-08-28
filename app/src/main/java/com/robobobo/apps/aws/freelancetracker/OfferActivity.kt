package com.robobobo.apps.aws.freelancetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.robobobo.apps.aws.freelancetracker.database.MyDataBase
import com.robobobo.apps.aws.freelancetracker.database.Offer
import kotlinx.android.synthetic.main.activity_offer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.browse
import org.jetbrains.anko.startActivity
import java.io.IOException
import kotlin.properties.Delegates.notNull

class OfferActivity : AppCompatActivity() {

    private var openedFromNotification: Boolean by notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offer)
        openedFromNotification = intent?.extras?.get(FROM_NOTIFICATION) as? Boolean
            ?: error("Needs from_notification flag")
        val offer = intent?.extras?.get(OFFER) as? Offer ?: error("Needs offer")

        val dao = MyDataBase.create(this).offersDao

        offer_title.text = offer.title
        offer_date.text = offer.date
        offer_site.text = offer.site.designation
        offer_suggestions.text = getString(R.string.suggestions, offer.suggestions)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        lifecycleScope.launch(Dispatchers.Main) {
            offer_progress_bar.isVisible = true
            offer_info.isVisible = false

            var fullOffer: FullOffer
            while (true) {
                try {
                    fullOffer = offer.site.loadFullOffer(offer)
                    break
                } catch (e: IOException) {
                }
            }

            dao.markAsNotNew(offer)

            offer_text.text = fullOffer.fullText

            offer_answer.setOnClickListener {
                browse(fullOffer.answerLink)
            }

            offer_progress_bar.isVisible = false
            offer_info.isVisible = true
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        if (openedFromNotification)
            startActivity<MainActivity>()
        else
            onBackPressed()
        return true
    }

    companion object {
        const val OFFER = "offer"
        const val FROM_NOTIFICATION = "notf"
    }
}
