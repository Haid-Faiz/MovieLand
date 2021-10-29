package com.example.movieland.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.movieland.utils.Constants.SESSION_DATASTORE
import com.example.movieland.utils.Constants.SESSION_ID_KEY
import com.example.movieland.utils.Constants.SIGN_IN_SESSION_KEY
import com.example.movieland.utils.Constants.USER_ACCOUNT_ID_KEY
import com.example.movieland.utils.Constants.USER_NAME_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.datastore by preferencesDataStore(SESSION_DATASTORE)

class SessionPrefs(private val context: Context) {

    private val _datastore = context.datastore

    private val accessTokenKey = stringPreferencesKey(SIGN_IN_SESSION_KEY)
    private val userNameKey = stringPreferencesKey(USER_NAME_KEY)
    private val accountIdKey = intPreferencesKey(USER_ACCOUNT_ID_KEY)
    private val sessionIdKey = stringPreferencesKey(SESSION_ID_KEY)

    suspend fun clearSessionPrefs() {
        _datastore.edit {
            it.clear()
        }
    }

    suspend fun saveSessionId(sessionID: String) = _datastore.edit {
        it[sessionIdKey] = sessionID
    }

    fun getSessionId(): Flow<String?> = _datastore.data.map {
        it[sessionIdKey]
    }  // again we are returning flow of string

    suspend fun saveAccessToken(accessToken: String) = _datastore.edit {
        it[accessTokenKey] = accessToken
    }

    fun getAccessToken(): Flow<String?> = _datastore.data.map {
        it[accessTokenKey]
    }  // again we are returning flow of string

    suspend fun saveAccountId(accountId: Int) = _datastore.edit {
        it[accountIdKey] = accountId
    }

    fun getAccountId(): Flow<Int?> = _datastore.data.map {
        it[accountIdKey]
    }  // again we are returning flow of string

    suspend fun saveUserName(username: String) = _datastore.edit {
        it[userNameKey] = username
    }

    fun getUserName(): Flow<String?> = _datastore.data.map {
        it[userNameKey]
    }  // again we are returning flow of string
}