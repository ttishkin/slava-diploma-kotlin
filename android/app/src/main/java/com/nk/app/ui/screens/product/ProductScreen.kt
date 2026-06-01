package com.nk.app.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
        topBar = {
            TopAppBar(
                title = { Text("Продукт") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Избранное",
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
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
                        .padding(16.dp)
                ) {
                    // Название и категория
                    Text(p.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    p.categoryName?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(Modifier.height(16.dp))

                    // КБЖУ карточка
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Пищевая ценность на ${p.grams}г", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                NutrientItem("Ккал", "${p.kcal}", MaterialTheme.colorScheme.primary)
                                NutrientItem("Белки", "${p.protein}г", MaterialTheme.colorScheme.tertiary)
                                NutrientItem("Жиры", "${p.fat}г", MaterialTheme.colorScheme.secondary)
                                NutrientItem("Углеводы", "${p.carb}г", MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Цена
                    Text(
                        "${p.price} ₽",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    // Кнопки
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.addToCart() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("В корзину")
                        }
                        OutlinedButton(
                            onClick = { showMealDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MenuBook, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("В дневник")
                        }
                    }

                    if (state.addedToCart) {
                        Spacer(Modifier.height(8.dp))
                        Text("Добавлено в корзину!", color = MaterialTheme.colorScheme.primary)
                    }
                    if (state.addedToDiary) {
                        Spacer(Modifier.height(8.dp))
                        Text("Добавлено в дневник!", color = MaterialTheme.colorScheme.primary)
                    }

                    // Состав
                    p.sostav?.let {
                        Spacer(Modifier.height(16.dp))
                        Text("Состав", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }

                    p.benefit?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("Польза", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }

                    // Рейтинг
                    if (p.avgRating != null) {
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(4.dp))
                            Text("${p.avgRating} (${p.reviewCount ?: 0} отзывов)")
                        }
                    }

                    // Отзывы
                    p.reviews?.let { reviews ->
                        Spacer(Modifier.height(16.dp))
                        Text("Отзывы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        reviews.forEach { review ->
                            Spacer(Modifier.height(8.dp))
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(review.author ?: "Аноним", fontWeight = FontWeight.Bold)
                                        Row {
                                            repeat(review.rating) {
                                                Icon(Icons.Default.Star, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    }
                                    review.text?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
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
            title = { Text("Приём пищи") },
            text = {
                Column {
                    listOf("breakfast" to "Завтрак", "lunch" to "Обед", "dinner" to "Ужин", "snack" to "Перекус").forEach { (key, label) ->
                        TextButton(
                            onClick = {
                                viewModel.addToDiary(key)
                                showMealDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(label) }
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

@Composable
fun NutrientItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
