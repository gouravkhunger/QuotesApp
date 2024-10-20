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

package com.github.gouravkhunger.quotesapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.gouravkhunger.quotesapp.models.Quote
import com.github.gouravkhunger.quotesapp.repository.QuoteRepository
import com.github.gouravkhunger.quotesapp.store.Preference
import com.github.gouravkhunger.quotesapp.util.CheckInternet
import com.github.gouravkhunger.quotesapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.IOException
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val internet: CheckInternet,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    // observable variables
    val quote: MutableLiveData<Resource<Quote>> = MutableLiveData()
    val bookmarked: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        // get quote when the view model is called for the first time
        getRandomQuote()
    }

    // function that gets quote from a background thread
    fun getRandomQuote() = viewModelScope.launch {
        bookmarked.postValue(false)
        safeQuotesCall()
    }

    // function that checks for possible phone states before making API requests
    // for example: internet connectivity issues
    private suspend fun safeQuotesCall() {
        try {
            if (internet.hasInternetConnection()) {
                quote.postValue(Resource.Loading())
                val response = quoteRepository.getRandomQuote()
                quote.postValue(handleQuoteResponse(response))
            } else {
                quote.postValue(Resource.Error("No internet connection."))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> quote.postValue(Resource.Error("Network Failure"))
                else -> quote.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    // convert API response to resource type that can be used to fetch status of response
    private fun handleQuoteResponse(response: Response<List<Quote>>): Resource<Quote> {
        if (response.isSuccessful) {
            return Resource.Success(response.body()!![0])
        }
        return Resource.Error(response.message())
    }

    // save a particular quote to the database
    fun saveQuote(quote: Quote) = viewModelScope.launch {
        quoteRepository.upsert(quote)
        bookmarked.postValue(true)
    }

    // get all the saved quotes from the database
    fun getSavedQuotes() = quoteRepository.getSavedQuotes()

    // delete a particular quote
    fun deleteQuote(quote: Quote) = viewModelScope.launch {
        quoteRepository.deleteQuote(quote)
        bookmarked.postValue(false)
    }

    fun <T> saveSetting(pref: Preference<T>, value: T) = viewModelScope.launch {
        quoteRepository.saveSetting(pref, value)
    }

    fun <T> getSetting(pref: Preference<T>): T = runBlocking {
        return@runBlocking quoteRepository.getSetting(pref)
    }
}
