package com.nk.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
fun ProfileScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Профиль") })

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (!state.isLoggedIn) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, null, Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Войдите в аккаунт", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onNavigateToAuth) { Text("Войти") }
                }
            }
            return
        }

        val user = state.user ?: return

        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Карточка пользователя
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(user.name ?: user.email, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${user.points}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Бонусы", style = MaterialTheme.typography.labelSmall)
                        }
                        user.kcalNorm?.let {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$it", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                Text("Ккал/день", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        val goalLabel = when (user.goal) {
                            "lose" -> "Похудение"
                            "gain" -> "Набор"
                            else -> "Поддержание"
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(goalLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Цель", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Параметры
            if (user.height != null || user.weight != null || user.age != null) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Параметры", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            user.age?.let { ProfileStat("Возраст", "$it лет") }
                            user.height?.let { ProfileStat("Рост", "$it см") }
                            user.weight?.let { ProfileStat("Вес", "$it кг") }
                        }
                    }
                }
            }

            // Действия
            OutlinedCard(
                onClick = onNavigateToOrders,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Мои заказы", style = MaterialTheme.typography.bodyLarge)
                }
            }

            OutlinedCard(
                onClick = onNavigateToOnboarding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Пересчитать КБЖУ", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Выйти")
            }
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
