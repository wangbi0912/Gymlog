package com.gymlog.app.presentation.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymlog.app.domain.model.*
import com.gymlog.app.presentation.components.*
import com.gymlog.app.presentation.theme.*
import com.gymlog.app.presentation.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    sessionId: String,
    viewModel: ReviewViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val review by viewModel.review.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadReview(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 审查", fontWeight = FontWeight.Light) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            loading -> LoadingState()
            review == null && !loading -> EmptyState("暂无审查数据")
            review != null -> {
                val r = review!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = PageHorizontal)
                ) {
                    Spacer(Modifier.height(SpacingXl))

                    // Score
                    r.score?.let { score ->
                        Text(
                            "$score",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 96.sp),
                            fontWeight = FontWeight.Light,
                            color = when {
                                score >= 80 -> AccentSage
                                score >= 60 -> AccentWarm
                                else -> AccentWarmDeep
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(SpacingSm))
                        Text(
                            if (score >= 70) "Approved" else "Needs Attention",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(SpacingXl))
                    }

                    // Overall comment
                    r.overallComment?.let { comment ->
                        Row {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(60.dp)
                                    .background(AccentSage)
                            )
                            Spacer(Modifier.width(SpacingMd))
                            Text(comment, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(SpacingXl))
                    }

                    // Items by category
                    val strengths = r.items.filter { it.category == ReviewCategory.STRENGTH }
                    val issues = r.items.filter { it.category == ReviewCategory.ISSUE }
                    val suggestions = r.items.filter { it.category == ReviewCategory.SUGGESTION }

                    if (strengths.isNotEmpty()) {
                        ReviewSection("亮点", strengths, viewModel)
                        Spacer(Modifier.height(SpacingMd))
                    }
                    if (issues.isNotEmpty()) {
                        ReviewSection("需关注", issues, viewModel)
                        Spacer(Modifier.height(SpacingMd))
                    }
                    if (suggestions.isNotEmpty()) {
                        ReviewSection("建议", suggestions, viewModel)
                        Spacer(Modifier.height(SpacingMd))
                    }

                    // Tags
                    if (r.tags.isNotEmpty()) {
                        Text("标签", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(SpacingSm))
                        Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
                            r.tags.forEach { tag ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 0.dp
                                ) {
                                    Text(
                                        tag,
                                        modifier = Modifier.padding(horizontal = SpacingSm, vertical = SpacingXs),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    // Status info
                    Spacer(Modifier.height(SpacingXl))
                    Text(
                        when (r.status) {
                            ReviewStatus.PENDING -> "状态: 等待提交"
                            ReviewStatus.QUEUED -> "状态: 队列中"
                            ReviewStatus.REVIEWING -> "状态: 审查中..."
                            ReviewStatus.COMPLETED -> "状态: 已完成"
                            ReviewStatus.FAILED -> "状态: 失败 - ${r.errorMessage ?: "未知错误"}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (r.status == ReviewStatus.FAILED) {
                        Spacer(Modifier.height(SpacingMd))
                        GymLogButton("重试", onClick = { viewModel.executeReview(r.id) })
                    }

                    if (r.status == ReviewStatus.PENDING) {
                        Spacer(Modifier.height(SpacingMd))
                        GymLogButton("提交审查", onClick = { viewModel.submitForReview(r.sessionId) })
                    }

                    Spacer(Modifier.height(SpacingXxl))
                }
            }
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    items: List<ReviewItem>,
    viewModel: ReviewViewModel
) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(SpacingSm))
    items.forEach { item ->
        GymLogCard(modifier = Modifier.fillMaxWidth()) {
            Text(item.content, style = MaterialTheme.typography.bodyMedium)
            if (item.userAction == null) {
                Spacer(Modifier.height(SpacingSm))
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
                    TextButton(onClick = { viewModel.resolveItem(item.id) }) {
                        Text("已采纳", color = AccentSage)
                    }
                    TextButton(onClick = { viewModel.dismissItem(item.id) }) {
                        Text("忽略", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Spacer(Modifier.height(SpacingXs))
                Text(
                    if (item.userAction == UserAction.RESOLVED) "已采纳" else "已忽略",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.userAction == UserAction.RESOLVED) AccentSage else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(SpacingSm))
    }
}
