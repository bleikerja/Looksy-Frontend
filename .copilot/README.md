# Looksy Frontend - Copilot Documentation

This `.copilot` folder contains comprehensive documentation to help GitHub Copilot provide better assistance for the Looksy Android app project.

## 📚 Documentation Index

### Core Documentation

1. **[project-overview.md](project-overview.md)**

   - High-level project description
   - Technology stack
   - Project structure
   - Navigation flow
   - Development environment setup

2. **[architecture.md](architecture.md)**

   - MVVM architecture pattern
   - Layer breakdown (View, ViewModel, Repository, DAO, Model)
   - Data flow patterns
   - Dependency injection approach
   - State management strategies
   - Database design

3. **[conventions.md](conventions.md)**

   - Kotlin coding style
   - Naming conventions
   - Compose best practices
   - ViewModel patterns
   - Room database patterns
   - Navigation patterns
   - Performance considerations

4. **[dependencies.md](dependencies.md)**

   - Complete dependency list with versions
   - Library purposes and key APIs
   - Usage patterns for major libraries
   - Build tool configuration
   - Testing dependencies

5. **[data-models.md](data-models.md)**

   - Clothes entity schema
   - All enum types (Size, Season, Type, Material, WashingNotes)
   - Type conversions
   - Data validation patterns
   - Sample data examples

6. **[troubleshooting.md](troubleshooting.md)**
   - Common build issues
   - Runtime problems and solutions
   - Navigation debugging
   - UI issues
   - Performance tips
   - Debugging techniques

### Feature Guides

Located in `feature-guides/` folder:

1. **[camera-image-handling.md](feature-guides/camera-image-handling.md)**

   - CameraX implementation
   - Permission handling
   - Image capture and storage
   - Image display with Coil
   - Navigation flow for photos

2. **[navigation-routing.md](feature-guides/navigation-routing.md)**
   - Navigation Compose setup
   - Route definitions
   - Bottom navigation implementation
   - All screen destinations
   - Argument passing patterns
   - Back stack management

## 🎯 How to Use This Documentation

### For GitHub Copilot

When asking Copilot for help:

1. **Be specific about the file/feature** you're working on
2. **Mention architectural layers** (e.g., "in the ViewModel", "in the DAO")
3. **Reference existing patterns** (e.g., "following the pattern in ClothesViewModel")
4. **Specify conventions** (e.g., "using the project's enum naming convention")

### Example Prompts

**Good prompts:**

- "Add a new field 'color' to the Clothes entity following the project's Room pattern"
- "Create a new screen for editing clothes following the navigation pattern"
- "Add a filter by season in ClothesDao using Flow"

**Less effective:**

- "Add color field" (too vague)
- "Make a screen" (no context)

### For Developers

1. **Start with `project-overview.md`** to understand the big picture
2. **Read `architecture.md`** to understand the codebase structure
3. **Consult `conventions.md`** when writing new code
4. **Check `troubleshooting.md`** when encountering issues
5. **Refer to feature guides** for specific implementations

## 📝 Updating This Documentation

### When to Update

Update these files when:

- Adding new dependencies
- Changing architecture patterns
- Adding new features
- Discovering common issues
- Updating coding conventions

### How to Update

1. Keep files **concise** and **accurate**
2. Use **code examples** to illustrate patterns
3. Update the **date** at the bottom of changed files
4. Cross-reference related documentation
5. Remove outdated information

## 🔍 Quick Reference

### Project Structure

```
com.example.looksy/
├── Application/          # App class & DI
├── ViewModels/          # ClothesViewModel
├── Repository/          # ClothesRepository
├── dao/                 # ClothesDao (Room)
├── dataClassClones/     # Data models & enums
├── Factory/             # ViewModelFactory
├── screens/             # Screen composables
├── ui/theme/            # Material3 theme
└── *.kt files           # Main activity, routes, etc.
```

### Key Technologies

- **Kotlin** 2.2.20
- **Jetpack Compose** with Material3
- **Room Database** 2.8.1 (with KSP)
- **Navigation Compose** 2.9.5
- **CameraX** 1.5.0
- **Coil** 2.7.0 for images

### Architecture Layers

```
Composable → ViewModel → Repository → DAO → Room DB
```

### Common File Patterns

- Screens: `*Screen.kt` (PascalCase)
- ViewModels: `*ViewModel.kt`
- Repositories: `*Repository.kt`
- DAOs: `*Dao.kt` (interface)
- Enums: PascalCase or leading underscore for numbers

## 🚀 Getting Started (New Developer)

1. Read `project-overview.md`
2. Study `architecture.md`
3. Skim `conventions.md`
4. Build and run the app
5. Explore `Routes.kt` to understand navigation
6. Look at `ClothesViewModel.kt` for state management example
7. Check `ClothesDao.kt` for database queries

## 📦 Current Version

- **Project**: Looksy Frontend
- **Version**: 1.0
- **Last Updated**: 2025-10-17
- **Min SDK**: 28 (Android 9.0)
- **Target SDK**: 36

## 🤝 Contributing Patterns

When adding new features:

1. **Follow existing patterns** (see conventions.md)
2. **Use ViewModel for state** (see architecture.md)
3. **Query via DAO with Flow** (see data-models.md)
4. **Navigate with type-safe routes** (see navigation-routing.md)
5. **Handle permissions properly** (see camera-image-handling.md)

## 📞 Need Help?

1. Check `troubleshooting.md` for common issues
2. Search for similar patterns in existing code
3. Consult the relevant feature guide
4. Ask Copilot with specific context

---

**Note**: This documentation is designed to help both AI assistants and human developers understand the Looksy codebase quickly and accurately.
