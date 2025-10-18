# Looksy - Project Overview

## Description

Looksy is an Android wardrobe management application that helps users catalog, organize, and plan outfits with their clothing items. Users can take photos of their clothes, categorize them, and create outfit combinations.

## Project Type

- **Platform**: Android Native
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)

## Key Features

1. **Camera Integration**: Take photos of clothing items
2. **Wardrobe Management**: Store and organize clothing by categories
3. **Outfit Planning**: Create and view outfit combinations
4. **Category Browsing**: Browse clothes by type (Tops, Pants, Dress, etc.)
5. **Detailed Item View**: View and edit individual clothing item details
6. **Local Storage**: Room database for offline-first storage

## Target SDK & Configuration

- **Namespace**: `com.example.looksy`
- **Application ID**: `com.example.looksy`
- **Min SDK**: 28 (Android 9.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Java Version**: 11

## Project Structure

```
com.example.looksy/
├── Application/          # Application class for dependency injection
├── ViewModels/          # ViewModels for state management
├── Repository/          # Data layer abstraction
├── dao/                 # Room DAO interfaces
├── dataClassClones/     # Data models and enums
├── Factory/             # ViewModel factories
├── screens/             # Composable screens
├── ui/theme/            # Material3 theming
├── MainActivity.kt      # Entry point
├── Routes.kt            # Navigation configuration
└── ClothesDatabase.kt   # Room database setup
```

## Navigation Flow

```
Home (FullOutfitScreen) ←→ Bottom Navigation
  ↑                              ↓
  └──────────────────→ ChoseClothes (CategoriesScreen)
                              ↓
                      SpecificCategoryScreen
                              ↓
                      ClothInformationScreen (Details)
                              ↑
Camera (Scan) ───→ AddNewClothesScreen
```

## Development Environment

- **Build System**: Gradle with Kotlin DSL
- **IDE**: Android Studio (recommended)
- **Version Catalog**: Uses `libs.versions.toml` for dependency management
