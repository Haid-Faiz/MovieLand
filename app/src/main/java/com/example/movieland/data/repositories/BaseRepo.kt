package com.example.movieland.data.repositories

import com.example.movieland.utils.Resource
import com.example.movieland.utils.SessionPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

abstract class BaseRepo constructor(
    private val sessionPrefs: SessionPrefs
) {

    suspend fun <T> safeApiCall(api: suspend () -> Response<T>): Resource<T> {

        return withContext(Dispatchers.IO) {
            try {
                val response: Response<T> = api()
                if (response.isSuccessful) Resource.Success(data = response.body()!!)
                else Resource.Error(message = response.message())

            } catch (e: HttpException) {
                Resource.Error(e.message ?: "Something went wrong")
            } catch (e: IOException) {
                Resource.Error(e.message ?: "Please check your network")
            } catch (e: Exception) {
                Resource.Error(message = e.message ?: "Something went wrong")
            }
        }
    }


    fun parseJsonError(errorBody: ResponseBody) {
        val jsonError = JSONObject(errorBody.toString())
        jsonError.getString("")
        jsonError.getString("")
    }

    suspend fun saveSessionToken(sessionToken: String) {
        sessionPrefs.saveSessionToken(sessionToken)
    }

    fun getSessionToken(): Flow<String?> = sessionPrefs.getSessionToken()
}