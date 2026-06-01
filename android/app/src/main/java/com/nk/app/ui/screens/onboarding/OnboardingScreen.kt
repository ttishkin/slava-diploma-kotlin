package com.nk.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.local.TokenStore
import com.nk.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class OnboardingState(
    val step: Int = 0,
    val sex: String = "m",
    val age: Int = 25,
    val height: Int = 175,
    val weight: Int = 70,
    val activity: Double = 1.375,
    val goal: String = "keep",
    val kcalNorm: Int = 0,
    val proteinG: Int = 0,
    val fatG: Int = 0,
    val carbG: Int = 0,
    val done: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val tokenStore: TokenStore
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun setSex(v: String) { _state.value = _state.value.copy(sex = v) }
    fun setAge(v: Int) { _state.value = _state.value.copy(age = v.coerceIn(10, 100)) }
    fun setHeight(v: Int) { _state.value = _state.value.copy(height = v.coerceIn(120, 220)) }
    fun setWeight(v: Int) { _state.value = _state.value.copy(weight = v.coerceIn(30, 250)) }
    fun setActivity(v: Double) { _state.value = _state.value.copy(activity = v) }
    fun setGoal(v: String) { _state.value = _state.value.copy(goal = v) }

    fun next() {
        if (_state.value.step == 0) _state.value = _state.value.copy(step = 1)
    }

    fun back() {
        if (_state.value.step > 0) _state.value = _state.value.copy(step = _state.value.step - 1)
    }

    fun calculate() {
        val s = _state.value
        val bmr = 10.0 * s.weight + 6.25 * s.height - 5.0 * s.age + if (s.sex == "m") 5 else -161
        var tdee = bmr * s.activity
        when (s.goal) {
            "lose" -> tdee *= 0.85
            "gain" -> tdee *= 1.12
        }
        val kcal = (tdee / 10).roundToInt() * 10
        _state.value = s.copy(
            step = 2,
            kcalNorm = kcal,
            proteinG = (kcal * 0.25 / 4).roundToInt(),
            fatG = (kcal * 0.30 / 9).roundToInt(),
            carbG = (kcal * 0.45 / 4).roundToInt()
        )
    }

    fun finish() {
        viewModelScope.launch {
            tokenStore.setOnboarded()
            _state.value = _state.value.copy(done = true)
        }
    }

    fun skip() {
        viewModelScope.launch {
            tokenStore.setOnboarded()
            _state.value = _state.value.copy(done = true)
        }
    }
}

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.done) { if (state.done) onComplete() }

    when (state.step) {
        0 -> WelcomeStep(onNext = { viewModel.next() }, onSkip = { viewModel.skip() })
        1 -> FormStep(state = state, viewModel = viewModel)
        2 -> ResultStep(state = state, onFinish = { viewModel.finish() })
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шаг 0 — Welcome (градиент, бренд)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun WelcomeStep(onNext: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF3A2A78), Color(0xFF141019))))
    ) {
        // Hero (60%)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Бейдж бренда
            Box(
                modifier = Modifier
                    .size(122.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("НК", fontSize = 50.sp, fontWeight = FontWeight.W800, color = Color(0xFF5B3FA0), letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(22.dp))
            Text("Невский Кондитер", fontSize = 27.sp, fontWeight = FontWeight.W800, color = Color.White, letterSpacing = 0.4.sp)
            Spacer(Modifier.height(9.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NkGreen)
                    .padding(horizontal = 13.dp, vertical = 5.dp)
            ) {
                Text("ЗОЖ · правильное питание", fontSize = 12.sp, fontWeight = FontWeight.W800, color = NkGreenDark, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "«Потому что хочется сладкого!»",
                fontSize = 17.sp, color = Color.White.copy(alpha = 0.92f),
                fontStyle = FontStyle.Italic, textAlign = TextAlign.Center
            )
        }

        // Body — кнопки
        Column(Modifier.padding(22.dp)) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NkGreen, contentColor = NkGreenDark),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(15.dp)
            ) {
                Text("Начать", fontWeight = FontWeight.W700, fontSize = 17.sp)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("Пропустить", color = NkGreen, fontWeight = FontWeight.W500, fontSize = 17.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шаг 1 — Форма (пол, возраст, рост, вес, активность, цель)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FormStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NkBg)
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Пилл «Шаг 1 из 2» — фиолетовый как в PWA
        Box(
            Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(NkViolet.copy(alpha = 0.12f))
                .padding(horizontal = 13.dp, vertical = 6.dp)
        ) {
            Text("Шаг 1 из 2", fontSize = 13.sp, fontWeight = FontWeight.W700, color = NkViolet)
        }

        Spacer(Modifier.height(6.dp))
        Text("Расскажите о себе", fontSize = 24.sp, fontWeight = FontWeight.W800, letterSpacing = (-0.4).sp)

        // Пол — сегментированный контрол
        FieldLabel("Пол")
        SegControl(
            items = listOf("m" to "Мужской", "f" to "Женский"),
            selected = state.sex,
            onSelect = { viewModel.setSex(it) }
        )

        // Возраст
        FieldLabel("Возраст")
        OnbInput(state.age.toString()) { viewModel.setAge(it.toIntOrNull() ?: 25) }

        // Рост
        FieldLabel("Рост, см")
        OnbInput(state.height.toString()) { viewModel.setHeight(it.toIntOrNull() ?: 175) }

        // Вес
        FieldLabel("Вес, кг")
        OnbInput(state.weight.toString()) { viewModel.setWeight(it.toIntOrNull() ?: 70) }

        // Активность — dropdown (select) в стиле PWA
        FieldLabel("Активность")
        ActivityDropdown(state.activity) { viewModel.setActivity(it) }

        // Цель — сегментированный контрол
        FieldLabel("Цель")
        SegControl(
            items = listOf("lose" to "Похудеть", "keep" to "Поддержать", "gain" to "Набрать"),
            selected = state.goal,
            onSelect = { viewModel.setGoal(it) }
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.calculate() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NkGreen, contentColor = NkGreenDark),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(15.dp)
        ) {
            Text("Рассчитать норму", fontWeight = FontWeight.W700, fontSize = 17.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шаг 2 — Результат (верх = градиент-герой, низ = карточка макросов)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ResultStep(state: OnboardingState, onFinish: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        // Hero секция — градиент, 48% высоты
        Column(
            modifier = Modifier
                .weight(0.48f)
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF3A2A78), Color(0xFF141019))))
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎯", fontSize = 64.sp)
            Spacer(Modifier.height(18.dp))
            Text(
                "${state.kcalNorm} ккал/день",
                fontSize = 30.sp, fontWeight = FontWeight.W800, color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Ваша ориентировочная суточная\nнорма по формуле Миффлина–Сан Жеора.",
                fontSize = 16.sp, color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center, lineHeight = 22.sp
            )
        }

        // Body секция — фон приложения
        Column(
            modifier = Modifier
                .weight(0.52f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(22.dp)
        ) {
            // Карточка макросов — listcard стиль из PWA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, NkSep, RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    MacroRow("Белки", "${state.proteinG} г")
                    HorizontalDivider(color = NkSep)
                    MacroRow("Жиры", "${state.fatG} г")
                    HorizontalDivider(color = NkSep)
                    MacroRow("Углеводы", "${state.carbG} г")
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "Норму можно изменить вручную в профиле в любой момент.",
                fontSize = 13.sp, color = NkLabel2
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NkGreen, contentColor = NkGreenDark),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(15.dp)
            ) {
                Text("Перейти в приложение", fontWeight = FontWeight.W700, fontSize = 17.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Компоненты
// ═══════════════════════════════════════════════════════════════════

/** Лейбл поля — как .field label в PWA */
@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 13.sp, color = NkLabel2, fontWeight = FontWeight.W500, modifier = Modifier.padding(top = 4.dp, bottom = 6.dp))
}

