package com.example.movieland.di

import android.content.Context
import com.example.datasource.remote.ApiClient
import com.example.datasource.remote.api.TMDBApiServiceV3
import com.example.datasource.remote.api.TMDBApiServiceV4
import com.example.movieland.utils.SessionPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesApiClient(): ApiClient = ApiClient()

    @Provides
    @Singleton
    fun providesTmdbV3Service(apiClient: ApiClient): TMDBApiServiceV3 =
        apiClient.retrofit.create(TMDBApiServiceV3::class.java)

    @Provides
    @Singleton
    fun providesTmdbV4Service(apiClient: ApiClient): TMDBApiServiceV4 =
        apiClient.retrofit.create(TMDBApiServiceV4::class.java)

    @Provides
    @Singleton
    fun providesSessionPrefs(@ApplicationContext context: Context): SessionPrefs =
        SessionPrefs(context)
}