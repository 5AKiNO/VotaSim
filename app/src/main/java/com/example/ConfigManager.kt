package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

data class CandidateModel(
    val id: String,
    val name: String,
    val party: String = "",
    val initials: String = "",
    val list: String = "",
    val works: Boolean = true,
    val isBlanco: Boolean = false,
    val optionId: Int? = null,
    val colorType: String? = null,
    val customColorHex: String? = null
)

data class PartyPalette(
    val id: String,
    val name: String,
    val partyName: String,
    val hexColor: String
)

val STANDARD_PARTY_PALETTES = listOf(
    PartyPalette("acr_red", "Rojo Institucional (ACR)", "AGRUPACIÓN CÍVICA ROJA", "#C8102E"),
    PartyPalette("mca_blue", "Azul Institucional (MCA)", "MOVIMIENTO CIUDADANO AZUL", "#1E40AF"),
    PartyPalette("alianza_green", "Verde Alianza (APV)", "ALIANZA PROGRESO VERDE", "#15803D"),
    PartyPalette("dark_green", "Verde Oscuro", "ALIANZA UNIDOS", "#14532D"),
    PartyPalette("light_green", "Verde Claro", "MOVIMIENTO ECOLÓGICO", "#4ADE80"),
    PartyPalette("cyan_fc", "Celeste Institucional", "FRENTE CÍVICO CELESTE", "#0284C7"),
    PartyPalette("purple_up", "Púrpura Institucional", "UNIÓN PROGRESISTA", "#7E22CE"),
    PartyPalette("orange_mn", "Naranja Institucional", "MOVIMIENTO RENOVACIÓN", "#EA580C"),
    PartyPalette("yellow_ac", "Amarillo Institucional", "ACUERDO CIUDADANO", "#FACC15"),
    PartyPalette("teal", "Turquesa", "MOVIMIENTO SOCIAL", "#0D9488"),
    PartyPalette("rose", "Rosa / Magenta", "MOVIMIENTO INDEPENDIENTE", "#E11D48"),
    PartyPalette("slate", "Gris Pizarra", "CONCERTACIÓN VECINAL", "#334155")
)

data class CandidateImportResult(
    val intendentes: List<CandidateModel> = emptyList(),
    val juntas: List<CandidateModel> = emptyList(),
    val concejales: List<CandidateModel> = emptyList(),
    val warnings: List<String> = emptyList(),
    val totalCount: Int = 0
)

data class DimensionsConfig(
    val autoResponsive: Boolean = true,
    val intendenteCardHeightDp: Int = 138,
    val juntaCardHeightDp: Int = 126,
    val concejalCardHeightDp: Int = 92,
    val votoBlancoCardHeightDp: Int = 72,
    val avatarSizeIntendenteDp: Int = 76,
    val avatarSizeConcejalDp: Int = 48,
    val avatarSizeSummaryDp: Int = 90,
    val listNumberTextSizeSp: Int = 32,
    val candidateNameTextSizeSp: Int = 14,
    val partyNameTextSizeSp: Int = 11,
    val optionNumberTextSizeSp: Int = 22,
    val gridSpacingDp: Int = 10,
    val gridColumnsIntendente: Int = 2,
    val gridColumnsJunta: Int = 3,
    val gridColumnsConcejal: Int = 4
)

data class ScreenMetricsInfo(
    val widthDp: Float,
    val heightDp: Float,
    val isTabletOrDesktop: Boolean,
    val isUltraWide: Boolean,
    val isPortrait: Boolean = false,
    val deviceCategory: String
)

fun evaluateScreenMetrics(widthDp: Float, heightDp: Float): ScreenMetricsInfo {
    val isPortrait = heightDp > widthDp
    val minDim = minOf(widthDp, heightDp)
    val isTabletOrDesktop = minDim >= 600f
    val isUltraWide = !isPortrait && (widthDp >= 1000f || (widthDp / heightDp.coerceAtLeast(1f)) > 1.85f)
    val deviceCategory = when {
        widthDp >= 1100f || (widthDp >= 900f && heightDp >= 600f) -> "Notebook / Escritorio"
        minDim >= 600f -> if (isPortrait) "Tablet Vertical" else "Tablet Horizontal"
        isPortrait -> "Teléfono Vertical"
        else -> "Teléfono Horizontal"
    }
    return ScreenMetricsInfo(
        widthDp = widthDp,
        heightDp = heightDp,
        isTabletOrDesktop = isTabletOrDesktop,
        isUltraWide = isUltraWide,
        isPortrait = isPortrait,
        deviceCategory = deviceCategory
    )
}

