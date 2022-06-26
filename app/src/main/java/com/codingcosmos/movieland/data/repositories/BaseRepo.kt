package com.codingcosmos.movieland.data.repositories

import com.codingcosmos.datasource.remote.models.responses.TmdbErrorResponse
import com.codingcosmos.movieland.utils.ErrorType
import com.codingcosmos.movieland.utils.Resource
import com.codingcosmos.movieland.utils.SessionPrefs
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
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
                if (response.isSuccessful) {
                    Resource.Success(data = response.body()!!)
                } else {
                    // val code = e.code() HTTP Exception code
                    val errorResponse: TmdbErrorResponse? = convertErrorBody(response.errorBody())
                    Resource.Error(
                        errorMessage = errorResponse?.statusMessage ?: "Something went wrong",
                        errorType = ErrorType.HTTP
                    )
                }
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> Resource.Error(
                        "Please check your network connection",
                        errorType = ErrorType.NETWORK
                    )

                    is HttpException ->
                        // val code = e.code() HTTP Exception code
                        Resource.Error(
                            errorMessage = throwable.message ?: "Something went wrong",
                            errorType = ErrorType.HTTP
                        )

                    else -> Resource.Error(
                        errorMessage = "Something went wrong",
                        errorType = ErrorType.UNKNOWN
                    )
                }
            }
        }
    }

    private fun convertErrorBody(errorBody: ResponseBody?): TmdbErrorResponse? {
        return try {
            errorBody?.source()?.let {
                val moshiAdapter = Moshi.Builder().build().adapter(TmdbErrorResponse::class.java)
                moshiAdapter.fromJson(it)
            }
        } catch (exception: Exception) {
            null
        }
    }

//    private fun parseJsonError(errorBody: ResponseBody) {
//        val jsonError = JSONObject(errorBody.string())
//        jsonError.getString("")
//        jsonError.getString("")
//    }

    suspend fun clearSessionPrefs() {
        sessionPrefs.clearSessionPrefs()
    }

    suspend fun saveSessionId(sessionId: String) {
        sessionPrefs.saveSessionId(sessionID = sessionId)
    }

    fun getSessionId(): Flow<String?> = sessionPrefs.getSessionId()

    suspend fun saveAccessToken(accessToken: String) {
        sessionPrefs.saveAccessToken(accessToken)
    }

    fun getAccessToken(): Flow<String?> = sessionPrefs.getAccessToken()

    suspend fun saveAccountId(accountId: Int) {
        sessionPrefs.saveAccountId(accountId)
    }

    fun getAccountId(): Flow<Int?> = sessionPrefs.getAccountId()

    suspend fun saveUserName(username: String) {
        sessionPrefs.saveUserName(username)
    }

    fun getUserName(): Flow<String?> = sessionPrefs.getUserName()
}
