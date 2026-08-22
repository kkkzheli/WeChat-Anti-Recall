package kkkzheli.antirecall.wechat.ui.compose.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contactNames by remember { viewModel.contactNames }
    val groupNames by remember { viewModel.groupNames }
    val selectedContacts by remember { viewModel.selectedContacts }
    val selectedGroups by remember { viewModel.selectedGroups }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter") },
                actions = {
                    if (selectedContacts.isNotEmpty() || selectedGroups.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearFilter() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear all")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (selectedContacts.isNotEmpty() || selectedGroups.isNotEmpty()) {
                Text(
                    text = "Active Filters",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SelectedChips(
                    items = selectedContacts.toList(),
                    onRemove = { viewModel.toggleContact(it) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                if (selectedGroups.isNotEmpty()) {
                    SelectedChips(
                        items = selectedGroups.toList(),
                        onRemove = { viewModel.toggleGroup(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Contacts (${contactNames.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FilterChipRow(
                items = contactNames,
                selectedItems = selectedContacts,
                onToggle = { viewModel.toggleContact(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Groups (${groupNames.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FilterChipRow(
                items = groupNames,
                selectedItems = selectedGroups,
                onToggle = { viewModel.toggleGroup(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SelectedChips(
    items: List<String>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            Card(
                onClick = { onRemove(item) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = item,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove $item",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    items: List<String>,
    selectedItems: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            val isSelected = selectedItems.contains(item)
            val backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            val textColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val borderColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }

            Card(
                onClick = { onToggle(item) },
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.cardBorder(
                    borderColor = borderColor,
                    borderStroke = Stroke(1.dp)
                )
            ) {
                Text(
                    text = item,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