fun computeAutoResponsiveDimensions(
    availableWidthDp: Float,
    availableHeightDp: Float,
    isFullScreen: Boolean = false
): DimensionsConfig {
    val isPortrait = availableHeightDp > availableWidthDp
    val minDim = minOf(availableWidthDp, availableHeightDp)
    val isTablet = minDim >= 600f

    if (isPortrait) {
        // --- ADAPTIVE VERTICAL (PORTRAIT) MODE ENGINE ---
        val usableH = (availableHeightDp - if (isFullScreen) 64f else 168f).coerceAtLeast(240f)
        val usableW = availableWidthDp.coerceAtLeast(300f)

        // Vertical columns: 2 for intendente, 2 for junta (readable party names), 3 for concejales (room for avatar + option)
        val intCols = if (isTablet && usableW >= 700f) 3 else 2
        val juntaCols = if (isTablet && usableW >= 700f) 3 else 2
        val concejalCols = if (isTablet && usableW >= 700f) 4 else 3

        val intCardH = if (isTablet) (usableH * 0.26f).toInt().coerceIn(135, 185) else (usableH * 0.22f).toInt().coerceIn(118, 146)
        val votoBlancoH = if (isTablet) (usableH * 0.11f).toInt().coerceIn(65, 88) else (usableH * 0.10f).toInt().coerceIn(56, 68)

        val avatarIntendente = (intCardH * 0.52f).toInt().coerceIn(48, if (isTablet) 85 else 68)
        val avatarSummary = (usableH * 0.14f).toInt().coerceIn(58, if (isTablet) 110 else 82)

        val juntaCardH = if (isTablet) (usableH * 0.20f).toInt().coerceIn(105, 150) else (usableH * 0.17f).toInt().coerceIn(94, 120)

        val concejalCardH = if (isTablet) (usableH * 0.15f).toInt().coerceIn(84, 115) else (usableH * 0.13f).toInt().coerceIn(76, 92)
        val avatarConcejal = (concejalCardH * 0.50f).toInt().coerceIn(34, if (isTablet) 54 else 42)

        val listNumberSp = if (isTablet) 36 else 28
        val candidateNameSp = if (isTablet) 15 else 13
        val partyNameSp = if (isTablet) 12 else 10
        val optionNumberSp = if (isTablet) 24 else 20
        val gridSpacing = if (isTablet) 12 else 8

        return DimensionsConfig(
            autoResponsive = true,
            intendenteCardHeightDp = intCardH,
            juntaCardHeightDp = juntaCardH,
            concejalCardHeightDp = concejalCardH,
            votoBlancoCardHeightDp = votoBlancoH,
            avatarSizeIntendenteDp = avatarIntendente,
            avatarSizeConcejalDp = avatarConcejal,
            avatarSizeSummaryDp = avatarSummary,
            listNumberTextSizeSp = listNumberSp,
            candidateNameTextSizeSp = candidateNameSp,
            partyNameTextSizeSp = partyNameSp,
            optionNumberTextSizeSp = optionNumberSp,
            gridSpacingDp = gridSpacing,
            gridColumnsIntendente = intCols,
            gridColumnsJunta = juntaCols,
            gridColumnsConcejal = concejalCols
        )
    } else {
        // --- ADAPTIVE HORIZONTAL (LANDSCAPE) MODE ENGINE ---
        val usableH = (availableHeightDp - if (isFullScreen) 64f else 164f).coerceAtLeast(180f)
        val usableW = availableWidthDp.coerceAtLeast(320f)

        val isLargeScreen = usableW >= 840f || usableH >= 460f
        val isExtraLarge = usableW >= 1100f || usableH >= 580f

        // 1. Intendente (2 cards row 1 + 1 Voto Blanco row 2)
        val intCardH = (usableH * 0.56f).toInt().coerceIn(110, if (isExtraLarge) 240 else if (isLargeScreen) 210 else 145)
        val votoBlancoH = (usableH * 0.23f).toInt().coerceIn(52, if (isExtraLarge) 110 else if (isLargeScreen) 95 else 75)

        // Avatar sizes
        val avatarIntendente = (intCardH * 0.58f).toInt().coerceIn(54, if (isExtraLarge) 120 else 100)
        val avatarSummary = (usableH * 0.28f).toInt().coerceIn(60, if (isExtraLarge) 130 else 110)

        // Junta card (3 cols, usually 2 rows or 3 rows)
        val juntaCardH = (usableH * 0.42f).toInt().coerceIn(95, if (isExtraLarge) 200 else if (isLargeScreen) 175 else 135)

        // Concejal card (4 cols, 3 rows + voto blanco)
        val concejalCardH = (usableH * 0.25f).toInt().coerceIn(68, if (isExtraLarge) 140 else if (isLargeScreen) 120 else 96)
        val avatarConcejal = (concejalCardH * 0.54f).toInt().coerceIn(36, if (isExtraLarge) 75 else 60)

        // Dynamic Typographies based on card height & screen size
        val listNumberSp = (intCardH * 0.23f).toInt().coerceIn(24, if (isExtraLarge) 50 else if (isLargeScreen) 44 else 34)
        val candidateNameSp = (intCardH * 0.105f).toInt().coerceIn(12, if (isExtraLarge) 22 else if (isLargeScreen) 19 else 15)
        val partyNameSp = (intCardH * 0.082f).toInt().coerceIn(9, if (isExtraLarge) 17 else if (isLargeScreen) 15 else 12)
        val optionNumberSp = (concejalCardH * 0.25f).toInt().coerceIn(16, if (isExtraLarge) 34 else if (isLargeScreen) 28 else 23)

        // Dynamic Grid spacing
        val gridSpacing = when {
            isExtraLarge -> 16
            isLargeScreen -> 12
            else -> 8
        }

        return DimensionsConfig(
            autoResponsive = true,
            intendenteCardHeightDp = intCardH,
            juntaCardHeightDp = juntaCardH,
            concejalCardHeightDp = concejalCardH,
            votoBlancoCardHeightDp = votoBlancoH,
            avatarSizeIntendenteDp = avatarIntendente,
            avatarSizeConcejalDp = avatarConcejal,
            avatarSizeSummaryDp = avatarSummary,
            listNumberTextSizeSp = listNumberSp,
            candidateNameTextSizeSp = candidateNameSp,
            partyNameTextSizeSp = partyNameSp,
            optionNumberTextSizeSp = optionNumberSp,
            gridSpacingDp = gridSpacing,
            gridColumnsIntendente = 2,
            gridColumnsJunta = 3,
            gridColumnsConcejal = 4
        )
    }
}

data class TextsConfig(
    val screenSaverTitle: String = "SIMULADOR DE VOTACIÓN MUNICIPAL",
    val screenSaverSubtitle: String = "Justicia Electoral - Capacitación Cívica",
    val screenSaverTapToStart: String = "TOCA LA PANTALLA PARA COMENZAR",
    val headerElecciones: String = "ELECCIONES MUNICIPALES",
    val headerMetadata: String = "Elección: SIMULACRO ELECTORAL MUNICIPAL | Depto: 01-CENTRAL | Distrito: MUNICIPIO MODELO",
    val titleIntendente: String = "Candidatos a INTENDENTE MUNICIPAL",
    val subtitleIntendente: String = "Seleccione una opción",
    val titleJunta: String = "Candidatos a CONCEJAL DE JUNTA MUNICIPAL",
    val subtitleJunta: String = "Seleccione una lista",
    val titleConcejal: String = "Candidatos a CONCEJAL DE JUNTA MUNICIPAL",
    val subtitleConcejal: String = "Seleccione una opción preferencial",
    val bannerConcejalSubtitle: String = "LISTA 1 - AGRUPACIÓN CÍVICA ROJA",
    val titleSummary: String = "CONFIRMACIÓN DEL VOTO",
    val summaryIntendenteHeader: String = "INTENDENTE MUNICIPAL",
    val summaryJuntaHeader: String = "JUNTA MUNICIPAL",
    val btnModificar: String = "Modificar",
    val btnReiniciar: String = "Reiniciar\nSelección",
    val btnImprimir: String = "Imprimir\nSelección",
    val btnVolverAtras: String = "Volver Atrás",
    val txtAltoContraste: String = "Vista alto\ncontraste",
    val txtConfigurar: String = "Configurar",
    val txtVotoBlanco: String = "VOTO EN BLANCO",
    val printTitle: String = "IMPRIMIENDO SELECCIÓN...",
    val printSubtitle: String = "Por favor, retire su boleta física e introduzca en la urna."
)

data class ColorsConfig(
    val primaryPartyColorHex: String = "#C8102E",
    val screenSaverBgHex: String = "#0F172A",
    val headerBgHex: String = "#4B5563",
    val footerBgHex: String = "#D1D5DB",
    val summaryButtonRestartHex: String = "#F97316",
    val summaryButtonPrintHex: String = "#16A34A",
    val highContrastAccentHex: String = "#FFD700"
)

data class UiSimulatorConfig(
    val version: Int = 1,
    val intendentes: List<CandidateModel> = defaultIntendentes(),
    val juntas: List<CandidateModel> = defaultJuntas(),
    val concejales: List<CandidateModel> = defaultConcejales(),
    val dimensions: DimensionsConfig = DimensionsConfig(),
    val texts: TextsConfig = TextsConfig(),
    val colors: ColorsConfig = ColorsConfig(),
    val isFullScreen: Boolean = false,
    val unhideAllCandidates: Boolean = false
)

fun parseHexColor(hex: String, fallback: Color = Color.Black): Color {
    return parseColorRobust(hex, fallback)
}

