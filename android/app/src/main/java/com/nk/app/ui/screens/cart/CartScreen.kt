package com.nk.app.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nk.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onOrderSuccess: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    // Заказ оформлен
    state.order?.let { order ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CheckCircle, null,
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Заказ оформлен!", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Номер: ${order.no}", fontSize = 16.sp, color = NkLabel2)
            Text("Итого: ${order.total} ₽", fontSize = 16.sp)
            Text("Начислено бонусов: ${order.bonusEarned}", fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onOrderSuccess,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 15.dp, horizontal = 24.dp)
            ) {
                Text("К заказам", fontWeight = FontWeight.W700, fontSize = 17.sp)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Заголовок
        Text(
            "Корзина",
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

        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Корзина пуста", fontSize = 17.sp, color = NkLabel2)
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(state.items) { item ->
                // Элемент корзины в стиле PWA (.listcard > .row)
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.product.name,
                                fontWeight = FontWeight.W600,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${item.product.price} ₽ × ${item.qty} = ${item.product.price * item.qty} ₽",
                                fontSize = 13.sp,
                                color = NkLabel2
                            )
                        }
                        // Контроль количества в стиле PWA (.qty)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            QtyButton("-") { viewModel.updateQty(item.product.id, item.qty - 1) }
                            Text(
                                "${item.qty}",
                                fontWeight = FontWeight.W700,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            QtyButton("+") { viewModel.updateQty(item.product.id, item.qty + 1) }
                        }
                    }
                }
            }

            // Промокод
            item {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f))
                    ) {
                        TextField(
                            value = state.promoCode,
                            onValueChange = { viewModel.setPromoCode(it) },
                            placeholder = { Text("Промокод", color = NkLabel3) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true
                        )
                    }
                    Button(
                        onClick = { viewModel.checkPromo() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.09f),
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text("Применить", fontWeight = FontWeight.W700)
                    }
                }
                if (state.promoDiscount != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Скидка ${state.promoDiscount}% применена!",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Доставка — прогресс-бар до бесплатной доставки
        if (state.delivery > 0 && state.subtotal > 0) {
            val progress = (state.subtotal.toFloat() / 1000f).coerceAtMost(1f)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("До бесплатной доставки", fontSize = 13.sp, color = NkLabel2)
                    Text("${1000 - state.subtotal} ₽", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = NkSep,
                )
            }
        }

        // Итого — карточка в стиле PWA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, NkSep, RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Column {
                SummaryRow("Подитог", "${state.subtotal} ₽")
                Spacer(Modifier.height(4.dp))
                SummaryRow(
                    "Доставка",
                    if (state.delivery == 0) "Бесплатно" else "${state.delivery} ₽",
                    valueColor = if (state.delivery == 0) NkSystemGreen else null
                )
                if (state.promoAmount > 0) {
                    Spacer(Modifier.height(4.dp))
                    SummaryRow("Скидка", "-${state.promoAmount} ₽", valueColor = MaterialTheme.colorScheme.primary)
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = NkSep
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Итого", fontWeight = FontWeight.W800, fontSize = 22.sp)
                    Text("${state.total} ₽", fontWeight = FontWeight.W800, fontSize = 22.sp)
                }

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.placeOrder() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isOrdering,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 15.dp)
                ) {
                    if (state.isOrdering) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Оформить заказ", fontWeight = FontWeight.W700, fontSize = 17.sp)
                    }
                }
                state.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun QtyButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            .let { modifier ->
                modifier
            },
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.size(30.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text,
                fontSize = 18.sp,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 15.sp, color = NkLabel2)
        Text(
            value,
            fontSize = 15.sp,
            fontWeight = FontWeight.W500,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
