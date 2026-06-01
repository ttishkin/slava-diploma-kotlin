package com.nk.app.ui.screens.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
fun CartScreen(
    onOrderSuccess: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Если заказ оформлен — показываем результат
    state.order?.let { order ->
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Заказ оформлен!", style = MaterialTheme.typography.headlineSmall)
            Text("Номер: ${order.no}", style = MaterialTheme.typography.bodyLarge)
            Text("Итого: ${order.total} ₽", style = MaterialTheme.typography.bodyLarge)
            Text("Начислено бонусов: ${order.bonusEarned}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onOrderSuccess) { Text("К заказам") }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Корзина") })

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Корзина пуста", style = MaterialTheme.typography.bodyLarge)
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.items) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.product.name, fontWeight = FontWeight.Bold)
                            Text("${item.product.price} ₽ × ${item.qty} = ${item.product.price * item.qty} ₽",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.updateQty(item.product.id, item.qty - 1) }) {
                                Icon(Icons.Default.Remove, "Минус")
                            }
                            Text("${item.qty}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.updateQty(item.product.id, item.qty + 1) }) {
                                Icon(Icons.Default.Add, "Плюс")
                            }
                        }
                    }
                }
            }

            // Промокод
            item {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.promoCode,
                        onValueChange = { viewModel.setPromoCode(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Промокод") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { viewModel.checkPromo() }) { Text("Применить") }
                }
                if (state.promoDiscount != null) {
                    Text("Скидка ${state.promoDiscount}% применена!", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Итого
        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Подитог"); Text("${state.subtotal} ₽")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Доставка"); Text(if (state.delivery == 0) "Бесплатно" else "${state.delivery} ₽")
                }
                if (state.promoAmount > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Скидка"); Text("-${state.promoAmount} ₽", color = MaterialTheme.colorScheme.primary)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Итого", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("${state.total} ₽", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                if (state.delivery > 0) {
                    Text(
                        "Бесплатная доставка от 1000 ₽",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.placeOrder() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isOrdering
                ) {
                    if (state.isOrdering) CircularProgressIndicator(Modifier.size(18.dp))
                    else Text("Оформить заказ")
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
