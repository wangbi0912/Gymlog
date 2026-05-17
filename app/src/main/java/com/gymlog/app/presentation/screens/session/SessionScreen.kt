package com.gymlog.app.presentation.screens.session

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.ExerciseRepository
import com.gymlog.app.presentation.components.*
import com.gymlog.app.presentation.theme.*
import com.gymlog.app.presentation.viewmodel.SessionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    viewModel: SessionViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Check if there's an in-progress session
    LaunchedEffect(Unit) {
        // Start new if no active session
        if (!state.isActive && !state.savedSuccessfully) {
            viewModel.startNewSession()
        }
    }

    if (state.savedSuccessfully) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            if (state.isActive) {
                Column {
                    // Rest timer bar
                    if (state.isRestTimerActive) {
                        RestTimerBar(
                            remaining = state.restSecondsRemaining,
                            total = state.restTotalSeconds,
                            onSkip = { viewModel.skipRest() },
                            onAddTime = { viewModel.addRestTime(15) }
                        )
                    }
                    // Top info bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = PageHorizontal, vertical = SpacingSm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onNavigateBack) {
                            Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            formatTime(state.elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            "${"%.0f".format(state.totalVolumeKg)}kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (!state.isActive && !state.savedSuccessfully) {
            LoadingState()
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Exercise list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = PageHorizontal, vertical = SpacingMd),
                verticalArrangement = Arrangement.spacedBy(SpacingMd)
            ) {
                itemsIndexed(state.exercises, key = { _, ex -> ex.id }) { _, exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        isExpanded = exercise.isExpanded,
                        onToggleExpand = { viewModel.toggleExerciseExpanded(exercise.id) },
                        onAddSet = {
                            viewModel.showSetInput(exercise.id, exercise.lastWeight, exercise.lastReps)
                        },
                        onRemove = { viewModel.removeExercise(exercise.id) }
                    )
                }

                item {
                    GymLogSecondaryButton(
                        text = "+ 添加动作",
                        onClick = { viewModel.setShowExercisePicker(true) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(80.dp))
                }
            }

            // Bottom bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                GymLogButton(
                    text = "完成训练",
                    onClick = { viewModel.showCompleteDialog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PageHorizontal, vertical = SpacingMd),
                    enabled = state.exercises.isNotEmpty()
                )
            }
        }
    }

    // Exercise picker dialog
    if (state.showExercisePicker) {
        ExercisePickerDialog(
            onDismiss = { viewModel.setShowExercisePicker(false) },
            onSelect = { exercise ->
                viewModel.addExercise(exercise)
                viewModel.setShowExercisePicker(false)
            }
        )
    }

    // Set input dialog
    if (state.showSetInput) {
        SetInputDialog(
            state = state,
            viewModel = viewModel
        )
    }

    // Complete dialog
    if (state.showCompleteDialog) {
        CompleteDialog(
            state = state,
            viewModel = viewModel
        )
    }

    // RPE Guide
    if (state.showRpeGuide) {
        RpeGuideDialog(onDismiss = { viewModel.setShowRpeGuide(false) })
    }
}

