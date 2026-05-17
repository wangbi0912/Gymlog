package com.gymlog.app.presentation.screens.body

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gymlog.app.presentation.components.*
import com.gymlog.app.presentation.theme.*
import com.gymlog.app.presentation.viewmodel.BodyDataViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyDataScreen(
    viewModel: BodyDataViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    val measurements by viewModel.measurements.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("身体数据", fontWeight = FontWeight.Light) },
                navigationIcon = { TextButton(onClick = onNavigateBack) { Text("返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = PageHorizontal)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(SpacingLg))

            measurements.firstOrNull()?.let { latest ->
                Text("当前体重", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(SpacingSm))
                Text(
                    "${latest.weightKg} kg",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Light
                )
                Text(
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(latest.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(SpacingXl))

            Text("记录体重", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(SpacingMd))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingMd)
            ) {
                GymLogTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    placeholder = "体重 (kg)",
                    modifier = Modifier.weight(1f)
                )
                GymLogButton("保存", onClick = {
                    weight.toFloatOrNull()?.let { w ->
                        viewModel.saveWeight(w)
                        weight = ""
                    }
                })
            }

            Spacer(Modifier.height(SpacingXl))

            Text("历史记录", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(SpacingMd))

            measurements.forEach { m ->
                GymLogCard(modifier = Modifier.fillMaxWidth().padding(bottom = SpacingSm)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(m.date)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text("${m.weightKg} kg", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
