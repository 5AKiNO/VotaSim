package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.toColorInt
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object CandidatePhotoManager {

    /**
     * Determines standard clean folder name for candidate's party:
     * e.g., "acr", "mca", "apv", etc.
     */
    fun getPartyFolderName(candidate: CandidateModel): String {
        val cleanInitials = candidate.initials.trim().lowercase()
            .replace(Regex("[^a-z0-9_]"), "")
        if (cleanInitials.isNotBlank()) return cleanInitials

        val cleanParty = candidate.party.trim().lowercase()
            .replace("partido ", "")
            .replace("alianza ", "")
            .replace("movimiento ", "")
            .replace(" ", "_")
            .replace(Regex("[^a-z0-9_]"), "")
        if (cleanParty.isNotBlank()) return cleanParty.take(15)

        val cleanList = candidate.list.trim().lowercase().replace(" ", "_")
        return if (cleanList.isNotBlank()) "lista_$cleanList" else "general"
    }

    /**
     * Checks if a candidate matches a folder name or party key
     */
    fun matchesParty(candidate: CandidateModel, folder: String): Boolean {
        val cleanFolder = folder.trim().lowercase().replace(" ", "_")
        val cleanInitials = candidate.initials.trim().lowercase().replace(" ", "_")
        val cleanList = candidate.list.trim().lowercase().replace(" ", "_")
        val cleanParty = candidate.party.trim().lowercase().replace(" ", "_")

        if (cleanFolder.isBlank()) return false
        if (cleanInitials.isNotBlank() && (cleanFolder == cleanInitials || cleanFolder.contains(cleanInitials))) return true
        if (cleanFolder == "lista_$cleanList" || cleanFolder == "lista$cleanList" || cleanFolder == cleanList) return true
        if (cleanParty.isNotBlank() && (cleanParty.contains(cleanFolder) || cleanFolder.contains(cleanParty.take(6)))) return true
        return false
    }

    /**
     * Finds image file if stored locally in app internal files
     */
    fun getCandidateImageFile(context: Context, candidateId: String): File? {
        val png = File(context.filesDir, "candidate_$candidateId.png")
        if (png.exists() && png.length() > 0) return png
        val jpg = File(context.filesDir, "candidate_$candidateId.jpg")
        if (jpg.exists() && jpg.length() > 0) return jpg
        return null
    }

    /**
     * Creates a high-quality placeholder avatar image if candidate doesn't have custom image yet
     */
    fun createCandidatePlaceholderBitmap(
        candidate: CandidateModel,
        labelNumber: String,
        categoryTitle: String
    ): Bitmap {
        val width = 320
        val height = 320
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val hex = candidate.customColorHex ?: when (candidate.colorType) {
            "red" -> "#C8102E"
            "blue" -> "#1E40AF"
            "green" -> "#15803D"
            else -> "#334155"
        }
        val bgColor = try { hex.toColorInt() } catch (_: Exception) { 0xFF334155.toInt() }

        // Canvas light background
        canvas.drawColor(0xFFF8FAFC.toInt())

        // Card header banner
        val bgPaint = Paint().apply {
            color = bgColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 48f, bgPaint)

        // Circle avatar area
        val circlePaint = Paint().apply {
            color = bgColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width / 2f, 150f, 72f, circlePaint)

        // Number / Initials in circle
        val numPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 52f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(labelNumber, width / 2f, 168f, numPaint)

        // Category header text
        val headerPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 20f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val pLabel = candidate.initials.ifBlank { "LISTA " + candidate.list }
        canvas.drawText("$pLabel • $categoryTitle", width / 2f, 32f, headerPaint)

        // Candidate Name text
        val textPaint = Paint().apply {
            color = 0xFF0F172A.toInt()
            textSize = 19f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val nameWords = candidate.name.split(" ").filter { it.isNotBlank() }
        if (nameWords.size > 2) {
            val line1 = nameWords.take(2).joinToString(" ")
            val line2 = nameWords.drop(2).joinToString(" ")
            canvas.drawText(line1, width / 2f, 255f, textPaint)
            canvas.drawText(line2, width / 2f, 285f, textPaint)
        } else {
            canvas.drawText(candidate.name, width / 2f, 270f, textPaint)
        }

        return bitmap
    }

    /**
     * Exports all candidate photos into a structured ZIP stream:
     * - <partido>/0.jpg (Intendente)
     * - <partido>/1.jpg .. 12.jpg (Concejales)
     * Includes INSTRUCCIONES_FOTOS.txt.
     */
    fun exportPhotosToZip(
        context: Context,
        config: UiSimulatorConfig,
        outputStream: OutputStream
    ): Int {
        var exportedCount = 0
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
            // Instructions file
            val instructions = """
===================================================================
ORGANIZACIÓN DE FOTOS DE CANDIDATOS - SIMULADOR DE VOTACIÓN MUNICIPAL
===================================================================

Las fotos están organizadas en carpetas con la sigla de la agrupación:
Ejemplos:
- acr/
- mca/
- apv/

Dentro de cada carpeta se encuentran las fotos numeradas:
- 0.jpg  : Foto del candidato a INTENDENTE
- 1.jpg  : Foto del CONCEJAL Opción 1
- 2.jpg  : Foto del CONCEJAL Opción 2
...
- 12.jpg : Foto del CONCEJAL Opción 12

PASOS PARA MODIFICAR O IMPORTAR:
1. Reemplace los archivos en cada carpeta con sus fotos reales (.jpg o .png).
2. Conserve los mismos nombres de archivos (0.jpg, 1.jpg, etc.).
3. Comprima la estructura en un archivo .ZIP.
4. En la aplicación, abra Configuración -> Fotos de Candidatos -> Importar Fotos (.zip).
===================================================================
            """.trimIndent()
            zipOut.putNextEntry(ZipEntry("INSTRUCCIONES_FOTOS.txt"))
            zipOut.write(instructions.toByteArray(Charsets.UTF_8))
            zipOut.closeEntry()

            // 1. Export Intendentes: "<partido>/0.jpg"
            for (intendente in config.intendentes) {
                if (intendente.isBlanco) continue
                val folder = getPartyFolderName(intendente)
                val path = "$folder/0.jpg"
                zipOut.putNextEntry(ZipEntry(path))

                val customFile = getCandidateImageFile(context, intendente.id)
                if (customFile != null) {
                    customFile.inputStream().use { it.copyTo(zipOut) }
                } else {
                    val bmp = createCandidatePlaceholderBitmap(intendente, "0", "INTENDENTE")
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, zipOut)
                }
                zipOut.closeEntry()
                exportedCount++
            }

            // 2. Export Concejales: "<partido>/1.jpg" .. "12.jpg"
            val groupedConcejales = config.concejales
                .filter { !it.isBlanco }
                .groupBy { getPartyFolderName(it) }

            for ((folder, concejalesList) in groupedConcejales) {
                concejalesList.forEachIndexed { idx, concejal ->
                    val opt = concejal.optionId ?: (idx + 1)
                    val path = "$folder/$opt.jpg"
                    zipOut.putNextEntry(ZipEntry(path))

                    val customFile = getCandidateImageFile(context, concejal.id)
                    if (customFile != null) {
                        customFile.inputStream().use { it.copyTo(zipOut) }
                    } else {
                        val bmp = createCandidatePlaceholderBitmap(concejal, "$opt", "OPCIÓN $opt")
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, zipOut)
                    }
                    zipOut.closeEntry()
                    exportedCount++
                }
            }
            zipOut.finish()
        }
        return exportedCount
    }

    data class ImportPhotosResult(
        val totalImported: Int,
        val matchedDetails: List<String>,
        val warnings: List<String>
    )

    /**
     * Imports photos from a ZIP stream:
     * - Identifies party folder (acr, mca, etc.)
     * - Identifies candidate: 0 -> Intendente, 1..12 -> Concejal
     * - Writes to internal storage candidate_<id>.png
     */
    fun importPhotosFromZip(
        context: Context,
        config: UiSimulatorConfig,
        inputStream: InputStream
    ): ImportPhotosResult {
        var totalImported = 0
        val matchedDetails = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        try {
            val zipIn = ZipInputStream(BufferedInputStream(inputStream))
            var entry: ZipEntry? = zipIn.nextEntry

            while (entry != null) {
                if (!entry.isDirectory) {
                    val rawName = entry.name.replace('\\', '/')
                    if (!rawName.startsWith("__MACOSX") && !rawName.contains(".DS_Store") && !rawName.endsWith(".txt", ignoreCase = true)) {
                        val parts = rawName.split('/').filter { it.isNotBlank() }
                        if (parts.isNotEmpty()) {
                            val fileName = parts.last()
                            val nameWithoutExt = fileName.substringBeforeLast('.')
                            val ext = fileName.substringAfterLast('.', "").lowercase()

                            if (ext in listOf("jpg", "jpeg", "png", "webp")) {
                                val folderName = if (parts.size >= 2) parts[parts.size - 2] else ""
                                val number = nameWithoutExt.filter { it.isDigit() }.toIntOrNull()
                                val isIntendente = number == 0 || nameWithoutExt.contains("intendente", ignoreCase = true)

                                val matchedCandidate: CandidateModel? = if (isIntendente) {
                                    config.intendentes.filter { !it.isBlanco }.find { cand ->
                                        folderName.isBlank() || matchesParty(cand, folderName)
                                    }
                                } else if (number != null && number > 0) {
                                    val partyConcejales = config.concejales.filter { !it.isBlanco && (folderName.isBlank() || matchesParty(it, folderName)) }
                                    partyConcejales.find { it.optionId == number }
                                        ?: partyConcejales.getOrNull(number - 1)
                                } else {
                                    null
                                }

                                if (matchedCandidate != null) {
                                    val targetFile = File(context.filesDir, "candidate_${matchedCandidate.id}.png")
                                    FileOutputStream(targetFile).use { out ->
                                        zipIn.copyTo(out)
                                    }
                                    totalImported++
                                    val label = if (isIntendente) "Intendente" else "Concejal Opción $number"
                                    matchedDetails.add("${matchedCandidate.party.ifBlank { matchedCandidate.initials }}: $label (${matchedCandidate.name})")
                                } else {
                                    warnings.add("No se pudo asociar: $rawName (carpeta: $folderName, número: $nameWithoutExt)")
                                }
                            }
                        }
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()
        } catch (e: Exception) {
            e.printStackTrace()
            warnings.add("Error al leer archivo ZIP: ${e.localizedMessage}")
        }

        return ImportPhotosResult(totalImported, matchedDetails, warnings)
    }
}
