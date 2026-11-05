package com.example.fincalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    primary = Color(0xFF4CAF50),      // Мягкий зеленый акцент
                    onPrimary = Color.Black
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Калькулятор", "План цели")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        Text(
            text = "Капитал",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Минималистичный переключатель вкладок
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Отображение выбранного экрана
        if (selectedTab == 0) {
            CalculatorTab()
        } else {
            GoalPlannerTab()
        }
    }
}

// ------------------- ВКЛАДКА 1: КАЛЬКУЛЯТОР -------------------
@Composable
fun CalculatorTab() {
    var initialText by remember { mutableStateOf("100000") }
    var monthlyText by remember { mutableStateOf("10000") }
    var rateText by remember { mutableStateOf("16") }
    var yearsText by remember { mutableStateOf("3") }

    val initial = initialText.toDoubleOrNull() ?: 0.0
    val monthly = monthlyText.toDoubleOrNull() ?: 0.0
    val rate = rateText.toDoubleOrNull() ?: 0.0
    val years = yearsText.toIntOrNull() ?: 0

    val months = years * 12
    val monthlyRate = (rate / 100.0) / 12.0

    val totalAmount = if (monthlyRate > 0) {
        val compoundInitial = initial * (1 + monthlyRate).pow(months)
        val compoundMonthly = monthly * (((1 + monthlyRate).pow(months) - 1) / monthlyRate)
        compoundInitial + compoundMonthly
    } else {
        initial + (monthly * months)
    }

    val totalInvested = initial + (monthly * months)
    val totalInterest = (totalAmount - totalInvested).coerceAtLeast(0.0)

    val formatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
        maximumFractionDigits = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Карточка результата
        ResultCard(
            mainTitle = "Итоговый капитал",
            mainValue = formatter.format(totalAmount),
            leftTitle = "Вложено своих",
            leftValue = formatter.format(totalInvested),
            rightTitle = "Начислено %",
            rightValue = "+${formatter.format(totalInterest)}"
        )

        InputField(value = initialText, onValueChange = { initialText = it }, label = "Стартовый капитал (₽)")
        InputField(value = monthlyText, onValueChange = { monthlyText = it }, label = "Пополнение в месяц (₽)")
        InputField(value = rateText, onValueChange = { rateText = it }, label = "Ставка (% годовых)")
        InputField(value = yearsText, onValueChange = { yearsText = it }, label = "Срок (лет)")

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ------------------- ВКЛАДКА 2: ПЛАНИРОВЩИК ЦЕЛИ -------------------
@Composable
fun GoalPlannerTab() {
    var targetText by remember { mutableStateOf("1000000") }
    var currentText by remember { mutableStateOf("50000") }
    var rateText by remember { mutableStateOf("16") }
    var yearsText by remember { mutableStateOf("2") }

    val target = targetText.toDoubleOrNull() ?: 0.0
    val current = currentText.toDoubleOrNull() ?: 0.0
    val rate = rateText.toDoubleOrNull() ?: 0.0
    val years = yearsText.toIntOrNull() ?: 0

    val months = years * 12
    val monthlyRate = (rate / 100.0) / 12.0

    // Формула аннуитета для нахождения ежемесячного пополнения (PMT)
    val monthlyDepositNeeded = when {
        months == 0 -> 0.0
        monthlyRate <= 0.0 -> ((target - current) / months).coerceAtLeast(0.0)
        else -> {
            val futureValueOfCurrent = current * (1 + monthlyRate).pow(months)
            val neededFromContributions = target - futureValueOfCurrent
            if (neededFromContributions <= 0) {
                0.0 // Уже достаточно текущих денег под этот процент
            } else {
                neededFromContributions / (((1 + monthlyRate).pow(months) - 1) / monthlyRate)
            }
        }
    }

    val totalInvested = current + (monthlyDepositNeeded * months)
    val totalInterest = (target - totalInvested).coerceAtLeast(0.0)

    val formatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
        maximumFractionDigits = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Карточка результата
        ResultCard(
            mainTitle = "Нужно откладывать в месяц",
            mainValue = formatter.format(monthlyDepositNeeded),
            leftTitle = "Вложишь всего",
            leftValue = formatter.format(totalInvested),
            rightTitle = "Помогут %",
            rightValue = "+${formatter.format(totalInterest)}"
        )

        InputField(value = targetText, onValueChange = { targetText = it }, label = "Цель накопления (₽)")
        InputField(value = currentText, onValueChange = { currentText = it }, label = "Уже накоплено сейчас (₽)")
        InputField(value = rateText, onValueChange = { rateText = it }, label = "Ожидаемая ставка (% годовых)")
        InputField(value = yearsText, onValueChange = { yearsText = it }, label = "За сколько лет накопить")

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ------------------- ОБЩИЕ ЭЛЕМЕНТЫ ДИЗАЙНА -------------------
@Composable
fun ResultCard(
    mainTitle: String,
    mainValue: String,
    leftTitle: String,
    leftValue: String,
    rightTitle: String,
    rightValue: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(mainTitle, color = Color.Gray, fontSize = 14.sp)
            Text(
                text = mainValue,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(leftTitle, color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text = leftValue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.LightGray
                    )
                }
                Column {
                    Text(rightTitle, color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text = rightValue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() }) onValueChange(newValue)
        },
        label = { Text(label, color = Color.Gray) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFF2C2C2C)
        )
    )
}