/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.raywenderlich.kotlin.coroutines.domain.repository

import com.example.coroutinesexample.data.api.MovieApiService
import com.raywenderlich.kotlin.coroutines.data.database.MovieDao
import com.example.coroutinesexample.di.API_KEY
import com.raywenderlich.kotlin.coroutines.data.model.Movie
import com.example.coroutinesexample.data.model.Result
import com.example.coroutinesexample.domain.repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Connects to the end entity, and exposes functionality to the user.
 */
class MovieRepositoryImpl(
    private val movieApiService: MovieApiService,
    private val movieDao: MovieDao
) : MovieRepository {

    override suspend fun getMovies(): Result<List<Movie>> = withContext(Dispatchers.) {

        val cashedMoviesDeffer = async { movieDao.getSavedMovies() }
        val resultDeffer = async { movieApiService.getMovies(API_KEY).execute() }

        val cashedMovies = cashedMoviesDeffer.await()
        try {
            val result = resultDeffer.await()
            val movesResponse = result.body()?.movies

            if (result.isSuccessful && movesResponse != null) {
                Result(movesResponse, null)
            } else {
                Result(cashedMovies, null)
            }
        } catch (error: Throwable) {
            if (error is IOException && cashedMovies.isEmpty()) {
                Result(null, error)
            } else {
                Result(cashedMovies, null)
            }
        }
    }
}