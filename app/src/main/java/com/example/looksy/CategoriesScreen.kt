package com.example.looksy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.looksy.ui.theme.LooksyTheme
import com.example.looksy.R

data class Category(val name: String, val iconRes: Int)
data class Item(val name: String, val imageRes: Int)
data class CategoryItems(val categoryName: String, val items: List<Item>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<Category>,
    categoryItems: List<CategoryItems>,
    navBar: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = { navBar() }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Header()
            CategoriesBlock(categories = categories)
            ItemsContainer(categoryItems = categoryItems)
        }
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(value = "", onValueChange = {}, placeholder = { Text("Search") })
        Image(
            painter = painterResource(id = R.drawable.avatar), // Replace with your avatar
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun CategoriesBlock(categories: List<Category>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                CategoryIcon(category = category)
            }
        }
    }
}

@Composable
fun CategoryIcon(category: Category) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center, // Ensures the image is centered within the padded area
            modifier = Modifier
                .size(50.dp) // 1. Total size of the container is 25dp
                .clip(CircleShape) // 2. Make the container a circle
                .background(Color.LightGray) // 3. Set the background color
                .padding(12.dp) // 4. Apply internal padding. The inner space is now 25 - 4 - 4 = 17dp
        ) {
            Image(
                painter = painterResource(id = category.iconRes),
                contentDescription = category.name,
                // The image will fill the space inside the padding
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(text = category.name)
    }
}

@Composable
fun ItemsContainer(categoryItems: List<CategoryItems>) {
    Column(modifier = Modifier.padding(16.dp)) {
        categoryItems.forEach { categoryItem ->
            ItemsBlock(categoryItem = categoryItem)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ItemsBlock(categoryItem: CategoryItems) {
    Column {
        Text(
            text = "${categoryItem.categoryName} (${categoryItem.items.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(250.dp) // Adjust height as needed
        ) {
            items(categoryItem.items) { item ->
                ItemContainer(item = item)
            }
        }
    }
}

@Composable
fun ItemContainer(item: Item) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.name,
            modifier = Modifier.size(120.dp)
        )
        Text(text = item.name)
    }
}

@Preview(showBackground = true)
@Composable
fun CategoriesScreenPreview() {
    val sampleCategories = listOf(
        Category("Shirt", R.drawable.shirt_category),
        Category("Pants", R.drawable.pants_category),
        Category("Glasses", R.drawable.glasses_category),
        Category("Shoes", R.drawable.shoes_category),
        Category("Watch", R.drawable.watch_category),
        Category("Watch", R.drawable.watch_category)
    )

    val sampleItems1 = listOf(
        Item("Black T-shirt", android.R.drawable.ic_menu_report_image),
        Item("Grey T-shirt", android.R.drawable.ic_menu_report_image)
    )

    val sampleItems2 = listOf(
        Item("Orange Cardigan", android.R.drawable.ic_menu_report_image),
        Item("Colorful Sweater", android.R.drawable.ic_menu_report_image)
    )


    val sampleCategoryItems = listOf(
        CategoryItems("T-shirts", sampleItems1),
        CategoryItems("Sweaters", sampleItems2)
    )

    LooksyTheme {
        CategoriesScreen(
            categories = sampleCategories,
            categoryItems = sampleCategoryItems,
            navBar = { }
        )
    }

    //ToDo: Get informaton in fun CategoriesScreen from Backend
}
