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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// -------------------------------------------------------------
// DIÁLOGO: EXPORTAR FOTOS DE CANDIDATOS (.ZIP)
// -------------------------------------------------------------
@Composable
fun ExportPhotosDialog(
    config: UiSimulatorConfig,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Summary of parties
    val partyFolders = remember(config) {
        val allCands = (config.intendentes + config.concejales).filter { !it.isBlanco }
        allCands.map { CandidatePhotoManager.getPartyFolderName(it) }.distinct()
    }

    // Launcher for saving ZIP directly to user-chosen storage location
    val createZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isExporting = true
                errorMessage = null
                try {
                    val count = withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            CandidatePhotoManager.exportPhotosToZip(context, config, out)
                        } ?: 0
                    }
                    isExporting = false
                    exportSuccessMessage = "Se exportaron con éxito $count fotos organizadas por carpetas de partido en su archivo ZIP."
                    Toast.makeText(context, "Archivo ZIP guardado correctamente", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    isExporting = false
                    errorMessage = "Error al guardar el archivo ZIP: ${e.localizedMessage}"
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exportar Fotos de Candidatos (.zip)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Se empaquetarán todas las fotos de los candidatos en un archivo ZIP estructurado por carpetas de partido:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "ESTRUCTURA DE CARPETAS Y ARCHIVOS:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Cada carpeta lleva la sigla de la agrupación (ej: acr/, mca/, apv/)\n" +
                                   "• Intendente: 0.jpg\n" +
                                   "• Concejales: 1.jpg al 12.jpg (según opción)\n" +
                                   "• Incluye archivo INSTRUCCIONES_FOTOS.txt",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }

                Text(
                    text = "Partidos incluidos: ${partyFolders.joinToString(", ")}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isExporting) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Generando y empaquetando fotos...", fontSize = 11.sp, color = Color.Gray)
                }

                if (exportSuccessMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        border = BorderStroke(1.dp, Color(0xFF16A34A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(exportSuccessMessage!!, fontSize = 11.sp, color = Color(0xFF166534))
                        }
                    }
                }

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage!!, fontSize = 11.sp, color = Color(0xFF991B1B))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save as file to Downloads / USB / Drive
                    Button(
                        onClick = {
                            SoundPlayer.playClick()
                            createZipLauncher.launch("fotos_candidatos_elecciones.zip")
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isExporting
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar Archivo ZIP", fontSize = 11.sp)
                    }

                    // Share directly via apps
                    OutlinedButton(
                        onClick = {
                            SoundPlayer.playClick()
                            coroutineScope.launch {
                                isExporting = true
                                errorMessage = null
                                try {
                                    val cacheFile = File(context.cacheDir, "fotos_candidatos.zip")
                                    val count = withContext(Dispatchers.IO) {
                                        FileOutputStream(cacheFile).use { out ->
                                            CandidatePhotoManager.exportPhotosToZip(context, config, out)
                                        }
                                    }
                                    isExporting = false
                                    exportSuccessMessage = "Se empaquetaron $count fotos en el archivo temporal."

                                    val contentUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        cacheFile
                                    )
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/zip"
                                        putExtra(Intent.EXTRA_STREAM, contentUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val chooser = Intent.createChooser(sendIntent, "Compartir paquete de fotos ZIP")
                                    context.startActivity(chooser)
                                } catch (e: Exception) {
                                    isExporting = false
                                    errorMessage = "Error al compartir fotos: ${e.localizedMessage}"
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isExporting
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir ZIP", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

// -------------------------------------------------------------
// DIÁLOGO: IMPORTAR FOTOS DE CANDIDATOS (.ZIP)
// -------------------------------------------------------------
@Composable
fun ImportPhotosDialog(
    config: UiSimulatorConfig,
    onDismiss: () -> Unit,
    onPhotosImported: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isImporting by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<CandidatePhotoManager.ImportPhotosResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val openZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isImporting = true
                errorMessage = null
                importResult = null
                try {
                    val result = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            CandidatePhotoManager.importPhotosFromZip(context, config, stream)
                        } ?: CandidatePhotoManager.ImportPhotosResult(0, emptyList(), listOf("No se pudo abrir el archivo seleccionado"))
                    }
                    isImporting = false
                    importResult = result
                    if (result.totalImported > 0) {
                        SoundPlayer.playPrint()
                        Toast.makeText(context, "Se importaron ${result.totalImported} fotos correctamente", Toast.LENGTH_LONG).show()
                        onPhotosImported()
                    } else if (result.warnings.isNotEmpty()) {
                        errorMessage = result.warnings.firstOrNull()
                    }
                } catch (e: Exception) {
                    isImporting = false
                    errorMessage = "Error al procesar el archivo ZIP: ${e.localizedMessage}"
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Importar Fotos de Candidatos (.zip)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Selecciona un archivo .zip que contenga las carpetas de los partidos y las fotos renombradas con números:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "FORMATO ESPERADO DENTRO DEL .ZIP:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "partido/\n" +
                                   "├── 0.jpg        (Intendente)\n" +
                                   "├── 1.jpg        (Concejal Opción 1)\n" +
                                   "├── 2.jpg        (Concejal Opción 2)\n" +
                                   "└── ... 12.jpg   (Concejal Opción 12)\n\n" +
                                   "Ejemplos de carpetas válidas: acr, mca, apv, etc.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }

                if (isImporting) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Descomprimiendo y asignando fotos a los candidatos...", fontSize = 11.sp, color = Color.Gray)
                }

                Button(
                    onClick = {
                        SoundPlayer.playClick()
                        openZipLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isImporting
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Seleccionar Archivo .ZIP de Fotos")
                }

                importResult?.let { result ->
                    if (result.totalImported > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            border = BorderStroke(1.dp, Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "¡Importación exitosa! ${result.totalImported} fotos asignadas.",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF166534)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = result.matchedDetails.take(6).joinToString("\n") +
                                            if (result.matchedDetails.size > 6) "\n... y ${result.matchedDetails.size - 6} más" else "",
                                    fontSize = 10.sp,
                                    color = Color(0xFF166534),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    if (result.warnings.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    "Avisos (${result.warnings.size}):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = result.warnings.take(4).joinToString("\n"),
                                    fontSize = 10.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage!!, fontSize = 11.sp, color = Color(0xFF991B1B))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Listo")
            }
        }
    )
}

// -------------------------------------------------------------
// DIÁLOGO: EXPORTAR CONFIGURACIÓN DE DIMENSIONES (.JSON)
// -------------------------------------------------------------
@Composable
fun ExportDimensionsDialog(
    dimensions: DimensionsConfig,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val jsonStr = remember(dimensions) { ConfigManager.dimensionsToJson(dimensions) }

    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(jsonStr.toByteArray(Charsets.UTF_8))
                        }
                    }
                    Toast.makeText(context, "Configuración de dimensiones guardada con éxito", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar archivo: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .widthIn(max = 780.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar Dimensiones Personalizadas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Puedes copiar este formato JSON o guardarlo como archivo para transferir tu configuración de dimensiones a otros dispositivos o pantallas:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = jsonStr,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("DimensionesConfig", jsonStr)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Dimensiones copiadas al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar JSON", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            createJsonLauncher.launch("dimensiones_personalizadas.json")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar Archivo", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

// -------------------------------------------------------------
// DIÁLOGO: IMPORTAR CONFIGURACIÓN DE DIMENSIONES (.JSON)
// -------------------------------------------------------------
@Composable
fun ImportDimensionsDialog(
    onDismiss: () -> Unit,
    onApplyDimensions: (DimensionsConfig) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var jsonInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var parsedPreview by remember { mutableStateOf<DimensionsConfig?>(null) }

    val openJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader().use { it.readText() }
                        }
                    } ?: ""
                    jsonInput = content
                    val parsed = ConfigManager.dimensionsFromJson(content)
                    if (parsed != null) {
                        parsedPreview = parsed
                        errorMessage = null
                    } else {
                        errorMessage = "El archivo no contiene un formato de dimensiones válido"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error al leer archivo: ${e.localizedMessage}"
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .widthIn(max = 780.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importar Configuración de Dimensiones", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Pega el código JSON de dimensiones o selecciona un archivo .json guardado previamente:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            openJsonLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cargar Archivo .json", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (text.isNotBlank()) {
                                jsonInput = text
                                val parsed = ConfigManager.dimensionsFromJson(text)
                                if (parsed != null) {
                                    parsedPreview = parsed
                                    errorMessage = null
                                } else {
                                    errorMessage = "El texto del portapapeles no es un JSON de dimensiones válido"
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pegar Portapapeles", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = {
                        jsonInput = it
                        val parsed = ConfigManager.dimensionsFromJson(it)
                        if (parsed != null) {
                            parsedPreview = parsed
                            errorMessage = null
                        } else if (it.isNotBlank()) {
                            errorMessage = "Formato JSON no válido"
                            parsedPreview = null
                        } else {
                            errorMessage = null
                            parsedPreview = null
                        }
                    },
                    label = { Text("Código JSON de Dimensiones") },
                    placeholder = { Text("{\n  \"intendenteCardHeightDp\": 138,\n  ...\n}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    shape = RoundedCornerShape(10.dp)
                )

                parsedPreview?.let { preview ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFF22C55E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vista previa de configuración detectada:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF166534))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Altura Intendente: ${preview.intendenteCardHeightDp} dp | Junta: ${preview.juntaCardHeightDp} dp | Concejal: ${preview.concejalCardHeightDp} dp\n" +
                                "• Fotos Intendente: ${preview.avatarSizeIntendenteDp} dp | Concejal: ${preview.avatarSizeConcejalDp} dp\n" +
                                "• Columnas Intendente: ${preview.gridColumnsIntendente} | Junta: ${preview.gridColumnsJunta} | Concejales: ${preview.gridColumnsConcejal}",
                                fontSize = 10.sp,
                                color = Color(0xFF166534),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dims = parsedPreview ?: ConfigManager.dimensionsFromJson(jsonInput)
                    if (dims != null) {
                        SoundPlayer.playPrint()
                        ConfigManager.saveCustomDimensions(context, dims)
                        onApplyDimensions(dims)
                        Toast.makeText(context, "Dimensiones importadas y aplicadas con éxito", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    } else {
                        errorMessage = "Por favor ingrese o cargue una configuración válida"
                    }
                },
                enabled = parsedPreview != null
            ) {
                Text("Aplicar Dimensiones")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
