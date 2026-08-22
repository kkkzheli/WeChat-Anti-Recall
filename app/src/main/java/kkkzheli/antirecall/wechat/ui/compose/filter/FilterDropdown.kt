package kkkzheli.antirecall.wechat.ui.compose.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contactNames by viewModel.contactNames.collectAsStateWithLifecycle()
    val groupNames by viewModel.groupNames.collectAsStateWithLifecycle()
    val selectedContact by viewModel.selectedContact.collectAsStateWithLifecycle()
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()

    var contactFilterText by remember { mutableStateOf("") }
    var groupFilterText by remember { mutableStateOf("") }

    val filteredContacts = contactNames.filter { it.contains(contactFilterText, ignoreCase = true) }
    val filteredGroups = groupNames.filter { it.contains(groupFilterText, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("筛选联系人/群聊") },
                actions = {
                    if (selectedContact != null || selectedGroup != null) {
                        IconButton(onClick = { viewModel.clearFilter() }) {
                            Icon(Icons.Default.Close, contentDescription = "清除筛选")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
            // Contacts section
            Text(
                text = "联系人 (${contactNames.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = contactFilterText,
                onValueChange = { contactFilterText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索联系人") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )

            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.4f).padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(filteredContacts) { contact ->
                    val isSelected = selectedContact == contact
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectContact(contact) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = contact,
                            modifier = Modifier.weight(1f),
                            style = if (isSelected) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold) else MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Groups section
            Text(
                text = "群聊 (${groupNames.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = groupFilterText,
                onValueChange = { groupFilterText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索群聊") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )

            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.4f).padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(filteredGroups) { group ->
                    val isSelected = selectedGroup == group
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectGroup(group) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = group,
                            modifier = Modifier.weight(1f),
                            style = if (isSelected) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold) else MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active filter display
            if (selectedContact != null || selectedGroup != null) {
                AssistChip(
                    onClick = { viewModel.clearFilter() },
                    label = { Text(
                        buildString {
                            if (selectedContact != null) append("联系人: $selectedContact")
                            if (selectedContact != null && selectedGroup != null) append("  |  ")
                            if (selectedGroup != null) append("群聊: $selectedGroup")
                        }
                    )},
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = "清除", modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
            }
        }
    }
}
