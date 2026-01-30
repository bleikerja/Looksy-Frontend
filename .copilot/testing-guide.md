# Testing Guide - Looksy Android App

## Overview

This guide documents the testing conventions and patterns used in the Looksy project. The project uses a combination of **unit tests** (`test/`) and **instrumented tests** (`androidTest/`) to ensure code quality and reliability.

## Test Structure

```
app/src/
├── test/java/com/example/looksy/           # Unit tests (JVM)
│   ├── ClothesViewModelTest.kt             # ViewModel logic tests
│   └── OutfitRepositoryTest.kt             # Repository layer tests
└── androidTest/java/com/example/looksy/    # Instrumented tests (Android device/emulator)
    ├── ConfirmationDialogTest.kt           # UI component tests
    └── headerTest.kt                       # UI component tests
```

## Test Categories

### 1. Unit Tests (`test/`)

**Purpose**: Test business logic, ViewModels, and Repositories without Android framework dependencies.

**Runs on**: JVM (fast execution, no device needed)

**Key Libraries**:

- **JUnit 4**: Test framework (`org.junit.Test`)
- **MockK**: Mocking library (`io.mockk:mockk:1.13.11`)
- **Coroutines Test**: Testing coroutines (`kotlinx-coroutines-test:1.8.0`)

### 2. Instrumented Tests (`androidTest/`)

**Purpose**: Test UI components, Compose screens, and Android framework interactions.

**Runs on**: Android device or emulator

**Key Libraries**:

- **JUnit 4**: Test framework
- **Compose Testing**: UI testing (`androidx.compose.ui:ui-test-junit4`)
- **Espresso**: Android UI testing (via `androidx.test.espresso:espresso-core`)

---

## Unit Testing Patterns

### ViewModel Testing (Example: ClothesViewModelTest)

**Pattern**: Mock repository, verify ViewModel delegates calls correctly.

```kotlin
@ExperimentalCoroutinesApi
class ClothesViewModelTest {

    private lateinit var viewModel: ClothesViewModel
    private lateinit var repository: ClothesRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher.scheduler)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = ClothesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insert() should call repository's insert`() = testScope.runTest {
        // Given
        val clothes = Clothes(
            id = 1,
            size = Size._M,
            seasonUsage = Season.Summer,
            type = Type.Tops,
            material = Material.Cotton,
            clean = true,
            washingNotes = WashingNotes.Temperature30,
            imagePath = "",
            isSynced = false
        )

        // When
        viewModel.insert(clothes)
        advanceUntilIdle()

        // Then
        coVerify { repository.insert(clothes) }
    }
}
```

**Key Points**:

1. **Dispatcher Setup**: Always set `Dispatchers.setMain(testDispatcher)` in `@Before` for coroutine tests
2. **TestScope**: Use `TestScope` with `StandardTestDispatcher` for controlled coroutine execution
3. **MockK relaxed**: Use `mockk(relaxed = true)` to auto-mock all methods
4. **advanceUntilIdle()**: Advances coroutines to completion before verification
5. **coVerify**: Use for verifying suspend function calls
6. **Given-When-Then**: Follow this structure for clarity

### Repository Testing (Example: OutfitRepositoryTest)

**Pattern**: Mock DAO, verify Repository delegates calls correctly and handles data flow.

```kotlin
class OutfitRepositoryTest {

    private lateinit var outfitDao: OutfitDao
    private lateinit var outfitRepository: OutfitRepository

    @Before
    fun setUp() {
        outfitDao = mockk(relaxed = true)
        outfitRepository = OutfitRepository(outfitDao)
    }

    @Test
    fun `insert() should call dao's insert`() = runTest {
        // Given
        val outfit = Outfit(
            id = 0,
            dressId = 1,
            topsId = 2,
            skirtId = null,
            pantsId = 3,
            jacketId = null
        )

        // When
        outfitRepository.insert(outfit)

        // Then
        coVerify { outfitDao.insert(outfit) }
    }

