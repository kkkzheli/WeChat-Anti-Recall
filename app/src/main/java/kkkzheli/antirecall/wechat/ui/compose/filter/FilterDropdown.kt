package kkkzheli.antirecall.wechat.ui.compose.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onContactSelected: (String) -> Unit = {},
    onGroupSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var contactNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var groupNames by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.contactNames.collect { names ->
            contactNames = names
        }
    }

    LaunchedEffect(Unit) {
        viewModel.groupNames.collect { names ->
            groupNames = names
        }
    }

    var contactFilterText by remember { mutableStateOf("") }
    var groupFilterText by remember { mutableStateOf("") }

    val filteredContacts = contactNames.filter { name -> name.contains(contactFilterText, ignoreCase = true) }
    val filteredGroups = groupNames.filter { name -> name.contains(groupFilterText, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            val contactsText = "Contacts (${contactNames.size})"
            Text(
                text = contactsText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = contactFilterText,
                onValueChange = { contactFilterText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search contacts") },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.height(200.dp).padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredContacts) { contact ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(8.dp).padding(end = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color.Blue
                            ) {
                                Box(modifier = Modifier.size(6.dp))
                            }
                        }
                        Text(
                            text = contact,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Divider()

            // Groups section
            val groupsText = "Groups (${groupNames.size})"
            Text(
                text = groupsText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = groupFilterText,
                onValueChange = { groupFilterText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search groups") },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.height(200.dp).padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredGroups) { group ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(8.dp).padding(end = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color(0xFFFF9800)
                            ) {
                                Box(modifier = Modifier.size(6.dp))
                            }
                        }
                        Text(
                            text = group,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
