package com.streamvault.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.data.models.*
import com.streamvault.app.ui.components.*
import com.streamvault.app.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isTv: Boolean = false,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val serverConfigs by viewModel.serverConfigs.collectAsState()
    val activeConfig by viewModel.activeConfig.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val userInfo by viewModel.userInfo.collectAsState()
    val appInfo by viewModel.appInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        StreamVaultTopBar(
            title = "Ajustes",
            showBack = !isTv,
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Info
            if (userInfo != null) {
                item {
                    AccountCard(userInfo = userInfo!!, activeConfig = activeConfig)
                }
            }

            // Server Lists
            item {
                SettingsSection(
                    title = "📋 Listas de Servidores",
                    subtitle = "Gestiona tus listas IPTV"
                ) {
                    // Add new button
                    OutlinedButton(
                        onClick = { viewModel.showAddDialog(true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CyanPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(CyanPrimary, ElectricBlue)
                            )
                        )
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar nueva lista")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Server list
                    serverConfigs.forEach { config ->
                        ServerConfigCard(
                            config = config,
                            isActive = config.isActive,
                            onActivate = { viewModel.activateServer(config) },
                            onDelete = { viewModel.deleteConfig(config.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (serverConfigs.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.Dns,
                            title = "Sin servidores",
                            message = "Agrega tu primera lista IPTV para comenzar"
                        )
                    }
                }
            }

            // App Info
            item {
                SettingsSection(
                    title = "ℹ️ Información",
                    subtitle = "Acerca de StreamVault"
                ) {
                    InfoRow("Versión", appInfo.version)
                    InfoRow("Build", appInfo.buildNumber)
                    InfoRow("Paquete", appInfo.packageName)
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = DarkSurfaceVariant
                    )
                    InfoRow("Servidor activo", activeConfig?.url ?: "Ninguno")
                    InfoRow("Listas guardadas", "${serverConfigs.size}")
                }
            }

            // Danger Zone
            item {
                SettingsSection(
                    title = "⚠️ Zona de peligro",
                    subtitle = "Acciones irreversibles"
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorRed
                        )
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpiar historial de reproducción")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Add Server Dialog
    if (showAddDialog) {
        AddServerDialog(
            onDismiss = { viewModel.showAddDialog(false) },
            onConfirm = { name, url, user, pass ->
                viewModel.addServer(name, url, user, pass)
            }
        )
    }
}

@Composable
private fun AccountCard(
    userInfo: UserInfo,
    activeConfig: ServerConfigEntity?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(CyanPrimary, ElectricBlue)
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = userInfo.username ?: "Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userInfo.status?.uppercase() ?: "DESCONOCIDO",
                        style = MaterialTheme.typography.labelSmall,
                        color = when (userInfo.status) {
                            "Active" -> SuccessGreen
                            "Banned" -> ErrorRed
                            "Disabled" -> WarningYellow
                            else -> TextSecondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = DarkSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoColumn("Conexiones", userInfo.activeCons ?: "0")
                InfoColumn("Máx.", userInfo.maxConnections ?: "N/A")
                InfoColumn("Expira", userInfo.expDate?.take(10) ?: "N/A")
            }

            if (userInfo.isTrial == "1") {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WarningYellow.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "⚠️ Cuenta de prueba",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = WarningYellow,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = CyanPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ServerConfigCard(
    config: ServerConfigEntity,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) CyanPrimary.copy(alpha = 0.1f) else DarkSurfaceVariant
        ),
        border = if (isActive) {
            BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isActive) CyanPrimary.copy(alpha = 0.2f)
                        else DarkSurface,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Filled.CheckCircle else Icons.Filled.Dns,
                    contentDescription = null,
                    tint = if (isActive) CyanPrimary else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = config.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
                Text(
                    text = "Usuario: ${config.username}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }

            if (!isActive) {
                TextButton(onClick = onActivate) {
                    Text("Activar", color = CyanPrimary)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = ErrorRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
fun AddServerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String, username: String, password: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "Agregar servidor",
                style = MaterialTheme.typography.titleLarge,
                color = CyanPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Mi IPTV") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        cursorColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL del servidor") },
                    placeholder = { Text("http://ejemplo.com:8080") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        cursorColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        cursorColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        cursorColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        unfocusedLabelColor = TextSecondary
                    )
                )
            }
        },
        confirmButton = {
            GradientButton(
                text = "Guardar",
                onClick = {
                    if (name.isNotBlank() && url.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                        onConfirm(name, url.trimEnd('/'), username, password)
                    }
                },
                icon = Icons.Filled.Save,
                enabled = name.isNotBlank() && url.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
