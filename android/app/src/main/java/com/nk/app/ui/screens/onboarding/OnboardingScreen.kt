package com.nk.app.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.repository.AuthRepository
import com.nk.app.domain.model.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val step: Int = 0, // 0=welcome, 1=form, 2=result
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val sex: String = "male",
    val age: String = "",
    val height: String = "",
    val weight: String = "",
    val activity: String = "moderate",
    val goal: String = "keep",
    val kcalNorm: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val done: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun update(fn: OnboardingState.() -> OnboardingState) {
        _state.value = _state.value.fn()
    }

    fun next() {
        val s = _state.value
        if (s.step < 1) {
            _state.value = s.copy(step = s.step + 1)
        } else {
            register()
        }
    }

    fun back() {
        val s = _state.value
        if (s.step > 0) _state.value = s.copy(step = s.step - 1)
    }

    private fun register() {
        val s = _state.value
        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            try {
                val resp = authRepo.register(RegisterRequest(
                    email = s.email,
                    password = s.password,
                    name = s.name.ifBlank { null },
                    sex = s.sex,
                    age = s.age.toIntOrNull(),
                    height = s.height.toIntOrNull(),
                    weight = s.weight.toIntOrNull(),
                    activity = s.activity,
                    goal = s.goal
                ))
                _state.value = s.copy(
                    step = 2,
                    kcalNorm = resp.user.kcalNorm,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = s.copy(error = e.message ?: "Ошибка регистрации", isLoading = false)
            }
        }
    }

    fun finish() {
        _state.value = _state.value.copy(done = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.done) {
        if (state.done) onComplete()
    }

    Scaffold(
        topBar = {
            if (state.step > 0 && state.step < 2) {
                TopAppBar(
                    title = { Text("Регистрация") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.back() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                        }
                    }
                )
            }
        }
    ) { padding ->
        when (state.step) {
            0 -> WelcomeStep(
                modifier = Modifier.padding(padding),
                onNext = { viewModel.next() }
            )
            1 -> FormStep(
                state = state,
                modifier = Modifier.padding(padding),
                onUpdate = { viewModel.update(it) },
                onSubmit = { viewModel.next() }
            )
            2 -> ResultStep(
                kcalNorm = state.kcalNorm ?: 2000,
                modifier = Modifier.padding(padding),
                onFinish = { viewModel.finish() }
            )
        }
    }
}

@Composable
fun WelcomeStep(modifier: Modifier, onNext: () -> Unit) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Невский Кондитер", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("ЗОЖ + сладости", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Text("Рассчитаем вашу норму калорий\nи подберём сладости под бюджет", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(48.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Начать")
        }
    }
}

@Composable
fun FormStep(
    state: OnboardingState,
    modifier: Modifier,
    onUpdate: (OnboardingState.() -> OnboardingState) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = state.email, onValueChange = { v -> onUpdate { copy(email = v) } },
            modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true
        )
        OutlinedTextField(
            value = state.password, onValueChange = { v -> onUpdate { copy(password = v) } },
            modifier = Modifier.fillMaxWidth(), label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(), singleLine = true
        )
        OutlinedTextField(
            value = state.name, onValueChange = { v -> onUpdate { copy(name = v) } },
            modifier = Modifier.fillMaxWidth(), label = { Text("Имя") }, singleLine = true
        )

        // Пол
        Text("Пол", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("male" to "Мужской", "female" to "Женский").forEach { (v, l) ->
                FilterChip(
                    selected = state.sex == v,
                    onClick = { onUpdate { copy(sex = v) } },
                    label = { Text(l) }
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.age, onValueChange = { v -> onUpdate { copy(age = v) } },
                modifier = Modifier.weight(1f), label = { Text("Возраст") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
            )
            OutlinedTextField(
                value = state.height, onValueChange = { v -> onUpdate { copy(height = v) } },
                modifier = Modifier.weight(1f), label = { Text("Рост, см") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
            )
            OutlinedTextField(
                value = state.weight, onValueChange = { v -> onUpdate { copy(weight = v) } },
                modifier = Modifier.weight(1f), label = { Text("Вес, кг") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
            )
        }

        // Активность
        Text("Активность", style = MaterialTheme.typography.labelLarge)
        val activities = listOf(
            "sedentary" to "Сидячий", "light" to "Лёгкая",
            "moderate" to "Средняя", "active" to "Высокая", "very_active" to "Очень высокая"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            activities.forEach { (v, l) ->
                FilterChip(
                    selected = state.activity == v,
                    onClick = { onUpdate { copy(activity = v) } },
                    label = { Text(l, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // Цель
        Text("Цель", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("lose" to "Похудеть", "keep" to "Поддержать", "gain" to "Набрать").forEach { (v, l) ->
                FilterChip(
                    selected = state.goal == v,
                    onClick = { onUpdate { copy(goal = v) } },
                    label = { Text(l) }
                )
            }
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && state.email.isNotBlank() && state.password.length >= 6
        ) {
            if (state.isLoading) CircularProgressIndicator(Modifier.size(18.dp))
            else Text("Рассчитать КБЖУ")
        }
    }
}

@Composable
fun ResultStep(kcalNorm: Int, modifier: Modifier, onFinish: () -> Unit) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ваша норма", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "$kcalNorm",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text("ккал / день", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Card {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Сладкий бюджет", style = MaterialTheme.typography.titleSmall)
                Text(
                    "≈ ${kcalNorm * 20 / 100} ккал",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text("20% от дневной нормы", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "200 приветственных бонусов начислено!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
            Text("К каталогу")
        }
    }
}
