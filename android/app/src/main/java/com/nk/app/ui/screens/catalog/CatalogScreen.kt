package com.nk.app.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nk.app.domain.model.Product
import com.nk.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (Int) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Заголовок в стиле PWA (крупный, жирный)
        Text(
            text = "Каталог",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        // Поиск в стиле PWA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f))
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Search, "Поиск",
                    tint = NkLabel2,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(7.dp))
                TextField(
                    value = state.query,
                    onValueChange = { viewModel.search(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Поиск продуктов...", color = NkLabel3, fontSize = 17.sp)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                if (state.query.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.search("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Clear, "Очистить", tint = NkLabel2, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Категории — чипы в стиле PWA
        if (state.categories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                item {
                    NkChip(
                        label = "Все",
                        selected = state.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) }
                    )
                }
                items(state.categories) { cat ->
                    NkChip(
                        label = "${cat.glyph} ${cat.name}",
                        selected = state.selectedCategory == cat.id,
                        onClick = { viewModel.selectCategory(cat.id) }
                    )
                }
            }
        }

        // Теги — чипы в стиле PWA с цветами
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            val tags = listOf(
                Triple("sugar", "Без сахара", TagSugarText),
                Triple("prot", "Протеин", TagProtText),
                Triple("nut", "Орехи", TagNutText),
                Triple("fat", "Без жира", TagFatText)
            )
            items(tags) { (tag, label, color) ->
                NkChip(
                    label = label,
                    selected = state.selectedTag == tag,
                    onClick = { viewModel.selectTag(tag) },
                    accentColor = color
                )
            }
        }

        // Сортировка
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            val sorts = listOf(
                null to "По умолчанию",
                "price_asc" to "Цена ↑",
                "price_desc" to "Цена ↓",
                "kcal_asc" to "Ккал ↑"
            )
            items(sorts) { (sort, label) ->
                NkChip(
                    label = label,
                    selected = state.sort == sort,
                    onClick = { viewModel.setSort(sort) }
                )
            }
        }

        // Контент
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.search(state.query) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Повторить", fontWeight = FontWeight.W700)
                        }
                    }
                }
            }
            state.products.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ничего не найдено", color = NkLabel2)
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.products) { product ->
                        ProductCard(product = product, onClick = { onProductClick(product.id) })
                    }
                }
            }
        }
    }
}

/**
 * Чип в стиле PWA
 */
@Composable
fun NkChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color? = null
) {
    val bgColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        accentColor ?: MaterialTheme.colorScheme.onBackground
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        NkSep
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.W600,
            color = textColor,
            maxLines = 1
        )
    }
}

/**
 * Карточка продукта в стиле PWA
 */
@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, NkSep, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        Column {
            // Картинка продукта (118dp как в PWA)
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Бейджи
            Row(
                modifier = Modifier.padding(start = 11.dp, top = if (product.imageUrl != null) 6.dp else 10.dp, end = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (product.isHit) {
                    NkBadge("Хит", NkOrange)
                }
                if (product.isNovelty) {
                    NkBadge("New", NkViolet)
                }
            }

            // Название продукта
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.W600,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 11.dp, end = 11.dp, top = 8.dp, bottom = 2.dp),
                lineHeight = 17.sp
            )

            // Калории
            Text(
                text = "${product.kcal} ккал · ${product.grams}г",
                fontSize = 12.sp,
                color = NkLabel2,
                modifier = Modifier.padding(horizontal = 11.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Цена и рейтинг
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 11.dp, end = 11.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.price} ₽",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.avgRating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFE8C24A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${product.avgRating}",
                            fontSize = 12.sp,
                            color = NkLabel2
                        )
                    }
                }
            }
        }
    }
}

/**
 * Бейдж (Хит / New) в стиле PWA
 */
@Composable
fun NkBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.W800,
            color = color
        )
    }
}
