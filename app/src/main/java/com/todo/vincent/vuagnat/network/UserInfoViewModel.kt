package com.todo.vincent.vuagnat.network

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todo.vincent.vuagnat.tasklist.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class UserInfoViewModel: ViewModel() {

    private val webService = Api.userWebService

    private val _userStateFlow = MutableStateFlow<UserInfo>(UserInfo("emil", "firstName", "lastName", ""))
    public val userStateFlow: StateFlow<UserInfo> = _userStateFlow.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val response = Api.userWebService.getInfo() // Call HTTP (opération longue)
            if (response.isSuccessful) { // à cette ligne, on a reçu la réponse de l'API
                Log.e("Network", "Error: ${response.message()}")
            }
            val fetchedUser = response.body()!!
            _userStateFlow.value = fetchedUser // on modifie le flow, ce qui déclenche ses observers
        }
    }

    fun update(user: UserInfo) {
        viewModelScope.launch {
            val response = webService.update(user) // TODO: appel réseau
            if (!response.isSuccessful) {
                Log.e("Network", "Error: ${response.raw()}")
                return@launch
            }

            val updatedUser = response.body()!!
            _userStateFlow.value = updatedUser
        }
    }

}