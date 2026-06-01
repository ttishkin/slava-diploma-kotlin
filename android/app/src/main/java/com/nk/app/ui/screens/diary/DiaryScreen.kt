package com.nk.app.ui.screens.diary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(viewModel: DiaryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Дневник питания") })

        // Навигация по дням
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val prev = LocalDate.parse(state.day).minusDays(1).toString()
                viewModel.setDay(prev)
            }) { Icon(Icons.Default.ChevronLeft, "Предыдущий") }

            Text(
                text = state.day,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                val next = LocalDate.parse(state.day).plusDays(1).toString()
                viewModel.setDay(next)
            }) { Icon(Icons.Default.ChevronRight, "Следующий") }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Кольцо калорий
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CalorieRing(
                            progress = state.progress,
                            consumed = state.totalKcal,
                            norm = state.kcalNorm
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (state.remaining >= 0) "Осталось ${state.remaining} ккал"
                            else "Превышение на ${-state.remaining} ккал",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.remaining >= 0) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Сладкий бюджет: ${state.kcalNorm * 20 / 100} ккал",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // По приёмам пищи
            val mealNames = mapOf(
                "breakfast" to "Завтрак", "lunch" to "Обед",
                "dinner" to "Ужин", "snack" to "Перекус"
            )
            mealNames.forEach { (key, label) ->
                val mealEntries = state.meals[key] ?: emptyList()
                if (mealEntries.isNotEmpty()) {
                    item {
                        Text(
                            "$label (${mealEntries.sumOf { it.kcal }} ккал)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(mealEntries) { entry ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(entry.name, fontWeight = FontWeight.Bold)
                                    Text("${entry.grams}г · ${entry.kcal} ккал",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { viewModel.deleteEntry(entry.id) }) {
                                    Icon(Icons.Default.Close, "Удалить", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            if (state.entries.isEmpty()) {
                item {
                    Text(
                        "Нет записей за этот день.\nДобавьте продукт из каталога.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CalorieRing(progress: Float, consumed: Int, norm: Int) {
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    val track = MaterialTheme.colorScheme.outline

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
        Canvas(Modifier.size(140.dp)) {
            val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            val topLeft = Offset(stroke.width / 2, stroke.width / 2)

            // Фон
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = stroke)

            // Прогресс
            val sweepAngle = (progress * 360f).coerceAtMost(360f)
            val color = if (progress <= 1f) primary else error
            drawArc(color, -90f, sweepAngle, false, topLeft, arcSize, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$consumed", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("/ $norm ккал", style = MaterialTheme.typography.labelSmall)
        }
    }
}
