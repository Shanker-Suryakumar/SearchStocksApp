package com.example.searchstocksapp.main.koin

import com.example.searchstocksapp.main.Configurations
import com.example.searchstocksapp.search.rest.SearchApi
import com.example.searchstocksapp.search.rest.SearchRepository
import com.example.searchstocksapp.search.viewmodel.SearchViewModel
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.BuildConfig
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule: Module = module {
    factory {
        ObjectMapper()
            .registerModule(
                KotlinModule
                    .Builder()
                    .build()
            )
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    factory {
        OkHttpClient.Builder().addInterceptor { chain ->
            val builder = chain.request().newBuilder()
            chain.proceed(builder.build())
        }.addInterceptor(HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            } else {
                setLevel(HttpLoggingInterceptor.Level.NONE)
            }
        })
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    factory {
        Retrofit.Builder()
            .baseUrl(Configurations.BASE_URL)
            .client(get())
            .addConverterFactory(JacksonConverterFactory.create(get()))
            .build()
    }

    single { get<Retrofit>().create(SearchApi::class.java) }

    single { SearchRepository(get()) }
}

val mainModule: Module = module {
    viewModel {
        SearchViewModel(get())
    }
}

val koinModules = listOf(
    networkModule,
    mainModule
)