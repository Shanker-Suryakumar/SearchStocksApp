package com.example.searchstocksapp.main.events

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

abstract class BaseUiEvent(val id: String = UUID.randomUUID().toString())

interface UiEventHandler<UiEvent : BaseUiEvent> {
    val uiEvent: StateFlow<List<UiEvent>>

    fun sendEvent(event: UiEvent)
    fun onEventConsumed(eventId: String)
}

class UiEventHandlerImpl<UiEvent : BaseUiEvent> : UiEventHandler<UiEvent> {
    private val _uiEvent: MutableStateFlow<List<UiEvent>> = MutableStateFlow(emptyList())
    override val uiEvent: StateFlow<List<UiEvent>> = _uiEvent

    override fun sendEvent(event: UiEvent) {
        val newEvent = _uiEvent.value + event
        _uiEvent.update { newEvent }
    }

    override fun onEventConsumed(eventId: String) {
        val newEvent = _uiEvent.value.filter { it.id != eventId }
    }
}