package com.example.searchstocksapp.search.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.searchstocksapp.main.events.BaseUiEvent
import com.example.searchstocksapp.main.events.UiEventHandler
import com.example.searchstocksapp.main.events.UiEventHandlerImpl
import com.example.searchstocksapp.search.rest.SearchRepository
import com.example.searchstocksapp.search.rest.dto.SearchResptDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

data class SearchVMUiState(
    val isLoading: Boolean = true,
    val stockListResp: List<SearchResptDto> = listOf(),
    val stockListUIResp: Flow<List<SearchResptDto>> = flowOf(listOf())
)

sealed class SearchVMUiEvent : BaseUiEvent() {
    class ShowError(val exception: Throwable? = null, val errorMessage: Int? = null) :
        SearchVMUiEvent()

    class ShowMessage(val message: String) : SearchVMUiEvent()
}

class SearchViewModel(private val searchRepo: SearchRepository) : ViewModel(),
    UiEventHandler<SearchVMUiEvent> by UiEventHandlerImpl() {
    private val _searchVMUiState: MutableStateFlow<SearchVMUiState> = MutableStateFlow(
        SearchVMUiState(isLoading = false)
    )
    val searchVMUiState: StateFlow<SearchVMUiState> = _searchVMUiState

    var searchText by mutableStateOf("")
        private set

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    init {
        _searchVMUiState.update {
            it.copy(
                stockListResp = getStockList(),
                stockListUIResp = flowOf(_searchVMUiState.value.stockListResp)
            )
        }
    }

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<SearchResptDto>> =
        snapshotFlow { searchText }
            .debounce(500L)
            .onEach { _isSearching.update { true } }
            .combine(_searchVMUiState.value.stockListUIResp) { searchText, stocks ->
                when {
                    searchText.isNotEmpty() -> stocks.filter { stocksItem ->
                        stocksItem.name?.contains(searchText, ignoreCase = true) == true
                    }

                    else -> stocks
                }
            }
            .onEach { _isSearching.update { false } }
            .stateIn(
                scope = viewModelScope,
                initialValue = emptyList(),
                started = SharingStarted.WhileSubscribed(5000)
            )

    private fun getStockList(): List<SearchResptDto> {
        viewModelScope.launch {
            _searchVMUiState.update {
                it.copy(isLoading = true)
            }

            runBlocking {
                withContext(Dispatchers.IO) {
                    kotlin.runCatching {
                        val stockListRequest = searchRepo.getStockList()

                        if (stockListRequest.isSuccessful) {
                            println("=======================> Stock List Resp: ${stockListRequest.body()}")
                            stockListRequest.body()?.let { stockListResponse ->
                                _searchVMUiState.update {
                                    it.copy(
                                        isLoading = false,
                                        stockListResp = stockListResponse
                                    )
                                }
                            }

                            return@runCatching _searchVMUiState.value.stockListResp
                        } else {
                            sendEvent(event = SearchVMUiEvent.ShowMessage(message = "Something went wrong!!"))
                        }
                    }.recover {
                        sendEvent(event = SearchVMUiEvent.ShowError(exception = it))
                    }
                }
            }

            _searchVMUiState.update {
                it.copy(isLoading = false)
            }
        }
        return _searchVMUiState.value.stockListResp
    }

    fun onSearchTextChange(text: String) {
        searchText = text
    }

    fun onBackClick(activity: Activity) {
        activity.finishAffinity()
    }
}