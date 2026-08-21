package kkkzheli.antirecall.wechat.ui.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel

/**
 * Settings screen for the Anti Recall app.
 * Author: kkkzheli
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onClearConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF24292E), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "GitHub 项目", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = MainViewModel.AUTHOR_NAME + "/WeChat-Anti-Recall", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { openUrl(context, MainViewModel.GITHUB_REPO) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("在浏览器中打开")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "作者", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = MainViewModel.AUTHOR_NAME.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = MainViewModel.AUTHOR_NAME, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(text = "Anti Recall Project", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "版本信息", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = MainViewModel.APP_NAME + " v1.0.0", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "基于 Material Design 3 / Jetpack Compose\nTarget API 34 · minSdk 23", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions section
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "权限状态", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

                    val npd: Boolean? = (viewModel as @UnsafeVariance Any).javaClass.declaredFields.firstOrNull { it.name == "_notificationPermissionDenied" || it.name == "notificationPermissionDenied" }?.get(viewModel) as? Boolean
                    PermissionStatusRow(
                        icon = Icons.Default.Info,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "通知权限",
                        status = "已授权",
                        statusColor = Color(0xFF07C160),
                    )

                    Divider(modifier = Modifier.padding(horizontal = 0.dp, vertical = 8.dp))

                    PermissionStatusRow(
                        icon = Icons.Default.Info,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "悬浮窗权限",
                        status = "已授权",
                        statusColor = Color(0xFF07C160),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("清除所有消息")
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF4444)) },
            title = { Text("确认清除") },
            text = { Text("此操作将删除所有已捕获的消息，且无法恢复。确定要继续吗？") },
            confirmButton = {
                TextButton(onClick = { onClearConfirmed(); showClearDialog = false }) {
                    Text("清除", color = Color(0xFFFF4444))
                }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun PermissionStatusRow(icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, title: String, status: String, statusColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = title, fontSize = 14.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = status, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = statusColor)
    }
}

private fun openUrl(context: Context, urlString: String) {
    try {
        val uri = Uri.parse(urlString)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {}
}
