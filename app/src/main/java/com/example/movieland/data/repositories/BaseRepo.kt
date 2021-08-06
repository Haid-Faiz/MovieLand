package com.example.movieland.data.repositories

import okhttp3.ResponseBody
import org.json.JSONObject

abstract class BaseRepo {




    fun parseJsonError(errorBody: ResponseBody) {
        val jsonError = JSONObject(errorBody.toString())
        jsonError.getString("")
        jsonError.getString("")
    }
}