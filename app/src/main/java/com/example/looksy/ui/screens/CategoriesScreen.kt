package com.example.looksy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import coil.compose.AsyncImage
import com.example.looksy.ui.components.Header
import com.example.looksy.R
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Type
import com.example.looksy.ui.components.LooksyButton

data class Category(val name: String, val iconRes: Int)
data class Item(val name: String, val imageRes: Int)
data class CategoryItems(val category: Type, val items: List<Clothes>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<Category>,
    categoryItems: List<CategoryItems>,
    onClick: (String) -> Unit = {},
    onButtonClicked: (Int) -> Unit = {},
    onNavigateToDiscard: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            Header(
                onNavigateBack = { },
                onNavigateToRightIcon = { onNavigateToDiscard() },
                clothesData = null,
                headerText = "Dein Kleiderschrank",
                rightIconContentDescription = "Vorschläge zum Aussortieren",
                rightIcon = Icons.Default.DeleteSweep,
                isFirstHeader = true
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp)
            .fillMaxSize()
        ) {
            ItemsContainer(
                categoryItems = categoryItems,
                modifier = Modifier.weight(1f),
                onButtonClicked = onButtonClicked,
                onClick = onClick
            )
        }
    }
}

@Composable
fun CategoriesBlock(categories: List<Category>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Categories", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
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
                .background(Color(219, 48, 34, 64)) // 3. Set the background color
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
fun ItemsContainer(
    categoryItems: List<CategoryItems>,
    modifier: Modifier = Modifier,
    onButtonClicked: (Int) -> Unit,
    onClick: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(249, 246, 242), shape = RoundedCornerShape(20.dp))
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categoryItems.forEach { categoryItem ->
            ItemsBlock(
                categoryItem = categoryItem,
                onButtonClicked = onButtonClicked,
                onClick = onClick
            )
        }
    }
}

@Composable
fun ItemsBlock(categoryItem: CategoryItems, onButtonClicked: (Int) -> Unit, onClick: (String) -> Unit) {
    Column {
        ItemsTitle(categoryItem = categoryItem, onClick = onClick)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            //columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            //verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .height(170.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            categoryItem.items.forEach { item ->
                ItemContainer(
                    item = item,
                    modifier = Modifier
                        .size(165.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(255, 255, 255))
                        .padding(16.dp),
                    onClick = { onButtonClicked(item.id) }
                )
            }
        }
    }
}

@Composable
fun ItemsTitle(categoryItem: CategoryItems, onClick: (String) -> Unit ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${categoryItem.category} (${categoryItem.items.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        LooksyButton(
            onClick = { onClick(categoryItem.category.name) },
            picture = {
                Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "See more",
                    modifier = Modifier.size(26.dp)
                )
            }
        )
    }
}

@Composable
fun ItemContainer(item: Clothes, modifier: Modifier, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AsyncImage(
            model = item.imagePath,
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .aspectRatio(1f), // quadratisch
            error = painterResource(id = R.drawable.clothicon) // Fallback-Bild
        )
        Spacer(modifier = Modifier.height(8.dp))
        //Text(text = item.)
    }
}

@Preview(showBackground = true)
@Composable
fun CategoriesScreenPreview() {
    val sampleCategories = listOf(
        Category("Shirts", R.drawable.shirt_category),
        Category("Pants", R.drawable.pants_category),
        Category("Dresses", android.R.drawable.ic_search_category_default),
        Category("Shorts", android.R.drawable.ic_search_category_default),
        Category("Sweaters`", android.R.drawable.ic_search_category_default),
//        Category("Glasses", R.drawable.glasses_category),
//        Category("Shoes", R.drawable.shoes_category),
//        Category("Watch", R.drawable.watch_category)
    )

    val shirts = listOf(
        Item("Black T-shirt", R.drawable.black_t_shirt),
        Item("Grey T-shirt", R.drawable.white_t_shirt)
    )

    val sweaters = listOf(
        Item("Orange Cardigan", R.drawable.orange_cardigan),
        Item("Colorful Sweater", R.drawable.colorful_sweater)
    )

    val pants = listOf(
        Item("Blue Jeans", R.drawable.orange_cardigan),
        Item("Cargo Pants", R.drawable.colorful_sweater)
    )

    val dresses = listOf(
        Item("Blue Dress", android.R.drawable.ic_menu_gallery),
        Item("Yellow Dress", android.R.drawable.ic_menu_gallery)
    )

    val shorts = listOf(
        Item("blue chino shorts", android.R.drawable.ic_menu_gallery),
        Item("grey sport shorts", android.R.drawable.ic_menu_gallery)
    )

/*
    val sampleCategoryItems = listOf(
        CategoryItems("Shirts", shirts),
        CategoryItems("Sweaters", sweaters),
        CategoryItems("Pants", pants),
//        CategoryItems("Dresses", dresses),
//        CategoryItems("Shorts", shorts)
    )
 */
/*
    LooksyTheme {
        CategoriesScreen(
            categories = sampleCategories,
            categoryItems = sampleCategoryItems,
            navBar = { }
        )
    }
*/
}
