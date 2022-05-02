package com.todo.vincent.vuagnat.tasklist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task(@SerialName("id") val id: String, @SerialName("title") val title: String, @SerialName("description") val description: String = "abc") : java.io.Serializable