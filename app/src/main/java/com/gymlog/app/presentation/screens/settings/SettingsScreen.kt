package com.gymlog.app.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymlog.app.domain.model.LLMProvider
import com.gymlog.app.presentation.components.*
import com.gymlog.app.presentation.theme.*
import com.gymlog.app.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val llmConfig by viewModel.llmConfig.collectAsState()
    val restSeconds by viewModel.restTimerSeconds.collectAsState()
    val enableScore by viewModel.enableReviewScore.collectAsState()
    var showApiKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Light) },
                navigationIcon = { TextButton(onClick = onNavigateBack) { Text("返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PageHorizontal)
        ) {
            Spacer(Modifier.height(SpacingLg))

            // API Key
            Text("LLM 配置", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(SpacingMd))

            Text("提供商", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(SpacingSm))
            LLMProvider.entries.forEach { p ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    RadioButton(
                        selected = llmConfig.provider == p,
                        onClick = { viewModel.updateProvider(p) }
                    )
                    Spacer(Modifier.width(SpacingSm))
                    Text(p.label, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(SpacingMd))
            Text("API Key", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(SpacingSm))
            Row {
                GymLogTextField(
                    value = llmConfig.apiKey,
                    onValueChange = { viewModel.updateApiKey(it) },
                    placeholder = if (showApiKey) "sk-..." else "••••••••",
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { showApiKey = !showApiKey }) {
                    Text(if (showApiKey) "隐藏" else "显示")
                }
            }

            Spacer(Modifier.height(SpacingMd))
            Text("模型", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(SpacingSm))
            GymLogTextField(
                value = llmConfig.modelName,
                onValueChange = { viewModel.updateModel(it) },
                placeholder = "gpt-4o-mini"
            )

            Spacer(Modifier.height(SpacingXl))
            GymLogDivider()
            Spacer(Modifier.height(SpacingXl))

            // Rest timer
            Text("训练设置", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(SpacingMd))

            Text("休息计时器: ${restSeconds}秒", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(SpacingSm))
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
                listOf(30, 60, 90, 120, 180).forEach { sec ->
                    FilterChip(
                        selected = restSeconds == sec,
                        onClick = { viewModel.updateRestTimer(sec) },
                        label = { Text("${sec}s") }
                    )
                }
            }

            Spacer(Modifier.height(SpacingMd))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("显示训练质量分", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = enableScore,
                    onCheckedChange = { viewModel.updateEnableScore(it) }
                )
            }

            Spacer(Modifier.height(SpacingXl))
            GymLogDivider()
            Spacer(Modifier.height(SpacingXl))

            // Data
            Text("数据管理", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(SpacingMd))

            GymLogSecondaryButton("导出数据 (JSON)", onClick = { viewModel.exportData() }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(SpacingSm))
            GymLogSecondaryButton("导出训练摘要 (CSV)", onClick = { viewModel.exportData() }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(SpacingXl))
            GymLogDivider()
            Spacer(Modifier.height(SpacingXl))

            // About
            Text("GymLog v1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("你的AI训练伙伴", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(SpacingXxl))
        }
    }
}
