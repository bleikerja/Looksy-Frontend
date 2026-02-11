package com.example.looksy.ui.navigation

import android.net.Uri

interface NavigationDestination {
    val route: String
}

object RouteArgs {
    const val TYPE = "imageType"
    const val IMAGE_URI = "imageUri"
    const val ID = "id"
}

sealed class Routes(override val route: String) : NavigationDestination {
    data object Home : Routes("home")
    data object ChoseClothes : Routes("chose clothes")
    data object SavedOutfits : Routes("saved_outfits")
    data object ExistingClothes : Routes("existing clothes")

    data object WashingMachine : Routes("washing_machine")
    data object Discard : Routes("discard")
    
    data object OutfitDetails : Routes("outfit_details/{${RouteArgs.ID}}") {
        fun createRoute(id: Int): String {
            return "outfit_details/$id"
        }
    }
    
    data object Details : Routes("details/{${RouteArgs.ID}}") {
        fun createRoute(id: Int): String {
            return "details/$id"
        }
    }

    data object SpecificCategory : Routes("specific_category/{${RouteArgs.TYPE}}") {
        fun createRoute(type: String): String {
            val encodedPath = Uri.encode(type)
            return "specific_category/$encodedPath"
        }
    }

    data object AddNewClothes : Routes("add_new_clothes/{${RouteArgs.IMAGE_URI}}") {
        fun createRoute(imageUri: String): String {
            return "add_new_clothes/$imageUri"
        }
    }

    data object EditClothes : Routes("edit_clothes/{${RouteArgs.ID}}/{${RouteArgs.IMAGE_URI}}") {
        fun createRoute(id: Int, imageUri: String? = null) = "edit_clothes/$id/$imageUri"
    }

    data object Scan : Routes("scan/{${RouteArgs.ID}}") {
        fun createRoute(id: Int?) = "scan/$id"
    }
}