    @Test
    fun `getAllOutfits() should return flow from dao`() = runTest {
        // Given
        val outfits = listOf(
            Outfit(id = 1, dressId = 1, topsId = null, skirtId = null, pantsId = null, jacketId = null)
        )
        coEvery { outfitDao.getAllOutfits() } returns flowOf(outfits)

        // When
        val result = outfitRepository.allOutfits

        // Then
        assertNotNull(result)
        coVerify { outfitDao.getAllOutfits() }
    }
}
```

**Key Points**:

1. **No TestDispatcher needed**: `runTest` handles coroutine context automatically
2. **coEvery**: Use to stub suspend functions with return values
3. **flowOf()**: Create test Flows for mocking
4. **Test nullable fields**: Verify entities work with nullable fields (see `outfit can be stored with all IDs null`)

---

## Instrumented Testing Patterns

### Compose UI Testing (Example: ConfirmationDialogTest)

**Pattern**: Set content, find UI elements, perform actions, verify state changes.

```kotlin
class ConfirmationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun confirmationDialog_displaysContent_andHandlesClicks() {
        var confirmed = false
        var dismissed = false

        composeTestRule.setContent {
            ConfirmationDialog(
                title = "Löschen bestätigen",
                text = "Möchtest du dieses Kleidungsstück wirklich endgültig löschen?",
                confirmText = "Ja",
                dismissText = "Nein",
                onConfirm = { confirmed = true },
                onDismiss = { dismissed = true }
            )
        }

        // Verify elements are displayed
        composeTestRule
            .onNodeWithText("Löschen bestätigen")
            .assertIsDisplayed()

        // Perform click
        composeTestRule
            .onNodeWithText("Ja")
            .performClick()

        // Verify callback was called
        assertTrue(confirmed)
    }
}
```

**Key Points**:

1. **@get:Rule**: Use `createComposeRule()` for Compose UI tests
2. **setContent**: Set the composable to test
3. **onNodeWithText**: Find elements by text (most common)
4. **onNodeWithContentDescription**: Find elements by content description (icons, images)
5. **assertIsDisplayed()**: Verify visibility
6. **assertDoesNotExist()**: Verify absence
7. **performClick()**: Simulate user interaction
8. **Callback verification**: Use local variables to verify lambdas were called

### Testing with Data (Example: headerTest)

**Pattern**: Pass real data entities, verify UI reacts correctly.

```kotlin
val testClothes = Clothes(
    id = 42,
    type = Type.Tops,
    size = Size._38,
    seasonUsage = Season.inBetween,
    material = Material.Wool,
    clean = true,
    washingNotes = WashingNotes.None,
    imagePath = "",
    isSynced = false
)

class HeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun header_showsRightIcon_andCallsNavigationWithId() {
        var clickedId: Int? = -1

        composeTestRule.setContent {
            Header(
                onNavigateBack = {},
                onNavigateToRightIcon = { id -> clickedId = id },
                clothesData = testClothes,
                headerText = "Details",
                rightIconContentDescription = "Einstellungen",
                rightIcon = Icons.Default.Settings,
                isFirstHeader = false
            )
        }

        // Click right icon
        composeTestRule.onNodeWithContentDescription("Einstellungen").performClick()

        // Verify correct ID was passed
        assert(clickedId == 42)
    }
}
```

**Key Points**:

1. **Top-level test data**: Create reusable test entities outside classes
2. **Capture lambda params**: Use local variables to capture callback parameters
3. **Test null scenarios**: Verify UI handles null data correctly (see `header_showsRightIcon_whenClothesDataIsNull`)
4. **Test conditional rendering**: Verify elements show/hide based on flags (see `header_hidesBackButton_whenIsFirstHeaderIsTrue`)

---

## Testing Conventions

### 1. Test Naming

**Unit Tests**:

```kotlin
`methodName() should expectedBehavior`
`insert() should call repository's insert`
```

**UI Tests**:

```kotlin
componentName_action_expectedResult
confirmationDialog_displaysContent_andHandlesClicks
header_callsNavigateBack_whenClicked
```

### 2. Test Structure (Given-When-Then)

```kotlin
@Test
fun `test name describes behavior`() = runTest {
    // Given (Arrange) - Set up test data and mocks
    val testData = createTestData()
    coEvery { mock.method() } returns expected

    // When (Act) - Execute the code under test
    val result = systemUnderTest.doSomething(testData)

    // Then (Assert) - Verify the expected outcome
    assertEquals(expected, result)
    coVerify { mock.method() }
}
```

### 3. Required Annotations

**Unit Tests**:

- `@Before`: Setup mocks, initialize test objects
- `@After`: Clean up (e.g., `Dispatchers.resetMain()`)
- `@Test`: Mark test methods
- `@ExperimentalCoroutinesApi`: Required for coroutine testing utilities

**UI Tests**:

- `@get:Rule`: Apply test rules (e.g., `createComposeRule()`)
- `@Test`: Mark test methods

### 4. Test Data Creation

**Use existing enums and data classes**:

```kotlin
val testClothes = Clothes(
    id = 1,
    size = Size._M,
    seasonUsage = Season.Summer,
    type = Type.Tops,
    material = Material.Cotton,
    clean = true,
    washingNotes = WashingNotes.Temperature30,
    imagePath = "",
    isSynced = false
)
```

**Test edge cases**:

- Nullable fields (Outfit with all IDs null)
- Empty collections
- Invalid input handling

---

## Running Tests

### Command Line

```bash
# Run all unit tests
.\gradlew test

