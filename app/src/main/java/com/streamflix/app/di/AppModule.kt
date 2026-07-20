package com.streamflix.app.di

import android.content.Context
import androidx.room.Room
import com.streamflix.app.data.local.dao.FavoriteDao
import com.streamflix.app.data.local.dao.LastClickedSearchDao
import com.streamflix.app.data.local.dao.SearchHistoryDao
import com.streamflix.app.data.local.dao.WatchHistoryDao
import com.streamflix.app.data.local.database.StreamFlixDatabase
import com.streamflix.app.data.remote.api.BeinApi
import com.streamflix.app.data.remote.api.CinemanaApi
import com.streamflix.app.data.remote.api.OpenSubtitlesApi
import com.streamflix.app.data.remote.api.SportsApi
import com.streamflix.app.data.remote.api.TmdbApi
import com.streamflix.app.data.remote.api.TraktApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StreamFlixDatabase =
        Room.databaseBuilder(context, StreamFlixDatabase::class.java, StreamFlixDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideFavoriteDao(db: StreamFlixDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideWatchHistoryDao(db: StreamFlixDatabase): WatchHistoryDao = db.watchHistoryDao()
    @Provides fun provideSearchHistoryDao(db: StreamFlixDatabase): SearchHistoryDao = db.searchHistoryDao()
    @Provides fun provideLastClickedSearchDao(db: StreamFlixDatabase): LastClickedSearchDao = db.lastClickedSearchDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        isLenient = true
    }

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .retryOnConnectionFailure(true)
        .build()

    @Provides @Singleton @Named("sports")
    fun provideSportsRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(SportsApi.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideSportsApi(@Named("sports") retrofit: Retrofit): SportsApi = retrofit.create(SportsApi::class.java)

    @Provides @Singleton @Named("bein")
    fun provideBeinRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BeinApi.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideBeinApi(@Named("bein") retrofit: Retrofit): BeinApi = retrofit.create(BeinApi::class.java)

    @Provides @Singleton @Named("tmdb")
    fun provideTmdbRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(TmdbApi.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideTmdbApi(@Named("tmdb") retrofit: Retrofit): TmdbApi = retrofit.create(TmdbApi::class.java)

    @Provides @Singleton
    fun provideCinemanaApi(): CinemanaApi = CinemanaApi.create()

    @Provides @Singleton @Named("trakt")
    fun provideTraktRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(TraktApi.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideTraktApi(@Named("trakt") retrofit: Retrofit): TraktApi = retrofit.create(TraktApi::class.java)

    @Provides @Singleton @Named("opensubs")
    fun provideOpenSubsRetrofit(json: Json): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", OpenSubtitlesApi.USER_AGENT)
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(req)
            }
            .build()
        return Retrofit.Builder()
            .baseUrl(OpenSubtitlesApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides @Singleton
    fun provideOpenSubtitlesApi(@Named("opensubs") retrofit: Retrofit): OpenSubtitlesApi =
        retrofit.create(OpenSubtitlesApi::class.java)
}
