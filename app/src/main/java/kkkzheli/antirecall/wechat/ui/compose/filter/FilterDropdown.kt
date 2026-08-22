package kkkzheli.antirecall.wechat.ui.compose.filter

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contactNames by viewModel.contactNames.collectAsStateWithLifecycle()
    val groupNames by viewModel.groupNames.collectAsStateWithLifecycle()
    val selectedContacts by viewModel.selectedContacts.collectAsStateWithLifecycle()
    val selectedGroups by viewModel.selectedGroups.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()
    val showDatePicker by viewModel.showDatePicker.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.filter_screen_title)) },
                actions = {
                    if (selectedContacts.isNotEmpty() || selectedGroups.isNotEmpty() || startDate != null || endDate != null) {
                        IconButton(onClick = { viewModel.clearFilter() }) {
                            Icon(Icons.Default.ClearAll, contentDescription = stringResource(R.string.filter_clear_all_desc))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.filter_back_desc))
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
            // Active filters
            if (selectedContacts.isNotEmpty() || selectedGroups.isNotEmpty() || startDate != null) {
                Text(text = stringResource(R.string.filter_active_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                SelectedChips(
                    items = buildList {
                        addAll(selectedContacts.toList().map { "[c]$it" })
                        addAll(selectedGroups.toList().map { "[g]$it" })
                        if (startDate != null || endDate != null) add("[d]${dateRangeLabel(startDate, endDate)}")
                    },
                    onRemove = { item ->
                        when {
                            item.startsWith("[c]") -> viewModel.toggleContact(item.substringAfter("[c]"))
                            item.startsWith("[g]") -> viewModel.toggleGroup(item.substringAfter("[g]"))
                            item.startsWith("[d]") -> viewModel.setDateRange(null, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            // Date range button
            Card(
                onClick = { viewModel.showDatePicker(true) },
                colors = CardDefaults.cardColors(
                    containerColor = if (startDate != null || endDate != null) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (startDate != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.filter_date_range),
                            fontWeight = FontWeight.SemiBold,
                            color = if (startDate != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (startDate != null && endDate != null) "${dateStr(startDate!!)} ~ ${dateStr(endDate!!)}" else if (startDate != null) "Start: ${dateStr(startDate!!)}" else stringResource(R.string.filter_select_date_range),
                            fontSize = 12.sp,
                            color = if (startDate != null) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.filter_contacts_count, contactNames.count()), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            FilterChipRow(items = contactNames, selectedItems = selectedContacts, onToggle = { viewModel.toggleContact(it) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(R.string.filter_groups_count, groupNames.count()), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            FilterChipRow(items = groupNames, selectedItems = selectedGroups, onToggle = { viewModel.toggleGroup(it) }, modifier = Modifier.fillMaxWidth())
        }
    }

    if (showDatePicker) {
        DateRangePickerDialog(
            currentStart = startDate,
            currentEnd = endDate,
            onStartDateChanged = { s -> viewModel.setDateRange(s, endDate); viewModel.showDatePicker(false) },
            onEndDateChanged = { e -> viewModel.setDateRange(startDate, e); viewModel.showDatePicker(false) },
            onDismiss = { viewModel.showDatePicker(false) },
        )
    }
}

private fun dateStr(dt: LocalDate?): String {
    return dt?.let { "${it.year}-${it.monthValue.toString().padStart(2,'0')}-${it.dayOfMonth.toString().padStart(2,'0')}" } ?: "--"
}

private fun dateRangeLabel(start: LocalDate?, end: LocalDate?): String {
    return "${dateStr(start)} ~ ${dateStr(end)}"
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = item, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Medium, maxLines = 1)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.filter_remove), modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
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
            val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val fg = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            val border = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            Card(
                onClick = { onToggle(item) },
                colors = CardDefaults.cardColors(containerColor = bg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(width = 1.dp, color = border, shape = RoundedCornerShape(16.dp))
            ) {
                Text(text = item, color = fg, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
            }
        }
    }
}
