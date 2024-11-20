package com.grupo18.twister.core.screens.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo18.twister.R
import com.grupo18.twister.core.components.CustomBottomNavigationBar

@Composable
fun SearchScreen(navController: NavController) {
    Scaffold(
        bottomBar = { CustomBottomNavigationBar(navController) } // Añadimos la BottomBar aquí
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Barra de búsqueda
            SearchBar()
            Spacer(modifier = Modifier.height(16.dp))

            // Slider de imágenes
            ImageSlider()
            Spacer(modifier = Modifier.height(24.dp))

            // Categorías
            Text(
                text = "Categories",
                style = TextStyle(fontSize = 20.sp, color = Color.Black),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            CategoryGrid()
        }
    }
}
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black)
        )
        Icon(
            Icons.Default.Search,
            contentDescription = "Search Icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ImageSlider() {
    val imageList = listOf(
        R.drawable.sample_slider,
        R.drawable.sample_slider2,
        R.drawable.sample_slider3
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(imageList) { index, imageResId ->
    Image(
        painter = painterResource(id = imageResId),
        contentDescription = "Slider Image $index",
        modifier = Modifier
            .height(150.dp)
            .width(300.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}
    }
}

@Composable
fun CategoryGrid() {
    val categories = listOf(
        R.drawable.rectangle,
        R.drawable.rectangle,
        R.drawable.rectangle,
        R.drawable.rectangle,
        R.drawable.rectangle,
        R.drawable.rectangle
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxHeight(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            Image(
                painter = painterResource(id = category),
                contentDescription = "Category Image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { /* Acción al hacer clic en la categoría */ }
            )
        }
    }
}
