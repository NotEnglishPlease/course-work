package com.example.helloworldmessenger.models

import com.google.firebase.Timestamp

/**
 * Message
 * Этот класс используется работы с сообщениями
 * @param id - идентификатор сообщения в firebase
 * @param conversation_id - идентификатор соответсвующего диалога (чата) в firebase
 * @param sender_id - идентификатор отправителя в firebase
 * @param text - текст сообщения
 * @param timestamp - время отправки сообщения
 * @param is_read - статус сообщения (прочитано или нет)
 */
data class Message(
    val id: String = "",
    val conversation_id: String = "",
    val sender_id: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    var is_read: Boolean = false
)
