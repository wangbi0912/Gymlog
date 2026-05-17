package com.gymlog.app.presentation.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymlog.app.domain.model.ReviewStatus
import com.gymlog.app.presentation.components.*
import com.gymlog.app.presentation.theme.*
import com.gymlog.app.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onStartSession: () -> Unit,
    onViewReview: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBodyData: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToExerciseDetail: (String) -> Unit
) {
    val data by viewModel.data.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GymLog", fontWeight = FontWeight.Light) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Text("设置", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            LoadingState()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PageHorizontal)
        ) {
            Spacer(Modifier.height(SpacingMd))

            // Monthly count + streak
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("本月训练", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${data.monthlySessionCount} 次",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Light
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("连续打卡", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${data.streakDays} 天",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(Modifier.height(SpacingLg))

            // Weekly summary
            GymLogCard {
                Text("本周", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(SpacingSm))
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingXl)) {
                    Column {
                        Text("${data.weeklySessions}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                        Text("次训练", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column {
                        Text("${"%.0f".format(data.weeklyVolumeKg)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                        Text("kg 容量", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column {
                        Text("${data.weeklySets}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                        Text("组", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(SpacingMd))

            // Latest review
            data.latestReview?.let { review ->
                GymLogCard(onClick = { onViewReview(review.sessionId) }) {
                    Text("最新审查", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(SpacingSm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        review.score?.let { score ->
                            Text(
                                "$score",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Light,
                                color = when {
                                    score >= 80 -> AccentSage
                                    score >= 60 -> AccentWarm
                                    else -> AccentWarmDeep
                                }
                            )
                        }
                        Spacer(Modifier.width(SpacingMd))
                        Column {
                            Text(
                                if (review.score != null && review.score >= 70) "Approved" else "Needs Attention",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            review.overallComment?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(SpacingMd))
            }

            // Body data
            data.latestWeight?.let { weight ->
                GymLogCard(onClick = onNavigateToBodyData) {
                    Text("身体数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(SpacingSm))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${weight.weightKg}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                        Text(" kg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        data.weightChange7d?.let { change ->
                            Spacer(Modifier.width(SpacingMd))
                            val prefix = if (change <= 0) "↓" else "↑"
                            Text(
                                "$prefix${"%.1f".format(kotlin.math.abs(change))}kg (7天)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(SpacingMd))
            }

            // Navigation cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                GymLogCard(modifier = Modifier.weight(1f), onClick = onNavigateToHistory) {
                    Text("历史", style = MaterialTheme.typography.bodyMedium)
                    Text("查看训练记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                GymLogCard(modifier = Modifier.weight(1f), onClick = onNavigateToTemplates) {
                    Text("模板", style = MaterialTheme.typography.bodyMedium)
                    Text("管理训练模板", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(SpacingXxl))
            Spacer(Modifier.height(SpacingXxl))
        }

        // FAB
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.BottomCenter) {
            GymLogButton(
                text = "开始训练",
                onClick = onStartSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PageHorizontal)
                    .padding(bottom = SpacingXl)
            )
        }
    }
}
