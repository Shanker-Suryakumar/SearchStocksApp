package com.example.searchstocksapp.search.rest.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchResptDto(
    @JsonProperty("ticker")
    val ticker: String? = null,
    @JsonProperty("name")
    val name: String? = null,
    @JsonProperty("currentPrice")
    val currentPrice: Double? = null
)