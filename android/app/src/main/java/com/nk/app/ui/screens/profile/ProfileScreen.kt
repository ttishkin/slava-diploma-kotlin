package com.nk.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nk.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToWarehouse: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Перезагружаем профиль каждый раз когда экран становится видимым
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Заголовок
        Text(
            "Профиль",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }

        if (!state.isLoggedIn) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👤", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Войдите в аккаунт", fontSize = 17.sp, color = NkLabel2)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToAuth,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 15.dp)
                    ) {
                        Text("Войти", fontWeight = FontWeight.W700, fontSize = 17.sp)
                    }
                }
            }
            return
        }

        val user = state.user ?: return

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Карточка сладкого бюджета — главная фича в стиле PWA
            user.kcalNorm?.let { norm ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, NkSep, RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Сладкий бюджет", fontSize = 13.sp, color = NkLabel2)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${norm * 20 / 100} ккал",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.W800,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "20% от нормы",
                            fontSize = 13.sp,
                            color = NkLabel3
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = NkSep
                        )
                        Text(
                            "Дневная норма: $norm ккал",
                            fontSize = 15.sp,
                            color = NkLabel2
                        )
                    }
                }
            }

            // Карточка пользователя
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, NkSep, RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        user.name ?: user.email,
                        fontWeight = FontWeight.W700,
                        fontSize = 20.sp
                    )
                    Text(user.email, fontSize = 13.sp, color = NkLabel2)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ProfileStatBlock("${user.points}", "Бонусы", MaterialTheme.colorScheme.primary)
                        val goalLabel = when (user.goal) {
                            "lose" -> "Похудение"
                            "gain" -> "Набор"
                            else -> "Поддержание"
                        }
                        ProfileStatBlock(goalLabel, "Цель", NkViolet)
                    }
                }
            }

            // Параметры тела
            if (user.height != null || user.weight != null || user.age != null) {
                ProfileInfoCard {
                    val activityLabel = when {
                        user.activity == null -> "—"
                        user.activity < 1.3 -> "Сидячий"
                        user.activity < 1.5 -> "Лёгкая"
                        user.activity < 1.7 -> "Средняя"
                        user.activity < 1.9 -> "Высокая"
                        else -> "Очень высокая"
                    }
                    val items = listOfNotNull(
                        user.age?.let { "Возраст" to "$it лет" },
                        user.height?.let { "Рост" to "$it см" },
                        user.weight?.let { "Вес" to "$it кг" },
                        "Пол" to if (user.sex == "male") "Муж" else "Жен",
                        "Активность" to activityLabel
                    )
                    items.forEachIndexed { i, (label, value) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontSize = 16.sp, color = NkLabel2)
                            Text(value, fontSize = 16.sp, fontWeight = FontWeight.W600)
                        }
                        if (i < items.lastIndex) {
                            HorizontalDivider(color = NkSep)
                        }
                    }
                }
            }

            // Действия
            ActionRow(icon = Icons.Default.Receipt, label = "Мои заказы", onClick = onNavigateToOrders)
            ActionRow(icon = Icons.Default.Tune, label = "Пересчитать КБЖУ", onClick = onNavigateToOnboarding)

            // Склад — только для admin
            if (user.role == "admin") {
                ActionRow(icon = Icons.Default.Warehouse, label = "Склад / Заказы", onClick = onNavigateToWarehouse)
            }

            Spacer(Modifier.height(8.dp))

            // Выход
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NkRed.copy(alpha = 0.12f))
                    .clickable { viewModel.logout() }
                    .padding(15.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = NkRed, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти", color = NkRed, fontWeight = FontWeight.W700, fontSize = 17.sp)
                }
            }

            // Футер
            Text(
                "Невский Кондитер · ЗОЖ",
                fontSize = 12.sp,
                color = NkLabel3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileStatBlock(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.W700, fontSize = 18.sp, color = color)
        Text(label, fontSize = 11.sp, color = NkLabel2)
    }
}

@Composable
fun ProfileInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, NkSep, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, NkSep, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(21.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.W500)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = NkLabel3, modifier = Modifier.size(20.dp))
        }
    }
}
