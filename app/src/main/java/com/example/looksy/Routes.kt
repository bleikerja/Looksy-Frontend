package com.example.looksy

interface NavigationDestination{
    val route:String
}

sealed class Routes(override val route: String): NavigationDestination{
    data object Home : Routes("Home")
}