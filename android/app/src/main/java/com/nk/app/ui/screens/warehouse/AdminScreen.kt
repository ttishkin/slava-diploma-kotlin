package com.nk.app.ui.screens.warehouse

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.api.NkApi
import com.nk.app.domain.model.*
import com.nk.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════════════════════════════════

data class AdminState(
    val orders: List<Order> = emptyList(),
    val users: List<AdminUser> = emptyList(),
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val stats: AdminStats? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val api: NkApi
) : ViewModel() {
    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val orders = api.getAdminOrders()
                val users = api.getAdminUsers()
                val products = api.getProducts()
                val categories = api.getCategories()
                val stats = try { api.getAdminStats() } catch (_: Exception) { null }
                _state.value = AdminState(
                    orders = orders, users = users, products = products,
                    categories = categories, stats = stats, isLoading = false
                )
            } catch (e: Exception) {
                Log.e("Admin", "Ошибка: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "${e.javaClass.simpleName}: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun updateOrderStatus(id: Int, status: String) {
        viewModelScope.launch {
            try {
                api.updateOrderStatus(id, UpdateStatusRequest(status))
                loadAll()
            } catch (_: Exception) {}
        }
    }

    fun deleteOrder(id: Int) {
        viewModelScope.launch {
            try { api.deleteOrder(id); loadAll() } catch (_: Exception) {}
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            try { api.deleteProduct(id); loadAll() } catch (_: Exception) {}
        }
    }

    fun updateProduct(id: Int, req: CreateProductRequest) {
        viewModelScope.launch {
            try { api.updateProduct(id, req); loadAll() } catch (_: Exception) {}
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            try { api.deleteUser(id); loadAll() } catch (_: Exception) {}
        }
    }

    fun updateUserRole(id: Int, role: String) {
        viewModelScope.launch {
            try { api.updateUserRole(id, UpdateUserRoleRequest(role)); loadAll() } catch (_: Exception) {}
        }
    }

    fun updateCategory(id: Int, req: CreateCategoryRequest) {
        viewModelScope.launch {
            try { api.updateCategory(id, req); loadAll() } catch (_: Exception) {}
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            try { api.deleteCategory(id); loadAll() } catch (_: Exception) {}
        }
    }

    fun createProduct(req: CreateProductRequest) {
        viewModelScope.launch {
            try { api.createProduct(req); loadAll() } catch (_: Exception) {}
        }
    }

    fun createCategory(req: CreateCategoryRequest) {
        viewModelScope.launch {
            try { api.createCategory(req); loadAll() } catch (_: Exception) {}
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Экран заказов (админ)
// ═══════════════════════════════════════════════════════════════════

@Composable
fun AdminOrdersScreen(vm: AdminViewModel) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.loadAll() }

    if (state.isLoading) { LoadingBox(); return }
    if (state.error != null) { ErrorBox(state.error!!) { vm.loadAll() }; return }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Статистика
        state.stats?.let { s ->
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatMini("📦 ${s.orders}", "заказов", Modifier.weight(1f))
                    StatMini("💰 ${s.revenue}₽", "выручка", Modifier.weight(1f))
                    StatMini("👥 ${s.users}", "юзеров", Modifier.weight(1f))
                }
            }
        }

        if (state.orders.isEmpty()) {
            item { EmptyText("Нет заказов") }
        } else {
            items(state.orders) { order ->
                OrderAdminCard(order, vm)
            }
        }
    }
}

@Composable
private fun OrderAdminCard(order: Order, vm: AdminViewModel) {
    var statusExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AdminCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(order.no, fontWeight = FontWeight.W700, fontSize = 15.sp, color = NkLabel)
                order.userEmail?.let { Text(it, fontSize = 12.sp, color = NkLabel3) }
            }
            StatusBadge(order.status)
        }
        Spacer(Modifier.height(6.dp))
        order.items.forEach { item ->
            Text("${item.productName ?: "#${item.productId}"} × ${item.qty}", fontSize = 13.sp, color = NkLabel2)
        }
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = NkSep)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(order.createdAt.take(10), fontSize = 12.sp, color = NkLabel3)
            Text("${order.total} ₽", fontWeight = FontWeight.W700, fontSize = 16.sp, color = NkLabel)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                AdminBtn("Статус ▾") { statusExpanded = true }
                DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }, containerColor = NkCard2) {
                    listOf("new", "processing", "shipped", "delivered", "cancelled").forEach { s ->
                        DropdownMenuItem(
                            text = { Text(statusLabel(s), color = if (s == order.status) NkLabel3 else NkLabel) },
                            onClick = { statusExpanded = false; if (s != order.status) vm.updateOrderStatus(order.id, s) }
                        )
                    }
                }
            }
            AdminBtnDanger("Удалить") { showDeleteConfirm = true }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog("Удалить заказ ${order.no}?", onConfirm = { vm.deleteOrder(order.id); showDeleteConfirm = false }, onDismiss = { showDeleteConfirm = false })
    }
}

