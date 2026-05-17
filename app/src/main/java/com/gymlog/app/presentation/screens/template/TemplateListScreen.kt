package com.gymlog.app.presentation.screens.template

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gymlog.app.domain.model.TrainingTemplate
import com.gymlog.app.presentation.components.*
import com.gymlog.app.presentation.theme.*
import com.gymlog.app.presentation.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListScreen(
    viewModel: TemplateViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit,
    onStartFromTemplate: (String) -> Unit
) {
    val templates by viewModel.templates.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("训练模板", fontWeight = FontWeight.Light) },
                navigationIcon = { TextButton(onClick = onNavigateBack) { Text("返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = PageHorizontal, vertical = SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingMd)
        ) {
            items(templates, key = { it.id }) { template ->
                TemplateCard(template = template, onStart = { onStartFromTemplate(template.id) })
            }

            if (templates.isEmpty()) {
                item { EmptyState("暂无模板") }
            }
        }
    }
}

@Composable
private fun TemplateCard(template: TrainingTemplate, onStart: () -> Unit) {
    GymLogCard(onClick = onStart) {
        Text(template.name, style = MaterialTheme.typography.bodyLarge)
        template.tag?.let {
            Spacer(Modifier.height(SpacingXs))
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(SpacingSm))
        Text(
            template.exercises.joinToString(" → ") { it.exerciseName },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
        )
        template.estimatedDurationMin?.let {
            Spacer(Modifier.height(SpacingSm))
            Text("预计 ${it}分钟", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
