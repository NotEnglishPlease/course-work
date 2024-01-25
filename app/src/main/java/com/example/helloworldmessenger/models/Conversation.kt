package com.example.helloworldmessenger.models

/**
 * Conversation
 * Этот класс используется работы с диалогами (чатами)
 * @param id - идентификатор диалога в firebase
 * @param participants - участники диалога (чата)
 */
data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList()
)
