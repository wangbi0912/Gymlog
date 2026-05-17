package com.gymlog.app.presentation.screens.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gymlog.app.presentation.components.EmptyState
import com.gymlog.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("动作详情", fontWeight = FontWeight.Light) },
                navigationIcon = { TextButton(onClick = onNavigateBack) { Text("返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = PageHorizontal)
        ) {
            Spacer(Modifier.height(SpacingXxl))
            EmptyState("选择动作查看详情和趋势图")
        }
    }
}
