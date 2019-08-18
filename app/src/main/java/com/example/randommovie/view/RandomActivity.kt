package ru.arston.randommovie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.randommovie.DetailsActivity
import com.example.randommovie.network.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.arston.randommovie.Models.Genre
import ru.arston.randommovie.Models.Movie
import ru.arston.randommovie.databinding.FragmentRandomBinding
import java.util.*


class RandomActivity : Fragment() {

    lateinit var binding: FragmentRandomBinding
    private val apiKey: String = BuildConfig.TMDB_API_KEY
    private val url = "https://api.themoviedb.org/3/"
    private val imageUrl = "http://image.tmdb.org/t/p/w500"

    lateinit var genreList: List<Genre.Attributes>
    lateinit var movieList: List<Movie.Result>
    var page: Int = 0

    val years = ArrayList<String>()
    val genreAll = mutableMapOf<Int, Int>()
    var genreID: Int = 0
    var movieYear: String = "0"

    var apiService = Api.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_random, container, false)

        binding.buttonRandom.setOnClickListener {
            getMovie()
        }

        apiService.getGenres(apiKey).enqueue(object : Callback<Genre> {
            override fun onFailure(call: Call<Genre>, t: Throwable) {

            }

            override fun onResponse(call: Call<Genre>, response: Response<Genre>) {
                genreList = response.body()?.genres!!
                var j = 1
                for (i in genreList.indices) {
                    genreAll[j] = genreList[i].id!!
                    j++
                }
                spinnerAdapter()
            }
        })

        years.clear()
        for (i in 1895..2019) years.add(i.toString())
        years.reverse()
        years.add(0, "Год")
        binding.spinnerYear.adapter = ArrayAdapter(context, R.layout.item_spinner, years)
        binding.spinnerYear?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                movieYear = binding.spinnerYear.selectedItem as String
            }
        }

        getMovie()

        return binding.root
    }

    /**
     * Метод отправляет запрос на сервер и, получив все фильмы,
     * генерирует номер случайной Json-страницы (page) из всех полученных, затем отправлет новый запрос,
     * передав в качестве одного из параметров запроса номер сгенерированной страницы.
     */
    private fun getMovie() {

        GlobalScope.launch(Dispatchers.Main) {
            // Если выбран какой-либо жанр
            if (genreID != 0) {
                val allMovieRequest = apiService.getAllMovie(genreID, movieYear, "US", apiKey)
                try {
                    val response = allMovieRequest.await()
                    if (response.isSuccessful) {
                        if (response.body()?.totalPages!! <= 1000) {
                            page = (1..response.body()?.totalPages!!).random()
                        } else page = (1..1000).random()


                    } else {
                        Log.e("MainActivity ", response.errorBody().toString())
                    }
                } catch (e: Exception) {

                }

                val randomMovieRequest = apiService.getRandomMovie(genreID, movieYear, page, "US", apiKey)
                try {
                    val response = randomMovieRequest.await()
                    if (response.isSuccessful) {
                        val movieResponse = response.body()
                        movieList = movieResponse?.results!!
                        setAdapter()

                    } else {
                        Log.e("MainActivity ", response.errorBody().toString())
                    }
                } catch (e: Exception) {

                }
                // Если жанр не выбран
            } else {
                val allMovieRequest = apiService.getAllMovieWithoutGenre(movieYear, "US", apiKey)
                try {
                    val response = allMovieRequest.await()
                    if (response.isSuccessful) {
                        if (response.body()?.totalPages!! <= 1000) {
                            page = (1..response.body()?.totalPages!!).random()
                        } else page = (1..1000).random()


                    } else {
                        Log.e("MainActivity ", response.errorBody().toString())
                    }
                } catch (e: Exception) {

                }

                val movieRequest = apiService.getWithoutGenre(movieYear, page, "US", apiKey)
                try {
                    val response = movieRequest.await()
                    if (response.isSuccessful) {
                        val movieResponse = response.body()
                        movieList = movieResponse?.results!!
                        setAdapter()

                    } else {
                        Log.e("MainActivity ", response.errorBody().toString())
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    fun spinnerAdapter() {

        binding.spinnerGenre.adapter =
            ArrayAdapter.createFromResource(context, R.array.movie_genres, R.layout.item_spinner)

        binding.spinnerGenre?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    genreID = 0
                } else genreID = genreAll[position]!!
            }
        }
    }

    /**
     * Функция заполняет данными CardView
     */
    private fun setAdapter() {
        val r = (0..movieList.size).random()
        Glide.with(this).load(imageUrl + movieList[r].posterPath).diskCacheStrategy(
            DiskCacheStrategy.ALL
        ).into(binding.cardImage)
        binding.cardTitle.text = movieList[r].title
        binding.movieRating.rating = (movieList[r].voteAverage!! / 2).toFloat()

        binding.cardView.setOnClickListener {
            val intent = Intent(activity, DetailsActivity::class.java)
            intent.putExtra("MovieID", movieList[r].id.toString())
            intent.putExtra("title", movieList[r].title)
            activity?.startActivity(intent)
        }
    }
}


