package com.gymlog.app.presentation.screens.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymlog.app.domain.model.*
import com.gymlog.app.presentation.components.GymLogButton
import com.gymlog.app.presentation.components.GymLogSecondaryButton
import com.gymlog.app.presentation.components.GymLogTextField
import com.gymlog.app.presentation.theme.*
import com.gymlog.app.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onComplete: () -> Unit
) {
    val step by viewModel.currentStep.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = PageHorizontal)
        ) {
            Spacer(Modifier.height(SpacingXxl))
            Text(
                "GymLog",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(SpacingXs))
            Text(
                "你的AI训练伙伴",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(SpacingXxl))

            // Step indicator
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .padding(horizontal = 4.dp)
                            .then(
                                if (i == step) Modifier else Modifier
                            )
                    ) {
                        val color = if (i <= step) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.outline
                        // Simple dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(color, shape = MaterialTheme.shapes.small)
                        )
                    }
                }
            }

            Spacer(Modifier.height(SpacingXl))

            Crossfade(targetState = step, label = "step") { currentStep ->
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    when (currentStep) {
                        0 -> StepBodyData(viewModel)
                        1 -> StepTrainingProfile(viewModel)
                        2 -> StepApiKey(viewModel)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = SpacingXl),
                horizontalArrangement = Arrangement.spacedBy(SpacingMd)
            ) {
                if (step > 0) {
                    GymLogSecondaryButton("上一步", onClick = { viewModel.prevStep() }, modifier = Modifier.weight(1f))
                }
                if (step < 2) {
                    GymLogButton("下一步", onClick = { viewModel.nextStep() }, modifier = Modifier.weight(1f))
                } else {
                    GymLogButton("开始使用", onClick = {
                        viewModel.complete()
                        onComplete()
                    }, modifier = Modifier.weight(1f))
                }
            }

            if (step > 0) {
                TextButton(onClick = {
                    viewModel.skip()
                    onComplete()
                }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = SpacingMd)) {
                    Text("跳过设置，直接开始", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StepBodyData(viewModel: OnboardingViewModel) {
    val gender by viewModel.gender.collectAsState()
    val birthYear by viewModel.birthYear.collectAsState()
    val heightCm by viewModel.heightCm.collectAsState()
    val weightKg by viewModel.weightKg.collectAsState()

    Text("身体数据", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(SpacingMd))
    Text("这些数据用于AI教练生成个性化建议", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingLg))

    Text("性别", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingSm))
    Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Gender.entries.forEach { g ->
            FilterChip(
                selected = gender == g,
                onClick = { viewModel.setGender(g) },
                label = { Text(when(g) { Gender.MALE -> "男"; Gender.FEMALE -> "女"; Gender.OTHER -> "其他" }) }
            )
        }
    }

    Spacer(Modifier.height(SpacingMd))
    Text("出生年份", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingSm))
    GymLogTextField(birthYear, { viewModel.setBirthYear(it) }, placeholder = "1995")

    Spacer(Modifier.height(SpacingMd))
    Text("身高 (cm)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingSm))
    GymLogTextField(heightCm, { viewModel.setHeightCm(it) }, placeholder = "175")

    Spacer(Modifier.height(SpacingMd))
    Text("体重 (kg)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingSm))
    GymLogTextField(weightKg, { viewModel.setWeightKg(it) }, placeholder = "75")
}

@Composable
private fun StepTrainingProfile(viewModel: OnboardingViewModel) {
    val experience by viewModel.trainingExperience.collectAsState()
    val goal by viewModel.trainingGoal.collectAsState()
    val frequency by viewModel.weeklyFrequency.collectAsState()

    Text("训练档案", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(SpacingMd))
    Text("帮助AI教练更好地了解你的训练背景", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingLg))

    Text("训练年限", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingSm))
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        TrainingExperience.entries.forEach { e ->
            Row(modifier = Modifier.clickable { viewModel.setTrainingExperience(e) }.padding(vertical = 4.dp)) {
                RadioButton(selected = experience == e, onClick = { viewModel.setTrainingExperience(e) })
                Spacer(Modifier.width(SpacingSm))
                Text(e.label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    Spacer(Modifier.height(SpacingMd))
    Text("训练目标", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingSm))
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        TrainingGoal.entries.forEach { g ->
            Row(modifier = Modifier.clickable { viewModel.setTrainingGoal(g) }.padding(vertical = 4.dp)) {
                RadioButton(selected = goal == g, onClick = { viewModel.setTrainingGoal(g) })
                Spacer(Modifier.width(SpacingSm))
                Text(g.label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    Spacer(Modifier.height(SpacingMd))
    Text("每周训练天数: ${frequency}天", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(SpacingSm))
    Slider(
        value = frequency.toFloat(),
        onValueChange = { viewModel.setWeeklyFrequency(it.toInt()) },
        valueRange = 1f..7f,
        steps = 5,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.onBackground,
            activeTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            inactiveTrackColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun StepApiKey(viewModel: OnboardingViewModel) {
    val apiKey by viewModel.apiKey.collectAsState()
    val provider by viewModel.selectedProvider.collectAsState()

    Text("AI 教练配置", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(SpacingMd))
    Text("配置你的LLM API Key，开启AI教练功能。可以稍后在设置中修改。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingLg))

    Text("模型提供商", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingSm))
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        LLMProvider.entries.forEach { p ->
            Row(modifier = Modifier.clickable { viewModel.setProvider(p) }.padding(vertical = 4.dp)) {
                RadioButton(selected = provider == p, onClick = { viewModel.setProvider(p) })
                Spacer(Modifier.width(SpacingSm))
                Text(p.label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    Spacer(Modifier.height(SpacingMd))
    Text("API Key", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(SpacingSm))
    var showKey by remember { mutableStateOf(false) }
    GymLogTextField(
        value = apiKey,
        onValueChange = { viewModel.setApiKey(it) },
        placeholder = if (showKey) "sk-..." else "••••••••"
    )
    TextButton(onClick = { showKey = !showKey }) {
        Text(if (showKey) "隐藏" else "显示", style = MaterialTheme.typography.bodySmall)
    }

    Spacer(Modifier.height(SpacingMd))
    Text("数据完全本地存储，API Key 仅用于调用你选择的LLM服务", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