/** Инпут — стиль PWA: border 1px --sep, bg --card, border-radius 11px, padding 13px, font 17px */
@Composable
private fun OnbInput(value: String, onChange: (String) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .border(1.dp, NkSep, RoundedCornerShape(11.dp))
            .background(NkCard)
    ) {
        TextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = NkGreen,
                focusedTextColor = NkLabel,
                unfocusedTextColor = NkLabel
            )
        )
    }
}

/**
 * Сегментированный контрол — как .seg в PWA:
 * bg rgba(255,255,255,.07), border-radius 10px, padding 3px
 * Active button: bg --card2, font-weight 700
 */
@Composable
private fun SegControl(items: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items.forEach { (value, label) ->
            val isOn = selected == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isOn) NkCard2 else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 15.sp,
                    fontWeight = if (isOn) FontWeight.W700 else FontWeight.W500,
                    color = if (isOn) Color.White else NkLabel
                )
            }
        }
    }
}

/**
 * Dropdown активности — стиль PWA select: border --sep, bg --card, radius 11px
 */
@Composable
private fun ActivityDropdown(current: Double, onSelect: (Double) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        1.2 to "Минимальная (сидячий образ)",
        1.375 to "Лёгкая (1–3 трен/нед)",
        1.55 to "Средняя (3–5 трен/нед)",
        1.725 to "Высокая (6–7 трен/нед)"
    )
    val currentLabel = options.firstOrNull { it.first == current }?.second ?: ""

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .border(1.dp, NkSep, RoundedCornerShape(11.dp))
            .background(NkCard)
            .clickable { expanded = true }
            .padding(13.dp)
    ) {
        Text(currentLabel, fontSize = 17.sp, color = NkLabel)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = NkCard2
        ) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label, fontSize = 15.sp, color = NkLabel) },
                    onClick = { onSelect(value); expanded = false }
                )
            }
        }
    }
}

/**
 * Строка макронутриента — как row2 в PWA (.row стиль: flex, padding 12px 14px)
 */
@Composable
private fun MacroRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, color = NkLabel2)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.W600, color = NkLabel)
    }
}
