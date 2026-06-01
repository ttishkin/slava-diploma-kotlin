package com.nk.app.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nk.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    productId: Int,
    onBack: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showMealDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Продукт") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    // Кнопка избранного в стиле PWA (42dp, круглая)
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.dp, NkSep, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Избранное",
                            tint = if (state.isFavorite) NkRed else NkLabel2,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            state.product != null -> {
                val p = state.product!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Картинка продукта (240dp как в PWA)
                    p.imageUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = p.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Название
                    Text(
                        p.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    p.categoryName?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NkLabel2,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Рейтинг
                    if (p.avgRating != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            repeat(5) { i ->
                                Icon(
                                    Icons.Default.Star, null,
                                    tint = if (i < (p.avgRating?.toInt() ?: 0)) Color(0xFFE8C24A) else NkLabel3,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("(${p.reviewCount ?: 0})", fontSize = 13.sp, color = NkLabel2)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // КБЖУ — 4 блока в стиле PWA
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NutrientBlock("Ккал", "${p.kcal}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        NutrientBlock("Белки", "${p.protein}г", MacroProtein, Modifier.weight(1f))
                        NutrientBlock("Жиры", "${p.fat}г", MacroFat, Modifier.weight(1f))
                        NutrientBlock("Углеводы", "${p.carb}г", MacroCarb, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Цена
                    Text(
                        "${p.price} ₽",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W800,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        "за ${p.grams}г",
                        fontSize = 13.sp,
                        color = NkLabel2,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Кнопки действий — в стиле PWA
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // В корзину (основная)
                        Button(
                            onClick = { viewModel.addToCart() },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 15.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("В корзину", fontWeight = FontWeight.W700, fontSize = 17.sp)
                        }
                        // В дневник (вторичная)
                        Button(
                            onClick = { showMealDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.09f),
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 15.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("В дневник", fontWeight = FontWeight.W700, fontSize = 15.sp)
                        }
                    }

                    if (state.addedToCart) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Добавлено в корзину!",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    if (state.addedToDiary) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Добавлено в дневник!",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Состав
                    p.sostav?.let {
                        Spacer(Modifier.height(16.dp))
                        SectionCard("Состав", it)
                    }

                    p.benefit?.let {
                        Spacer(Modifier.height(8.dp))
                        SectionCard("Польза", it)
                    }

                    // Отзывы
                    p.reviews?.let { reviews ->
                        if (reviews.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Отзывы",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            reviews.forEach { review ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, NkSep, RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                review.author ?: "Аноним",
                                                fontWeight = FontWeight.W600,
                                                fontSize = 16.sp
                                            )
                                            Row {
                                                repeat(review.rating) {
                                                    Icon(Icons.Default.Star, null, Modifier.size(16.dp), tint = Color(0xFFE8C24A))
                                                }
                                            }
                                        }
                                        review.text?.let {
                                            Spacer(Modifier.height(4.dp))
                                            Text(it, fontSize = 13.sp, color = NkLabel2)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    // Диалог выбора приёма пищи
    if (showMealDialog) {
        AlertDialog(
            onDismissRequest = { showMealDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Приём пищи") },
            text = {
                Column {
                    listOf(
                        "breakfast" to "🌅 Завтрак",
                        "lunch" to "☀️ Обед",
                        "dinner" to "🌙 Ужин",
                        "snack" to "🍪 Перекус"
                    ).forEach { (key, label) ->
                        TextButton(
                            onClick = {
                                viewModel.addToDiary(key)
                                showMealDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMealDialog = false }) { Text("Отмена") }
            }
        )
    }
}

/**
 * Блок нутриента в стиле PWA (.kb)
 */
@Composable
fun NutrientBlock(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .border(1.dp, NkSep, RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 19.sp, fontWeight = FontWeight.W800, color = color)
            Text(label, fontSize = 11.sp, color = NkLabel2)
        }
    }
}

/**
 * Секция в карточке (Состав / Польза)
 */
@Composable
fun SectionCard(title: String, content: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, NkSep, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Column {
            Text(title, fontWeight = FontWeight.W600, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(content, fontSize = 15.sp, color = NkLabel2)
        }
    }
}
