package com.example.movieland.utils

import com.example.datasource.remote.models.responses.Genre

object Helpers {

//    fun getGenresText(ids: List<Int>?): String? {
//        if (ids == null) {
//            return null
//        }
//        return getGenreListFromIds(ids).joinToString(" • ") { it.name }
//    }

    fun getMovieGenreListFromIds(ids: List<Int>?): List<Genre> {
        if (ids == null) {
            return emptyList()
        }
        val results = mutableListOf<Genre>()
        ids.forEach {
            moviesGenresMap.containsKey(it) && results.add(Genre(it, moviesGenresMap[it]!!))
        }
        return results
    }

    fun getAllMovieGenreList() = arrayListOf(
        Genre(28, "Action"),
        Genre(12, "Adventure"),
        Genre(16, "Animation"),
        Genre(35, "Comedy"),
        Genre(80, "Crime"),
        Genre(99, "Documentary"),
        Genre(18, "Drama"),
        Genre(10751, "Family"),
        Genre(14, "Fantasy"),
        Genre(36, "History"),
        Genre(27, "Horror"),
        Genre(10402, "Music"),
        Genre(9648, "Mystery"),
        Genre(10749, "Romance"),
        Genre(878, "Science Fiction"),
        Genre(10770, "TV Movie"),
        Genre(53, "Thriller"),
        Genre(10752, "War"),
        Genre(37, "Western"),
    )

    fun getAllTvGenreList() = arrayListOf(
        Genre(10759, "Action & Adventure"),
        Genre(16, "Animation"),
        Genre(35, "Comedy"),
        Genre(80, "Crime"),
        Genre(99, "Documentary"),
        Genre(18, "Drama"),
        Genre(10751, "Family"),
        Genre(10762, "Kids"),
        Genre(9648, "Mystery"),
        Genre(10763, "News"),
        Genre(10764, "Reality"),
        Genre(10765, "Sci-Fi & Fantasy"),
        Genre(10766, "Soap"),
        Genre(10767, "Talk"),
        Genre(10768, "War & Politics"),
        Genre(37, "Western")
    )

    private
    val moviesGenresMap: HashMap<Int, String> = hashMapOf(
        28 to "Action",
        12 to "Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        14 to "Fantasy",
        36 to "History",
        27 to "Horror",
        10402 to "Music",
        9648 to "Mystery",
        10749 to "Romance",
        878 to "Science Fiction",
        10770 to "TV Movie",
        53 to "Thriller",
        10752 to "War",
        37 to "Western",
    )

    private
    val tvShowsGenresMap: HashMap<Int, String> = hashMapOf(
        10759 to "Action & Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        10762 to "Kids",
        9648 to "Mystery",
        10763 to "News",
        10764 to "Reality",
        10765 to "Sci-Fi & Fantasy",
        10766 to "Soap",
        10767 to "Talk",
        10768 to "War & Politics",
        37 to "Western"
    )
}