fun parseColorRobust(colorStr: String?, fallback: Color = Color(0xFFC8102E)): Color {
    if (colorStr.isNullOrBlank()) return fallback
    val s = colorStr.trim().lowercase()

    // 1. Text names in Spanish & English
    when (s) {
        "red", "rojo", "acr" -> return Color(0xFFC8102E)
        "blue", "azul", "mca" -> return Color(0xFF1E40AF)
        "green", "verde", "alianza", "apv" -> return Color(0xFF15803D)
        "dark_green", "darkgreen", "verde_oscuro", "verdeoscuro" -> return Color(0xFF14532D)
        "light_green", "lightgreen", "verde_claro", "verdeclaro" -> return Color(0xFF4ADE80)
        "yellow", "amarillo", "ac" -> return Color(0xFFFACC15)
        "orange", "naranja", "mr" -> return Color(0xFFEA580C)
        "purple", "purpura", "púrpura", "morado", "violeta", "up" -> return Color(0xFF7E22CE)
        "cyan", "celeste", "sky", "fcc" -> return Color(0xFF0284C7)
        "teal", "turquesa" -> return Color(0xFF0D9488)
        "rose", "pink", "rosa", "rosado", "magenta" -> return Color(0xFFE11D48)
        "slate", "gray", "grey", "gris" -> return Color(0xFF334155)
        "black", "negro" -> return Color(0xFF111827)
        "white", "blanco" -> return Color(0xFFFFFFFF)
    }

    // 2. Flexible Hexadecimal parsing (#RRGGBB, RRGGBB, #AARRGGBB, AARRGGBB, #RGB, RGB)
    return try {
        var clean = s.removePrefix("0x").removePrefix("#").trim()
        if (clean.length == 3) { // #RGB -> #RRGGBB
            clean = "${clean[0]}${clean[0]}${clean[1]}${clean[1]}${clean[2]}${clean[2]}"
        }
        if (clean.length == 6) {
            clean = "FF$clean"
        }
        if (clean.length == 8) {
            val colorLong = clean.toLong(16)
            Color(colorLong)
        } else {
            fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

fun getCandidatePrimaryColor(candidate: CandidateModel?, defaultColor: Color): Color {
    if (candidate == null || candidate.isBlanco) return Color.White
    if (!candidate.customColorHex.isNullOrBlank()) {
        return parseColorRobust(candidate.customColorHex, defaultColor)
    }
    if (!candidate.colorType.isNullOrBlank()) {
        return parseColorRobust(candidate.colorType, defaultColor)
    }
    val p = candidate.party.uppercase()
    return when {
        p.contains("ROJA") || p.contains("ACR") || candidate.list == "1" -> Color(0xFFC8102E)
        p.contains("AZUL") || p.contains("MCA") || candidate.list == "2" -> Color(0xFF1E40AF)
        p.contains("VERDE") || p.contains("APV") || p.contains("ALIANZA") || candidate.list == "5" -> Color(0xFF15803D)
        p.contains("CELESTE") || p.contains("FCC") -> Color(0xFF0284C7)
        p.contains("PROGRESISTA") || p.contains("MORADO") -> Color(0xFF7E22CE)
        p.contains("RENOVACIÓN") || p.contains("NARANJA") -> Color(0xFFEA580C)
        p.contains("ACUERDO") || p.contains("AMARILLO") || candidate.list == "15" -> Color(0xFFFACC15)
        else -> defaultColor
    }
}

fun getContrastingTextColor(backgroundColor: Color): Color {
    val r = backgroundColor.red
    val g = backgroundColor.green
    val b = backgroundColor.blue
    val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return if (luminance > 0.55f) Color(0xFF0F172A) else Color.White
}

fun getContrastingSubTextColor(backgroundColor: Color): Color {
    val r = backgroundColor.red
    val g = backgroundColor.green
    val b = backgroundColor.blue
    val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return if (luminance > 0.55f) Color(0xFF334155) else Color.White.copy(alpha = 0.92f)
}

fun defaultIntendentes(): List<CandidateModel> {
    return listOf(
        CandidateModel(
            id = "i_l1",
            name = "JUAN PÉREZ GÓMEZ",
            party = "AGRUPACIÓN CÍVICA ROJA",
            initials = "ACR",
            list = "1",
            works = true,
            colorType = "red",
            customColorHex = "#C8102E"
        ),
        CandidateModel(
            id = "i_l2",
            name = "MARÍA LÓPEZ VERA",
            party = "MOVIMIENTO CIUDADANO AZUL",
            initials = "MCA",
            list = "2",
            works = true,
            colorType = "blue",
            customColorHex = "#1E40AF"
        ),
        CandidateModel(
            id = "i_l5",
            name = "CARLOS RODRÍGUEZ DÍAZ",
            party = "ALIANZA PROGRESO VERDE",
            initials = "APV",
            list = "5",
            works = true,
            colorType = "green",
            customColorHex = "#15803D"
        ),
        CandidateModel(
            id = "i_blanco",
            name = "VOTO EN BLANCO",
            party = "VOTO EN BLANCO",
            initials = "",
            list = "",
            isBlanco = true,
            works = true
        )
    )
}

fun defaultJuntas(): List<CandidateModel> {
    return listOf(
        CandidateModel(
            id = "j_l1",
            name = "AGRUPACIÓN CÍVICA ROJA",
            party = "AGRUPACIÓN CÍVICA ROJA",
            initials = "ACR",
            list = "1",
            works = true,
            colorType = "red",
            customColorHex = "#C8102E"
        ),
        CandidateModel(
            id = "j_l2",
            name = "MOVIMIENTO CIUDADANO AZUL",
            party = "MOVIMIENTO CIUDADANO AZUL",
            initials = "MCA",
            list = "2",
            works = true,
            colorType = "blue",
            customColorHex = "#1E40AF"
        ),
        CandidateModel(
            id = "j_l5",
            name = "ALIANZA PROGRESO VERDE",
            party = "ALIANZA PROGRESO VERDE",
            initials = "APV",
            list = "5",
            works = true,
            colorType = "green",
            customColorHex = "#15803D"
        ),
        CandidateModel(
            id = "j_blanco",
            name = "VOTO EN BLANCO",
            party = "VOTO EN BLANCO",
            initials = "",
            list = "",
            isBlanco = true,
            works = true
        )
    )
}

fun defaultConcejales(): List<CandidateModel> {
    return listOf(
        // LISTA 1 - ACR (12 Concejales)
        CandidateModel(id = "c1_l1", optionId = 1, name = "ANDRÉS MARTÍNEZ SOSA", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c2_l1", optionId = 2, name = "LAURA BENÍTEZ ROJAS", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c3_l1", optionId = 3, name = "ROBERTO GIMÉNEZ TORRES", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c4_l1", optionId = 4, name = "SOFÍA VILLALBA CABRERA", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c5_l1", optionId = 5, name = "DIEGO FERNÁNDEZ ORTIZ", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c6_l1", optionId = 6, name = "VALERIA CÁCERES NÚÑEZ", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c7_l1", optionId = 7, name = "JAVIER ACOSTA MEDINA", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c8_l1", optionId = 8, name = "PATRICIA RAMÍREZ DUARTE", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c9_l1", optionId = 9, name = "MARCOS AYALA DELGADO", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c10_l1", optionId = 10, name = "CAMILA MORÍNIGO VEGA", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c11_l1", optionId = 11, name = "GUSTAVO CARDOZO SILVA", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),
        CandidateModel(id = "c12_l1", optionId = 12, name = "ELENA BARRIOS MENDOZA", party = "AGRUPACIÓN CÍVICA ROJA", initials = "ACR", list = "1", colorType = "red", customColorHex = "#C8102E"),

        // LISTA 2 - MCA (12 Concejales)
        CandidateModel(id = "c1_l2", optionId = 1, name = "FERNANDO GONZÁLEZ RIVAS", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c2_l2", optionId = 2, name = "GABRIELA SÁNCHEZ FRANCO", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c3_l2", optionId = 3, name = "ALEJANDRO ROMERO PAZ", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c4_l2", optionId = 4, name = "NATALIA FLEITAS VARGAS", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c5_l2", optionId = 5, name = "RICARDO BOGADO CASTRO", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c6_l2", optionId = 6, name = "CLARA OCAMPOS SALINAS", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c7_l2", optionId = 7, name = "SEBASTIÁN ALCARAZ MORA", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c8_l2", optionId = 8, name = "PAULA QUINTANA LÓPEZ", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c9_l2", optionId = 9, name = "MATÍAS CORONEL BRÍTEZ", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c10_l2", optionId = 10, name = "VERÓNICA ESCOBAR RUIZ", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c11_l2", optionId = 11, name = "NICOLÁS GALEANO PAREDES", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),
        CandidateModel(id = "c12_l2", optionId = 12, name = "ADRIANA PERALTA MEZA", party = "MOVIMIENTO CIUDADANO AZUL", initials = "MCA", list = "2", colorType = "blue", customColorHex = "#1E40AF"),

        // LISTA 5 - APV (12 Concejales)
        CandidateModel(id = "c1_l5", optionId = 1, name = "HÉCTOR SILVERO AGUILAR", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c2_l5", optionId = 2, name = "LUCÍA TORRES VALDEZ", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c3_l5", optionId = 3, name = "MARTÍN CUEVAS LEDESMA", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c4_l5", optionId = 4, name = "CAROLINA BÁEZ MIRANDA", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c5_l5", optionId = 5, name = "ESTEBAN MALDONADO RÍOS", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c6_l5", optionId = 6, name = "LORENA GODOY NAVARRO", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c7_l5", optionId = 7, name = "OSCAR DOMÍNGUEZ PÉREZ", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c8_l5", optionId = 8, name = "BEATRIZ CANTERO ARCE", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c9_l5", optionId = 9, name = "RAÚL ESPÍNOLA CORREA", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c10_l5", optionId = 10, name = "SILVIA HERRERA CAMPOS", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c11_l5", optionId = 11, name = "FEDERICO SANTACRUZ VERA", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D"),
        CandidateModel(id = "c12_l5", optionId = 12, name = "MONICA ARÉVALOS LUNA", party = "ALIANZA PROGRESO VERDE", initials = "APV", list = "5", colorType = "green", customColorHex = "#15803D")
    )
}

object ConfigManager {
    private const val PREFS_NAME = "ui_simulator_config_prefs"
    private const val KEY_CONFIG_JSON = "config_json"

    fun loadConfig(context: Context): UiSimulatorConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_CONFIG_JSON, null) ?: return UiSimulatorConfig()
        return fromJson(jsonStr) ?: UiSimulatorConfig()
    }

    fun saveConfig(context: Context, config: UiSimulatorConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = toJson(config)
        prefs.edit().putString(KEY_CONFIG_JSON, jsonStr).apply()
    }

    fun resetToDefault(context: Context): UiSimulatorConfig {
        val defaultConfig = UiSimulatorConfig()
        saveConfig(context, defaultConfig)
        return defaultConfig
    }

    fun toJson(config: UiSimulatorConfig): String {
        val root = JSONObject()
        root.put("version", config.version)
        root.put("isFullScreen", config.isFullScreen)
        root.put("unhideAllCandidates", config.unhideAllCandidates)

        // Candidates: Intendentes
        val intArr = JSONArray()
        config.intendentes.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("party", c.party)
            obj.put("initials", c.initials)
            obj.put("list", c.list)
            obj.put("works", c.works)
            obj.put("isBlanco", c.isBlanco)
            if (c.optionId != null) obj.put("optionId", c.optionId)
            if (c.colorType != null) obj.put("colorType", c.colorType)
            if (c.customColorHex != null) obj.put("customColorHex", c.customColorHex)
            intArr.put(obj)
        }
        root.put("intendentes", intArr)

        // Candidates: Juntas
        val juntaArr = JSONArray()
        config.juntas.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("party", c.party)
            obj.put("initials", c.initials)
            obj.put("list", c.list)
            obj.put("works", c.works)
            obj.put("isBlanco", c.isBlanco)
            if (c.optionId != null) obj.put("optionId", c.optionId)
            if (c.colorType != null) obj.put("colorType", c.colorType)
            if (c.customColorHex != null) obj.put("customColorHex", c.customColorHex)
            juntaArr.put(obj)
        }
        root.put("juntas", juntaArr)

        // Candidates: Concejales
        val concArr = JSONArray()
        config.concejales.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("party", c.party)
            obj.put("initials", c.initials)
            obj.put("list", c.list)
            obj.put("works", c.works)
            obj.put("isBlanco", c.isBlanco)
            if (c.optionId != null) obj.put("optionId", c.optionId)
            if (c.colorType != null) obj.put("colorType", c.colorType)
            if (c.customColorHex != null) obj.put("customColorHex", c.customColorHex)
            concArr.put(obj)
        }
        root.put("concejales", concArr)

        // Dimensions
        val dimObj = JSONObject()
        val d = config.dimensions
        dimObj.put("autoResponsive", d.autoResponsive)
        dimObj.put("intendenteCardHeightDp", d.intendenteCardHeightDp)
        dimObj.put("juntaCardHeightDp", d.juntaCardHeightDp)
        dimObj.put("concejalCardHeightDp", d.concejalCardHeightDp)
        dimObj.put("votoBlancoCardHeightDp", d.votoBlancoCardHeightDp)
        dimObj.put("avatarSizeIntendenteDp", d.avatarSizeIntendenteDp)
        dimObj.put("avatarSizeConcejalDp", d.avatarSizeConcejalDp)
        dimObj.put("avatarSizeSummaryDp", d.avatarSizeSummaryDp)
        dimObj.put("listNumberTextSizeSp", d.listNumberTextSizeSp)
        dimObj.put("candidateNameTextSizeSp", d.candidateNameTextSizeSp)
        dimObj.put("partyNameTextSizeSp", d.partyNameTextSizeSp)
        dimObj.put("optionNumberTextSizeSp", d.optionNumberTextSizeSp)
        dimObj.put("gridSpacingDp", d.gridSpacingDp)
        dimObj.put("gridColumnsIntendente", d.gridColumnsIntendente)
        dimObj.put("gridColumnsJunta", d.gridColumnsJunta)
        dimObj.put("gridColumnsConcejal", d.gridColumnsConcejal)
        root.put("dimensions", dimObj)

        // Texts
        val txtObj = JSONObject()
        val t = config.texts
        txtObj.put("screenSaverTitle", t.screenSaverTitle)
        txtObj.put("screenSaverSubtitle", t.screenSaverSubtitle)
        txtObj.put("screenSaverTapToStart", t.screenSaverTapToStart)
        txtObj.put("headerElecciones", t.headerElecciones)
        txtObj.put("headerMetadata", t.headerMetadata)
        txtObj.put("titleIntendente", t.titleIntendente)
        txtObj.put("subtitleIntendente", t.subtitleIntendente)
        txtObj.put("titleJunta", t.titleJunta)
        txtObj.put("subtitleJunta", t.subtitleJunta)
        txtObj.put("titleConcejal", t.titleConcejal)
        txtObj.put("subtitleConcejal", t.subtitleConcejal)
        txtObj.put("bannerConcejalSubtitle", t.bannerConcejalSubtitle)
        txtObj.put("titleSummary", t.titleSummary)
        txtObj.put("summaryIntendenteHeader", t.summaryIntendenteHeader)
        txtObj.put("summaryJuntaHeader", t.summaryJuntaHeader)
        txtObj.put("btnModificar", t.btnModificar)
        txtObj.put("btnReiniciar", t.btnReiniciar)
        txtObj.put("btnImprimir", t.btnImprimir)
        txtObj.put("btnVolverAtras", t.btnVolverAtras)
        txtObj.put("txtAltoContraste", t.txtAltoContraste)
        txtObj.put("txtConfigurar", t.txtConfigurar)
        txtObj.put("txtVotoBlanco", t.txtVotoBlanco)
        txtObj.put("printTitle", t.printTitle)
        txtObj.put("printSubtitle", t.printSubtitle)
        root.put("texts", txtObj)

        // Colors
        val colObj = JSONObject()
        val c = config.colors
        colObj.put("primaryPartyColorHex", c.primaryPartyColorHex)
        colObj.put("screenSaverBgHex", c.screenSaverBgHex)
        colObj.put("headerBgHex", c.headerBgHex)
        colObj.put("footerBgHex", c.footerBgHex)
        colObj.put("summaryButtonRestartHex", c.summaryButtonRestartHex)
        colObj.put("summaryButtonPrintHex", c.summaryButtonPrintHex)
        colObj.put("highContrastAccentHex", c.highContrastAccentHex)
        root.put("colors", colObj)

        return root.toString(2)
    }

    fun fromJson(jsonStr: String): UiSimulatorConfig? {
        return try {
            val root = JSONObject(jsonStr)
            val isFullScreen = root.optBoolean("isFullScreen", false)
            val unhideAllCandidates = root.optBoolean("unhideAllCandidates", false)

            fun parseCandidates(arr: JSONArray?): List<CandidateModel> {
                if (arr == null) return emptyList()
                val list = mutableListOf<CandidateModel>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        CandidateModel(
                            id = obj.optString("id", "cand_$i"),
                            name = obj.optString("name", ""),
                            party = obj.optString("party", ""),
                            initials = obj.optString("initials", ""),
                            list = obj.optString("list", ""),
                            works = obj.optBoolean("works", true),
                            isBlanco = obj.optBoolean("isBlanco", false),
                            optionId = if (obj.has("optionId")) obj.optInt("optionId") else null,
                            colorType = if (obj.has("colorType")) obj.optString("colorType") else null,
                            customColorHex = if (obj.has("customColorHex")) obj.optString("customColorHex") else null
                        )
                    )
                }
                return list
            }

            val intendentes = if (root.has("intendentes")) parseCandidates(root.getJSONArray("intendentes")) else defaultIntendentes()
            val juntas = if (root.has("juntas")) parseCandidates(root.getJSONArray("juntas")) else defaultJuntas()
            val concejales = if (root.has("concejales")) parseCandidates(root.getJSONArray("concejales")) else defaultConcejales()

            val dObj = root.optJSONObject("dimensions")
            val defD = DimensionsConfig()
            val dimensions = if (dObj != null) {
                DimensionsConfig(
                    autoResponsive = dObj.optBoolean("autoResponsive", defD.autoResponsive),
                    intendenteCardHeightDp = dObj.optInt("intendenteCardHeightDp", defD.intendenteCardHeightDp),
                    juntaCardHeightDp = dObj.optInt("juntaCardHeightDp", defD.juntaCardHeightDp),
                    concejalCardHeightDp = dObj.optInt("concejalCardHeightDp", defD.concejalCardHeightDp),
                    votoBlancoCardHeightDp = dObj.optInt("votoBlancoCardHeightDp", defD.votoBlancoCardHeightDp),
                    avatarSizeIntendenteDp = dObj.optInt("avatarSizeIntendenteDp", defD.avatarSizeIntendenteDp),
                    avatarSizeConcejalDp = dObj.optInt("avatarSizeConcejalDp", defD.avatarSizeConcejalDp),
                    avatarSizeSummaryDp = dObj.optInt("avatarSizeSummaryDp", defD.avatarSizeSummaryDp),
                    listNumberTextSizeSp = dObj.optInt("listNumberTextSizeSp", defD.listNumberTextSizeSp),
                    candidateNameTextSizeSp = dObj.optInt("candidateNameTextSizeSp", defD.candidateNameTextSizeSp),
                    partyNameTextSizeSp = dObj.optInt("partyNameTextSizeSp", defD.partyNameTextSizeSp),
                    optionNumberTextSizeSp = dObj.optInt("optionNumberTextSizeSp", defD.optionNumberTextSizeSp),
                    gridSpacingDp = dObj.optInt("gridSpacingDp", defD.gridSpacingDp),
                    gridColumnsIntendente = dObj.optInt("gridColumnsIntendente", defD.gridColumnsIntendente),
                    gridColumnsJunta = dObj.optInt("gridColumnsJunta", defD.gridColumnsJunta),
                    gridColumnsConcejal = dObj.optInt("gridColumnsConcejal", defD.gridColumnsConcejal)
                )
            } else defD

            val tObj = root.optJSONObject("texts")
            val defT = TextsConfig()
            val texts = if (tObj != null) {
                TextsConfig(
                    screenSaverTitle = tObj.optString("screenSaverTitle", defT.screenSaverTitle),
                    screenSaverSubtitle = tObj.optString("screenSaverSubtitle", defT.screenSaverSubtitle),
                    screenSaverTapToStart = tObj.optString("screenSaverTapToStart", defT.screenSaverTapToStart),
                    headerElecciones = tObj.optString("headerElecciones", defT.headerElecciones),
                    headerMetadata = tObj.optString("headerMetadata", defT.headerMetadata),
                    titleIntendente = tObj.optString("titleIntendente", defT.titleIntendente),
                    subtitleIntendente = tObj.optString("subtitleIntendente", defT.subtitleIntendente),
                    titleJunta = tObj.optString("titleJunta", defT.titleJunta),
                    subtitleJunta = tObj.optString("subtitleJunta", defT.subtitleJunta),
                    titleConcejal = tObj.optString("titleConcejal", defT.titleConcejal),
                    subtitleConcejal = tObj.optString("subtitleConcejal", defT.subtitleConcejal),
                    bannerConcejalSubtitle = tObj.optString("bannerConcejalSubtitle", defT.bannerConcejalSubtitle),
                    titleSummary = tObj.optString("titleSummary", defT.titleSummary),
                    summaryIntendenteHeader = tObj.optString("summaryIntendenteHeader", defT.summaryIntendenteHeader),
                    summaryJuntaHeader = tObj.optString("summaryJuntaHeader", defT.summaryJuntaHeader),
                    btnModificar = tObj.optString("btnModificar", defT.btnModificar),
                    btnReiniciar = tObj.optString("btnReiniciar", defT.btnReiniciar),
                    btnImprimir = tObj.optString("btnImprimir", defT.btnImprimir),
                    btnVolverAtras = tObj.optString("btnVolverAtras", defT.btnVolverAtras),
                    txtAltoContraste = tObj.optString("txtAltoContraste", defT.txtAltoContraste),
                    txtConfigurar = tObj.optString("txtConfigurar", defT.txtConfigurar),
                    txtVotoBlanco = tObj.optString("txtVotoBlanco", defT.txtVotoBlanco),
                    printTitle = tObj.optString("printTitle", defT.printTitle),
                    printSubtitle = tObj.optString("printSubtitle", defT.printSubtitle)
                )
            } else defT

            val cObj = root.optJSONObject("colors")
            val defC = ColorsConfig()
            val colors = if (cObj != null) {
                ColorsConfig(
                    primaryPartyColorHex = cObj.optString("primaryPartyColorHex", defC.primaryPartyColorHex),
                    screenSaverBgHex = cObj.optString("screenSaverBgHex", defC.screenSaverBgHex),
                    headerBgHex = cObj.optString("headerBgHex", defC.headerBgHex),
                    footerBgHex = cObj.optString("footerBgHex", defC.footerBgHex),
                    summaryButtonRestartHex = cObj.optString("summaryButtonRestartHex", defC.summaryButtonRestartHex),
                    summaryButtonPrintHex = cObj.optString("summaryButtonPrintHex", defC.summaryButtonPrintHex),
                    highContrastAccentHex = cObj.optString("highContrastAccentHex", defC.highContrastAccentHex)
                )
            } else defC

            UiSimulatorConfig(
                version = root.optInt("version", 1),
                intendentes = intendentes,
                juntas = juntas,
                concejales = concejales,
                dimensions = dimensions,
                texts = texts,
                colors = colors,
                isFullScreen = isFullScreen,
                unhideAllCandidates = unhideAllCandidates
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsvCell(value: String, delimiter: String): String {
        var res = value
        if (res.contains(delimiter) || res.contains("\"") || res.contains("\n") || res.contains("\r")) {
            res = res.replace("\"", "\"\"")
            return "\"$res\""
        }
        return res
    }

    fun exportCandidatesToCsv(config: UiSimulatorConfig, delimiter: String = ";"): String {
        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel compatibility
        sb.append("\uFEFF")
        
        // Header
        val headers = listOf(
            "CATEGORIA",
            "NOMBRE",
            "LISTA",
            "SIGLAS",
            "PARTIDO",
            "OPCION",
            "COLOR_HEX",
            "HABILITADO",
            "ES_BLANCO",
            "ID"
        )
        sb.append(headers.joinToString(delimiter)).append("\r\n")

        fun appendRow(category: String, c: CandidateModel) {
            val color = c.customColorHex ?: (c.colorType ?: "")
            val row = listOf(
                escapeCsvCell(category, delimiter),
                escapeCsvCell(c.name, delimiter),
                escapeCsvCell(c.list, delimiter),
                escapeCsvCell(c.initials, delimiter),
                escapeCsvCell(c.party, delimiter),
                escapeCsvCell(c.optionId?.toString() ?: "", delimiter),
                escapeCsvCell(color, delimiter),
                escapeCsvCell(if (c.works) "SI" else "NO", delimiter),
                escapeCsvCell(if (c.isBlanco) "SI" else "NO", delimiter),
                escapeCsvCell(c.id, delimiter)
            )
            sb.append(row.joinToString(delimiter)).append("\r\n")
        }

        config.intendentes.forEach { appendRow("INTENDENTE", it) }
        config.juntas.forEach { appendRow("JUNTA", it) }
        config.concejales.forEach { appendRow("CONCEJAL", it) }

        return sb.toString()
    }

    fun generateCsvTemplate(delimiter: String = ";"): String {
        val sb = StringBuilder()
        sb.append("\uFEFF")
        val headers = listOf(
            "CATEGORIA",
            "NOMBRE",
            "LISTA",
            "SIGLAS",
            "PARTIDO",
            "OPCION",
            "COLOR_HEX",
            "HABILITADO",
            "ES_BLANCO",
            "ID"
        )
        sb.append(headers.joinToString(delimiter)).append("\r\n")

        val examples = listOf(
            listOf("INTENDENTE", "JUAN PÉREZ GÓMEZ", "1", "ACR", "AGRUPACIÓN CÍVICA ROJA", "", "red", "SI", "NO", "i_l1"),
            listOf("INTENDENTE", "MARÍA LÓPEZ VERA", "2", "MCA", "MOVIMIENTO CIUDADANO AZUL", "", "blue", "SI", "NO", "i_l2"),
            listOf("INTENDENTE", "CARLOS RODRÍGUEZ DÍAZ", "5", "APV", "ALIANZA PROGRESO VERDE", "", "green", "SI", "NO", "i_l5"),
            listOf("INTENDENTE", "VOTO EN BLANCO", "", "", "VOTO EN BLANCO", "", "", "SI", "SI", "i_blanco"),
            listOf("JUNTA", "AGRUPACIÓN CÍVICA ROJA", "1", "ACR", "AGRUPACIÓN CÍVICA ROJA", "", "red", "SI", "NO", "j_l1"),
            listOf("JUNTA", "MOVIMIENTO CIUDADANO AZUL", "2", "MCA", "MOVIMIENTO CIUDADANO AZUL", "", "blue", "SI", "NO", "j_l2"),
            listOf("JUNTA", "ALIANZA PROGRESO VERDE", "5", "APV", "ALIANZA PROGRESO VERDE", "", "green", "SI", "NO", "j_l5"),
            listOf("JUNTA", "VOTO EN BLANCO", "", "", "VOTO EN BLANCO", "", "", "SI", "SI", "j_blanco"),
            listOf("CONCEJAL", "ANDRÉS MARTÍNEZ SOSA", "1", "ACR", "AGRUPACIÓN CÍVICA ROJA", "1", "red", "SI", "NO", "c1_l1"),
            listOf("CONCEJAL", "LAURA BENÍTEZ ROJAS", "1", "ACR", "AGRUPACIÓN CÍVICA ROJA", "2", "red", "SI", "NO", "c2_l1"),
            listOf("CONCEJAL", "FERNANDO GONZÁLEZ RIVAS", "2", "MCA", "MOVIMIENTO CIUDADANO AZUL", "1", "blue", "SI", "NO", "c1_l2"),
            listOf("CONCEJAL", "GABRIELA SÁNCHEZ FRANCO", "2", "MCA", "MOVIMIENTO CIUDADANO AZUL", "2", "blue", "SI", "NO", "c2_l2"),
            listOf("CONCEJAL", "HÉCTOR SILVERO AGUILAR", "5", "APV", "ALIANZA PROGRESO VERDE", "1", "green", "SI", "NO", "c1_l5"),
            listOf("CONCEJAL", "LUCÍA TORRES VALDEZ", "5", "APV", "ALIANZA PROGRESO VERDE", "2", "green", "SI", "NO", "c2_l5")
        )

        examples.forEach { row ->
            sb.append(row.map { escapeCsvCell(it, delimiter) }.joinToString(delimiter)).append("\r\n")
        }

        return sb.toString()
    }

    fun parseCandidatesCsv(csvContent: String): CandidateImportResult {
        var clean = csvContent.trim()
        if (clean.startsWith("\uFEFF")) {
            clean = clean.substring(1).trim()
        }
        if (clean.isBlank()) {
            return CandidateImportResult(warnings = listOf("El archivo o texto está vacío"))
        }

        val rows = parseCsvToMatrix(clean)
        if (rows.isEmpty()) {
            return CandidateImportResult(warnings = listOf("No se encontraron filas de datos válidas"))
        }

        val firstRow = rows[0].map { it.trim().uppercase() }
        var colCategoria = -1
        var colNombre = -1
        var colLista = -1
        var colSiglas = -1
        var colPartido = -1
        var colOpcion = -1
        var colColorHex = -1
        var colHabilitado = -1
        var colEsBlanco = -1
        var colId = -1

        var dataStartIdx = 0

        // Detect if first row is a header
        var headerMatches = 0
        firstRow.forEachIndexed { idx, col ->
            when {
                col in listOf("CATEGORIA", "CARGO", "TIPO", "CATEGORY", "TIPO_CANDIDATO") -> { colCategoria = idx; headerMatches++ }
                col in listOf("NOMBRE", "CANDIDATO", "NOMBRE_CANDIDATO", "NAME", "POSTULANTE") -> { colNombre = idx; headerMatches++ }
                col in listOf("LISTA", "NUMERO_LISTA", "NUM_LISTA", "LIST", "NRO_LISTA") -> { colLista = idx; headerMatches++ }
                col in listOf("SIGLAS", "SIGLA", "SIG", "INITIALS", "ABREV") -> { colSiglas = idx; headerMatches++ }
                col in listOf("PARTIDO", "PARTIDO_POLITICO", "MOVIMIENTO", "AGRUPACION", "PARTY") -> { colPartido = idx; headerMatches++ }
                col in listOf("OPCION", "OPCION_ID", "NUM_OPCION", "NUMERO_OPCION", "PREFERENCIA", "OPTION") -> { colOpcion = idx; headerMatches++ }
                col in listOf("COLOR_HEX", "COLOR", "HEX", "COLOR_PARTIDO", "COLOR_CODE") -> { colColorHex = idx; headerMatches++ }
                col in listOf("HABILITADO", "ACTIVO", "WORKS", "DISPONIBLE", "ENABLE", "ESTADO") -> { colHabilitado = idx; headerMatches++ }
                col in listOf("ES_BLANCO", "BLANCO", "IS_BLANCO", "VOTO_BLANCO") -> { colEsBlanco = idx; headerMatches++ }
                col in listOf("ID", "CODIGO", "IDENTIFICADOR", "KEY") -> { colId = idx; headerMatches++ }
            }
        }

        if (headerMatches >= 2) {
            dataStartIdx = 1
        } else {
            // Positional fallback
            colCategoria = 0
            colNombre = 1
            colLista = 2
            colSiglas = 3
            colPartido = 4
            colOpcion = 5
            colColorHex = 6
            colHabilitado = 7
            colEsBlanco = 8
            colId = 9
            dataStartIdx = 0
        }

        val parsedIntendentes = mutableListOf<CandidateModel>()
        val parsedJuntas = mutableListOf<CandidateModel>()
        val parsedConcejales = mutableListOf<CandidateModel>()
        val warnings = mutableListOf<String>()

        var rowNumber = 0
        for (i in dataStartIdx until rows.size) {
            rowNumber++
            val row = rows[i]
            if (row.all { it.isBlank() }) continue

            fun getCell(colIndex: Int): String {
                return if (colIndex >= 0 && colIndex < row.size) row[colIndex].trim() else ""
            }

            val rawCategory = getCell(colCategoria).uppercase()
            val rawName = getCell(colNombre)
            val rawList = getCell(colLista)
            val rawSiglas = getCell(colSiglas)
            val rawParty = getCell(colPartido)
            val rawOpcion = getCell(colOpcion)
            val rawColor = getCell(colColorHex)
            val rawHabilitado = getCell(colHabilitado).uppercase()
            val rawEsBlanco = getCell(colEsBlanco).uppercase()
            val rawId = getCell(colId)

            if (rawName.isBlank() && rawParty.isBlank() && rawList.isBlank()) continue

            val isBlanco = rawEsBlanco in listOf("SI", "S", "TRUE", "1", "YES", "Y") || rawName.uppercase().contains("BLANCO")
            val works = if (rawHabilitado.isNotBlank()) {
                rawHabilitado in listOf("SI", "S", "TRUE", "1", "YES", "Y", "HABILITADO", "ACTIVO")
            } else true

            val optionInt = rawOpcion.toIntOrNull()

            // Resolve Category
            val category = when {
                rawCategory.contains("INTEND") || rawCategory.contains("ALCALDE") -> "intendente"
                rawCategory.contains("JUNTA") || rawCategory.contains("LISTA") -> "junta"
                rawCategory.contains("CONCEJ") || rawCategory.contains("PREFERENCIAL") || rawCategory.contains("OPCION") -> "concejal"
                optionInt != null -> "concejal"
                rawName.uppercase().startsWith("LISTA ") -> "junta"
                else -> "intendente"
            }

            val finalId = if (rawId.isNotBlank()) rawId else "${category.first()}_imp_${rowNumber}_${System.currentTimeMillis() % 10000}"

            val parsedColorHex = if (rawColor.isNotBlank()) {
                val cTrim = rawColor.trim()
                if (cTrim.startsWith("#") || (cTrim.length in 3..8 && cTrim.all { it in "0123456789abcdefABCDEF" })) {
                    if (cTrim.startsWith("#")) cTrim else "#$cTrim"
                } else null
            } else null

            val parsedColorType = if (rawColor.isNotBlank()) rawColor.trim().lowercase() else null

            val candidate = CandidateModel(
                id = finalId,
                name = rawName.ifBlank { if (isBlanco) "VOTO EN BLANCO" else "CANDIDATO $rowNumber" },
                party = rawParty,
                initials = rawSiglas,
                list = rawList,
                works = works,
                isBlanco = isBlanco,
                optionId = optionInt,
                colorType = parsedColorType,
                customColorHex = parsedColorHex
            )

            when (category) {
                "intendente" -> parsedIntendentes.add(candidate)
                "junta" -> parsedJuntas.add(candidate)
                "concejal" -> parsedConcejales.add(candidate)
            }
        }

        val total = parsedIntendentes.size + parsedJuntas.size + parsedConcejales.size
        if (total == 0) {
            warnings.add("No se pudieron extraer candidatos válidos del formato.")
        }

        return CandidateImportResult(
            intendentes = parsedIntendentes,
            juntas = parsedJuntas,
            concejales = parsedConcejales,
            warnings = warnings,
            totalCount = total
        )
    }

    private fun parseCsvToMatrix(text: String): List<List<String>> {
        val lines = text.split(Regex("\r\n|\n|\r"))
        if (lines.isEmpty()) return emptyList()

        // Determine delimiter from first non-empty lines
        val sampleLine = lines.firstOrNull { it.isNotBlank() } ?: ""
        val countSemicolon = sampleLine.count { it == ';' }
        val countComma = sampleLine.count { it == ',' }
        val countTab = sampleLine.count { it == '\t' }

        val delimiter = when {
            countTab > countSemicolon && countTab > countComma -> '\t'
            countComma > countSemicolon -> ','
            else -> ';'
        }

        val result = mutableListOf<List<String>>()
        var currentTokens = mutableListOf<String>()
        var currentToken = StringBuilder()
        var insideQuotes = false

        for (line in lines) {
            var i = 0
            while (i < line.length) {
                val c = line[i]
                if (c == '"') {
                    if (insideQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        currentToken.append('"')
                        i++ // skip escaped quote
                    } else {
                        insideQuotes = !insideQuotes
                    }
                } else if (c == delimiter && !insideQuotes) {
                    currentTokens.add(currentToken.toString().trim())
                    currentToken = StringBuilder()
                } else {
                    currentToken.append(c)
                }
                i++
            }

            if (!insideQuotes) {
                currentTokens.add(currentToken.toString().trim())
                if (currentTokens.any { it.isNotBlank() }) {
                    result.add(currentTokens)
                }
                currentTokens = mutableListOf()
                currentToken = StringBuilder()
            } else {
                // Multi-line quote continuation
                currentToken.append("\n")
            }
        }

        if (currentToken.isNotEmpty() || currentTokens.isNotEmpty()) {
            currentTokens.add(currentToken.toString().trim())
            if (currentTokens.any { it.isNotBlank() }) {
                result.add(currentTokens)
            }
        }

        return result
    }

    // ---------------- DIMENSIONES PERSONALIZADAS: GUARDAR / IMPORTAR / EXPORTAR ----------------
    private const val PREF_CUSTOM_DIMENSIONS = "saved_custom_dimensions"

    fun dimensionsToJson(dimensions: DimensionsConfig): String {
        val root = JSONObject()
        val d = dimensions
        root.put("autoResponsive", d.autoResponsive)
        root.put("intendenteCardHeightDp", d.intendenteCardHeightDp)
        root.put("juntaCardHeightDp", d.juntaCardHeightDp)
        root.put("concejalCardHeightDp", d.concejalCardHeightDp)
        root.put("votoBlancoCardHeightDp", d.votoBlancoCardHeightDp)
        root.put("avatarSizeIntendenteDp", d.avatarSizeIntendenteDp)
        root.put("avatarSizeConcejalDp", d.avatarSizeConcejalDp)
        root.put("avatarSizeSummaryDp", d.avatarSizeSummaryDp)
        root.put("listNumberTextSizeSp", d.listNumberTextSizeSp)
        root.put("candidateNameTextSizeSp", d.candidateNameTextSizeSp)
        root.put("partyNameTextSizeSp", d.partyNameTextSizeSp)
        root.put("optionNumberTextSizeSp", d.optionNumberTextSizeSp)
        root.put("gridSpacingDp", d.gridSpacingDp)
        root.put("gridColumnsIntendente", d.gridColumnsIntendente)
        root.put("gridColumnsJunta", d.gridColumnsJunta)
        root.put("gridColumnsConcejal", d.gridColumnsConcejal)
        return root.toString(2)
    }

    fun dimensionsFromJson(jsonStr: String): DimensionsConfig? {
        return try {
            val root = JSONObject(jsonStr)
            val dObj = if (root.has("dimensions")) root.getJSONObject("dimensions") else root
            val defD = DimensionsConfig()
            DimensionsConfig(
                autoResponsive = dObj.optBoolean("autoResponsive", false),
                intendenteCardHeightDp = dObj.optInt("intendenteCardHeightDp", defD.intendenteCardHeightDp),
                juntaCardHeightDp = dObj.optInt("juntaCardHeightDp", defD.juntaCardHeightDp),
                concejalCardHeightDp = dObj.optInt("concejalCardHeightDp", defD.concejalCardHeightDp),
                votoBlancoCardHeightDp = dObj.optInt("votoBlancoCardHeightDp", defD.votoBlancoCardHeightDp),
                avatarSizeIntendenteDp = dObj.optInt("avatarSizeIntendenteDp", defD.avatarSizeIntendenteDp),
                avatarSizeConcejalDp = dObj.optInt("avatarSizeConcejalDp", defD.avatarSizeConcejalDp),
                avatarSizeSummaryDp = dObj.optInt("avatarSizeSummaryDp", defD.avatarSizeSummaryDp),
                listNumberTextSizeSp = dObj.optInt("listNumberTextSizeSp", defD.listNumberTextSizeSp),
                candidateNameTextSizeSp = dObj.optInt("candidateNameTextSizeSp", defD.candidateNameTextSizeSp),
                partyNameTextSizeSp = dObj.optInt("partyNameTextSizeSp", defD.partyNameTextSizeSp),
                optionNumberTextSizeSp = dObj.optInt("optionNumberTextSizeSp", defD.optionNumberTextSizeSp),
                gridSpacingDp = dObj.optInt("gridSpacingDp", defD.gridSpacingDp),
                gridColumnsIntendente = dObj.optInt("gridColumnsIntendente", defD.gridColumnsIntendente),
                gridColumnsJunta = dObj.optInt("gridColumnsJunta", defD.gridColumnsJunta),
                gridColumnsConcejal = dObj.optInt("gridColumnsConcejal", defD.gridColumnsConcejal)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveCustomDimensions(context: Context, dimensions: DimensionsConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_CUSTOM_DIMENSIONS, dimensionsToJson(dimensions)).apply()
    }

    fun loadCustomDimensions(context: Context): DimensionsConfig? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(PREF_CUSTOM_DIMENSIONS, null) ?: return null
        return dimensionsFromJson(json)
    }

    fun hasCustomDimensionsSaved(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(PREF_CUSTOM_DIMENSIONS)
    }
}

