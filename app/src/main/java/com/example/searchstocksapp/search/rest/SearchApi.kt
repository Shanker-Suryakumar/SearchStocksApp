package com.example.searchstocksapp.search.rest

import com.example.searchstocksapp.main.Configurations
import com.example.searchstocksapp.search.rest.dto.SearchResptDto
import retrofit2.Response
import retrofit2.http.GET

interface SearchApi {
    @GET(Configurations.SEARCH_LIST_PATH)
    suspend fun getStockList(): Response<List<SearchResptDto>>
}