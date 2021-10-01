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

package com.github.gouravkhunger.quotesapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.github.gouravkhunger.quotesapp.R
import com.github.gouravkhunger.quotesapp.db.QuoteDataBase
import com.github.gouravkhunger.quotesapp.repository.QuoteRepository
import com.github.gouravkhunger.quotesapp.ui.fragments.BookmarkFragmentDirections
import com.github.gouravkhunger.quotesapp.ui.fragments.QuoteFragmentDirections
import com.github.gouravkhunger.quotesapp.viewmodels.QuoteViewModel
import com.github.gouravkhunger.quotesapp.viewmodels.QuoteViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_quotes.*

class QuotesActivity : AppCompatActivity() {

    // variables
    lateinit var viewModel: QuoteViewModel
    var atHome = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)

        // initialise variables
        val quoteRepository = QuoteRepository(QuoteDataBase(this))
        val viewModelProviderFactory = QuoteViewModelProviderFactory(application, quoteRepository)

        viewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(QuoteViewModel::class.java)

        // logic to switch between fragments
        myBookmarksImgBtn.setOnClickListener {
            val action = QuoteFragmentDirections
                .actionQuoteFragmentToBookmarkFragment()
            val navController = Navigation.findNavController(quotesNavHostFragment)
            navController.navigate(action)
            it.visibility = View.GONE
            backToQuotePage.visibility = View.VISIBLE
            activity_title.text = resources.getText(R.string.myBookMarks)
            atHome = false
        }

        backToQuotePage.setOnClickListener {
            val action = BookmarkFragmentDirections
                .actionBookmarkFragmentToQuoteFragment()
            val navController = Navigation.findNavController(quotesNavHostFragment)

            navController.navigate(action)
            it.visibility = View.GONE
            myBookmarksImgBtn.visibility = View.VISIBLE
            activity_title.text = resources.getText(R.string.app_name)
            atHome = true
        }

        // Update theme once everything is set up
        setTheme(R.style.Theme_QuotesApp)

    }

    // implementation to handle error cases regarding navigation icons
    // this function updates the icons and sets variables according
    // to how navigation was carried out
    override fun onBackPressed() {
        super.onBackPressed()
        if (!atHome) {
            backToQuotePage.visibility = View.GONE
            myBookmarksImgBtn.visibility = View.VISIBLE
	    activity_title.text = resources.getText(R.string.app_name)
            atHome = true
        }
    }
}