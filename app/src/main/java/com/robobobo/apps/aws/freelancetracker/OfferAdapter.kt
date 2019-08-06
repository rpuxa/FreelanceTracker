package com.robobobo.apps.aws.freelancetracker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.robobobo.apps.aws.freelancetracker.database.Offer
import kotlinx.android.synthetic.main.offer_item.view.*
import org.jetbrains.anko.startActivity

class OfferAdapter :
    ListAdapter<Offer, OfferAdapter.OfferViewHolder>(Diff) {

   inner class OfferViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        private val site: TextView = view.site
        private val title: TextView = view.title
        private val description: TextView = view.description
        private val time: TextView = view.time
        private val suggestions: TextView = view.suggestions
        private val isNew : ImageView = view.is_new

        @SuppressLint("SetTextI18n")
        fun bind(offer: Offer) {
            val context = itemView.context
            site.text = offer.site.name
            title.text = offer.title
            description.text = offer.description
            time.text = offer.date
            suggestions.text = context.getString(R.string.suggestions, offer.suggestions)
            isNew.isInvisible = !offer.isNew
            itemView.setOnClickListener {
                isNew.isInvisible = true
                context.startActivity<OfferActivity>(
                    OfferActivity.OFFER to offer,
                    OfferActivity.FROM_NOTIFICATION to false
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.offer_item,
            parent,
            false
        )

        return OfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    private object Diff : DiffUtil.ItemCallback<Offer>() {
        override fun areItemsTheSame(oldItem: Offer, newItem: Offer) =
            oldItem.same(newItem)

        override fun areContentsTheSame(oldItem: Offer, newItem: Offer) =
            oldItem == newItem
    }
}