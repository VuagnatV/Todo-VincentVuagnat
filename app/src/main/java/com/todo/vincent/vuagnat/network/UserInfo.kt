package com.todo.vincent.vuagnat.network
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class UserInfo(
    @SerialName("email")
    val email: String,
    @SerialName("firstname")
    val firstName: String,
    @SerialName("lastname")
    val lastName: String,
    val avatar: String?
)