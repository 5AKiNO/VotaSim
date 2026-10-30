package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsDialog(
    config: UiSimulatorConfig,
    imageVersion: Int,
    onConfigChange: (UiSimulatorConfig) -> Unit,
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onSelectImage: (String) -> Unit,
    onResetImage: (String) -> Unit,
    onPhotosUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Contenido", "Dimensiones", "Textos", "Colores", "Seguridad")

    // Candidate editing state
    var editingCandidate by remember { mutableStateOf<CandidateModel?>(null) }
    var candidateCategory by remember { mutableStateOf<String>("intendente") }
    var isNewCandidate by remember { mutableStateOf(false) }

    // Export/Import JSON dialog state
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Export/Import Excel CSV dialog state
    var showExportExcelDialog by remember { mutableStateOf(false) }
    var showImportExcelDialog by remember { mutableStateOf(false) }
    var showTemplateExcelDialog by remember { mutableStateOf(false) }

    // Export/Import Candidate Photos ZIP dialog state
    var showExportPhotosDialog by remember { mutableStateOf(false) }
    var showImportPhotosDialog by remember { mutableStateOf(false) }

    // Export/Import Custom Dimensions JSON dialog state
    var showExportDimensionsDialog by remember { mutableStateOf(false) }
    var showImportDimensionsDialog by remember { mutableStateOf(false) }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isPortrait = configuration.screenHeightDp > configuration.screenWidthDp

    AlertDialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = if (isPortrait) {
            Modifier
                .fillMaxWidth(0.98f)
                .widthIn(max = 1400.dp)
                .fillMaxHeight(0.96f)
        } else {
            Modifier
                .fillMaxWidth(0.98f)
                .widthIn(min = 680.dp, max = 1400.dp)
                .fillMaxHeight(0.94f)
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Panel de Administración y Diseño",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Navigation Tabs (Scrollable on portrait/narrow mobile screens, Primary on wide)
                if (isPortrait || configuration.screenWidthDp < 600) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = {
                                    SoundPlayer.playClick()
                                    selectedTab = index
                                },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            )
                        }
                    }
                } else {
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = {
                                    SoundPlayer.playClick()
                                    selectedTab = index
                                },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> ContentConfigTab(
                            config = config,
                            imageVersion = imageVersion,
                            onUpdateConfig = onConfigChange,
                            onSelectImage = onSelectImage,
                            onResetImage = onResetImage,
                            onEditCandidate = { cand, cat, isNew ->
                                editingCandidate = cand
                                candidateCategory = cat
                                isNewCandidate = isNew
                            },
                            onShowExportExcel = { showExportExcelDialog = true },
                            onShowImportExcel = { showImportExcelDialog = true },
                            onShowTemplateExcel = { showTemplateExcelDialog = true },
                            onShowExportPhotos = { showExportPhotosDialog = true },
                            onShowImportPhotos = { showImportPhotosDialog = true }
                        )
                        1 -> DimensionsConfigTab(
                            config = config,
                            onUpdateDimensions = { newDim ->
                                val updated = config.copy(dimensions = newDim)
                                onConfigChange(updated)
                            },
                            onShowExportDimensions = { showExportDimensionsDialog = true },
                            onShowImportDimensions = { showImportDimensionsDialog = true }
                        )
                        2 -> TextsConfigTab(
                            config = config,
                            onUpdateTexts = { newTexts ->
                                val updated = config.copy(texts = newTexts)
                                onConfigChange(updated)
                            }
                        )
                        3 -> ColorsConfigTab(
                            config = config,
                            onUpdateColors = { newColors ->
                                val updated = config.copy(colors = newColors)
                                onConfigChange(updated)
                            },
                            onUpdateConfig = onConfigChange
                        )
                        4 -> SecurityAndBackupTab(
                            config = config,
                            onUpdateConfig = onConfigChange,
                            onLogout = onLogout,
                            onShowExport = { showExportDialog = true },
                            onShowImport = { showImportDialog = true },
                            onShowReset = { showResetConfirmDialog = true },
                            onShowExportExcel = { showExportExcelDialog = true },
                            onShowImportExcel = { showImportExcelDialog = true },
                            onShowTemplateExcel = { showTemplateExcelDialog = true },
                            onShowExportPhotos = { showExportPhotosDialog = true },
                            onShowImportPhotos = { showImportPhotosDialog = true },
                            onShowExportDimensions = { showExportDimensionsDialog = true },
                            onShowImportDimensions = { showImportDimensionsDialog = true }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    ConfigManager.saveConfig(context, config)
                    Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar y Aplicar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onClose) {
                Text("Cerrar")
            }
        }
    )

    // Candidate Add/Edit Dialog
    if (editingCandidate != null) {
        CandidateEditDialog(
            candidate = editingCandidate!!,
            category = candidateCategory,
            isNew = isNewCandidate,
            onDismiss = { editingCandidate = null },
            onSave = { updatedCandidate, applyToParty ->
                var newIntendentes = config.intendentes
                var newJuntas = config.juntas
                var newConcejales = config.concejales

                if (applyToParty && updatedCandidate.party.isNotBlank()) {
                    val p = updatedCandidate.party
                    val l = updatedCandidate.list
                    val colHex = updatedCandidate.customColorHex
                    val colType = updatedCandidate.colorType

                    newIntendentes = newIntendentes.map { c ->
                        if ((l.isNotBlank() && c.list == l) || c.party.equals(p, ignoreCase = true)) {
                            c.copy(customColorHex = colHex, colorType = colType)
                        } else c
                    }
                    newJuntas = newJuntas.map { c ->
                        if ((l.isNotBlank() && c.list == l) || c.party.equals(p, ignoreCase = true)) {
                            c.copy(customColorHex = colHex, colorType = colType)
                        } else c
                    }
                    newConcejales = newConcejales.map { c ->
                        if ((l.isNotBlank() && c.list == l) || c.party.equals(p, ignoreCase = true)) {
                            c.copy(customColorHex = colHex, colorType = colType)
                        } else c
                    }
                }

                val finalConfig = when (candidateCategory) {
                    "intendente" -> {
                        val list = newIntendentes.toMutableList()
                        val idx = list.indexOfFirst { it.id == updatedCandidate.id }
                        if (idx >= 0) list[idx] = updatedCandidate else list.add(updatedCandidate)
                        config.copy(intendentes = list, juntas = newJuntas, concejales = newConcejales)
                    }
                    "junta" -> {
                        val list = newJuntas.toMutableList()
                        val idx = list.indexOfFirst { it.id == updatedCandidate.id }
                        if (idx >= 0) list[idx] = updatedCandidate else list.add(updatedCandidate)
                        config.copy(intendentes = newIntendentes, juntas = list, concejales = newConcejales)
                    }
                    "concejal" -> {
                        val list = newConcejales.toMutableList()
                        val idx = list.indexOfFirst { it.id == updatedCandidate.id }
                        if (idx >= 0) list[idx] = updatedCandidate else list.add(updatedCandidate)
                        config.copy(intendentes = newIntendentes, juntas = newJuntas, concejales = list)
                    }
                    else -> config.copy(intendentes = newIntendentes, juntas = newJuntas, concejales = newConcejales)
                }
                onConfigChange(finalConfig)
                editingCandidate = null
            }
        )
    }

    // Export JSON Dialog
    if (showExportDialog) {
        val jsonStr = remember(config) { ConfigManager.toJson(config) }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Configuración JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Puedes copiar este código JSON para respaldar o transferir tu diseño:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonStr,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SimuladorConfig", jsonStr)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Configuración copiada al portapapeles", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar al Portapapeles")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExportDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        var importInput by remember { mutableStateOf("") }
        var importError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importar Configuración JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Pega el JSON de configuración para restaurar un diseño:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInput,
                        onValueChange = {
                            importInput = it
                            importError = null
                        },
                        placeholder = { Text("Pega el JSON aquí...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    if (importError != null) {
                        Text(
                            text = importError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importInput.isBlank()) {
                            importError = "El campo no puede estar vacío"
                        } else {
                            val parsed = ConfigManager.fromJson(importInput)
                            if (parsed != null) {
                                onConfigChange(parsed)
                                ConfigManager.saveConfig(context, parsed)
                                Toast.makeText(context, "Configuración importada con éxito", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                importError = "Formato JSON inválido"
                            }
                        }
                    }
                ) {
                    Text("Importar y Aplicar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("¿Restablecer valores de fábrica?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Esta acción devolverá todas las listas, candidatos, dimensiones, textos, colores y el PIN de configuración a su estado original predeterminado.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val defConfig = ConfigManager.resetToDefault(context)
                        AuthManager.setSettingsPin(context, AuthManager.DEFAULT_SETTINGS_PIN)
                        onConfigChange(defConfig)
                        Toast.makeText(context, "Valores de fábrica y PIN restablecidos", Toast.LENGTH_SHORT).show()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Restablecer", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Export Excel Dialog
    if (showExportExcelDialog) {
        ExportExcelDialog(
            config = config,
            onDismiss = { showExportExcelDialog = false },
            onShowTemplate = { showTemplateExcelDialog = true }
        )
    }

    // Import Excel Dialog
    if (showImportExcelDialog) {
        ImportExcelDialog(
            config = config,
            onDismiss = { showImportExcelDialog = false },
            onApply = { newConfig ->
                onConfigChange(newConfig)
                ConfigManager.saveConfig(context, newConfig)
            },
            onShowTemplate = { showTemplateExcelDialog = true }
        )
    }

    // Template Excel Dialog
    if (showTemplateExcelDialog) {
        TemplateExcelDialog(
            onDismiss = { showTemplateExcelDialog = false }
        )
    }

    // Export Candidate Photos ZIP Dialog
    if (showExportPhotosDialog) {
        ExportPhotosDialog(
            config = config,
            onDismiss = { showExportPhotosDialog = false }
        )
    }

    // Import Candidate Photos ZIP Dialog
    if (showImportPhotosDialog) {
        ImportPhotosDialog(
            config = config,
            onDismiss = { showImportPhotosDialog = false },
            onPhotosImported = {
                onPhotosUpdated()
            }
        )
    }

    // Export Custom Dimensions Dialog
    if (showExportDimensionsDialog) {
        ExportDimensionsDialog(
            dimensions = config.dimensions,
            onDismiss = { showExportDimensionsDialog = false }
        )
    }

    // Import Custom Dimensions Dialog
    if (showImportDimensionsDialog) {
        ImportDimensionsDialog(
            onDismiss = { showImportDimensionsDialog = false },
            onApplyDimensions = { importedDims ->
                val updated = config.copy(dimensions = importedDims)
                onConfigChange(updated)
            }
        )
    }
}

// ---------------- TAB 1: CONTENIDO Y CANDIDATOS ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentConfigTab(
    config: UiSimulatorConfig,
    imageVersion: Int,
    onUpdateConfig: (UiSimulatorConfig) -> Unit,
    onSelectImage: (String) -> Unit,
    onResetImage: (String) -> Unit,
    onEditCandidate: (CandidateModel, String, Boolean) -> Unit,
    onShowExportExcel: () -> Unit = {},
    onShowImportExcel: () -> Unit = {},
    onShowTemplateExcel: () -> Unit = {},
    onShowExportPhotos: () -> Unit = {},
    onShowImportPhotos: () -> Unit = {}
) {
    val context = LocalContext.current
    var subCategory by remember { mutableStateOf(0) }
    val subCategories = listOf("Intendentes", "Listas Junta", "Concejales", "Fondo")

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Action Bar: Excel & Photos ZIP
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Excel / CSV:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onShowExportExcel,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Exportar Excel", fontSize = 10.sp)
                        }

                        Button(
                            onClick = onShowImportExcel,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Importar Excel", fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Fotos por Partido (.ZIP):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onShowExportPhotos,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Exportar Fotos", fontSize = 10.sp)
                        }

                        Button(
                            onClick = onShowImportPhotos,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Importar Fotos", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        SecondaryScrollableTabRow(
            selectedTabIndex = subCategory,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp
        ) {
            subCategories.forEachIndexed { index, name ->
                Tab(
                    selected = subCategory == index,
                    onClick = {
                        SoundPlayer.playClick()
                        subCategory = index
                    },
                    text = { Text(name, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (subCategory) {
            0 -> {
                // Intendentes list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Candidatos a Intendente (${config.intendentes.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FilledTonalButton(
                        onClick = {
                            val newCand = CandidateModel(
                                id = "i_custom_${System.currentTimeMillis() % 10000}",
                                name = "NUEVO CANDIDATO",
                                party = "PARTIDO POLÍTICO",
                                initials = "PP",
                                list = "99",
                                works = true,
                                colorType = "red"
                            )
                            onEditCandidate(newCand, "intendente", true)
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(config.intendentes) { candidate ->
                        CandidateRowItem(
                            candidate = candidate,
                            imageFile = File(context.filesDir, "candidate_${candidate.id}.png"),
                            imageVersion = imageVersion,
                            onToggleEnabled = { enabled ->
                                val list = config.intendentes.map {
                                    if (it.id == candidate.id) it.copy(works = enabled) else it
                                }
                                onUpdateConfig(config.copy(intendentes = list))
                            },
                            onEdit = { onEditCandidate(candidate, "intendente", false) },
                            onDelete = if (!candidate.isBlanco && config.intendentes.size > 1) {
                                {
                                    val list = config.intendentes.filter { it.id != candidate.id }
                                    onUpdateConfig(config.copy(intendentes = list))
                                }
                            } else null,
                            onSelectImage = { onSelectImage(candidate.id) },
                            onResetImage = { onResetImage(candidate.id) }
                        )
                    }
                }
            }
            1 -> {
                // Listas Junta list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Listas para Junta Municipal (${config.juntas.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FilledTonalButton(
                        onClick = {
                            val newCand = CandidateModel(
                                id = "j_custom_${System.currentTimeMillis() % 10000}",
                                name = "NUEVA LISTA",
                                party = "NUEVO PARTIDO POLÍTICO",
                                initials = "NP",
                                list = "99",
                                works = true,
                                colorType = "red"
                            )
                            onEditCandidate(newCand, "junta", true)
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(config.juntas) { candidate ->
                        CandidateRowItem(
                            candidate = candidate,
                            imageFile = File(context.filesDir, "candidate_${candidate.id}.png"),
                            imageVersion = imageVersion,
                            onToggleEnabled = { enabled ->
                                val list = config.juntas.map {
                                    if (it.id == candidate.id) it.copy(works = enabled) else it
                                }
                                onUpdateConfig(config.copy(juntas = list))
                            },
                            onEdit = { onEditCandidate(candidate, "junta", false) },
                            onDelete = if (!candidate.isBlanco && config.juntas.size > 1) {
                                {
                                    val list = config.juntas.filter { it.id != candidate.id }
                                    onUpdateConfig(config.copy(juntas = list))
                                }
                            } else null,
                            onSelectImage = { onSelectImage(candidate.id) },
                            onResetImage = { onResetImage(candidate.id) }
                        )
                    }
                }
            }
            2 -> {
                // Concejales list with List/Party Filter
                var filterConcejalList by remember { mutableStateOf("ALL") }
                val uniqueListas = remember(config.juntas, config.concejales) {
                    val fromJuntas = config.juntas.filter { !it.isBlanco && it.list.isNotBlank() }.map { it.list.trim() }
                    val fromConcejales = config.concejales.filter { !it.isBlanco && it.list.isNotBlank() }.map { it.list.trim() }
                    (fromJuntas + fromConcejales).distinct().sortedBy { it.toIntOrNull() ?: 999 }
                }

                val filteredConcejales = remember(config.concejales, filterConcejalList) {
                    if (filterConcejalList == "ALL") config.concejales
                    else config.concejales.filter { it.list.trim() == filterConcejalList }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Header with Add Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (filterConcejalList == "ALL") "Todos los Concejales (${config.concejales.size})"
                                   else "Concejales Lista $filterConcejalList (${filteredConcejales.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        FilledTonalButton(
                            onClick = {
                                val targetList = if (filterConcejalList != "ALL") filterConcejalList else "1"
                                val matchingJunta = config.juntas.firstOrNull { it.list.trim() == targetList }
                                val maxOpt = (config.concejales.filter { it.list.trim() == targetList }.mapNotNull { it.optionId }.maxOrNull() ?: 0) + 1
                                val newCand = CandidateModel(
                                    id = "c${maxOpt}_l${targetList}_${System.currentTimeMillis() % 1000}",
                                    optionId = maxOpt,
                                    name = "NUEVO CONCEJAL",
                                    party = matchingJunta?.party ?: "AGRUPACIÓN CÍVICA ROJA",
                                    initials = matchingJunta?.initials ?: "ACR",
                                    list = targetList,
                                    works = true,
                                    colorType = matchingJunta?.colorType ?: "red",
                                    customColorHex = matchingJunta?.customColorHex
                                )
                                onEditCandidate(newCand, "concejal", true)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Filter Chips Row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = filterConcejalList == "ALL",
                                onClick = {
                                    SoundPlayer.playClick()
                                    filterConcejalList = "ALL"
                                },
                                label = { Text("Todos (${config.concejales.size})", fontSize = 11.sp) }
                            )
                        }
                        items(uniqueListas) { lNum ->
                            val matchingJunta = config.juntas.firstOrNull { it.list.trim() == lNum }
                            val pName = matchingJunta?.initials?.ifBlank { matchingJunta.party } ?: "Lista $lNum"
                            val count = config.concejales.count { it.list.trim() == lNum }
                            FilterChip(
                                selected = filterConcejalList == lNum,
                                onClick = {
                                    SoundPlayer.playClick()
                                    filterConcejalList = lNum
                                },
                                label = { Text("Lista $lNum ($pName - $count)", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredConcejales) { candidate ->
                            CandidateRowItem(
                                candidate = candidate,
                                imageFile = File(context.filesDir, "candidate_${candidate.id}.png"),
                                imageVersion = imageVersion,
                                onToggleEnabled = { enabled ->
                                    val list = config.concejales.map {
                                        if (it.id == candidate.id) it.copy(works = enabled) else it
                                    }
                                    onUpdateConfig(config.copy(concejales = list))
                                },
                                onEdit = { onEditCandidate(candidate, "concejal", false) },
                                onDelete = if (config.concejales.size > 1) {
                                    {
                                        val list = config.concejales.filter { it.id != candidate.id }
                                        onUpdateConfig(config.copy(concejales = list))
                                    }
                                } else null,
                                onSelectImage = { onSelectImage(candidate.id) },
                                onResetImage = { onResetImage(candidate.id) }
                            )
                        }
                    }
                }
            }
            3 -> {
                // Screen Saver background image
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Fondo de la Pantalla de Inicio / Protector", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Imagen personalizada que cubre la pantalla inicial de bienvenida", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    val bgFile = File(context.filesDir, "screensaver_bg.png")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bgFile.exists()) {
                                    AsyncImage(
                                        model = bgFile,
                                        contentDescription = "Fondo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("Fondo por defecto (Azul Oscuro)", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onSelectImage("screensaver") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Seleccionar Imagen")
                                }
                                if (bgFile.exists()) {
                                    OutlinedButton(
                                        onClick = { onResetImage("screensaver") },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Quitar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateRowItem(
    candidate: CandidateModel,
    imageFile: File,
    imageVersion: Int,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
    onSelectImage: () -> Unit,
    onResetImage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with party color border
            val candidateColor = getCandidatePrimaryColor(candidate, Color(0xFFC8102E))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(2.dp, candidateColor, CircleShape)
            ) {
                if (imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = candidate.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    CandidateAvatar(
                        name = candidate.name,
                        isBlanco = candidate.isBlanco,
                        id = candidate.id,
                        imageVersion = imageVersion,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Information
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = candidate.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(candidateColor)
                            .border(0.5.dp, Color.Gray, CircleShape)
                    )
                }
                val sub = if (candidate.optionId != null) "Opción ${candidate.optionId} - Lista ${candidate.list}"
                          else if (candidate.list.isNotBlank()) "Lista ${candidate.list} - ${candidate.party}"
                          else candidate.party
                Text(
                    text = sub,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            // Status enabled switch
            if (!candidate.isBlanco) {
                Switch(
                    checked = candidate.works,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Action buttons
            IconButton(onClick = onSelectImage, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Foto", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ---------------- DIALOGO DE EDICIÓN DE CANDIDATO ----------------
@Composable
fun CandidateEditDialog(
    candidate: CandidateModel,
    category: String,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (CandidateModel, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(candidate.name) }
    var party by remember { mutableStateOf(candidate.party) }
    var initials by remember { mutableStateOf(candidate.initials) }
    var list by remember { mutableStateOf(candidate.list) }
    var optionIdStr by remember { mutableStateOf(candidate.optionId?.toString() ?: "") }
    var colorType by remember { mutableStateOf(candidate.colorType ?: "red") }
    var customHex by remember { mutableStateOf(candidate.customColorHex ?: "") }
    var applyToAllInParty by remember { mutableStateOf(false) }

    val activeColor = remember(customHex, colorType) {
        if (customHex.isNotBlank()) {
            parseColorRobust(customHex, Color(0xFFC8102E))
        } else {
            parseColorRobust(colorType, Color(0xFFC8102E))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "Agregar Candidato" else "Editar Candidato",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.uppercase() },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = party,
                    onValueChange = { party = it.uppercase() },
                    label = { Text("Partido / Movimiento Político") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = list,
                        onValueChange = { list = it },
                        label = { Text("Nº Lista") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = initials,
                        onValueChange = { initials = it.uppercase() },
                        label = { Text("Siglas") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (category == "concejal" || candidate.optionId != null) {
                    OutlinedTextField(
                        value = optionIdStr,
                        onValueChange = { optionIdStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nº Opción Preferencial") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // PALETA INDIVIDUAL POR CANDIDATO / PARTIDO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paleta de Color Individual / Partido", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(activeColor)
                            .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
                    )
                }

                Text("Seleccionar tono predefinido:", fontSize = 11.sp, color = Color.Gray)
                
                // Color palette preset swatches
                val partyPresets = listOf(
                    Triple("Rojo Institucional", "red", "#C8102E"),
                    Triple("Azul Institucional", "blue", "#1E40AF"),
                    Triple("Verde Alianza", "green", "#15803D"),
                    Triple("Verde Bosque", "dark_green", "#14532D"),
                    Triple("Verde Claro", "light_green", "#4ADE80"),
                    Triple("Celeste", "cyan", "#0284C7"),
                    Triple("Morado", "purple", "#7E22CE"),
                    Triple("Naranja", "orange", "#EA580C"),
                    Triple("Amarillo", "yellow", "#FACC15"),
                    Triple("Turquesa", "teal", "#0D9488"),
                    Triple("Rosa", "rose", "#E11D48"),
                    Triple("Gris Pizarra", "slate", "#334155")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val rows = partyPresets.chunked(6)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowItems.forEach { (label, type, hex) ->
                                val swatchColor = parseHexColor(hex)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            width = if (customHex.equals(hex, ignoreCase = true) || (customHex.isBlank() && colorType == type)) 3.dp else 1.dp,
                                            color = if (customHex.equals(hex, ignoreCase = true) || (customHex.isBlank() && colorType == type)) MaterialTheme.colorScheme.primary else Color.Gray,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            colorType = type
                                            customHex = hex
                                        }
                                )
                            }
                        }
                    }
                }

                // Custom Hex input
                OutlinedTextField(
                    value = customHex,
                    onValueChange = { customHex = it },
                    label = { Text("Hexadecimal Personalizado (ej: #C8102E)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Option to apply to all members of this party/list
                if (party.isNotBlank() || list.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Aplicar a todo el grupo/partido", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Sincroniza este color a todos los candidatos de ${if (list.isNotBlank()) "Lista $list" else party}", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = applyToAllInParty,
                            onCheckedChange = { applyToAllInParty = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val opt = optionIdStr.toIntOrNull()
                    val finalHex = customHex.trim().ifBlank { null }
                    val updated = candidate.copy(
                        name = name.trim(),
                        party = party.trim(),
                        initials = initials.trim(),
                        list = list.trim(),
                        optionId = opt,
                        colorType = colorType,
                        customColorHex = finalHex
                    )
                    onSave(updated, applyToAllInParty)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ---------------- TAB 2: DIMENSIONES Y ESCALA ----------------
@Composable
fun DimensionsConfigTab(
    config: UiSimulatorConfig,
    onUpdateDimensions: (DimensionsConfig) -> Unit,
    onShowExportDimensions: () -> Unit = {},
    onShowImportDimensions: () -> Unit = {}
) {
    val context = LocalContext.current
    val d = config.dimensions
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val metrics = remember(screenWidth, screenHeight) {
        evaluateScreenMetrics(screenWidth, screenHeight)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // CARD: CONFIGURACIÓN PERSONALIZADA DE DIMENSIONES (GUARDAR / IMPORTAR / EXPORTAR)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("card_dimensiones_personalizadas"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Configuración Personalizada de Dimensiones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    if (ConfigManager.hasCustomDimensionsSaved(context)) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Guardada", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF16A34A)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Guarda tus ajustes personalizados de dimensiones para restaurarlos cuando quieras o expórtalos/impórtalos en archivo .json.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            SoundPlayer.playPrint()
                            ConfigManager.saveCustomDimensions(context, d)
                            Toast.makeText(context, "Dimensiones actuales guardadas como configuración personalizada", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar Dimensiones", fontSize = 11.sp)
                    }

                    if (ConfigManager.hasCustomDimensionsSaved(context)) {
                        OutlinedButton(
                            onClick = {
                                SoundPlayer.playClick()
                                val saved = ConfigManager.loadCustomDimensions(context)
                                if (saved != null) {
                                    onUpdateDimensions(saved)
                                    Toast.makeText(context, "Dimensiones personalizadas restauradas", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(0.9f),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cargar Guardada", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onShowExportDimensions,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar Dimensiones", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onShowImportDimensions,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Importar Dimensiones", fontSize = 11.sp)
                    }
                }
            }
        }
        // AUTO-RESPONSIVE ENGINE STATUS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (d.autoResponsive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (d.autoResponsive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (d.autoResponsive) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Ajuste Automático de Pantalla",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (d.autoResponsive) "Motor Activo: Escalado Proporcional Inteligente" else "Modo Manual Personalizado",
                                fontSize = 11.sp,
                                color = if (d.autoResponsive) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Switch(
                        checked = d.autoResponsive,
                        onCheckedChange = { enabled ->
                            SoundPlayer.playClick()
                            if (enabled) {
                                val autoDims = computeAutoResponsiveDimensions(screenWidth, screenHeight, config.isFullScreen)
                                onUpdateDimensions(autoDims.copy(autoResponsive = true))
                            } else {
                                onUpdateDimensions(d.copy(autoResponsive = false))
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                // Screen diagnostics badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Pantalla: ${screenWidth.toInt()} × ${screenHeight.toInt()} dp", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(metrics.deviceCategory, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (d.autoResponsive)
                        "La aplicación calcula dinámicamente la altura óptima de tarjetas, tamaño de números de lista, nombres y avatares según el alto y ancho real de este dispositivo o emulador."
                    else
                        "Has ajustado manualmente una o más dimensiones. Para volver al cálculo automático óptimo para esta pantalla, presiona el botón inferior.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        SoundPlayer.playClick()
                        val recalibrated = computeAutoResponsiveDimensions(screenWidth, screenHeight, config.isFullScreen)
                        onUpdateDimensions(recalibrated.copy(autoResponsive = true))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recalibrar automáticamente según esta pantalla", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("ALTURA DE TARJETAS (DP)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        DimensionSliderRow(
            title = "Altura Tarjeta Intendente",
            value = d.intendenteCardHeightDp,
            min = 80,
            max = 240,
            unit = "dp",
            onValueChange = { onUpdateDimensions(d.copy(intendenteCardHeightDp = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Altura Tarjeta Junta Municipal",
            value = d.juntaCardHeightDp,
            min = 80,
            max = 220,
            unit = "dp",
            onValueChange = { onUpdateDimensions(d.copy(juntaCardHeightDp = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Altura Tarjeta Concejal",
            value = d.concejalCardHeightDp,
            min = 60,
            max = 160,
            unit = "dp",
            onValueChange = { onUpdateDimensions(d.copy(concejalCardHeightDp = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Altura Tarjeta Voto en Blanco",
            value = d.votoBlancoCardHeightDp,
            min = 50,
            max = 140,
            unit = "dp",
            onValueChange = { onUpdateDimensions(d.copy(votoBlancoCardHeightDp = it, autoResponsive = false)) }
        )

        HorizontalDivider()
        Text("TAMAÑO DE FOTOS / AVATARES (DP)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        DimensionSliderRow(
            title = "Tamaño Foto Intendente",
            value = d.avatarSizeIntendenteDp,
            min = 40,
            max = 130,
            unit = "dp",
            onValueChange = { onUpdateDimensions(d.copy(avatarSizeIntendenteDp = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Tamaño Foto Concejal",
            value = d.avatarSizeConcejalDp,
            min = 30,
            max = 90,
            unit = "dp",
            onValueChange = { onUpdateDimensions(d.copy(avatarSizeConcejalDp = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Tamaño Foto en Resumen",
            value = d.avatarSizeSummaryDp,
            min = 50,
            max = 150,
            unit = "dp",
            onValueChange = { onUpdateDimensions(d.copy(avatarSizeSummaryDp = it, autoResponsive = false)) }
        )

        HorizontalDivider()
        Text("TAMAÑO DE TIPOGRAFÍAS (SP)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        DimensionSliderRow(
            title = "Tamaño Nº de Lista",
            value = d.listNumberTextSizeSp,
            min = 18,
            max = 56,
            unit = "sp",
            onValueChange = { onUpdateDimensions(d.copy(listNumberTextSizeSp = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Tamaño Nombre del Candidato",
            value = d.candidateNameTextSizeSp,
            min = 10,
            max = 26,
            unit = "sp",
            onValueChange = { onUpdateDimensions(d.copy(candidateNameTextSizeSp = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Tamaño Nombre del Partido",
            value = d.partyNameTextSizeSp,
            min = 8,
            max = 20,
            unit = "sp",
            onValueChange = { onUpdateDimensions(d.copy(partyNameTextSizeSp = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Tamaño Nº de Opción Concejal",
            value = d.optionNumberTextSizeSp,
            min = 14,
            max = 40,
            unit = "sp",
            onValueChange = { onUpdateDimensions(d.copy(optionNumberTextSizeSp = it, autoResponsive = false)) }
        )

        HorizontalDivider()
        Text("DISTRIBUCIÓN Y GRILLAS", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        DimensionSliderRow(
            title = "Columnas Grilla Intendente",
            value = d.gridColumnsIntendente,
            min = 2,
            max = 5,
            unit = "cols",
            onValueChange = { onUpdateDimensions(d.copy(gridColumnsIntendente = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Columnas Grilla Junta",
            value = d.gridColumnsJunta,
            min = 2,
            max = 4,
            unit = "cols",
            onValueChange = { onUpdateDimensions(d.copy(gridColumnsJunta = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Columnas Grilla Concejales",
            value = d.gridColumnsConcejal,
            min = 2,
            max = 6,
            unit = "cols",
            onValueChange = { onUpdateDimensions(d.copy(gridColumnsConcejal = it, autoResponsive = false)) }
        )

        DimensionSliderRow(
            title = "Espaciado entre Tarjetas",
            value = d.gridSpacingDp,
            min = 4,
            max = 28,
            unit = "dp",
            onValueChange = { onUpdateDimensions(d.copy(gridSpacingDp = it, autoResponsive = false)) }
        )
    }
}

@Composable
fun DimensionSliderRow(
    title: String,
    value: Int,
    min: Int,
    max: Int,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("$value $unit", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = min.toFloat()..max.toFloat(),
                steps = max - min - 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------- TAB 3: TEXTOS Y TÍTULOS ----------------
@Composable
fun TextsConfigTab(
    config: UiSimulatorConfig,
    onUpdateTexts: (TextsConfig) -> Unit
) {
    val t = config.texts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("PANTALLA DE INICIO (PROTECTOR)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = t.screenSaverTitle,
            onValueChange = { onUpdateTexts(t.copy(screenSaverTitle = it)) },
            label = { Text("Título Pantalla Inicio") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.screenSaverSubtitle,
            onValueChange = { onUpdateTexts(t.copy(screenSaverSubtitle = it)) },
            label = { Text("Subtítulo Pantalla Inicio") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.screenSaverTapToStart,
            onValueChange = { onUpdateTexts(t.copy(screenSaverTapToStart = it)) },
            label = { Text("Texto Toca para Comenzar") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        Text("ENCABEZADO Y METADATOS", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = t.headerElecciones,
            onValueChange = { onUpdateTexts(t.copy(headerElecciones = it)) },
            label = { Text("Insignia Encabezado") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.headerMetadata,
            onValueChange = { onUpdateTexts(t.copy(headerMetadata = it)) },
            label = { Text("Texto Metadatos de Distrito") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        Text("PASO 1: INTENDENTE MUNICIPAL", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = t.titleIntendente,
            onValueChange = { onUpdateTexts(t.copy(titleIntendente = it)) },
            label = { Text("Título Paso Intendente") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.subtitleIntendente,
            onValueChange = { onUpdateTexts(t.copy(subtitleIntendente = it)) },
            label = { Text("Subtítulo Paso Intendente") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        Text("PASO 2: JUNTA MUNICIPAL (LISTAS)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = t.titleJunta,
            onValueChange = { onUpdateTexts(t.copy(titleJunta = it)) },
            label = { Text("Título Paso Junta") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.subtitleJunta,
            onValueChange = { onUpdateTexts(t.copy(subtitleJunta = it)) },
            label = { Text("Subtítulo Paso Junta") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        Text("PASO 3: CONCEJAL PREFERENCIAL", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = t.titleConcejal,
            onValueChange = { onUpdateTexts(t.copy(titleConcejal = it)) },
            label = { Text("Título Paso Concejal") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.subtitleConcejal,
            onValueChange = { onUpdateTexts(t.copy(subtitleConcejal = it)) },
            label = { Text("Subtítulo Paso Concejal") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.bannerConcejalSubtitle,
            onValueChange = { onUpdateTexts(t.copy(bannerConcejalSubtitle = it)) },
            label = { Text("Banner Subtítulo Lista 1") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        Text("PASO 4: RESUMEN Y BOTONES", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = t.titleSummary,
            onValueChange = { onUpdateTexts(t.copy(titleSummary = it)) },
            label = { Text("Título Confirmación del Voto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.summaryIntendenteHeader,
            onValueChange = { onUpdateTexts(t.copy(summaryIntendenteHeader = it)) },
            label = { Text("Encabezado Columna Intendente") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.summaryJuntaHeader,
            onValueChange = { onUpdateTexts(t.copy(summaryJuntaHeader = it)) },
            label = { Text("Encabezado Columna Junta") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = t.btnModificar,
                onValueChange = { onUpdateTexts(t.copy(btnModificar = it)) },
                label = { Text("Botón Modificar") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = t.btnVolverAtras,
                onValueChange = { onUpdateTexts(t.copy(btnVolverAtras = it)) },
                label = { Text("Botón Volver Atrás") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = t.txtVotoBlanco,
            onValueChange = { onUpdateTexts(t.copy(txtVotoBlanco = it)) },
            label = { Text("Texto Voto en Blanco") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        Text("PANTALLA DE IMPRESIÓN", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = t.printTitle,
            onValueChange = { onUpdateTexts(t.copy(printTitle = it)) },
            label = { Text("Título de Impresión") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = t.printSubtitle,
            onValueChange = { onUpdateTexts(t.copy(printSubtitle = it)) },
            label = { Text("Mensaje de Impresión") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------- TAB 4: COLORES Y ESTILOS ----------------
@Composable
fun ColorsConfigTab(
    config: UiSimulatorConfig,
    onUpdateColors: (ColorsConfig) -> Unit,
    onUpdateConfig: (UiSimulatorConfig) -> Unit = {}
) {
    val c = config.colors

    // Collect unique parties / lists to manage their colors as distinct groups
    val partyGroups = remember(config.intendentes, config.juntas, config.concejales) {
        val groups = mutableMapOf<String, CandidateModel>()
        (config.juntas + config.intendentes + config.concejales).forEach { cand ->
            if (!cand.isBlanco && (cand.party.isNotBlank() || cand.list.isNotBlank())) {
                val key = if (cand.list.isNotBlank()) "Lista ${cand.list} - ${cand.party}" else cand.party
                if (!groups.containsKey(key)) {
                    groups[key] = cand
                }
            }
        }
        groups
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("PALETAS INDIVIDUALES POR PARTIDO / GRUPO", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(
            text = "Personaliza el color de cada partido o grupo de concejales. Los cambios se aplican automáticamente a sus tarjetas, encabezados y pantallas de confirmación.",
            fontSize = 11.sp,
            color = Color.Gray
        )

        partyGroups.forEach { (groupName, sampleCand) ->
            val activeColor = getCandidatePrimaryColor(sampleCand, parseHexColor(c.primaryPartyColorHex))
            val currentHex = sampleCand.customColorHex ?: when (sampleCand.colorType?.lowercase()) {
                "red" -> c.primaryPartyColorHex
                "blue" -> "#1E40AF"
                "dark_green" -> "#14532D"
                "green", "light_green" -> "#15803D"
                "yellow" -> "#FACC15"
                "orange" -> "#EA580C"
                "purple" -> "#7E22CE"
                "cyan" -> "#0284C7"
                "teal" -> "#0D9488"
                "rose" -> "#E11D48"
                "slate" -> "#334155"
                else -> if (sampleCand.colorType?.startsWith("#") == true) sampleCand.colorType else c.primaryPartyColorHex
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(groupName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Afecta intendente, lista junta y sus concejales",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(activeColor)
                                .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val groupPresets = listOf(
                        "#C8102E", "#1E40AF", "#15803D", "#14532D", 
                        "#4ADE80", "#0284C7", "#7E22CE", "#EA580C", 
                        "#FACC15", "#0D9488", "#E11D48", "#334155"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        groupPresets.take(6).forEach { presetHex ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(presetHex))
                                    .border(
                                        width = if (currentHex.equals(presetHex, ignoreCase = true)) 2.dp else 1.dp,
                                        color = if (currentHex.equals(presetHex, ignoreCase = true)) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        val p = sampleCand.party
                                        val l = sampleCand.list
                                        val updatedInt = config.intendentes.map {
                                            if ((l.isNotBlank() && it.list == l) || it.party.equals(p, ignoreCase = true)) {
                                                it.copy(customColorHex = presetHex)
                                            } else it
                                        }
                                        val updatedJunta = config.juntas.map {
                                            if ((l.isNotBlank() && it.list == l) || it.party.equals(p, ignoreCase = true)) {
                                                it.copy(customColorHex = presetHex)
                                            } else it
                                        }
                                        val updatedConc = config.concejales.map {
                                            if ((l.isNotBlank() && it.list == l) || it.party.equals(p, ignoreCase = true)) {
                                                it.copy(customColorHex = presetHex)
                                            } else it
                                        }
                                        onUpdateConfig(config.copy(intendentes = updatedInt, juntas = updatedJunta, concejales = updatedConc))
                                    }
                            )
                        }

                        OutlinedTextField(
                            value = currentHex,
                            onValueChange = { newHex ->
                                val p = sampleCand.party
                                val l = sampleCand.list
                                val updatedInt = config.intendentes.map {
                                    if ((l.isNotBlank() && it.list == l) || it.party.equals(p, ignoreCase = true)) {
                                        it.copy(customColorHex = newHex)
                                    } else it
                                }
                                val updatedJunta = config.juntas.map {
                                    if ((l.isNotBlank() && it.list == l) || it.party.equals(p, ignoreCase = true)) {
                                        it.copy(customColorHex = newHex)
                                    } else it
                                }
                                val updatedConc = config.concejales.map {
                                    if ((l.isNotBlank() && it.list == l) || it.party.equals(p, ignoreCase = true)) {
                                        it.copy(customColorHex = newHex)
                                    } else it
                                }
                                onUpdateConfig(config.copy(intendentes = updatedInt, juntas = updatedJunta, concejales = updatedConc))
                            },
                            label = { Text("Hex", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text("COLORES GLOBALES DE LA INTERFAZ", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        ColorSettingItem(
            title = "Color Principal del Partido (Rojo por defecto)",
            description = "Color usado en encabezados, tarjetas activas y selección",
            hexValue = c.primaryPartyColorHex,
            presets = listOf("#C8102E", "#1E3A8A", "#15803D", "#EA580C", "#6D28D9", "#111827"),
            onColorChange = { onUpdateColors(c.copy(primaryPartyColorHex = it)) }
        )

        ColorSettingItem(
            title = "Fondo Pantalla de Inicio",
            description = "Fondo oscuro de bienvenida",
            hexValue = c.screenSaverBgHex,
            presets = listOf("#0F172A", "#000000", "#1E1B4B", "#18181B", "#172554"),
            onColorChange = { onUpdateColors(c.copy(screenSaverBgHex = it)) }
        )

        ColorSettingItem(
            title = "Fondo del Encabezado",
            description = "Barra superior de metadatos",
            hexValue = c.headerBgHex,
            presets = listOf("#4B5563", "#1F2937", "#374151", "#0F172A", "#1E293B"),
            onColorChange = { onUpdateColors(c.copy(headerBgHex = it)) }
        )

        ColorSettingItem(
            title = "Fondo del Pie de Página",
            description = "Barra inferior de navegación",
            hexValue = c.footerBgHex,
            presets = listOf("#D1D5DB", "#E5E7EB", "#9CA3AF", "#CBD5E1", "#F1F5F9"),
            onColorChange = { onUpdateColors(c.copy(footerBgHex = it)) }
        )

        ColorSettingItem(
            title = "Color Botón Reiniciar",
            description = "Botón naranja de reinicio en el resumen",
            hexValue = c.summaryButtonRestartHex,
            presets = listOf("#F97316", "#EA580C", "#DC2626", "#EAB308", "#4F46E5"),
            onColorChange = { onUpdateColors(c.copy(summaryButtonRestartHex = it)) }
        )

        ColorSettingItem(
            title = "Color Botón Imprimir",
            description = "Botón verde de confirmación e impresión",
            hexValue = c.summaryButtonPrintHex,
            presets = listOf("#16A34A", "#15803D", "#059669", "#2563EB", "#0D9488"),
            onColorChange = { onUpdateColors(c.copy(summaryButtonPrintHex = it)) }
        )

        ColorSettingItem(
            title = "Color Acento Alto Contraste",
            description = "Color amarillo del modo accesible",
            hexValue = c.highContrastAccentHex,
            presets = listOf("#FFD700", "#FFFF00", "#00FFFF", "#00FF00", "#FFA500"),
            onColorChange = { onUpdateColors(c.copy(highContrastAccentHex = it)) }
        )
    }
}

@Composable
fun ColorSettingItem(
    title: String,
    description: String,
    hexValue: String,
    presets: List<String>,
    onColorChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(description, fontSize = 11.sp, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(parseHexColor(hexValue))
                        .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { presetHex ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(presetHex))
                            .border(
                                width = if (hexValue.equals(presetHex, ignoreCase = true)) 2.dp else 1.dp,
                                color = if (hexValue.equals(presetHex, ignoreCase = true)) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { onColorChange(presetHex) }
                    )
                }
                OutlinedTextField(
                    value = hexValue,
                    onValueChange = onColorChange,
                    label = { Text("Hex", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ---------------- TAB 5: SEGURIDAD Y RESPALDO ----------------
@Composable
fun SecurityAndBackupTab(
    config: UiSimulatorConfig,
    onUpdateConfig: (UiSimulatorConfig) -> Unit,
    onLogout: () -> Unit,
    onShowExport: () -> Unit,
    onShowImport: () -> Unit,
    onShowReset: () -> Unit,
    onShowExportExcel: () -> Unit = {},
    onShowImportExcel: () -> Unit = {},
    onShowTemplateExcel: () -> Unit = {},
    onShowExportPhotos: () -> Unit = {},
    onShowImportPhotos: () -> Unit = {},
    onShowExportDimensions: () -> Unit = {},
    onShowImportDimensions: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var showCurrentPin by remember { mutableStateOf(false) }
    var showNewPin by remember { mutableStateOf(false) }
    var showConfirmPin by remember { mutableStateOf(false) }
    var pinChangeError by remember { mutableStateOf<String?>(null) }
    var pinChangeSuccess by remember { mutableStateOf<String?>(null) }

    var adminCodeInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordChangeError by remember { mutableStateOf<String?>(null) }
    var passwordChangeSuccess by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("VISUALIZACIÓN Y FUNCIONES DEL SIMULADOR", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Modo Pantalla Completa", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Oculta encabezado y pie de página para simulación pura", fontSize = 11.sp, color = Color.Gray)
            }
            Switch(
                checked = config.isFullScreen,
                onCheckedChange = { onUpdateConfig(config.copy(isFullScreen = it)) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Des-esconder y desbloquear candidatos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Permite seleccionar y votar por todas las listas/candidatos", fontSize = 11.sp, color = Color.Gray)
            }
            Switch(
                checked = config.unhideAllCandidates,
                onCheckedChange = { onUpdateConfig(config.copy(unhideAllCandidates = it)) }
            )
        }

        HorizontalDivider()
        Text("GESTIÓN EN EXCEL / HOJA DE CÁLCULO (.CSV)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(
            "Importa o exporta masivamente nombres de intendentes, listas de junta municipal, concejales, siglas, opciones y colores para personalizar fácilmente en Excel o Google Sheets.",
            fontSize = 11.sp,
            color = Color.Gray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onShowExportExcel,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exportar Excel")
            }
            Button(
                onClick = onShowImportExcel,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Importar Excel")
            }
        }

        OutlinedButton(
            onClick = onShowTemplateExcel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Ver Formato / Plantilla de Ejemplo")
        }

        HorizontalDivider()
        Text("FOTOS DE CANDIDATOS POR PARTIDO (.ZIP)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(
            "Exporta e importa todas las fotos de los candidatos organizadas en carpetas con la sigla de la agrupación (acr, mca, etc.) y numeradas (0.jpg para intendente, 1 al 12.jpg para concejales).",
            fontSize = 11.sp,
            color = Color.Gray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onShowExportPhotos,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exportar Fotos (.zip)")
            }
            Button(
                onClick = onShowImportPhotos,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Importar Fotos (.zip)")
            }
        }

        HorizontalDivider()
        Text("CONFIGURACIÓN PERSONALIZADA DE DIMENSIONES (.JSON)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(
            "Exporta e importa únicamente la calibración de dimensiones, escalas y columnas para guardar o transferir una configuración personalizada a otros dispositivos.",
            fontSize = 11.sp,
            color = Color.Gray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onShowExportDimensions,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exportar Dimensiones")
            }
            OutlinedButton(
                onClick = onShowImportDimensions,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Importar Dimensiones")
            }
        }

        HorizontalDivider()
        Text("RESPALDO COMPLETO DE CONFIGURACIÓN (JSON)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onShowExport,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exportar JSON")
            }
            OutlinedButton(
                onClick = onShowImport,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Importar JSON")
            }
        }

        Button(
            onClick = onShowReset,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restablecer Valores de Fábrica (Reset)", fontWeight = FontWeight.Bold)
        }

        HorizontalDivider()
        Text("SEGURIDAD Y ADMINISTRACIÓN", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

        // Card para cambiar el PIN de seguridad de la tuerca de configuración
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_cambiar_pin_configuracion"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cambiar PIN de la Tuerca de Configuración",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Este PIN protege el acceso a este panel cuando se presiona la tuerca de configuración.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentPinInput,
                    onValueChange = {
                        currentPinInput = it.filter { c -> c.isDigit() }.take(8)
                        pinChangeError = null
                        pinChangeSuccess = null
                    },
                    label = { Text("PIN Actual", fontSize = 11.sp) },
                    placeholder = { Text("Ingrese PIN actual") },
                    singleLine = true,
                    visualTransformation = if (showCurrentPin) VisualTransformation.None else PasswordVisualTransformation('•'),
                    trailingIcon = {
                        TextButton(onClick = { showCurrentPin = !showCurrentPin }) {
                            Text(
                                text = if (showCurrentPin) "Ocultar" else "Ver",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("config_current_pin_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = {
                        newPinInput = it.filter { c -> c.isDigit() }.take(8)
                        pinChangeError = null
                        pinChangeSuccess = null
                    },
                    label = { Text("Nuevo PIN (4 a 8 dígitos)", fontSize = 11.sp) },
                    placeholder = { Text("Ingrese nuevo PIN") },
                    singleLine = true,
                    visualTransformation = if (showNewPin) VisualTransformation.None else PasswordVisualTransformation('•'),
                    trailingIcon = {
                        TextButton(onClick = { showNewPin = !showNewPin }) {
                            Text(
                                text = if (showNewPin) "Ocultar" else "Ver",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("config_new_pin_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = confirmPinInput,
                    onValueChange = {
                        confirmPinInput = it.filter { c -> c.isDigit() }.take(8)
                        pinChangeError = null
                        pinChangeSuccess = null
                    },
                    label = { Text("Confirmar Nuevo PIN", fontSize = 11.sp) },
                    placeholder = { Text("Repita el nuevo PIN") },
                    singleLine = true,
                    visualTransformation = if (showConfirmPin) VisualTransformation.None else PasswordVisualTransformation('•'),
                    trailingIcon = {
                        TextButton(onClick = { showConfirmPin = !showConfirmPin }) {
                            Text(
                                text = if (showConfirmPin) "Ocultar" else "Ver",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("config_confirm_pin_input")
                )

                if (pinChangeError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = pinChangeError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (pinChangeSuccess != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = pinChangeSuccess!!,
                        color = Color(0xFF16A34A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            SoundPlayer.playClick()
                            val storedPin = AuthManager.getSettingsPin(context)
                            if (currentPinInput != storedPin && currentPinInput != AuthManager.ADMIN_CODE) {
                                pinChangeError = "El PIN actual es incorrecto"
                            } else if (newPinInput.length < 4) {
                                pinChangeError = "El nuevo PIN debe tener al menos 4 números"
                            } else if (newPinInput != confirmPinInput) {
                                pinChangeError = "Los nuevos PINs ingresados no coinciden"
                            } else {
                                AuthManager.setSettingsPin(context, newPinInput)
                                SoundPlayer.playPrint()
                                pinChangeSuccess = "¡PIN de seguridad actualizado con éxito! (Nuevo PIN: $newPinInput)"
                                pinChangeError = null
                                currentPinInput = ""
                                newPinInput = ""
                                confirmPinInput = ""
                                Toast.makeText(context, "PIN de configuración actualizado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.3f).testTag("save_new_pin_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar Nuevo PIN")
                    }

                    OutlinedButton(
                        onClick = {
                            SoundPlayer.playClick()
                            val storedPin = AuthManager.getSettingsPin(context)
                            if (currentPinInput != storedPin && currentPinInput != AuthManager.ADMIN_CODE) {
                                pinChangeError = "Ingrese el PIN actual (o código de administrador) para restablecer"
                            } else {
                                AuthManager.setSettingsPin(context, AuthManager.DEFAULT_SETTINGS_PIN)
                                SoundPlayer.playPrint()
                                pinChangeSuccess = "PIN restablecido al valor original"
                                pinChangeError = null
                                currentPinInput = ""
                                newPinInput = ""
                                confirmPinInput = ""
                                Toast.makeText(context, "PIN restablecido al valor original", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.1f).testTag("reset_pin_default_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restablecer PIN")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Cambiar Contraseña de Acceso", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Requiere autorización con código de Administrador", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = adminCodeInput,
                    onValueChange = {
                        adminCodeInput = it
                        passwordChangeError = null
                        passwordChangeSuccess = null
                    },
                    label = { Text("Código de Administrador", fontSize = 11.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation('*'),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPasswordInput,
                    onValueChange = {
                        newPasswordInput = it
                        passwordChangeError = null
                        passwordChangeSuccess = null
                    },
                    label = { Text("Nueva Contraseña", fontSize = 11.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation('*'),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = {
                        confirmPasswordInput = it
                        passwordChangeError = null
                        passwordChangeSuccess = null
                    },
                    label = { Text("Confirmar Nueva Contraseña", fontSize = 11.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation('*'),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                if (passwordChangeError != null) {
                    Text(passwordChangeError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                if (passwordChangeSuccess != null) {
                    Text(passwordChangeSuccess!!, color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        SoundPlayer.playClick()
                        if (adminCodeInput != AuthManager.ADMIN_CODE) {
                            passwordChangeError = "Código de administrador incorrecto"
                        } else if (newPasswordInput.isBlank()) {
                            passwordChangeError = "La nueva contraseña no puede estar vacía"
                        } else if (newPasswordInput != confirmPasswordInput) {
                            passwordChangeError = "Las contraseñas no coinciden"
                        } else {
                            AuthManager.setPassword(context, newPasswordInput)
                            passwordChangeSuccess = "Contraseña cambiada exitosamente"
                            passwordChangeError = null
                            adminCodeInput = ""
                            newPasswordInput = ""
                            confirmPasswordInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Nueva Contraseña")
                }
            }
        }

        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cerrar Sesión / Bloquear Simulador")
        }
    }
}

// ---------------- DIALOGOS DE EXCEL / CSV ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportExcelDialog(
    config: UiSimulatorConfig,
    onDismiss: () -> Unit,
    onShowTemplate: () -> Unit
) {
    val context = LocalContext.current
    var delimiter by remember { mutableStateOf(";") }
    val csvContent = remember(config, delimiter) { ConfigManager.exportCandidatesToCsv(config, delimiter) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar Candidatos a Excel (.csv)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Se generará una tabla estructurada con ${config.intendentes.size} intendentes, ${config.juntas.size} listas de junta y ${config.concejales.size} concejales.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Separador de columnas:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = delimiter == ";",
                            onClick = { delimiter = ";" },
                            label = { Text("; (Excel Español)", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = delimiter == ",",
                            onClick = { delimiter = "," },
                            label = { Text(", (Estándar)", fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = csvContent,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Candidatos_Simulador.csv", csvContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Tabla copiada. Puedes pegarla directamente en Excel", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar Tabla", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Candidatos_Simulador_Elecciones.csv")
                                putExtra(Intent.EXTRA_TEXT, csvContent)
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Compartir o Abrir con Excel")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir / Abrir", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Listo")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                onShowTemplate()
            }) {
                Text("Ver Formato / Plantilla")
            }
        }
    )
}

@Composable
fun ImportExcelDialog(
    config: UiSimulatorConfig,
    onDismiss: () -> Unit,
    onApply: (UiSimulatorConfig) -> Unit,
    onShowTemplate: () -> Unit
) {
    val context = LocalContext.current
    var csvInput by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader(Charsets.UTF_8).readText()
                    csvInput = content
                    Toast.makeText(context, "Archivo cargado correctamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                importError = "Error al leer archivo: ${e.message}"
            }
        }
    }

    val parsedResult = remember(csvInput) {
        if (csvInput.isNotBlank()) ConfigManager.parseCandidatesCsv(csvInput) else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.92f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importar Candidatos desde Excel / CSV", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Carga un archivo .csv exportado de Excel / Google Sheets, o pega las filas directamente.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cargar Archivo .CSV", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            csvInput = ConfigManager.generateCsvTemplate(";")
                            Toast.makeText(context, "Plantilla de ejemplo cargada", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cargar Ejemplo", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = csvInput,
                    onValueChange = {
                        csvInput = it
                        importError = null
                    },
                    placeholder = {
                        Text(
                            "Pega aquí la tabla de candidatos desde Excel...\nColumnas: CATEGORIA;NOMBRE;LISTA;SIGLAS;PARTIDO;OPCION;COLOR_HEX;HABILITADO;ES_BLANCO",
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )

                if (importError != null) {
                    Text(importError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Live Parsed Status
                if (parsedResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (parsedResult.totalCount > 0)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Vista previa del contenido detectado:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• Intendentes: ${parsedResult.intendentes.size}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("• Juntas: ${parsedResult.juntas.size}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("• Concejales: ${parsedResult.concejales.size}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (parsedResult.warnings.isNotEmpty()) {
                                parsedResult.warnings.forEach { w ->
                                    Text("⚠️ $w", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    // Sample preview items
                    val previewItems = (parsedResult.intendentes.take(2) + parsedResult.juntas.take(2) + parsedResult.concejales.take(2))
                    if (previewItems.isNotEmpty()) {
                        Text("Muestra de candidatos procesados:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            previewItems.forEach { cand ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val col = getCandidatePrimaryColor(cand, Color(0xFFC8102E))
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cand.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (cand.optionId != null) "Opc ${cand.optionId} / L.${cand.list}" else "Lista ${cand.list} - ${cand.party}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        if (parsedResult == null || parsedResult.totalCount == 0) {
                            importError = "No hay candidatos válidos para importar."
                        } else {
                            val newConfig = config.copy(
                                intendentes = if (parsedResult.intendentes.isNotEmpty()) parsedResult.intendentes else config.intendentes,
                                juntas = if (parsedResult.juntas.isNotEmpty()) parsedResult.juntas else config.juntas,
                                concejales = if (parsedResult.concejales.isNotEmpty()) parsedResult.concejales else config.concejales
                            )
                            onApply(newConfig)
                            Toast.makeText(context, "Candidatos importados exitosamente (${parsedResult.totalCount})", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    enabled = parsedResult != null && parsedResult.totalCount > 0
                ) {
                    Text("Reemplazar Todo", fontSize = 11.sp)
                }

                FilledTonalButton(
                    onClick = {
                        if (parsedResult == null || parsedResult.totalCount == 0) {
                            importError = "No hay candidatos válidos para importar."
                        } else {
                            fun mergeList(current: List<CandidateModel>, newItems: List<CandidateModel>): List<CandidateModel> {
                                val map = current.associateBy { it.id }.toMutableMap()
                                newItems.forEach { item ->
                                    map[item.id] = item
                                }
                                return map.values.toList()
                            }

                            val newConfig = config.copy(
                                intendentes = if (parsedResult.intendentes.isNotEmpty()) mergeList(config.intendentes, parsedResult.intendentes) else config.intendentes,
                                juntas = if (parsedResult.juntas.isNotEmpty()) mergeList(config.juntas, parsedResult.juntas) else config.juntas,
                                concejales = if (parsedResult.concejales.isNotEmpty()) mergeList(config.concejales, parsedResult.concejales) else config.concejales
                            )
                            onApply(newConfig)
                            Toast.makeText(context, "Candidatos combinados exitosamente", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    enabled = parsedResult != null && parsedResult.totalCount > 0
                ) {
                    Text("Combinar", fontSize = 11.sp)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun TemplateExcelDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val templateCsv = remember { ConfigManager.generateCsvTemplate(";") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Formato de Plantilla Excel (.csv)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Estructura de columnas compatible con Microsoft Excel y Google Sheets:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("1. CATEGORIA: INTENDENTE, JUNTA o CONCEJAL", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("2. NOMBRE: Nombre completo del postulante", fontSize = 11.sp)
                        Text("3. LISTA: Número de lista (ej: 1, 2, 3)", fontSize = 11.sp)
                        Text("4. SIGLAS: Siglas de la agrupación (ej: ACR, MCA)", fontSize = 11.sp)
                        Text("5. PARTIDO: Nombre de la agrupación política", fontSize = 11.sp)
                        Text("6. OPCION: Número de opción (1-12) para concejal", fontSize = 11.sp)
                        Text("7. COLOR_HEX: Color en hexadecimal (ej: #C8102E)", fontSize = 11.sp)
                        Text("8. HABILITADO: SI / NO", fontSize = 11.sp)
                        Text("9. ES_BLANCO: SI / NO", fontSize = 11.sp)
                        Text("10. ID: Identificador único opcional", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = templateCsv,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Plantilla_Candidatos.csv", templateCsv)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Plantilla copiada al portapapeles", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copiar Plantilla")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

