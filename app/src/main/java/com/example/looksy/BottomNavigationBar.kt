package com.example.looksy

import android.graphics.pdf.content.PdfPageGotoLinkContent
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.koin.java.KoinJavaComponent.inject

data class BottomNavigationItem(
    val titleId: Int,
    val selectedId: Int,
    val unselectedId: Int,
    val destination: NavigationDestination
)

@Composable
fun NavBar(
    items: List<BottomNavigationItem>
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val navFlow : NavigationFlow by inject(NavigationFlow::class.java)
    NavigationBar(containerColor = Color.Magenta) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White,
                    selectedIconColor = Color.Cyan
                ),
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if(selectedItem == index) item.selectedId
                            else item.unselectedId
                        ),
                        contentDescription = stringResource(item.titleId)
                    )
                },
                label = { Text(stringResource(item.titleId)) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navFlow
                }
            )
        }
    }
}