# Run all instrumented tests (requires device/emulator)
.\gradlew connectedAndroidTest

# Run specific test class
.\gradlew test --tests ClothesViewModelTest

# Run with coverage
.\gradlew testDebugUnitTest jacocoTestReport
```

### Android Studio

- **Unit Tests**: Right-click on test file → "Run 'TestName'"
- **Instrumented Tests**: Right-click on androidTest file → "Run 'TestName'" (device required)
- **All Tests**: Right-click on `test/` or `androidTest/` folder → "Run Tests"

---

## Common Testing Patterns

### 1. Testing Coroutines

```kotlin
@ExperimentalCoroutinesApi
class MyViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher.scheduler)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test suspend function`() = testScope.runTest {
        // Test code
        viewModel.suspendFunction()
        advanceUntilIdle() // Wait for all coroutines to complete
        // Verify
    }
}
```

### 2. Testing Flows

```kotlin
@Test
fun `test flow emission`() = runTest {
    // Given
    val expectedList = listOf(item1, item2)
    coEvery { dao.getFlow() } returns flowOf(expectedList)

    // When
    val flow = repository.getFlow()

    // Then
    flow.collect { result ->
        assertEquals(expectedList, result)
    }
}
```

### 3. Testing State Changes in ViewModels

```kotlin
@Test
fun `viewModel exposes stateFlow correctly`() = testScope.runTest {
    // Given
    val expectedClothes = listOf(testClothes)
    coEvery { repository.allClothes } returns flowOf(expectedClothes)

    // When
    val viewModel = ClothesViewModel(repository)
    advanceUntilIdle()

    // Then
    assertEquals(expectedClothes, viewModel.allClothes.value)
}
```

### 4. Testing Compose State

```kotlin
@Test
fun `test state-driven UI`() {
    var counter by mutableStateOf(0)

    composeTestRule.setContent {
        Text("Count: $counter")
    }

    // Initial state
    composeTestRule.onNodeWithText("Count: 0").assertIsDisplayed()

    // Update state
    counter = 5

    // Verify UI updated
    composeTestRule.onNodeWithText("Count: 5").assertIsDisplayed()
}
```

### 5. Testing Navigation Callbacks

```kotlin
@Test
fun `test navigation callback passes correct data`() {
    var receivedId: Int? = null

    composeTestRule.setContent {
        MyComponent(
            onNavigate = { id -> receivedId = id },
            itemId = 42
        )
    }

    composeTestRule.onNodeWithText("Navigate").performClick()

    assertEquals(42, receivedId)
}
```

---

## Best Practices

### ✅ Do

1. **Test one thing per test**: Each test should verify a single behavior
2. **Use descriptive names**: Test names should explain what's being tested
3. **Mock external dependencies**: Don't test Room, Retrofit, etc. - mock them
4. **Test edge cases**: Null values, empty lists, error states
5. **Use Given-When-Then**: Makes tests easier to understand
6. **Clean up after tests**: Use `@After` for cleanup (especially Dispatchers)
7. **Test public API only**: Don't test private methods directly
8. **Verify meaningful behavior**: Don't just verify method calls, verify outcomes

### ❌ Don't

1. **Don't use real database**: Mock DAOs in unit tests
2. **Don't test framework code**: Don't verify Room or Compose internals
3. **Don't write brittle tests**: Avoid hardcoding implementation details
4. **Don't ignore test failures**: Fix tests or update them when code changes
5. **Don't test everything**: Focus on business logic and critical paths
6. **Don't use GlobalScope**: Always use viewModelScope or testScope
7. **Don't forget advanceUntilIdle()**: Coroutines need explicit advancement in tests

---