// ═══════════════════════════════════════════════════════════════════
// Экран товаров (админ)
// ═══════════════════════════════════════════════════════════════════

@Composable
fun AdminProductsScreen(vm: AdminViewModel) {
    val state by vm.state.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AdminBtn("+ Добавить товар") { showCreate = true }
        }
        item {
            Text("Всего: ${state.products.size}", fontSize = 13.sp, color = NkLabel3)
        }
        if (state.products.isEmpty()) {
            item { EmptyText("Нет товаров") }
        } else {
            items(state.products) { product ->
                ProductAdminCard(product, vm, state.categories)
            }
        }
    }

    if (showCreate) {
        CreateProductDialog(state.categories, onDismiss = { showCreate = false }) { req ->
            vm.createProduct(req); showCreate = false
        }
    }
}

@Composable
private fun ProductAdminCard(product: Product, vm: AdminViewModel, categories: List<Category>) {
    var showDelete by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    AdminCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.W600, fontSize = 15.sp, color = NkLabel)
                Text("${product.kcal} ккал · ${product.grams}г · ${product.price} ₽", fontSize = 13.sp, color = NkLabel2)
                product.categoryName?.let { Text(it, fontSize = 12.sp, color = NkLabel3) }
                if (product.tags.isNotEmpty()) {
                    Text(product.tags.joinToString(", "), fontSize = 11.sp, color = NkViolet)
                }
            }
            IconButton(onClick = { showEdit = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, null, tint = NkGreen, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { showDelete = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = NkRed, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDelete) {
        ConfirmDialog("Удалить «${product.name}»?", onConfirm = { vm.deleteProduct(product.id); showDelete = false }, onDismiss = { showDelete = false })
    }

    if (showEdit) {
        EditProductDialog(product, categories, onDismiss = { showEdit = false }) { req ->
            vm.updateProduct(product.id, req); showEdit = false
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Экран пользователей (админ)
// ═══════════════════════════════════════════════════════════════════

@Composable
fun AdminUsersScreen(vm: AdminViewModel) {
    val state by vm.state.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.users.isEmpty()) {
            item { EmptyText("Нет пользователей") }
        } else {
            items(state.users) { user ->
                UserAdminCard(user, vm)
            }
        }
    }
}

@Composable
private fun UserAdminCard(user: AdminUser, vm: AdminViewModel) {
    var showDelete by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }

    AdminCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(user.name ?: user.email, fontWeight = FontWeight.W600, fontSize = 15.sp, color = NkLabel)
                Text(user.email, fontSize = 12.sp, color = NkLabel3)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Бонусы: ${user.points}", fontSize = 12.sp, color = NkGreen)
                    Text("Заказов: ${user.orderCount}", fontSize = 12.sp, color = NkLabel2)
                }
            }
            StatusBadge(if (user.role == "admin") "admin" else "user")
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                AdminBtn(if (user.role == "admin") "Роль: Админ ▾" else "Роль: Юзер ▾") { roleExpanded = true }
                DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }, containerColor = NkCard2) {
                    listOf("user", "admin").forEach { r ->
                        DropdownMenuItem(
                            text = { Text(if (r == "admin") "Админ" else "Пользователь", color = NkLabel) },
                            onClick = { roleExpanded = false; if (r != user.role) vm.updateUserRole(user.id, r) }
                        )
                    }
                }
            }
            AdminBtnDanger("Удалить") { showDelete = true }
        }
    }

    if (showDelete) {
        ConfirmDialog("Удалить пользователя ${user.email}?", onConfirm = { vm.deleteUser(user.id); showDelete = false }, onDismiss = { showDelete = false })
    }
}

// ═══════════════════════════════════════════════════════════════════
// Экран категорий (админ)
// ═══════════════════════════════════════════════════════════════════

@Composable
fun AdminCategoriesScreen(vm: AdminViewModel) {
    val state by vm.state.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AdminBtn("+ Добавить категорию") { showCreate = true }
        }
        if (state.categories.isEmpty()) {
            item { EmptyText("Нет категорий") }
        } else {
            items(state.categories) { cat ->
                CategoryAdminCard(cat, vm)
            }
        }
    }

    if (showCreate) {
        CreateCategoryDialog(onDismiss = { showCreate = false }) { req ->
            vm.createCategory(req); showCreate = false
        }
    }
}

