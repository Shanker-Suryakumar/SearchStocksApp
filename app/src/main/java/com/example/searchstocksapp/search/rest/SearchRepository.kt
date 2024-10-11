package com.example.searchstocksapp.search.rest

class SearchRepository(private val searchApi: SearchApi) {
    suspend fun getStockList() = searchApi.getStockList()
}