package kkkzheli.antirecall.wechat.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.compose.ui.res.stringResource
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.ui.compose.message.MessageCard
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel

/**
 * Search screen with query binding and filter dropdown.
 * Author: kkkzheli
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var localQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(localQuery) {
        viewModel.setSearchQuery(localQuery)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setSearchQuery("")
        }
    }

    // Observe messages from LiveData
    var allMessages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val queryKey = localQuery.hashCode()

    LaunchedEffect(viewModel.messages.value, queryKey) {
        val observer = Observer<List<Message>> { list ->
            allMessages = list ?: emptyList()
        }
        viewModel.messages.observeForever(observer)
        return@LaunchedEffect
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    FilterChip(
                        selected = false,
                        onClick = { showFilterSheet = true },
                        leadingIcon = {
                            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        label = { Text(stringResource(R.string.filter_title)) },
                        modifier = Modifier.padding(end = 4.dp),
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues),
        ) {
            OutlinedTextField(
                value = localQuery,
                onValueChange = { localQuery = it },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (localQuery.isNotEmpty()) {
                        IconButton(onClick = { localQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_clear))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
            )

            if (selectedFilter != null) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.search_active_filter, selectedFilter!!)) },
                    trailingIcon = {
                        IconButton(onClick = { selectedFilter = null }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_clear), modifier = Modifier.size(16.dp))
                        }
                    },
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
                )
            }

            val filteredResults = when {
                localQuery.isBlank() -> allMessages
                else -> allMessages.filter { msg ->
                    msg.content.contains(localQuery, ignoreCase = true) ||
                        msg.senderName.contains(localQuery, ignoreCase = true)
                }
            }

            if (filteredResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (localQuery.isEmpty()) stringResource(R.string.search_enter_keyword) else stringResource(R.string.search_no_results),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filteredResults, key = { it.id }) { message ->
                        MessageCard(message = message)
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            SheetContent(
                onFilterSelected = { filter -> selectedFilter = filter; showFilterSheet = false },
                onDismiss = { showFilterSheet = false },
            )
        }
    }
}

@Composable
private fun SheetContent(onFilterSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.search_filter_dialog_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        val options = listOf(
            R.string.filter_option_all to R.string.filter_option_all_desc,
            R.string.filter_option_contacts to R.string.filter_option_contacts_desc,
            R.string.filter_option_date to R.string.filter_option_date_desc,
        )
        options.forEach { (labelRes, descRes) ->
            FilterChip(
                selected = false,
                onClick = { onFilterSelected(ctx.getString(labelRes)) },
                label = { Text(ctx.getString(labelRes)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            )
            Text(
                text = ctx.getString(descRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, bottom = 12.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