@Composable
private fun RestTimerBar(remaining: Int, total: Int, onSkip: () -> Unit, onAddTime: () -> Unit) {
    val progress = if (total > 0) remaining.toFloat() / total else 0f
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = PageHorizontal, vertical = SpacingSm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("休息", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(SpacingSm))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(4.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    trackColor = MaterialTheme.colorScheme.outline,
                )
                Spacer(Modifier.width(SpacingSm))
                Text("${remaining}s", style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onSkip) { Text("跳过") }
                TextButton(onClick = onAddTime) { Text("+15s") }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: com.gymlog.app.presentation.viewmodel.SessionExerciseUi,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddSet: () -> Unit,
    onRemove: () -> Unit
) {
    GymLogCard {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.exerciseName, style = MaterialTheme.typography.bodyLarge)
                if (exercise.lastWeight != null && exercise.lastReps != null) {
                    Text(
                        "上次: ${"%.0f".format(exercise.lastWeight)}kg × ${exercise.lastReps}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "${exercise.sets.size}组",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isExpanded && exercise.sets.isNotEmpty()) {
            Spacer(Modifier.height(SpacingSm))
            GymLogDivider()
            Spacer(Modifier.height(SpacingSm))
            exercise.sets.forEach { set ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        set.setType.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "#${set.setNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        set.weightKg?.let { "${"%.0f".format(it)}kg" } ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(56.dp)
                    )
                    Text(
                        "× ${set.reps ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(40.dp)
                    )
                    set.rpe?.let { rpe ->
                        Text(
                            "RPE $rpe",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(SpacingSm))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            GymLogSecondaryButton(text = "+ 添加组", onClick = onAddSet, modifier = Modifier.height(36.dp))
            TextButton(onClick = onRemove) {
                Text("删除", color = AccentWarm, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SetInputDialog(
    state: com.gymlog.app.presentation.viewmodel.SessionUiState,
    viewModel: SessionViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.hideSetInput() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        title = {
            Text("添加组", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                // Set type chips
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
                    SetType.entries.forEach { type ->
                        FilterChip(
                            selected = state.setType == type,
                            onClick = { viewModel.setSetType(type) },
                            label = { Text(type.shortLabel, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    GymLogTextField(
                        value = state.setWeight,
                        onValueChange = { viewModel.setSetWeight(it) },
                        placeholder = "重量",
                        modifier = Modifier.weight(1f)
                    )
                    GymLogTextField(
                        value = state.setReps,
                        onValueChange = { viewModel.setSetReps(it) },
                        placeholder = "次数",
                        modifier = Modifier.weight(1f)
                    )
                }

                // RPE selector
                if (state.setType == SetType.WORKING || state.setType == SetType.FAILURE) {
                    Text("RPE (可选)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (1..10).forEach { rpe ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (state.setRpe == rpe) MaterialTheme.colorScheme.onBackground
                                        else MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { viewModel.setSetRpe(if (state.setRpe == rpe) null else rpe) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$rpe",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.setRpe == rpe) MaterialTheme.colorScheme.background
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    TextButton(onClick = { viewModel.setShowRpeGuide(true) }) {
                        Text("RPE 指南", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            GymLogButton("确认", onClick = { viewModel.addSet() })
        },
        dismissButton = {
            TextButton(onClick = { viewModel.hideSetInput() }) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun CompleteDialog(
    state: com.gymlog.app.presentation.viewmodel.SessionUiState,
    viewModel: SessionViewModel
) {
    var submitReview by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = { viewModel.hideCompleteDialog() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        title = { Text("完成训练", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                Text("时长: ${formatTime(state.elapsedSeconds)}", style = MaterialTheme.typography.bodyMedium)
                Text("容量: ${"%.0f".format(state.totalVolumeKg)}kg", style = MaterialTheme.typography.bodyMedium)
                Text("动作: ${state.exercises.size} 个", style = MaterialTheme.typography.bodyMedium)
                Text("总组数: ${state.exercises.sumOf { it.sets.size }}", style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(SpacingSm))
                GymLogTextField(
                    value = state.note,
                    onValueChange = { viewModel.setNote(it) },
                    placeholder = "训练备注（可选）"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = submitReview,
                        onCheckedChange = {
                            submitReview = it
                            viewModel.setSubmitForReview(it)
                        }
                    )
                    Text("提交AI审查", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            GymLogButton("保存", onClick = { viewModel.completeSession() })
        },
        dismissButton = {
            TextButton(onClick = { viewModel.hideCompleteDialog() }) {
                Text("继续训练")
            }
        }
    )
}

@Composable
private fun RpeGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        title = { Text("RPE 指南", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                listOf(
                    "1-2" to "非常轻松，可做 10+ 次",
                    "3-4" to "轻松，可做 6-8 次",
                    "5-6" to "中等，可做 4-6 次",
                    "7-8" to "困难，可做 2-4 次",
                    "9" to "非常困难，可做 1 次",
                    "10" to "极限，无法再多做一次"
                ).forEach { (level, desc) ->
                    Row {
                        Text(level, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp))
                        Text(desc, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("知道了") }
        }
    )
}

@Composable
private fun ExercisePickerDialog(
    onDismiss: () -> Unit,
    onSelect: (com.gymlog.app.domain.model.Exercise) -> Unit
) {
    val viewModel: com.gymlog.app.presentation.viewmodel.ExercisePickerViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
    val exercises by viewModel.allExercises.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        title = {
            Column {
                Text("选择动作", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(SpacingSm))
                GymLogTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "搜索动作..."
                )
            }
        },
        text = {
            val filtered = if (searchQuery.isBlank()) {
                exercises.groupBy { it.bodyPart }
            } else {
                exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    .groupBy { it.bodyPart }
            }

            if (filtered.isEmpty()) {
                Text("无匹配动作", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    filtered.forEach { (bodyPart, exList) ->
                        Text(
                            bodyPart.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = SpacingSm)
                        )
                        exList.forEach { ex ->
                            TextButton(
                                onClick = { onSelect(ex) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(ex.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
