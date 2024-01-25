package com.example.helloworldmessenger.models

/**
 * User
 * Этот класс используется работы с пользователями
 * @param id - идентификатор пользователя в firebase
 * @param name - имя пользователя
 * @param email - почта пользователя
 * @param is_online - статус пользователя
 * @param profile_picture - фотография профиля пользователя
 * @param conversations - список чатов пользователя
 * @param friends - список друзей пользователя
 * @param incoming_requests - список входящих заявок пользователя
 * @param outgoing_requests - список исходящих заявок пользователя
 */
data class User(
    val id: String = "",
    var name: String = "",
    val email: String = "",
    var is_online: Boolean = false,
    var profile_picture: String = "",
    val conversations: List<String> = emptyList(),
    val friends: List<String> = emptyList(),
    val incoming_requests: List<String> = emptyList(),
    val outgoing_requests: List<String> = emptyList(),
    var is_admin: Boolean = false
)