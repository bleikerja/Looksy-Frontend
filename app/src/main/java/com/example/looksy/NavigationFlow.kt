package com.example.looksy

import kotlinx.coroutines.flow.MutableStateFlow

class NavigationFlow {
    val destination: MutableStateFlow<NavigationDestination> = MutableStateFlow(Routes.Home)
    val lastDestination: MutableStateFlow<NavigationDestination> = MutableStateFlow(Routes.Home)

    fun navigate(destination: NavigationDestination){
        if (destination == this.destination.value)return
        lastDestination.value = this.destination.value
        this.destination.value = destination
    }
}