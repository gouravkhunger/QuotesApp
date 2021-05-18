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

package com.github.gouravkhunger.quotesapp.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.gouravkhunger.quotesapp.R
import com.github.gouravkhunger.quotesapp.ui.QuotesActivity
import com.github.gouravkhunger.quotesapp.ui.adapters.SavedQuotesAdapter
import com.github.gouravkhunger.quotesapp.viewmodels.QuoteViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_bookmarks.*

class BookmarkFragment : Fragment(R.layout.fragment_bookmarks) {

    // variables
    lateinit var viewModel: QuoteViewModel
    lateinit var savedQuotesAdapter: SavedQuotesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set up viewmodel and recycler view
        viewModel = (activity as QuotesActivity).viewModel
        setupRecyclerView()

        // copy quote when the item is long clicked
        savedQuotesAdapter.setOnItemLongClickListener {
            val clipBoardManager = (activity as QuotesActivity)
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val clipdata = ClipData.newPlainText("quote", it.quote)
            clipBoardManager.setPrimaryClip(clipdata)

            if (!(activity as QuotesActivity).atHome) Snackbar.make(
                view,
                "Quote Copied!",
                Snackbar.LENGTH_SHORT
            ).show()
            true
        }

        // callback which defines what should be done when items are swiped
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            // delete the quote on Swipe
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val quote = savedQuotesAdapter.differ.currentList[position]
                viewModel.deleteQuote(quote)
                Snackbar.make(view, "Removed Bookmark!", Snackbar.LENGTH_SHORT)
                    .apply {
                        // if the click was in error, then provide re-saving option
                        setAction("Undo") {
                            viewModel.saveQuote(quote)
                            if ((activity as QuotesActivity?) != null && !(activity as QuotesActivity).atHome) Snackbar.make(
                                view,
                                "Re-saved!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        setActionTextColor(ContextCompat.getColor(view.context, R.color.light_blue))
                        show()
                    }
            }
        }

        // attach the swipe behavior to each recycler view item
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(rvSavedQuotes)
        }

        // observe data changes and apply them to the recycler view
        viewModel.getSavedQuotes().observe(viewLifecycleOwner, { articles ->
            savedQuotesAdapter.differ.submitList(articles)

            // if no quotes present, then show textview and hide recyclerview
            if (articles.isEmpty()) {
                rvSavedQuotes.visibility = View.GONE
                tvNoBookmarks.visibility = View.VISIBLE
            } else {
                rvSavedQuotes.visibility = View.VISIBLE
                tvNoBookmarks.visibility = View.GONE
            }
        })

    }

    // function to set adapter and layout manager on the recycler view
    private fun setupRecyclerView() {
        savedQuotesAdapter = SavedQuotesAdapter()
        rvSavedQuotes.apply {
            adapter = savedQuotesAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

}