@Composable
private fun CategoryAdminCard(cat: Category, vm: AdminViewModel) {
    var showDelete by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    AdminCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(cat.glyph, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(cat.name, fontWeight = FontWeight.W600, fontSize = 15.sp, color = NkLabel)
                Text(cat.color, fontSize = 12.sp, color = NkLabel3)
            }
            IconButton(onClick = { showEdit = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, null, tint = NkGreen, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { showDelete = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = NkRed, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDelete) {
        ConfirmDialog("Удалить категорию «${cat.name}»?", onConfirm = { vm.deleteCategory(cat.id); showDelete = false }, onDismiss = { showDelete = false })
    }

    if (showEdit) {
        EditCategoryDialog(cat, onDismiss = { showEdit = false }) { req ->
            vm.updateCategory(cat.id, req); showEdit = false
        }
    }
}

@Composable
private fun EditCategoryDialog(cat: Category, onDismiss: () -> Unit, onSave: (CreateCategoryRequest) -> Unit) {
    var name by remember { mutableStateOf(cat.name) }
    var glyph by remember { mutableStateOf(cat.glyph) }
    var color by remember { mutableStateOf(cat.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NkBg2,
        title = { Text("Редактировать категорию", color = NkLabel) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField("Название", name) { name = it }
                DialogField("Эмодзи", glyph) { glyph = it }
                DialogField("Цвет (hex)", color) { color = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(CreateCategoryRequest(name, color, glyph))
            }) { Text("Сохранить", color = NkGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = NkLabel2) } }
    )
}

// ═══════════════════════════════════════════════════════════════════
// Диалоги создания
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun CreateProductDialog(categories: List<Category>, onDismiss: () -> Unit, onCreate: (CreateProductRequest) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    var grams by remember { mutableStateOf("") }
    var catId by remember { mutableIntStateOf(categories.firstOrNull()?.id ?: 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NkBg2,
        title = { Text("Новый товар", color = NkLabel) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField("Название", name) { name = it }
                DialogField("Цена, ₽", price) { price = it }
                DialogField("Ккал", kcal) { kcal = it }
                DialogField("Белки, г", protein) { protein = it }
                DialogField("Жиры, г", fat) { fat = it }
                DialogField("Углеводы, г", carb) { carb = it }
                DialogField("Вес, г", grams) { grams = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank() && price.isNotBlank()) {
                    onCreate(CreateProductRequest(
                        name = name, categoryId = catId,
                        price = price.toIntOrNull() ?: 0,
                        kcal = kcal.toIntOrNull() ?: 0,
                        protein = protein.toDoubleOrNull() ?: 0.0,
                        fat = fat.toDoubleOrNull() ?: 0.0,
                        carb = carb.toDoubleOrNull() ?: 0.0,
                        grams = grams.toIntOrNull() ?: 0
                    ))
                }
            }) { Text("Создать", color = NkGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = NkLabel2) } }
    )
}

@Composable
private fun EditProductDialog(product: Product, categories: List<Category>, onDismiss: () -> Unit, onSave: (CreateProductRequest) -> Unit) {
    var name by remember { mutableStateOf(product.name) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var kcal by remember { mutableStateOf(product.kcal.toString()) }
    var protein by remember { mutableStateOf(product.protein.toString()) }
    var fat by remember { mutableStateOf(product.fat.toString()) }
    var carb by remember { mutableStateOf(product.carb.toString()) }
    var grams by remember { mutableStateOf(product.grams.toString()) }
    var sostav by remember { mutableStateOf(product.sostav ?: "") }
    var benefit by remember { mutableStateOf(product.benefit ?: "") }
    var isHit by remember { mutableStateOf(product.isHit) }
    var isNovelty by remember { mutableStateOf(product.isNovelty) }
    var catId by remember { mutableIntStateOf(product.categoryId) }
    var catExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NkBg2,
        title = { Text("Редактировать товар", color = NkLabel) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialogField("Название", name) { name = it }
                DialogField("Цена, ₽", price) { price = it }
                DialogField("Ккал", kcal) { kcal = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { DialogField("Белки", protein) { protein = it } }
                    Box(Modifier.weight(1f)) { DialogField("Жиры", fat) { fat = it } }
                    Box(Modifier.weight(1f)) { DialogField("Углеводы", carb) { carb = it } }
                }
                DialogField("Вес, г", grams) { grams = it }
                DialogField("Состав", sostav) { sostav = it }
                DialogField("Польза", benefit) { benefit = it }

                // Категория — dropdown
                Text("Категория", fontSize = 13.sp, color = NkLabel2)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .border(1.dp, NkSep, RoundedCornerShape(11.dp))
                        .background(NkCard)
                        .clickable { catExpanded = true }
                        .padding(13.dp)
                ) {
                    Text(
                        categories.firstOrNull { it.id == catId }?.let { "${it.glyph} ${it.name}" } ?: "Выбрать",
                        color = NkLabel
                    )
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }, containerColor = NkCard2) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.glyph} ${c.name}", color = NkLabel) },
                                onClick = { catId = c.id; catExpanded = false }
                            )
                        }
                    }
                }

                // Чекбоксы
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isHit, onCheckedChange = { isHit = it }, colors = CheckboxDefaults.colors(checkedColor = NkGreen))
                    Text("Хит", color = NkLabel, fontSize = 14.sp)
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = isNovelty, onCheckedChange = { isNovelty = it }, colors = CheckboxDefaults.colors(checkedColor = NkViolet))
                    Text("Новинка", color = NkLabel, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(CreateProductRequest(
                        name = name, categoryId = catId,
                        price = price.toIntOrNull() ?: 0,
                        kcal = kcal.toIntOrNull() ?: 0,
                        protein = protein.toDoubleOrNull() ?: 0.0,
                        fat = fat.toDoubleOrNull() ?: 0.0,
                        carb = carb.toDoubleOrNull() ?: 0.0,
                        grams = grams.toIntOrNull() ?: 0,
                        sostav = sostav.ifBlank { null },
                        benefit = benefit.ifBlank { null },
                        isHit = isHit,
                        isNovelty = isNovelty,
                        imageUrl = product.imageUrl,
                        tags = product.tags
                    ))
                }
            }) { Text("Сохранить", color = NkGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = NkLabel2) } }
    )
}

