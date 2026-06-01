package com.nk.app.ui.screens.diary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nk.app.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(viewModel: DiaryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Заголовок
        Text(
            "Дневник",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        // Навигация по дням
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val prev = LocalDate.parse(state.day).minusDays(1).toString()
                viewModel.setDay(prev)
            }) {
                Icon(Icons.Default.ChevronLeft, "Предыдущий", tint = MaterialTheme.colorScheme.primary)
            }

            Text(
                text = state.day,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W700
            )

            IconButton(onClick = {
                val next = LocalDate.parse(state.day).plusDays(1).toString()
                viewModel.setDay(next)
            }) {
                Icon(Icons.Default.ChevronRight, "Следующий", tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Кольцо калорий
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, NkSep, RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CalorieRing(
                            progress = state.progress,
                            consumed = state.totalKcal,
                            norm = state.kcalNorm
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (state.remaining >= 0) "Осталось ${state.remaining} ккал"
                            else "Превышение на ${-state.remaining} ккал",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W600,
                            color = if (state.remaining >= 0) MaterialTheme.colorScheme.primary
                                   else NkRed
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Сладкий бюджет: ${state.kcalNorm * 20 / 100} ккал",
                            fontSize = 13.sp,
                            color = NkLabel2
                        )
                    }
                }
            }

            // Макронутриенты — 3 полоски прогресса в стиле PWA
            // Макронутриенты — сводка
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val totalKcal = state.totalKcal
                    val sweetBudget = state.kcalNorm * 20 / 100

                    MacroBar("Съедено", "${totalKcal} ккал", MacroProtein, Modifier.weight(1f))
                    MacroBar("Бюджет", "${sweetBudget} ккал", MacroFat, Modifier.weight(1f))
                    MacroBar("Норма", "${state.kcalNorm} ккал", MacroCarb, Modifier.weight(1f))
                }
            }

            // По приёмам пищи
            val mealNames = mapOf(
                "breakfast" to "🌅 Завтрак",
                "lunch" to "☀️ Обед",
                "dinner" to "🌙 Ужин",
                "snack" to "🍪 Перекус"
            )
            mealNames.forEach { (key, label) ->
                val mealEntries = state.meals[key] ?: emptyList()
                if (mealEntries.isNotEmpty()) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                label,
                                fontWeight = FontWeight.W700,
                                fontSize = 16.sp
                            )
                            Text(
                                "${mealEntries.sumOf { it.kcal }} ккал",
                                fontSize = 13.sp,
                                color = NkLabel2
                            )
                        }
                    }
                    items(mealEntries) { entry ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, NkSep, RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp, 14.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(entry.name, fontWeight = FontWeight.W600, fontSize = 16.sp)
                                    Text(
                                        "${entry.grams}г · ${entry.kcal} ккал",
                                        fontSize = 13.sp,
                                        color = NkLabel2
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteEntry(entry.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Удалить", tint = NkLabel3, modifier = Modifier.size(16.dp))
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        color = NkLabel2,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CalorieRing(progress: Float, consumed: Int, norm: Int) {
    val primary = MaterialTheme.colorScheme.primary
    val error = NkRed
    val track = NkSep

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Canvas(Modifier.size(160.dp)) {
            val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            val topLeft = Offset(stroke.width / 2, stroke.width / 2)

            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = stroke)

            val sweepAngle = (progress * 360f).coerceAtMost(360f)
            val color = if (progress <= 1f) primary else error
            drawArc(color, -90f, sweepAngle, false, topLeft, arcSize, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$consumed", fontSize = 28.sp, fontWeight = FontWeight.W800)
            Text("/ $norm ккал", fontSize = 13.sp, color = NkLabel2)
        }
    }
}

@Composable
fun MacroBar(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .border(1.dp, NkSep, RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp, 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.W800, color = color)
            Text(label, fontSize = 11.sp, color = NkLabel2)
        }
    }
}
