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

package com.github.gouravkhunger.quotesapp.repository

import com.github.gouravkhunger.quotesapp.api.QuoteAPI
import com.github.gouravkhunger.quotesapp.db.QuoteDao
import com.github.gouravkhunger.quotesapp.models.Quote
import javax.inject.Inject

// bridge between View Model, API and Database
class QuoteRepository @Inject constructor(
    private val dao: QuoteDao,
    private val api: QuoteAPI
) {

    suspend fun getRandomQuote() = api.getRandomQuote()

    suspend fun upsert(quote: Quote) = dao.upsert(quote)

    suspend fun deleteQuote(quote: Quote) =
        dao.deleteSavedQuote(quote)

    fun getSavedQuotes() = dao.getSavedQuotes()
}
