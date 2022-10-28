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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.gouravkhunger.quotesapp.R
import com.github.gouravkhunger.quotesapp.models.Quote
import com.github.gouravkhunger.quotesapp.ui.QuotesActivity
import com.github.gouravkhunger.quotesapp.util.Constants.Companion.MIN_SWIPE_DISTANCE
import com.github.gouravkhunger.quotesapp.util.Resource
import com.github.gouravkhunger.quotesapp.util.ShareUtils
import com.github.gouravkhunger.quotesapp.viewmodels.QuoteViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_quote.*
import kotlinx.android.synthetic.main.fragment_quote.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

@AndroidEntryPoint
class QuoteFragment : Fragment(R.layout.fragment_quote) {

    // variables
    private val viewModel by activityViewModels<QuoteViewModel>() // getting viewModel linked to activity
    private var quote: Quote? = null
    private var quoteShown = false
    private var isBookMarked = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.quote.observe(viewLifecycleOwner) { response ->

            // change UI based on what type of resource state the quote is
            // currently in
            when (response) {

                is Resource.Loading -> {
                    // quote is loading
                    showProgressBar()
                    noQuote.visibility = View.GONE
                    fab.visibility = View.GONE
                    quoteShare.visibility = View.GONE
                    quoteShown = false
                    quote = null
                }

                is Resource.Success -> {
                    // quote loaded successfully
                    hideProgressBar()
                    noQuote.visibility = View.GONE
                    fab.visibility = View.VISIBLE
                    quoteShare.visibility = View.VISIBLE
                    response.data.let { quoteResponse ->
                        quote = quoteResponse!!
                        quoteTv.text = resources.getString(R.string.quote, quoteResponse.quote)
                        authorTv.text = resources.getString(R.string.author, quoteResponse.author)
                        showTextViews()
                    }
                    quoteShown = true
                }

                is Resource.Error -> {
                    // there was some error while loading quote
                    hideProgressBar()
                    hideTextViews()
                    noQuote.visibility = View.VISIBLE
                    fab.visibility = View.GONE
                    quoteShare.visibility = View.GONE
                    response.message.let {
                        noQuote.text = it
                    }
                    quoteShown = false
                    quote = null
                }
            }
        }

        // observe bookmarked value from view model
        // and update fab icon based on value
        viewModel.bookmarked.observe(viewLifecycleOwner) {
            isBookMarked = it
            fab.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (isBookMarked) R.drawable.ic_bookmarked
                    else R.drawable.ic_unbookmarked
                )
            )
        }

        // detect left swipe on the "quote card".
        quoteCard.setOnTouchListener(
            View.OnTouchListener { v, event ->

                // variables to store current configuration of quote card.
                val displayMetrics = resources.displayMetrics
                val cardWidth = quoteCard.width
                val cardStart = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)

                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        var currentX = quoteCard.x
                        quoteCard.animate()
                            .x(cardStart)
                            .setDuration(150)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                                        delay(100)
                                        // check if the swipe distance was more than
                                        // minimum swipe required to load a new quote
                                        if (currentX < MIN_SWIPE_DISTANCE) {
                                            // Load a new quote if swiped adequately
                                            viewModel.getRandomQuote()
                                            currentX = 0f
                                        }
                                    }
                                }
                            }).start()
                        extraText.text = getString(R.string.info)
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // get the new co-ordinate of X-axis
                        val newX = event.rawX

                        // carry out swipe only if newX < cardStart, that is,
                        // the card is swiped to the left side, not to the right
                        // Detailed explanation at: https://genicsblog.com/swipe-animation-on-a-cardview-android
                        if (newX - cardWidth < cardStart) {
                            Log.d("Values", "$cardStart --- $newX ---- ${displayMetrics.widthPixels.toFloat()}  ---- ${newX - (cardWidth / 2)}")
                            quoteCard.animate()
                                .x(
                                    min(cardStart, newX - (cardWidth / 2))
                                )
                                .setDuration(0)
                                .start()
                            if (quoteCard.x < MIN_SWIPE_DISTANCE) extraText.text =
                                getString(R.string.release)
                            else extraText.text = getString(R.string.info)
                        }
                    }
                }

                // required to by-pass lint warning
                v.performClick()
                return@OnTouchListener true
            }
        )

        // perform save/delete quote action when fab is clicked
        fab.setOnClickListener {
            if (isBookMarked) {

                // delete quote if it is already bookmarked.
                viewModel.deleteQuote(quote!!)
                if ((activity as QuotesActivity).atHome) Snackbar.make(
                    requireActivity().findViewById(
                        R.id.quotesNavHostFragment
                    ),
                    "Removed Bookmark!", Snackbar.LENGTH_SHORT
                )
                    .apply {
                        setAction("Undo") {
                            viewModel.saveQuote(quote!!)
                            if ((activity as QuotesActivity).atHome) makeSnackBar(view, "Re-saved!")
                            isBookMarked = !isBookMarked
                        }
                        setActionTextColor(ContextCompat.getColor(view.context, R.color.light_blue))
                        show()
                    }

                // work around to hide fab while snackbar is visible
                if (fab != null) fab.visibility = View.INVISIBLE
                this.lifecycleScope.launch(context = Dispatchers.Default) {
                    delay(3000)
                    withContext(Dispatchers.Main) {
                        if (fab != null) fab.visibility = View.VISIBLE
                    }
                }
            } else {
                // save quote if not already saved
                viewModel.saveQuote(quote!!)
                makeSnackBar(view, "Successfully saved Quote!")
            }
        }
        quoteShare.setOnClickListener {
            // Hide share image to not get included in the image
            quoteShare.visibility = View.GONE

            // Actual sharing occurs here
            ShareUtils.share(view.cardHolder, activity as QuotesActivity)

            // Restore the hidden share button back
            quoteShare.visibility = View.VISIBLE
        }

        // copy quote when the clicked on the quote
        quoteTv.setOnClickListener {
            val clipBoardManager = (activity as QuotesActivity)
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val clipdata = ClipData.newPlainText(
                "quote",
                "\"${quote?.quote}\"\n\n- ${quote?.author}"
            )

            clipBoardManager.setPrimaryClip(clipdata)

            Snackbar.make(
                view,
                "Quote Copied!",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    // function names say it all
    private fun showProgressBar() {
        quoteLoading.visibility = View.VISIBLE
        hideTextViews()
    }

    private fun showTextViews() {
        quoteTv.visibility = View.VISIBLE
        authorTvShareBtnParent.visibility = View.VISIBLE
    }

    private fun hideTextViews() {
        quoteTv.visibility = View.GONE
        authorTvShareBtnParent.visibility = View.GONE
    }

    private fun hideProgressBar() {
        quoteLoading.visibility = View.GONE
    }

    private fun makeSnackBar(view: View, message: String) {
        if ((activity as QuotesActivity).atHome) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()

            // workaround to disable floating action button when a snackbar is made
            // to prevent double clicks while task is executing/snackbar is visible
            if (fab.visibility == View.VISIBLE) {
                if (fab != null) fab.visibility = View.INVISIBLE
                lifecycleScope.launch(context = Dispatchers.Default) {
                    delay(3000)
                    withContext(Dispatchers.Main) {
                        if (fab != null) fab.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
