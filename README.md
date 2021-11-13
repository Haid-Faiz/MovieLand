# MovieLand 
[![Build](https://github.com/Haid-Faiz/MovieLand/actions/workflows/build_ci.yml/badge.svg)](https://github.com/Haid-Faiz/MovieLand/actions/workflows/build_ci.yml)
[![Unit Tests](https://github.com/Haid-Faiz/MovieLand/actions/workflows/unit_tests_ci.yml/badge.svg)](https://github.com/Haid-Faiz/MovieLand/actions/workflows/unit_tests_ci.yml)
[![Kolint Lint](https://github.com/Haid-Faiz/MovieLand/actions/workflows/kotlin_lint_ci.yml/badge.svg)](https://github.com/Haid-Faiz/MovieLand/actions/workflows/kotlin_lint_ci.yml)
[![Platform](https://img.shields.io/badge/platform-android-blue.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-23%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=23)


**MovieLand** is a full fledge Android application that provides information about millions of Movies and TvShows. You can watch Trailers & more, get ratings, cast & etc. You can Rate Movies/Shows and even create your own WatchList, Favourite List.  
*It's UI is inspired from **Netflix** & **IMDB**.*

***You can Install the latest MovieLand app from below 👇***

[![MovieLand App](https://img.shields.io/badge/MovieLand-APK-FF9800?style=for-the-badge&logo=android)](https://github.com/PatilShreyas/Foodium/releases/latest/download/app.apk)


## App Look
<table>
   <ul>
      <li>
         <h4>App Intro & Search feature<h4>
      </li>
   </ul>
   <tr>
<td><img src = "https://user-images.githubusercontent.com/56159740/141646430-811625ea-10b5-49a9-a115-a37a03a5e8e2.gif" height = "380" width="200"></td>
<td><img src = "https://user-images.githubusercontent.com/56159740/141646430-811625ea-10b5-49a9-a115-a37a03a5e8e2.gif" height = "380" width="200"></td>
<td><img src = "https://user-images.githubusercontent.com/56159740/141646430-811625ea-10b5-49a9-a115-a37a03a5e8e2.gif" height = "380" width="200"></td>
  </tr>
</table>

<table>
      <ul>
      <li>
         <h4>Screen orientation changes & Pagination, etc<h4>
          </li>
   </ul>
  <tr>
<td><img src = "https://user-images.githubusercontent.com/56159740/141646430-811625ea-10b5-49a9-a115-a37a03a5e8e2.gif" height = "380" width="200"></td>
<td><img src = "https://user-images.githubusercontent.com/56159740/141646430-811625ea-10b5-49a9-a115-a37a03a5e8e2.gif" height = "380" width="200"></td>
  </tr>
</table>


## About

- This app uses [The Movie Database - TMDB Api](https://www.themoviedb.org/documentation/api) to fetch the data.
- Clean and Simple Material UI. (UI Design is inspired from Netflix & IMDB).
- Uses **OAuth2 flow** for Authentication
- *Low APK Size (only 3.2 MB).*

- **This project follows the MVVM structure with Modular Architecture.**

  There are two _modules_ in the project

  `app` - The UI of the app. The main project that forms the APK.
  
  `datastore` - The REST API consumption android library

### Developed with
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Data objects that notify views when the underlying database changes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes.
  - [ViewBinding](https://developer.android.com/topic/libraries/view-binding) - Generates a binding class for each XML layout file present in that module and allows you to more easily write code that interacts with views.
- [Dagger-Hilt](https://dagger.dev/hilt/) - Standard library to incorporate Dagger dependency injection into an Android application.
- [Retrofit](https://square.github.io/retrofit/) - A type-safe HTTP client for Android and Java.
- [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - For asynchronous and more..
- [Jetpack Navigation](https://developer.android.com/guide/navigation) - Android Jetpack's Navigation component helps you implement navigation, from simple button clicks to more complex patterns.
- [Jetpack Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) - The Paging library helps you load and display pages of data from a larger dataset from local storage or over network.
- [Moshi](https://github.com/square/moshi) - A modern JSON library for Kotlin and Java.
- [Moshi Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/moshi) - A Converter which uses Moshi for serialization to and from JSON.
- [Coil-kt](https://coil-kt.github.io/coil/) - An image loading library for Android backed by Kotlin Coroutines.
- [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/) - A cold asynchronous data stream that sequentially emits values and completes normally or with an exception.
- [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Jetpack DataStore is a data storage solution that allows you to
 store key-value pairs. Basically it's a replacement for SharedPreferences.