@Composable
private fun CreateCategoryDialog(onDismiss: () -> Unit, onCreate: (CreateCategoryRequest) -> Unit) {
    var name by remember { mutableStateOf("") }
    var glyph by remember { mutableStateOf("🍬") }
    var color by remember { mutableStateOf("#FF6B6B") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NkBg2,
        title = { Text("Новая категория", color = NkLabel) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField("Название", name) { name = it }
                DialogField("Эмодзи", glyph) { glyph = it }
                DialogField("Цвет (hex)", color) { color = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onCreate(CreateCategoryRequest(name, color, glyph))
            }) { Text("Создать", color = NkGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = NkLabel2) } }
    )
}

@Composable
private fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NkBg2,
        text = { Text(text, color = NkLabel) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Да, удалить", color = NkRed) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = NkLabel2) } }
    )
}

// ═══════════════════════════════════════════════════════════════════
// Общие компоненты
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun AdminCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, NkSep, RoundedCornerShape(18.dp))
            .background(NkCard)
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun AdminBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NkGreen.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = FontWeight.W600, fontSize = 15.sp, color = NkGreen)
    }
}

@Composable
private fun AdminBtnDanger(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NkRed.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = FontWeight.W600, fontSize = 13.sp, color = NkRed)
    }
}

@Composable
private fun StatMini(value: String, label: String, modifier: Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(13.dp))
            .border(1.dp, NkSep, RoundedCornerShape(13.dp))
            .background(NkCard)
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, fontWeight = FontWeight.W700, fontSize = 15.sp, color = NkLabel)
            Text(label, fontSize = 11.sp, color = NkLabel3)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "new" -> NkBlue; "processing" -> NkOrange; "shipped" -> NkViolet
        "delivered" -> NkSystemGreen; "cancelled" -> NkRed; "admin" -> NkViolet
        else -> NkLabel2
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(statusLabel(status), fontSize = 12.sp, fontWeight = FontWeight.W700, color = color)
    }
}

@Composable
private fun DialogField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NkGreen,
            unfocusedBorderColor = NkSep,
            cursorColor = NkGreen,
            focusedTextColor = NkLabel,
            unfocusedTextColor = NkLabel
        )
    )
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = NkGreen)
    }
}

@Composable
private fun ErrorBox(error: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠️", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(error, color = NkRed, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            AdminBtn("Повторить") { onRetry() }
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 15.sp, color = NkLabel2)
    }
}

private fun statusLabel(status: String): String = when (status) {
    "new" -> "Новый"; "processing" -> "Сборка"; "shipped" -> "Отправлен"
    "delivered" -> "Доставлен"; "cancelled" -> "Отменён"
    "admin" -> "Админ"; "user" -> "Юзер"
    else -> status
}
