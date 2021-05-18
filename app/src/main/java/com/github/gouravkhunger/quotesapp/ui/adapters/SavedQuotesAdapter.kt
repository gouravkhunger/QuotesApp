/*
 * MIT License
 *
 * Copyright (c) 2021 Gourav Khunger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.gouravkhunger.quotesapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.gouravkhunger.quotesapp.R
import com.github.gouravkhunger.quotesapp.models.Quote
import kotlinx.android.synthetic.main.quote_item.view.*

// Adapter of RecyclerView present in Bookmarked Quotes Fragment
class SavedQuotesAdapter : RecyclerView.Adapter<SavedQuotesAdapter.QuoteViewHolder>() {

    inner class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // differ callback that checks if elements are same of not
    private val differCallback = object : DiffUtil.ItemCallback<Quote>() {
        override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    // inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        return QuoteViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.quote_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    // Custom item onLongClick listener
    private var onItemLongClickListener: ((Quote) -> Boolean)? = null

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = differ.currentList[position]
        holder.itemView.apply {

            // set up each item in the recycler view
            rvQuoteTv.text = resources.getString(R.string.quote, quote.quote)
            rvAuthorTv.text = resources.getString(R.string.author, quote.author)

            rvQuoteTv.visibility = View.VISIBLE
            rvAuthorTv.visibility = View.VISIBLE

            rvQuoteLoading.visibility = View.GONE

            // onLongClick Listner definition
            setOnLongClickListener {
                onItemLongClickListener.let {
                    if (it != null) {
                        it(quote)
                    }
                    true
                }
            }
        }
    }

    // actual method to set custom defined listener
    fun setOnItemLongClickListener(listener: (Quote) -> Boolean) {
        onItemLongClickListener = listener
    }
}