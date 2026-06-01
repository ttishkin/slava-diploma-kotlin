package com.nk.app.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.nk.app.data.repository.CartRepository
import com.nk.app.domain.model.Order
import com.nk.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val cartRepo: CartRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OrdersState())
    val state: StateFlow<OrdersState> = _state

    init {
        viewModelScope.launch {
            try {
                _state.value = OrdersState(orders = cartRepo.getOrders(), isLoading = false)
            } catch (e: Exception) {
                _state.value = OrdersState(error = e.message, isLoading = false)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Мои заказы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
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
            state.orders.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Заказов пока нет", fontSize = 17.sp, color = NkLabel2)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.orders) { order ->
                        OrderCard(order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val statusLabel = when (order.status) {
        "new" -> "Новый"
        "processing" -> "В обработке"
        "shipped" -> "Отправлен"
        "delivered" -> "Доставлен"
        "cancelled" -> "Отменён"
        else -> order.status
    }
    val statusColor = when (order.status) {
        "delivered" -> NkSystemGreen
        "cancelled" -> NkRed
        else -> NkOrange
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, NkSep, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.no, fontWeight = FontWeight.W700, fontSize = 16.sp)
                Text(statusLabel, color = statusColor, fontWeight = FontWeight.W700, fontSize = 14.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text("${order.total} ₽", fontSize = 18.sp, fontWeight = FontWeight.W700)
            Spacer(Modifier.height(4.dp))
            order.items.forEach { item ->
                Text(
                    "${item.productName ?: "Товар #${item.productId}"} × ${item.qty}",
                    fontSize = 13.sp,
                    color = NkLabel2
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                order.createdAt.take(10),
                fontSize = 11.sp,
                color = NkLabel3
            )
        }
    }
}