## Troubleshooting

### "Unresolved reference: mockk"

**Solution**: Ensure test dependencies are in `app/build.gradle.kts`:

```kotlin
testImplementation("io.mockk:mockk:1.13.11")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
```

### "Module with the Main dispatcher is missing"

**Solution**: Set Main dispatcher in `@Before`:

```kotlin
@Before
fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
}

@After
fun tearDown() {
    Dispatchers.resetMain()
}
```

### Compose test can't find node

**Solution**:

1. Check exact text/content description
2. Use `printToLog(tag)` to see UI tree:

```kotlin
composeTestRule.onRoot().printToLog("MY_TAG")
```

3. Ensure `setContent` was called before finding nodes

### Test times out with coroutines

**Solution**: Use `advanceUntilIdle()` to progress coroutines:

```kotlin
viewModel.doSomething()
advanceUntilIdle() // Let all coroutines complete
coVerify { repository.method() }
```

---

## Adding New Tests

### For ViewModels:

1. Create test file in `test/java/com/example/looksy/`
2. Use pattern from `ClothesViewModelTest.kt`
3. Mock repository with `mockk(relaxed = true)`
4. Test each public method with Given-When-Then
5. Verify repository calls with `coVerify`

### For Repositories:

1. Create test file in `test/java/com/example/looksy/`
2. Use pattern from `OutfitRepositoryTest.kt`
3. Mock DAO with `mockk(relaxed = true)`
4. Test CRUD operations and flow handling
5. Test nullable field scenarios

### For Compose Components:

1. Create test file in `androidTest/java/com/example/looksy/`
2. Use pattern from `ConfirmationDialogTest.kt` or `headerTest.kt`
3. Add `@get:Rule val composeTestRule = createComposeRule()`
4. Use `setContent` to render component
5. Test with `onNodeWithText` / `onNodeWithContentDescription`
6. Verify with `assertIsDisplayed`, `assertDoesNotExist`, callback variables

---

## Test Coverage Goals

- **ViewModels**: 100% of public methods
- **Repositories**: 100% of CRUD operations
- **UI Components**: Critical user interactions and edge cases
- **DAOs**: Not tested directly (Room is tested by Google)
- **Screens**: Test navigation and data display (optional, can be complex)

---

## Quick Reference

### MockK Syntax

```kotlin
val mock = mockk<MyClass>()                    // Create mock
val relaxedMock = mockk<MyClass>(relaxed = true) // Auto-mock all methods
coEvery { mock.suspendMethod() } returns value  // Stub suspend function
every { mock.regularMethod() } returns value    // Stub regular function
coVerify { mock.suspendMethod() }               // Verify suspend call
verify { mock.regularMethod() }                 // Verify regular call
```

### Compose Test Syntax

```kotlin
composeTestRule.setContent { MyComposable() }
composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
composeTestRule.onNodeWithContentDescription("Icon").performClick()
composeTestRule.onNode(hasTestTag("my-tag")).assertExists()
composeTestRule.onRoot().printToLog("TAG") // Debug UI tree
```

### Coroutine Test Syntax

```kotlin
runTest { /* test code */ }                    // Simple coroutine test
testScope.runTest { /* test code */ }          // With custom scope
advanceUntilIdle()                             // Progress all coroutines
advanceTimeBy(1000)                            // Advance virtual time
```

---

## Example Test Templates

### ViewModel Test Template

```kotlin
@ExperimentalCoroutinesApi
class MyViewModelTest {
    private lateinit var viewModel: MyViewModel
    private lateinit var repository: MyRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher.scheduler)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = MyViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `methodName should expectedBehavior`() = testScope.runTest {
        // Given
        val testData = createTestData()

        // When
        viewModel.method(testData)
        advanceUntilIdle()

        // Then
        coVerify { repository.method(testData) }
    }
}
```

### Compose UI Test Template

```kotlin
class MyComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun componentName_action_expectedResult() {
        var callbackTriggered = false

        composeTestRule.setContent {
            MyComponent(
                onAction = { callbackTriggered = true }
            )
        }

        // Verify display
        composeTestRule.onNodeWithText("Button").assertIsDisplayed()

        // Perform action
        composeTestRule.onNodeWithText("Button").performClick()

        // Verify result
        assertTrue(callbackTriggered)
    }
}
```

---

**Last Updated**: January 2026
**Maintained By**: Looksy Development Team
