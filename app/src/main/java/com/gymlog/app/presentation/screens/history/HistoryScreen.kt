package com.gymlog.app.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gymlog.app.domain.model.*
import com.gymlog.app.presentation.components.*
import com.gymlog.app.presentation.theme.*
import com.gymlog.app.presentation.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史", fontWeight = FontWeight.Light) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (state.loading) {
            LoadingState()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = PageHorizontal)
        ) {
            Spacer(Modifier.height(SpacingSm))

            // Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    val newMonth = if (state.selectedMonth == 0) 11 else state.selectedMonth - 1
                    val newYear = if (state.selectedMonth == 0) state.selectedYear - 1 else state.selectedYear
                    viewModel.loadMonth(newYear, newMonth)
                }) { Text("<") }

                Text(
                    "${state.selectedYear}年 ${state.selectedMonth + 1}月",
                    style = MaterialTheme.typography.titleMedium
                )

                TextButton(onClick = {
                    val newMonth = if (state.selectedMonth == 11) 0 else state.selectedMonth + 1
                    val newYear = if (state.selectedMonth == 11) state.selectedYear + 1 else state.selectedYear
                    viewModel.loadMonth(newYear, newMonth)
                }) { Text(">") }
            }

            Spacer(Modifier.height(SpacingMd))

            // Calendar grid
            CalendarGrid(
                year = state.selectedYear,
                month = state.selectedMonth,
                trainingDays = state.trainingDays,
                dayVolumes = state.dayVolumes,
                maxDayVolume = state.maxDayVolume,
                selectedDay = state.selectedDay,
                onDayClick = { viewModel.selectDay(it) }
            )

            Spacer(Modifier.height(SpacingMd))
            GymLogDivider()
            Spacer(Modifier.height(SpacingMd))

            // Sessions for selected day or all
            LazyColumn(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                val sessions = if (state.selectedDay != null) {
                    viewModel.getSessionsForDay(state.selectedDay!!)
                } else {
                    state.filteredSessions.ifEmpty { state.sessions }
                }

                if (sessions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = SpacingXxl)) {
                            Text(
                                "暂无记录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                items(sessions, key = { it.id }) { session ->
                    SessionHistoryCard(session)
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int, month: Int,
    trainingDays: Set<Int>,
    dayVolumes: Map<Int, Float>,
    maxDayVolume: Float,
    selectedDay: Int?,
    onDayClick: (Int) -> Unit
) {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val dayHeaders = listOf("一", "二", "三", "四", "五", "六", "日")

    Column {
        // Day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { day ->
                Text(
                    day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(SpacingSm))

        // Calendar days
        var dayCounter = 1
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        val totalCells = offset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    if (cellIndex < offset || cellIndex - offset >= daysInMonth) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val day = cellIndex - offset + 1
                        val isTrainingDay = trainingDays.contains(day)
                        val volumeRatio = (dayVolumes[day] ?: 0f) / maxDayVolume
                        val isSelected = selectedDay == day

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .then(
                                    if (isTrainingDay) Modifier.background(
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f + volumeRatio * 0.5f),
                                        RoundedCornerShape(4.dp)
                                    ) else Modifier
                                )
                                .then(
                                    if (isSelected) Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    ) else Modifier
                                )
                                .clickable { if (isTrainingDay) onDayClick(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$day",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTrainingDay) MaterialTheme.colorScheme.onBackground
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(session: TrainingSession) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    GymLogCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(session.bodyPart.label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    dateFormat.format(Date(session.startTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${"%.0f".format(session.totalVolumeKg)}kg",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${session.durationMin}分钟 · ${session.workingSetCount}组",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (session.exercises.isNotEmpty()) {
            Spacer(Modifier.height(SpacingSm))
            Text(
                session.exercises.take(3).joinToString(" / ") { it.exerciseName },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
