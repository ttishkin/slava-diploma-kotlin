package com.nk.app.ui.screens.warehouse

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.api.NkApi
import com.nk.app.domain.model.AdminStats
import com.nk.app.domain.model.Order
import com.nk.app.domain.model.UpdateStatusRequest
import com.nk.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════
// Состояние
// ═══════════════════════════════════════════════════════════════════

data class WarehouseState(
    val orders: List<Order> = emptyList(),
    val stats: AdminStats? = null,
    val filterStatus: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

// ═══════════════════════════════════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════════════════════════════════

@HiltViewModel
class WarehouseViewModel @Inject constructor(
    private val api: NkApi
) : ViewModel() {
    private val _state = MutableStateFlow(WarehouseState())
    val state: StateFlow<WarehouseState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val orders = api.getAdminOrders()
                Log.d("Warehouse", "Загружено заказов: ${orders.size}")
                val stats = try { api.getAdminStats() } catch (e: Exception) {
                    Log.e("Warehouse", "Ошибка загрузки статистики: ${e.message}")
                    null
                }
                _state.value = _state.value.copy(
                    orders = orders,
                    stats = stats,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("Warehouse", "Ошибка загрузки: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "${e.javaClass.simpleName}: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun setFilter(status: String?) {
        _state.value = _state.value.copy(filterStatus = status)
    }

    fun updateStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            try {
                api.updateOrderStatus(orderId, UpdateStatusRequest(newStatus))
                load() // Перезагружаем данные
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Ошибка обновления статуса")
            }
        }
    }

    val filteredOrders: List<Order>
        get() {
            val s = _state.value
            return if (s.filterStatus == null) s.orders
            else s.orders.filter { it.status == s.filterStatus }
        }
}

// ═══════════════════════════════════════════════════════════════════
// UI
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseScreen(
    onBack: () -> Unit,
    viewModel: WarehouseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NkBg)
    ) {
        // Хедер
        TopAppBar(
            title = { Text("Склад", fontWeight = FontWeight.W800) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.load() }) {
                    Icon(Icons.Default.Refresh, "Обновить")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = NkBg,
                navigationIconContentColor = NkGreen,
                titleContentColor = NkLabel,
                actionIconContentColor = NkLabel2
            )
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NkGreen)
            }
            return
        }

        if (state.error != null) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Ошибка доступа", fontSize = 20.sp, fontWeight = FontWeight.W700, color = NkLabel)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.error!!,
                        color = NkRed,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Убедитесь, что вы вошли как admin@nk.ru\nи бэкенд запущен",
                        fontSize = 13.sp,
                        color = NkLabel3,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.load() },
                        colors = ButtonDefaults.buttonColors(containerColor = NkGreen, contentColor = NkGreenDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Повторить", fontWeight = FontWeight.W700)
                    }
                }
            }
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Статистика — 4 карточки
            item {
                state.stats?.let { stats ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("Заказы", "${stats.orders}", NkGreen, Modifier.weight(1f))
                        StatCard("Выручка", "${stats.revenue} ₽", NkOrange, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("Юзеры", "${stats.users}", NkViolet, Modifier.weight(1f))
                        StatCard("Товары", "${stats.products}", NkBlue, Modifier.weight(1f))
                    }
                }
            }

            // Статусы заказов — компактные счётчики
            item {
                state.stats?.let { stats ->
                    if (stats.byStatus.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                StatusChip("Все", state.orders.size, state.filterStatus == null) {
                                    viewModel.setFilter(null)
                                }
                            }
                            items(stats.byStatus) { sc ->
                                StatusChip(
                                    statusLabel(sc.status),
                                    sc.count,
                                    state.filterStatus == sc.status
                                ) {
                                    viewModel.setFilter(sc.status)
                                }
                            }
                        }
                    }
                }
            }

            // Заголовок списка
            item {
                Text(
                    "Заказы",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W800,
                    color = NkLabel,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Список заказов
            val orders = viewModel.filteredOrders
            if (orders.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Нет заказов", fontSize = 15.sp, color = NkLabel2)
                    }
                }
            } else {
                items(orders) { order ->
                    WarehouseOrderCard(order, onStatusChange = { newStatus ->
                        viewModel.updateStatus(order.id, newStatus)
                    })
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Компоненты
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, NkSep, RoundedCornerShape(18.dp))
            .background(NkCard)
            .padding(16.dp)
    ) {
        Column {
            Text(label, fontSize = 13.sp, color = NkLabel2)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.W800, color = color)
        }
    }
}

@Composable
private fun StatusChip(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, if (selected) NkGreen else NkSep, RoundedCornerShape(18.dp))
            .background(if (selected) NkGreen else NkCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            "$label ($count)",
            fontSize = 14.sp,
            fontWeight = FontWeight.W600,
            color = if (selected) NkGreenDark else NkLabel
        )
    }
}

@Composable
private fun WarehouseOrderCard(order: Order, onStatusChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, NkSep, RoundedCornerShape(18.dp))
            .background(NkCard)
            .padding(16.dp)
    ) {
        Column {
            // Шапка: номер + статус
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(order.no, fontWeight = FontWeight.W700, fontSize = 16.sp, color = NkLabel)
                    order.userEmail?.let {
                        Text(it, fontSize = 12.sp, color = NkLabel3)
                    }
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(statusColor(order.status).copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusLabel(order.status),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W700,
                        color = statusColor(order.status)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Товары
            order.items.forEach { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${item.productName ?: "Товар #${item.productId}"} × ${item.qty}",
                        fontSize = 14.sp,
                        color = NkLabel2,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${item.price * item.qty} ₽",
                        fontSize = 14.sp,
                        color = NkLabel2
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = NkSep)
            Spacer(Modifier.height(8.dp))

            // Итого и дата
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(order.createdAt.take(10), fontSize = 12.sp, color = NkLabel3)
                Text("${order.total} ₽", fontSize = 18.sp, fontWeight = FontWeight.W700, color = NkLabel)
            }

            if (order.delivery > 0) {
                Text("Доставка: ${order.delivery} ₽", fontSize = 12.sp, color = NkLabel3)
            }

            Spacer(Modifier.height(12.dp))

            // Кнопка смены статуса
            Box {
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NkGreen.copy(alpha = 0.12f),
                        contentColor = NkGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Изменить статус", fontWeight = FontWeight.W600, fontSize = 15.sp)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = NkCard2
                ) {
                    val statuses = listOf("new", "processing", "shipped", "delivered", "cancelled")
                    statuses.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(statusColor(status))
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(statusLabel(status), color = NkLabel)
                                }
                            },
                            onClick = {
                                expanded = false
                                if (status != order.status) onStatusChange(status)
                            },
                            enabled = status != order.status
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Утилиты
// ═══════════════════════════════════════════════════════════════════

private fun statusLabel(status: String): String = when (status) {
    "new" -> "Новый"
    "processing" -> "Сборка"
    "shipped" -> "Отправлен"
    "delivered" -> "Доставлен"
    "cancelled" -> "Отменён"
    else -> status
}

private fun statusColor(status: String) = when (status) {
    "new" -> NkBlue
    "processing" -> NkOrange
    "shipped" -> NkViolet
    "delivered" -> NkSystemGreen
    "cancelled" -> NkRed
    else -> NkLabel2
}
