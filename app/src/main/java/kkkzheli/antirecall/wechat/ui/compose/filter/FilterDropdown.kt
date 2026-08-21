package kkkzheli.antirecall.wechat.ui.compose.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel
import kotlin.math.roundToInt

/**
 * Filter screen for contacts and groups.
 * Author: kkkzheli
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onContactSelected: (String) -> Unit = {},
    onGroupSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Use .value directly on StateFlow to avoid collectAsState dependency
    val filteredContacts by lazy { viewModel.filteredContacts.value.orEmpty() }
    val filteredGroups by lazy { viewModel.filteredGroups.value.orEmpty() }
    val allContacts by lazy { viewModel.contactNames.value.orEmpty() }
    val allGroups by lazy { viewModel.groupNames.value.orEmpty() }

    var contactFilterText by mutableStateOf("")
    var groupFilterText by mutableStateOf("")

    LaunchedEffect(contactFilterText) {
        viewModel.setContactFilter(contactFilterText)
    }
    LaunchedEffect(groupFilterText) {
        viewModel.setGroupFilter(groupFilterText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("筛选") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeader(title = "联系人 (${allContacts.size})")
                Spacer(modifier = Modifier.height(8.dp))
                SearchField(value = contactFilterText, onValueChange = { contactFilterText = it })
            }

            items(filteredContacts, key = { it }) { name: String ->
                ContactItemUI(
                    name = name,
                    messageCount = estimateContactCount(name, allContacts, filteredContacts),
                    onClick = {
                        onContactSelected(name)
                        onBack()
                    },
                )
            }

            if (allGroups.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(title = "群聊 (${allGroups.size})")
                    Spacer(modifier = Modifier.height(8.dp))
                    SearchField(value = groupFilterText, onValueChange = { groupFilterText = it })
                }

                items(filteredGroups, key = { it }) { name: String ->
                    ContactItemUI(
                        name = name,
                        messageCount = estimateGroupCount(name, allGroups, filteredGroups),
                        isGroup = true,
                        onClick = {
                            onGroupSelected(name)
                            onBack()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("搜索...") },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
}

@Composable
private fun ContactItemUI(
    name: String,
    messageCount: Int,
    isGroup: Boolean = false,
    onClick: () -> Unit,
) {
    val dotColor = getSortIndicatorColor(name)

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(8.dp).background(dotColor, shape = androidx.compose.foundation.shape.CircleShape),
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            if (isGroup) {
                Text(text = "群聊", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }

        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.wrapContentWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(text = messageCount.toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun getSortIndicatorColor(name: String): Color {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return Color(0xFF999999)
    val firstChar = trimmed[0]
    return when {
        firstChar in 'a'..'z' || firstChar in 'A'..'Z' -> MaterialTheme.colorScheme.primary
        firstChar.code in '一'.code..'鿿'.code -> Color(0xFF07C160)
        else -> Color(0xFF999999)
    }
}

private fun estimateContactCount(name: String, allContacts: List<String>, filtered: List<String>): Int {
    val ratio = if (allContacts.isEmpty()) 1f else filtered.size.toFloat() / allContacts.size
    return ((allContacts.size + 50) * ratio).roundToInt().coerceAtLeast(1)
}

private fun estimateGroupCount(name: String, allGroups: List<String>, filtered: List<String>): Int {
    val ratio = if (allGroups.isEmpty()) 1f else filtered.size.toFloat() / allGroups.size
    return ((allGroups.size + 30) * ratio).roundToInt().coerceAtLeast(1)
}
