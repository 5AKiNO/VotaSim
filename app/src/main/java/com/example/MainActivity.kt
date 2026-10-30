package com.example

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.absoluteValue

// Authentication & Security Manager with persistent storage
object AuthManager {
    private const val PREFS_NAME = "voto_auth_prefs"
    private const val KEY_PASSWORD = "auth_password"
    private const val KEY_REMEMBER_PASSWORD = "auth_remember_password"
    private const val KEY_IS_LOGGED_IN = "auth_is_logged_in"
    private const val KEY_SAVED_USERNAME = "auth_saved_username"
    private const val KEY_SAVED_PASSWORD = "auth_saved_password"
    const val DEFAULT_USER = "lista1"
    const val DEFAULT_PASSWORD = "demo2026"
    const val ADMIN_CODE = "admin2026"
    private const val KEY_SETTINGS_PIN = "auth_settings_pin"
    const val DEFAULT_SETTINGS_PIN = "2026"

    fun getSettingsPin(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SETTINGS_PIN, DEFAULT_SETTINGS_PIN) ?: DEFAULT_SETTINGS_PIN
    }

    fun setSettingsPin(context: Context, newPin: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SETTINGS_PIN, newPin.trim()).apply()
    }

    fun checkSettingsPin(context: Context, pin: String): Boolean {
        val trimmed = pin.trim()
        val currentPin = getSettingsPin(context)
        return trimmed == currentPin || trimmed == ADMIN_CODE
    }

    fun getPassword(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
    }

    fun setPassword(context: Context, newPass: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PASSWORD, newPass).apply()
        if (isRememberPasswordEnabled(context)) {
            prefs.edit().putString(KEY_SAVED_PASSWORD, newPass).apply()
        }
    }

    fun isRememberPasswordEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMEMBER_PASSWORD, false)
    }

    fun isAutoLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getSavedUsername(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_USERNAME, DEFAULT_USER) ?: DEFAULT_USER
    }

    fun getSavedPassword(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_PASSWORD, "") ?: ""
    }

    fun saveLoginSession(context: Context, username: String, pass: String, remember: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_REMEMBER_PASSWORD, remember)
            .putBoolean(KEY_IS_LOGGED_IN, remember)
            .putString(KEY_SAVED_USERNAME, if (remember) username else "")
            .putString(KEY_SAVED_PASSWORD, if (remember) pass else "")
            .apply()
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
    }
}

// Sound helper using system tone generation for crisp terminal tactile beep feedback
object SoundPlayer {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {}
    }

    fun playClick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70) // short crisp beep
        } catch (_: Exception) {}
    }

    fun playPrint() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 350) // longer success beep
        } catch (_: Exception) {}
    }
}

typealias Candidate = CandidateModel

// Active Step in flow
enum class VotingStep {
    SCREEN_SAVER, // Step 0: Protector de pantalla (Tap to start)
    INTENDENTE,   // Step 1: Intendentes
    JUNTA,        // Step 2: Lists selection
    CONCEJAL,     // Step 3: Concejales associated to Lista 1
    SUMMARY       // Step 4: Final overview & confirmation
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VotingSimulatorApp(innerPadding)
                }
            }
        }
    }
}

// Helper function to copy selected image Uri contents into local app sandbox directory
fun saveUriToInternalStorage(context: Context, uri: android.net.Uri, fileName: String): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val file = File(context.filesDir, fileName)
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file.absolutePath
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun VotingSimulatorApp(innerPadding: PaddingValues) {
    val context = LocalContext.current

    // Core Dynamic Simulator Configuration (Observable state backed by persistent storage)
    var config by remember { mutableStateOf(ConfigManager.loadConfig(context)) }

    // Core Simulator State Management
    var votingStep by remember { mutableStateOf(VotingStep.SCREEN_SAVER) }
    var selectedIntendente by remember { mutableStateOf<Candidate?>(null) }
    var selectedJuntaList by remember { mutableStateOf<Candidate?>(null) }
    var selectedConcejal by remember { mutableStateOf<Candidate?>(null) }
    
    var isHighContrast by remember { mutableStateOf(false) }
    var isPrinting by remember { mutableStateOf(false) }

    // Authentication session state (defaults to auto-login if saved)
    var isLoggedIn by remember { mutableStateOf(AuthManager.isAutoLoggedIn(context)) }

    // Custom Dynamic Images Settings State
    var showPinDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var imageVersion by remember { mutableStateOf(0) }
    var targetConfiguringId by remember { mutableStateOf<String?>(null) }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {
                isLoggedIn = true
            }
        )
        return
    }

    // Native mobile system Back button handler to support fluid device navigation
    BackHandler(enabled = votingStep != VotingStep.SCREEN_SAVER) {
        when (votingStep) {
            VotingStep.INTENDENTE -> votingStep = VotingStep.SCREEN_SAVER
            VotingStep.JUNTA -> votingStep = VotingStep.INTENDENTE
            VotingStep.CONCEJAL -> votingStep = VotingStep.JUNTA
            VotingStep.SUMMARY -> votingStep = VotingStep.INTENDENTE
            else -> {}
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null && targetConfiguringId != null) {
            val targetId = targetConfiguringId!!
            val fileName = if (targetId == "screensaver") "screensaver_bg.png" else "candidate_$targetId.png"
            val savedPath = saveUriToInternalStorage(context, uri, fileName)
            if (savedPath != null) {
                imageVersion += 1
            }
        }
    }

    // Dynamic candidates filtered with unhide logic
    val unhide = config.unhideAllCandidates
    val intendentes = remember(config.intendentes, unhide) {
        if (unhide) config.intendentes.map { it.copy(works = true) } else config.intendentes
    }

    val juntas = remember(config.juntas, unhide) {
        if (unhide) config.juntas.map { it.copy(works = true) } else config.juntas
    }

    val concejales = remember(config.concejales, unhide) {
        if (unhide) config.concejales.map { it.copy(works = true) } else config.concejales
    }

    // Colors matching normal dynamic palettes or ADA Yellow-Black contrast mode
    val partyPrimaryColor = remember(config.colors.primaryPartyColorHex) {
        parseHexColor(config.colors.primaryPartyColorHex, Color(0xFFC8102E))
    }
    val screenSaverBgColor = remember(config.colors.screenSaverBgHex) {
        parseHexColor(config.colors.screenSaverBgHex, Color(0xFF0F172A))
    }
    val customHeaderBgColor = remember(config.colors.headerBgHex) {
        parseHexColor(config.colors.headerBgHex, Color(0xFF4B5563))
    }
    val customFooterBgColor = remember(config.colors.footerBgHex) {
        parseHexColor(config.colors.footerBgHex, Color(0xFFD1D5DB))
    }
    val customBtnRestartColor = remember(config.colors.summaryButtonRestartHex) {
        parseHexColor(config.colors.summaryButtonRestartHex, Color(0xFFF97316))
    }
    val customBtnPrintColor = remember(config.colors.summaryButtonPrintHex) {
        parseHexColor(config.colors.summaryButtonPrintHex, Color(0xFF16A34A))
    }
    val highContrastColor = remember(config.colors.highContrastAccentHex) {
        parseHexColor(config.colors.highContrastAccentHex, Color(0xFFFFD700))
    }

    val themeBg = if (isHighContrast) Color.Black else Color(0xFFE5E7EB)
    val themeCardBg = if (isHighContrast) Color.Black else Color.White
    val themeText = if (isHighContrast) highContrastColor else Color(0xFF1F2937)
    val themeSubText = if (isHighContrast) highContrastColor else Color(0xFF4B5563)
    val themeBorder = if (isHighContrast) highContrastColor else Color(0xFFCBD5E1)
    val themeHeaderBg = if (isHighContrast) Color(0xFF111111) else customHeaderBgColor
    val themeHeaderTxt = if (isHighContrast) highContrastColor else Color.White

    val texts = config.texts
    val isFullScreen = config.isFullScreen

    // Responsive Screen Container with Auto-Scale Engine
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBg)
            .padding(if (votingStep == VotingStep.SCREEN_SAVER) PaddingValues() else innerPadding)
    ) {
        val screenWidthDp = maxWidth.value
        val screenHeightDp = maxHeight.value
        val screenMetrics = remember(screenWidthDp, screenHeightDp) {
            evaluateScreenMetrics(screenWidthDp, screenHeightDp)
        }

        // Dynamic Dimensions calculated by auto-responsive engine or custom manual settings
        val dims = remember(config.dimensions, screenWidthDp, screenHeightDp, isFullScreen) {
            if (config.dimensions.autoResponsive) {
                computeAutoResponsiveDimensions(screenWidthDp, screenHeightDp, isFullScreen)
            } else {
                config.dimensions
            }
        }

        if (votingStep == VotingStep.SCREEN_SAVER) {
            // STEP 0: SCREEN SAVER
            val screenSaverFile = remember(imageVersion) { File(context.filesDir, "screensaver_bg.png") }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenSaverBgColor)
                    .clickable {
                        SoundPlayer.playClick()
                        votingStep = VotingStep.INTENDENTE
                    }
                    .testTag("pantalla_inicio_container"),
                contentAlignment = Alignment.Center
            ) {
                if (screenSaverFile.exists()) {
                    coil.compose.AsyncImage(
                        model = screenSaverFile,
                        contentDescription = "Fondo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.45f
                    )
                }

                // Floating controls on Screen Saver top-right
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = if (screenMetrics.isTabletOrDesktop) 32.dp else 20.dp, end = if (screenMetrics.isTabletOrDesktop) 32.dp else 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            SoundPlayer.playClick()
                            AuthManager.logout(context)
                            isLoggedIn = false
                            votingStep = VotingStep.SCREEN_SAVER
                        },
                        modifier = Modifier.testTag("pantalla_inicio_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloquear / Cerrar Sesión",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(if (screenMetrics.isTabletOrDesktop) 34.dp else 28.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            SoundPlayer.playClick()
                            showPinDialog = true
                        },
                        modifier = Modifier.testTag("pantalla_inicio_configuracion_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(if (screenMetrics.isTabletOrDesktop) 38.dp else 32.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .widthIn(max = 900.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(if (screenMetrics.isTabletOrDesktop) 36.dp else 16.dp)
                ) {
                    // Stylized central voting terminal icon
                    Box(
                        modifier = Modifier
                            .size(if (screenMetrics.isTabletOrDesktop) 110.dp else if (screenMetrics.isPortrait) 76.dp else 68.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .background(partyPrimaryColor, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(if (screenMetrics.isTabletOrDesktop) 52.dp else if (screenMetrics.isPortrait) 38.dp else 32.dp)
                            )
                            Text(
                                text = "VOTO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = if (screenMetrics.isTabletOrDesktop) 13.sp else 10.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(if (screenMetrics.isTabletOrDesktop) 24.dp else 12.dp))
                    Text(
                        text = texts.screenSaverTitle,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp,
                            fontSize = if (screenMetrics.isTabletOrDesktop) 30.sp else if (screenMetrics.isPortrait) 20.sp else 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(if (screenMetrics.isTabletOrDesktop) 10.dp else 6.dp))
                    Text(
                        text = texts.screenSaverSubtitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (screenMetrics.isTabletOrDesktop) 18.sp else 13.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(if (screenMetrics.isTabletOrDesktop) 36.dp else 18.dp))
                    Text(
                        text = texts.screenSaverTapToStart,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            fontSize = if (screenMetrics.isTabletOrDesktop) 20.sp else 15.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            // CONTAINER FOR MAIN SIMULATION STEPS 1-4
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1440.dp)
                    .align(Alignment.Center)
            ) {
                
                // HEADER SECTION
                if (!isFullScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (screenMetrics.isPortrait) 44.dp else 48.dp)
                            .background(themeHeaderBg)
                            .padding(horizontal = if (screenMetrics.isPortrait) 8.dp else 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Badge left
                        Row(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .padding(horizontal = if (screenMetrics.isPortrait) 6.dp else 8.dp, vertical = if (screenMetrics.isPortrait) 3.dp else 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "☑️ ",
                                fontSize = if (screenMetrics.isPortrait) 9.5.sp else 11.sp
                            )
                            Text(
                                text = texts.headerElecciones,
                                fontSize = if (screenMetrics.isPortrait) 9.5.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        }

                        // Metadata Info Center
                        Text(
                            text = texts.headerMetadata,
                            color = themeHeaderTxt.copy(alpha = 0.9f),
                            fontSize = if (screenMetrics.isPortrait) 9.sp else 11.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp),
                            textAlign = TextAlign.Center
                        )

                        // Flag right
                        FlagLogo(
                            modifier = Modifier
                                .width(if (screenMetrics.isPortrait) 26.dp else 30.dp)
                                .height(if (screenMetrics.isPortrait) 17.dp else 20.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                // STEP TITLE CONTAINER
                val showStepTitleBanner = votingStep != VotingStep.SUMMARY
                if (showStepTitleBanner) {
                    val currentStepPartyColor = when (votingStep) {
                        VotingStep.INTENDENTE -> partyPrimaryColor
                        VotingStep.CONCEJAL -> getCandidatePrimaryColor(selectedJuntaList, partyPrimaryColor)
                        else -> partyPrimaryColor
                    }
                    val isPartyHeader = votingStep == VotingStep.INTENDENTE || votingStep == VotingStep.CONCEJAL
                    val bannerBackground = if (isHighContrast) Color.Black else if (isPartyHeader) currentStepPartyColor else Color.White
                    val bannerTextColors = if (isHighContrast) highContrastColor else if (isPartyHeader) getContrastingTextColor(currentStepPartyColor) else Color.Black
                    val bannerSubTextColors = if (isHighContrast) highContrastColor else if (isPartyHeader) getContrastingSubTextColor(currentStepPartyColor) else Color(0xFF6B7280)
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bannerBackground)
                            .border(width = if (isPartyHeader) 0.dp else 1.dp, color = themeBorder)
                            .padding(vertical = if (screenMetrics.isPortrait) 6.dp else 8.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val (stepTitle, stepSubtitle) = when (votingStep) {
                            VotingStep.INTENDENTE -> Pair(texts.titleIntendente, texts.subtitleIntendente)
                            VotingStep.JUNTA -> Pair(texts.titleJunta, texts.subtitleJunta)
                            VotingStep.CONCEJAL -> Pair(texts.titleConcejal, texts.subtitleConcejal)
                            else -> Pair("", "")
                        }

                        Text(
                            text = stepTitle,
                            color = bannerTextColors,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            fontSize = if (screenMetrics.isPortrait) 16.sp else 18.sp,
                            textAlign = TextAlign.Center
                        )
                        if (votingStep == VotingStep.CONCEJAL) {
                            val subtitleText = if (selectedJuntaList != null && !selectedJuntaList!!.isBlanco) {
                                "LISTA ${selectedJuntaList?.list} - ${selectedJuntaList?.party}"
                            } else {
                                texts.bannerConcejalSubtitle
                            }
                            Text(
                                text = subtitleText,
                                color = bannerTextColors,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (screenMetrics.isPortrait) 12.5.sp else 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = stepSubtitle,
                            color = bannerSubTextColors,
                            fontSize = if (screenMetrics.isPortrait) 11.5.sp else 13.sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // MAIN CONTENT STEPS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(dims.gridSpacingDp.dp)
                ) {
                    when (votingStep) {
                        VotingStep.INTENDENTE -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(dims.gridColumnsIntendente),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(dims.gridSpacingDp.dp),
                                verticalArrangement = Arrangement.spacedBy(dims.gridSpacingDp.dp)
                            ) {
                                val nonBlancoIntendentes = intendentes.filter { !it.isBlanco }
                                val blancoIntendente = intendentes.firstOrNull { it.isBlanco }

                                items(nonBlancoIntendentes) { candidate ->
                                    CandidateCard(
                                        candidate = candidate,
                                        isHighContrast = isHighContrast,
                                        themeCardBg = themeCardBg,
                                        themeText = themeText,
                                        themeSubText = themeSubText,
                                        imageVersion = imageVersion,
                                        dimensions = dims,
                                        primaryColor = partyPrimaryColor,
                                        onClick = {
                                            SoundPlayer.playClick()
                                            selectedIntendente = candidate
                                            votingStep = VotingStep.JUNTA
                                        }
                                    )
                                }

                                if (blancoIntendente != null) {
                                    item(span = { GridItemSpan(dims.gridColumnsIntendente) }) {
                                        VotoBlancoCard(
                                            isHighContrast = isHighContrast,
                                            themeCardBg = themeCardBg,
                                            themeText = themeText,
                                            label = texts.txtVotoBlanco,
                                            height = dims.votoBlancoCardHeightDp.dp,
                                            onClick = {
                                                SoundPlayer.playClick()
                                                selectedIntendente = blancoIntendente
                                                votingStep = VotingStep.JUNTA
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        VotingStep.JUNTA -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(dims.gridColumnsJunta),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(dims.gridSpacingDp.dp),
                                verticalArrangement = Arrangement.spacedBy(dims.gridSpacingDp.dp)
                            ) {
                                val nonBlancoJuntas = juntas.filter { !it.isBlanco }
                                val blancoJunta = juntas.firstOrNull { it.isBlanco }

                                items(nonBlancoJuntas) { candidate ->
                                    JuntaListCard(
                                        candidate = candidate,
                                        isHighContrast = isHighContrast,
                                        themeCardBg = themeCardBg,
                                        themeText = themeText,
                                        themeSubText = themeSubText,
                                        dimensions = dims,
                                        primaryColor = partyPrimaryColor,
                                        onClick = {
                                            SoundPlayer.playClick()
                                            selectedJuntaList = candidate
                                            if (candidate.isBlanco) {
                                                selectedConcejal = candidate
                                                votingStep = VotingStep.SUMMARY
                                            } else {
                                                votingStep = VotingStep.CONCEJAL
                                            }
                                        }
                                    )
                                }

                                if (blancoJunta != null) {
                                    item {
                                        VotoBlancoCard(
                                            isHighContrast = isHighContrast,
                                            themeCardBg = themeCardBg,
                                            themeText = themeText,
                                            label = texts.txtVotoBlanco,
                                            height = dims.juntaCardHeightDp.dp,
                                            onClick = {
                                                SoundPlayer.playClick()
                                                selectedJuntaList = blancoJunta
                                                selectedConcejal = blancoJunta
                                                votingStep = VotingStep.SUMMARY
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        VotingStep.CONCEJAL -> {
                            val activeConcejales = remember(concejales, selectedJuntaList) {
                                if (selectedJuntaList != null && !selectedJuntaList!!.isBlanco) {
                                    val targetList = selectedJuntaList!!.list.trim()
                                    val filtered = concejales.filter { it.list.trim() == targetList }
                                    if (filtered.isNotEmpty()) filtered else concejales
                                } else {
                                    concejales
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(dims.gridColumnsConcejal),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(dims.gridSpacingDp.dp),
                                verticalArrangement = Arrangement.spacedBy(dims.gridSpacingDp.dp)
                            ) {
                                items(activeConcejales) { candidate ->
                                    ConcejalCard(
                                        candidate = candidate,
                                        isHighContrast = isHighContrast,
                                        themeCardBg = themeCardBg,
                                        themeText = themeText,
                                        imageVersion = imageVersion,
                                        dimensions = dims,
                                        primaryColor = getCandidatePrimaryColor(selectedJuntaList ?: candidate, partyPrimaryColor),
                                        onClick = {
                                            SoundPlayer.playClick()
                                            selectedConcejal = candidate
                                            votingStep = VotingStep.SUMMARY
                                        }
                                    )
                                }

                                val blancoConcejal = Candidate(
                                    id = "c_blanco",
                                    name = texts.txtVotoBlanco,
                                    party = texts.txtVotoBlanco,
                                    isBlanco = true,
                                    works = true
                                )
                                item(span = { GridItemSpan(dims.gridColumnsConcejal) }) {
                                    VotoBlancoCard(
                                        isHighContrast = isHighContrast,
                                        themeCardBg = themeCardBg,
                                        themeText = themeText,
                                        label = texts.txtVotoBlanco,
                                        height = dims.votoBlancoCardHeightDp.dp,
                                        onClick = {
                                            SoundPlayer.playClick()
                                            selectedConcejal = blancoConcejal
                                            votingStep = VotingStep.SUMMARY
                                        }
                                    )
                                }
                            }
                        }

                        VotingStep.SUMMARY -> {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val isLandscapeMini = maxHeight < 360.dp
                                val actionColWidth = when {
                                    isLandscapeMini -> 140.dp
                                    screenMetrics.isUltraWide -> 230.dp
                                    screenMetrics.isTabletOrDesktop -> 205.dp
                                    else -> 170.dp
                                }
                                val actionIconSize = when {
                                    isLandscapeMini -> 24.dp
                                    screenMetrics.isTabletOrDesktop -> 42.dp
                                    else -> 32.dp
                                }
                                val actionTextSize = when {
                                    isLandscapeMini -> 11.sp
                                    screenMetrics.isTabletOrDesktop -> 17.sp
                                    else -> 15.sp
                                }

                                val currentInt = selectedIntendente
                                val isIntParty = currentInt != null && !currentInt.isBlanco
                                val intPartyColor = getCandidatePrimaryColor(currentInt, partyPrimaryColor)
                                val intCardBg = when {
                                    isHighContrast -> themeCardBg
                                    isIntParty -> intPartyColor
                                    else -> themeCardBg
                                }
                                val intTxtColor = when {
                                    isHighContrast -> themeText
                                    isIntParty -> getContrastingTextColor(intPartyColor)
                                    else -> themeText
                                }
                                val intSubTxtColor = when {
                                    isHighContrast -> themeSubText
                                    isIntParty -> getContrastingSubTextColor(intPartyColor)
                                    else -> themeSubText
                                }

                                val currentJunta = selectedJuntaList
                                val currentConcejal = selectedConcejal
                                val isJuntaParty = currentJunta != null && !currentJunta.isBlanco && currentConcejal != null && !currentConcejal.isBlanco
                                val juntaPartyColor = getCandidatePrimaryColor(currentJunta ?: currentConcejal, partyPrimaryColor)
                                val juntaCardBg = when {
                                    isHighContrast -> themeCardBg
                                    isJuntaParty -> juntaPartyColor
                                    else -> themeCardBg
                                }
                                val juntaTxtColor = when {
                                    isHighContrast -> themeText
                                    isJuntaParty -> getContrastingTextColor(juntaPartyColor)
                                    else -> themeText
                                }
                                val juntaSubTxtColor = when {
                                    isHighContrast -> themeSubText
                                    isJuntaParty -> getContrastingSubTextColor(juntaPartyColor)
                                    else -> themeSubText
                                }

                                if (screenMetrics.isPortrait) {
                                    // VERTICAL / PORTRAIT SUMMARY LAYOUT (Stacked for mobile and portrait screens)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(themeBg)
                                            .border(1.dp, themeBorder)
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Card 1: Intendente municipal selection
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .background(intCardBg)
                                                .border(width = 1.dp, color = themeBorder)
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = texts.summaryIntendenteHeader,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Italic,
                                                color = intTxtColor,
                                                fontSize = if (screenMetrics.isTabletOrDesktop) 17.sp else 14.sp
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                if (currentInt == null || currentInt.isBlanco) {
                                                    Text(
                                                        text = texts.txtVotoBlanco,
                                                        fontSize = if (screenMetrics.isTabletOrDesktop) 24.sp else 19.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontStyle = FontStyle.Italic,
                                                        color = intTxtColor
                                                    )
                                                } else {
                                                    CandidateAvatar(
                                                        name = currentInt.name,
                                                        id = currentInt.id,
                                                        imageVersion = imageVersion,
                                                        modifier = Modifier
                                                            .size(if (screenMetrics.isTabletOrDesktop) 96.dp else dims.avatarSizeSummaryDp.coerceIn(56, 80).dp)
                                                            .padding(end = 12.dp)
                                                    )
                                                    Column(
                                                        horizontalAlignment = Alignment.Start,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = currentInt.party,
                                                            fontSize = if (screenMetrics.isTabletOrDesktop) 14.sp else 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontStyle = FontStyle.Italic,
                                                            color = intSubTxtColor,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "LISTA ${currentInt.list}",
                                                            fontSize = if (screenMetrics.isTabletOrDesktop) 20.sp else 16.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = intTxtColor
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = currentInt.name,
                                                            fontSize = if (screenMetrics.isTabletOrDesktop) 19.sp else 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = intTxtColor,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    SoundPlayer.playClick()
                                                    votingStep = VotingStep.INTENDENTE
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isHighContrast) Color.Black else if (isIntParty) Color.White else Color(0xFF4B5563),
                                                    contentColor = if (isHighContrast) highContrastColor else if (isIntParty) intPartyColor else Color.White
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                border = if (isHighContrast) BorderStroke(1.dp, highContrastColor) else null,
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 36.dp)
                                                    .testTag("modificar_intendente_button")
                                            ) {
                                                Text(
                                                    text = texts.btnModificar, 
                                                    fontStyle = FontStyle.Italic, 
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        // Card 2: Junta Municipal & Concejal selection
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .background(juntaCardBg)
                                                .border(width = 1.dp, color = themeBorder)
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = texts.summaryJuntaHeader,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Italic,
                                                color = juntaTxtColor,
                                                fontSize = if (screenMetrics.isTabletOrDesktop) 17.sp else 14.sp
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                if (currentJunta == null || currentJunta.isBlanco || currentConcejal == null || currentConcejal.isBlanco) {
                                                    Text(
                                                        text = texts.txtVotoBlanco,
                                                        fontSize = if (screenMetrics.isTabletOrDesktop) 24.sp else 19.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontStyle = FontStyle.Italic,
                                                        color = juntaTxtColor
                                                    )
                                                } else {
                                                    CandidateAvatar(
                                                        name = currentConcejal.name,
                                                        id = currentConcejal.id,
                                                        imageVersion = imageVersion,
                                                        modifier = Modifier
                                                            .size(if (screenMetrics.isTabletOrDesktop) 96.dp else dims.avatarSizeSummaryDp.coerceIn(56, 80).dp)
                                                            .padding(end = 12.dp)
                                                    )
                                                    Column(
                                                        horizontalAlignment = Alignment.Start,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = currentJunta.party,
                                                            fontSize = if (screenMetrics.isTabletOrDesktop) 14.sp else 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontStyle = FontStyle.Italic,
                                                            color = juntaSubTxtColor,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = if (currentConcejal.optionId != null) "OPCIÓN ${currentConcejal.optionId}" else "LISTA ${currentJunta.list}",
                                                            fontSize = if (screenMetrics.isTabletOrDesktop) 20.sp else 16.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = juntaTxtColor
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = currentConcejal.name,
                                                            fontSize = if (screenMetrics.isTabletOrDesktop) 19.sp else 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = juntaTxtColor,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "LISTA ${currentJunta.list}",
                                                            fontSize = if (screenMetrics.isTabletOrDesktop) 13.sp else 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = juntaSubTxtColor
                                                        )
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    SoundPlayer.playClick()
                                                    votingStep = VotingStep.JUNTA
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isHighContrast) Color.Black else if (isJuntaParty) Color.White else Color(0xFF4B5563),
                                                    contentColor = if (isHighContrast) highContrastColor else if (isJuntaParty) juntaPartyColor else Color.White
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                border = if (isHighContrast) BorderStroke(1.dp, highContrastColor) else null,
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 36.dp)
                                                    .testTag("modificar_junta_button")
                                            ) {
                                                Text(
                                                    text = texts.btnModificar, 
                                                    fontStyle = FontStyle.Italic, 
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        // Bottom Actions Row (Reiniciar & Imprimir side by side)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(64.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Orange button "Reiniciar Selección"
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .background(if (isHighContrast) Color.Black else customBtnRestartColor, RoundedCornerShape(6.dp))
                                                    .border(1.dp, if (isHighContrast) highContrastColor else Color.White, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        SoundPlayer.playClick()
                                                        selectedIntendente = null
                                                        selectedJuntaList = null
                                                        selectedConcejal = null
                                                        votingStep = VotingStep.INTENDENTE
                                                    }
                                                    .testTag("reiniciar_seleccion_button"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = "Reiniciar",
                                                        tint = if (isHighContrast) highContrastColor else Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Reiniciar\nSelección",
                                                        color = if (isHighContrast) highContrastColor else Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontStyle = FontStyle.Italic,
                                                        lineHeight = 14.sp
                                                    )
                                                }
                                            }

                                            // Green button "Imprimir Selección"
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .background(if (isHighContrast) Color.Black else customBtnPrintColor, RoundedCornerShape(6.dp))
                                                    .border(1.dp, if (isHighContrast) highContrastColor else Color.White, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        SoundPlayer.playPrint()
                                                        isPrinting = true
                                                    }
                                                    .testTag("imprimir_seleccion_button"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Imprimir",
                                                        tint = if (isHighContrast) highContrastColor else Color.White,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Imprimir\nSelección",
                                                        color = if (isHighContrast) highContrastColor else Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontStyle = FontStyle.Italic,
                                                        lineHeight = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // HORIZONTAL / LANDSCAPE SUMMARY LAYOUT (Side-by-side 3 columns)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(themeBg)
                                            .border(1.dp, themeBorder),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                    // Column 1: Intendente municipal selection
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(intCardBg)
                                            .border(width = 1.dp, color = themeBorder)
                                            .padding(if (isLandscapeMini) 4.dp else if (screenMetrics.isTabletOrDesktop) 14.dp else 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = texts.summaryIntendenteHeader,
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Italic,
                                            color = intTxtColor,
                                            fontSize = if (isLandscapeMini) 11.sp else if (screenMetrics.isTabletOrDesktop) 16.sp else 13.sp
                                        )

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (currentInt == null || currentInt.isBlanco) {
                                                Text(
                                                    text = texts.txtVotoBlanco,
                                                    fontSize = if (isLandscapeMini) 14.sp else if (screenMetrics.isTabletOrDesktop) 24.sp else 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontStyle = FontStyle.Italic,
                                                    color = intTxtColor
                                                )
                                            } else {
                                                Text(
                                                    text = currentInt.party,
                                                    fontSize = if (isLandscapeMini) 10.sp else if (screenMetrics.isTabletOrDesktop) 14.sp else 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontStyle = FontStyle.Italic,
                                                    color = intSubTxtColor,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "LISTA ${currentInt.list}",
                                                    fontSize = if (isLandscapeMini) 12.sp else if (screenMetrics.isTabletOrDesktop) 18.sp else 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = intTxtColor
                                                )
                                                Spacer(modifier = Modifier.height(if (isLandscapeMini) 2.dp else 6.dp))
                                                CandidateAvatar(
                                                    name = currentInt.name,
                                                    id = currentInt.id,
                                                    imageVersion = imageVersion,
                                                    modifier = Modifier.size(if (isLandscapeMini) 55.dp else dims.avatarSizeSummaryDp.dp)
                                                )
                                                Spacer(modifier = Modifier.height(if (isLandscapeMini) 2.dp else 6.dp))
                                                Text(
                                                    text = currentInt.name,
                                                    fontSize = if (isLandscapeMini) 13.sp else if (screenMetrics.isTabletOrDesktop) 19.sp else 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = intTxtColor,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                SoundPlayer.playClick()
                                                votingStep = VotingStep.INTENDENTE
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isHighContrast) Color.Black else if (isIntParty) Color.White else Color(0xFF4B5563),
                                                contentColor = if (isHighContrast) highContrastColor else if (isIntParty) intPartyColor else Color.White
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            border = if (isHighContrast) BorderStroke(1.dp, highContrastColor) else null,
                                            contentPadding = if (isLandscapeMini) PaddingValues(horizontal = 8.dp, vertical = 2.dp) else PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                            modifier = Modifier
                                                .heightIn(min = if (isLandscapeMini) 28.dp else 40.dp)
                                                .testTag("modificar_intendente_button")
                                        ) {
                                            Text(
                                                text = texts.btnModificar, 
                                                fontStyle = FontStyle.Italic, 
                                                fontWeight = FontWeight.Bold,
                                                fontSize = if (isLandscapeMini) 11.sp else if (screenMetrics.isTabletOrDesktop) 15.sp else 13.sp
                                            )
                                        }
                                    }

                                    // Column 2: Junta Municipal & Concejal selection
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(juntaCardBg)
                                            .border(width = 1.dp, color = themeBorder)
                                            .padding(if (isLandscapeMini) 4.dp else if (screenMetrics.isTabletOrDesktop) 14.dp else 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = texts.summaryJuntaHeader,
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Italic,
                                            color = juntaTxtColor,
                                            fontSize = if (isLandscapeMini) 11.sp else if (screenMetrics.isTabletOrDesktop) 16.sp else 13.sp
                                        )

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (currentJunta == null || currentJunta.isBlanco || currentConcejal == null || currentConcejal.isBlanco) {
                                                Text(
                                                    text = texts.txtVotoBlanco,
                                                    fontSize = if (isLandscapeMini) 14.sp else if (screenMetrics.isTabletOrDesktop) 24.sp else 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontStyle = FontStyle.Italic,
                                                    color = juntaTxtColor
                                                )
                                            } else {
                                                Text(
                                                    text = currentJunta.party,
                                                    fontSize = if (isLandscapeMini) 10.sp else if (screenMetrics.isTabletOrDesktop) 14.sp else 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontStyle = FontStyle.Italic,
                                                    color = juntaSubTxtColor,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (currentConcejal.optionId != null) "OPCIÓN ${currentConcejal.optionId}" else "LISTA ${currentJunta.list}",
                                                    fontSize = if (isLandscapeMini) 12.sp else if (screenMetrics.isTabletOrDesktop) 18.sp else 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = juntaTxtColor
                                                )
                                                Spacer(modifier = Modifier.height(if (isLandscapeMini) 2.dp else 6.dp))
                                                CandidateAvatar(
                                                    name = currentConcejal.name,
                                                    id = currentConcejal.id,
                                                    imageVersion = imageVersion,
                                                    modifier = Modifier.size(if (isLandscapeMini) 55.dp else dims.avatarSizeSummaryDp.dp)
                                                )
                                                Spacer(modifier = Modifier.height(if (isLandscapeMini) 2.dp else 6.dp))
                                                Text(
                                                    text = currentConcejal.name,
                                                    fontSize = if (isLandscapeMini) 13.sp else if (screenMetrics.isTabletOrDesktop) 19.sp else 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = juntaTxtColor,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(if (isLandscapeMini) 1.dp else 2.dp))
                                                Text(
                                                    text = "LISTA ${currentJunta.list}",
                                                    fontSize = if (isLandscapeMini) 11.sp else if (screenMetrics.isTabletOrDesktop) 15.sp else 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = juntaSubTxtColor
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                SoundPlayer.playClick()
                                                votingStep = VotingStep.JUNTA
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isHighContrast) Color.Black else if (isJuntaParty) Color.White else Color(0xFF4B5563),
                                                contentColor = if (isHighContrast) highContrastColor else if (isJuntaParty) juntaPartyColor else Color.White
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            border = if (isHighContrast) BorderStroke(1.dp, highContrastColor) else null,
                                            contentPadding = if (isLandscapeMini) PaddingValues(horizontal = 8.dp, vertical = 2.dp) else PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                            modifier = Modifier
                                                .heightIn(min = if (isLandscapeMini) 28.dp else 40.dp)
                                                .testTag("modificar_junta_button")
                                        ) {
                                            Text(
                                                text = texts.btnModificar, 
                                                fontStyle = FontStyle.Italic, 
                                                fontWeight = FontWeight.Bold,
                                                fontSize = if (isLandscapeMini) 11.sp else if (screenMetrics.isTabletOrDesktop) 15.sp else 13.sp
                                            )
                                        }
                                    }

                                    // Column 3: Summary Actions (Restart / Print)
                                    Column(
                                        modifier = Modifier
                                            .width(actionColWidth)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        // Orange button "Reiniciar Selección"
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .background(if (isHighContrast) Color.Black else customBtnRestartColor)
                                                .border(1.dp, if (isHighContrast) highContrastColor else Color.White)
                                                .clickable {
                                                    SoundPlayer.playClick()
                                                    selectedIntendente = null
                                                    selectedJuntaList = null
                                                    selectedConcejal = null
                                                    votingStep = VotingStep.INTENDENTE
                                                }
                                                .testTag("reiniciar_seleccion_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Reiniciar",
                                                    tint = if (isHighContrast) highContrastColor else Color.White,
                                                    modifier = Modifier.size(actionIconSize)
                                                )
                                                Spacer(modifier = Modifier.height(if (isLandscapeMini) 2.dp else 8.dp))
                                                Text(
                                                    text = texts.btnReiniciar,
                                                    color = if (isHighContrast) highContrastColor else Color.White,
                                                    fontSize = actionTextSize,
                                                    fontWeight = FontWeight.Bold,
                                                    fontStyle = FontStyle.Italic,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        // Green button "Imprimir Selección"
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .background(if (isHighContrast) Color.Black else customBtnPrintColor)
                                                .border(1.dp, if (isHighContrast) highContrastColor else Color.White)
                                                .clickable {
                                                    SoundPlayer.playPrint()
                                                    isPrinting = true
                                                }
                                                .testTag("imprimir_seleccion_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Imprimir",
                                                    tint = if (isHighContrast) highContrastColor else Color.White,
                                                    modifier = Modifier.size(actionIconSize)
                                                )
                                                Spacer(modifier = Modifier.height(if (isLandscapeMini) 2.dp else 8.dp))
                                                Text(
                                                    text = texts.btnImprimir,
                                                    color = if (isHighContrast) highContrastColor else Color.White,
                                                    fontSize = actionTextSize,
                                                    fontWeight = FontWeight.Bold,
                                                    fontStyle = FontStyle.Italic,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }

                    // Floating controls overlay ONLY when in Fullscreen Mode
                    if (isFullScreen) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    SoundPlayer.playClick()
                                    isHighContrast = !isHighContrast
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Alto contraste",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    SoundPlayer.playClick()
                                    showPinDialog = true
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configuración",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    SoundPlayer.playClick()
                                    val updated = config.copy(isFullScreen = false)
                                    config = updated
                                    ConfigManager.saveConfig(context, updated)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Salir de pantalla completa",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // BOTTOM FOOTER
                if (!isFullScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (screenMetrics.isPortrait) 50.dp else 54.dp)
                            .background(if (isHighContrast) Color.Black else customFooterBgColor)
                            .border(1.dp, themeBorder)
                            .padding(horizontal = if (screenMetrics.isPortrait) 8.dp else 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Accessibility (High contrast toggle row)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    SoundPlayer.playClick()
                                    isHighContrast = !isHighContrast
                                }
                                .padding(if (screenMetrics.isPortrait) 4.dp else 8.dp)
                                .testTag("alto_contraste_toggle"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Alto contraste",
                                tint = themeText,
                                modifier = Modifier.size(if (screenMetrics.isPortrait) 20.dp else 24.dp)
                            )
                            Spacer(modifier = Modifier.width(if (screenMetrics.isPortrait) 4.dp else 8.dp))
                            Text(
                                text = texts.txtAltoContraste,
                                color = themeText,
                                fontSize = if (screenMetrics.isPortrait) 9.5.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = if (screenMetrics.isPortrait) 11.sp else 12.sp
                            )
                        }

                        // Center Configuration toggle Row
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    SoundPlayer.playClick()
                                    showPinDialog = true
                                }
                                .padding(if (screenMetrics.isPortrait) 4.dp else 8.dp)
                                .testTag("configuracion_toggle"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuraciones",
                                tint = themeText,
                                modifier = Modifier.size(if (screenMetrics.isPortrait) 20.dp else 22.dp)
                            )
                            Spacer(modifier = Modifier.width(if (screenMetrics.isPortrait) 4.dp else 6.dp))
                            Text(
                                text = texts.txtConfigurar,
                                color = themeText,
                                fontSize = if (screenMetrics.isPortrait) 9.5.sp else 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Right Navigation Back button
                        val showBackButton = votingStep == VotingStep.JUNTA || votingStep == VotingStep.CONCEJAL
                        if (showBackButton) {
                            Button(
                                onClick = {
                                    SoundPlayer.playClick()
                                    votingStep = when (votingStep) {
                                        VotingStep.JUNTA -> VotingStep.INTENDENTE
                                        VotingStep.CONCEJAL -> VotingStep.JUNTA
                                        else -> VotingStep.INTENDENTE
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHighContrast) Color.Black else Color(0xFF6B7280),
                                    contentColor = if (isHighContrast) highContrastColor else Color.White
                                ),
                                shape = RoundedCornerShape(4.dp),
                                border = if (isHighContrast) BorderStroke(1.dp, highContrastColor) else null,
                                contentPadding = if (screenMetrics.isPortrait) PaddingValues(horizontal = 8.dp, vertical = 4.dp) else PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("volver_atras_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver atras",
                                    tint = if (isHighContrast) highContrastColor else Color.White,
                                    modifier = Modifier.size(if (screenMetrics.isPortrait) 16.dp else 20.dp)
                                )
                                Spacer(modifier = Modifier.width(if (screenMetrics.isPortrait) 3.dp else 6.dp))
                                Text(
                                    text = texts.btnVolverAtras, 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (screenMetrics.isPortrait) 11.sp else 13.sp
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(if (screenMetrics.isPortrait) 60.dp else 100.dp))
                        }
                    }
                }
            }
        }

        // FULLSCREEN PRINT ANIMATION DIALOG OVERLAY
        AnimatedVisibility(
            visible = isPrinting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    CircularProgressIndicator(
                        color = customBtnPrintColor,
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = texts.printTitle,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = texts.printSubtitle,
                        color = Color(0xFF94A3B8),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                // Simulated delay to show print completion, then resets state
                LaunchedEffect(isPrinting) {
                    if (isPrinting) {
                        delay(2400)
                        isPrinting = false
                        selectedIntendente = null
                        selectedJuntaList = null
                        selectedConcejal = null
                        votingStep = VotingStep.SCREEN_SAVER
                    }
                }
            }
        }

        // SECURITY PIN VERIFICATION FOR CONFIGURATION
        if (showPinDialog) {
            SettingsPinDialog(
                onDismiss = { showPinDialog = false },
                onPinVerified = {
                    showPinDialog = false
                    showSettingsDialog = true
                }
            )
        }

        // ADVANCED LIVE CONFIGURATION DIALOG
        if (showSettingsDialog) {
            AdvancedSettingsDialog(
                config = config,
                imageVersion = imageVersion,
                onConfigChange = { newConfig ->
                    config = newConfig
                },
                onClose = { showSettingsDialog = false },
                onLogout = {
                    AuthManager.logout(context)
                    isLoggedIn = false
                    showSettingsDialog = false
                    votingStep = VotingStep.SCREEN_SAVER
                },
                onSelectImage = { targetId ->
                    targetConfiguringId = targetId
                    imagePickerLauncher.launch("image/*")
                },
                onResetImage = { targetId ->
                    val fileName = if (targetId == "screensaver") "screensaver_bg.png" else "candidate_$targetId.png"
                    val file = File(context.filesDir, fileName)
                    if (file.exists()) {
                        file.delete()
                        imageVersion += 1
                    }
                },
                onPhotosUpdated = {
                    imageVersion += 1
                }
            )
        }
    }
}

// ---------------- LOGIN SCREEN ----------------
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf(AuthManager.getSavedUsername(context)) }
    var password by remember { mutableStateOf(AuthManager.getSavedPassword(context)) }
    var rememberPassword by remember { mutableStateOf(AuthManager.isRememberPasswordEnabled(context)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun executeLogin() {
        SoundPlayer.playClick()
        val storedPass = AuthManager.getPassword(context)
        if (username.trim().equals(AuthManager.DEFAULT_USER, ignoreCase = true) && password == storedPass) {
            AuthManager.saveLoginSession(context, username.trim(), password, rememberPassword)
            onLoginSuccess()
        } else {
            errorMessage = "Credenciales incorrectas. Verifique usuario y contraseña."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFDC2626), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Seguridad",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SIMULADOR DE VOTACIÓN",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Ingrese sus credenciales para acceder",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = null
                    },
                    label = { Text("Usuario", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFDC2626),
                        unfocusedBorderColor = Color(0xFF475569),
                        cursorColor = Color(0xFFDC2626)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_username_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Contraseña", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation('*'),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFDC2626),
                        unfocusedBorderColor = Color(0xFF475569),
                        cursorColor = Color(0xFFDC2626)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { executeLogin() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { rememberPassword = !rememberPassword }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberPassword,
                        onCheckedChange = { rememberPassword = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFDC2626),
                            uncheckedColor = Color(0xFF94A3B8),
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.testTag("remember_password_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Guardar contraseña y mantener sesión",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { executeLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    )
                ) {
                    Text(
                        text = "INICIAR SESIÓN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ---------------- CUSTOM COMPOSABLE UI DRAWINGS ----------------

// Canvas drawing representing candidates profile
@Composable
fun CandidateAvatar(
    name: String,
    isBlanco: Boolean = false,
    modifier: Modifier = Modifier,
    id: String? = null,
    imageVersion: Int = 0,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val context = LocalContext.current
    val customFile = remember(id, imageVersion) { 
        if (id != null) File(context.filesDir, "candidate_$id.png") else null 
    }
    
    if (customFile != null && customFile.exists()) {
        coil.compose.AsyncImage(
            model = customFile,
            contentDescription = name,
            modifier = modifier
                .clip(shape)
                .background(Color.LightGray, shape)
                .border(2.dp, Color.White, shape),
            contentScale = ContentScale.Crop
        )
    } else if (isBlanco) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            drawCircle(color = Color(0xFFE2E8F0), radius = w / 2f)
            
            val rectSlotW = w * 0.44f
            val rectSlotH = h * 0.52f
            drawRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(w * 0.28f, h * 0.24f),
                size = Size(rectSlotW, rectSlotH),
                style = Stroke(width = w * 0.04f)
            )
            drawCircle(
                color = Color(0xFFCBD5E1),
                radius = w * 0.05f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
        }
    } else {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            
            val hash = name.hashCode().absoluteValue
            val bgColors = listOf(
                Color(0xFFE0F7FA), Color(0xFFFFF9C4), Color(0xFFF1F8E9),
                Color(0xFFEDE7F6), Color(0xFFFCE4EC), Color(0xFFE8F5E9)
            )
            val bgColor = bgColors[hash % bgColors.size]
            
            drawCircle(
                color = bgColor,
                radius = w / 2f,
                center = center
            )
            
            val shoulderColor = Color(0xFFD32F2F)
            val shoulderPath = Path().apply {
                moveTo(w * 0.15f, h * 0.95f)
                quadraticTo(w * 0.5f, h * 0.65f, w * 0.85f, h * 0.95f)
                close()
            }
            drawPath(path = shoulderPath, color = shoulderColor)
            
            val skinColor = Color(0xFFFFD1A4)
            drawRect(
                color = skinColor,
                topLeft = Offset(w * 0.42f, h * 0.52f),
                size = Size(w * 0.16f, h * 0.15f)
            )
            
            drawOval(
                color = skinColor,
                topLeft = Offset(w * 0.32f, h * 0.28f),
                size = Size(w * 0.36f, h * 0.36f)
            )
            
            val isFemale = name.contains("NELLY", ignoreCase = true) ||
                    name.contains("ALICIA", ignoreCase = true) ||
                    name.contains("NOEMI", ignoreCase = true) ||
                    name.contains("BALBUENA", ignoreCase = true) ||
                    name.contains("FLORENTIN", ignoreCase = true) ||
                    name.contains("NIDIA", ignoreCase = true) ||
                    name.contains("RAMONA", ignoreCase = true)
            val hairColor = if (name.contains("RAMONA", ignoreCase = true) || name.contains("ALICIA", ignoreCase = true)) Color(0xFFF59E0B) else Color(0xFF212121)
            
            if (isFemale) {
                val hairPath = Path().apply {
                    moveTo(w * 0.31f, h * 0.45f)
                    cubicTo(w * 0.25f, h * 0.2f, w * 0.75f, h * 0.2f, w * 0.69f, h * 0.45f)
                    lineTo(w * 0.73f, h * 0.75f)
                    quadraticTo(w * 0.65f, h * 0.85f, w * 0.60f, h * 0.70f)
                    lineTo(w * 0.40f, h * 0.70f)
                    quadraticTo(w * 0.35f, h * 0.85f, w * 0.27f, h * 0.75f)
                    close()
                }
                drawPath(path = hairPath, color = hairColor)
            } else {
                val hairPath = Path().apply {
                    moveTo(w * 0.31f, h * 0.40f)
                    cubicTo(w * 0.28f, h * 0.22f, w * 0.72f, h * 0.22f, w * 0.69f, h * 0.40f)
                    lineTo(w * 0.68f, h * 0.32f)
                    quadraticTo(w * 0.5f, h * 0.26f, w * 0.32f, h * 0.32f)
                    close()
                }
                drawPath(path = hairPath, color = hairColor)
            }
            
            val eyeColor = Color(0xFF1E293B)
            drawCircle(color = eyeColor, radius = w * 0.025f, center = Offset(w * 0.44f, h * 0.43f))
            drawCircle(color = eyeColor, radius = w * 0.025f, center = Offset(w * 0.56f, h * 0.43f))
            
            val smilePath = Path().apply {
                moveTo(w * 0.45f, h * 0.54f)
                quadraticTo(w * 0.5f, h * 0.58f, w * 0.55f, h * 0.54f)
            }
            drawPath(path = smilePath, color = Color(0xFF8D6E63), style = Stroke(width = w * 0.015f))
            
            if (name.contains("ESPINOLA", ignoreCase = true) || name.contains("HERMINIO", ignoreCase = true)) {
                val mustachePath = Path().apply {
                    moveTo(w * 0.43f, h * 0.51f)
                    quadraticTo(w * 0.5f, h * 0.53f, w * 0.57f, h * 0.51f)
                    lineTo(w * 0.55f, h * 0.53f)
                    lineTo(w * 0.45f, h * 0.53f)
                    close()
                }
                drawPath(path = mustachePath, color = Color(0xFF111111))
            }
            
            if (name.contains("WEINERT", ignoreCase = true) || name.contains("ESCRIVANO", ignoreCase = true)) {
                val strokeW = w * 0.015f
                drawCircle(
                    color = Color(0xFF374151),
                    radius = w * 0.06f,
                    center = Offset(w * 0.43f, h * 0.45f),
                    style = Stroke(width = strokeW)
                )
                drawCircle(
                    color = Color(0xFF374151),
                    radius = w * 0.06f,
                    center = Offset(w * 0.57f, h * 0.45f),
                    style = Stroke(width = strokeW)
                )
                drawLine(
                    color = Color(0xFF374151),
                    start = Offset(w * 0.46f, h * 0.45f),
                    end = Offset(w * 0.54f, h * 0.45f),
                    strokeWidth = strokeW
                )
            }
        }
    }
}

// Canvas-drawn Paraguay flag to avoid external dependencies
@Composable
fun FlagLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stripeH = h / 3f
        
        drawRect(color = Color(0xFFD32F2F), topLeft = Offset(0f, 0f), size = Size(w, stripeH))
        drawRect(color = Color.White, topLeft = Offset(0f, stripeH), size = Size(w, stripeH))
        drawRect(color = Color(0xFF1976D2), topLeft = Offset(0f, stripeH * 2), size = Size(w, stripeH))
        
        drawCircle(
            color = Color(0xFFFBC02D),
            radius = stripeH * 0.35f,
            center = Offset(w / 2f, stripeH * 1.5f)
        )
    }
}

// ---------------- STEP COMPOSABLE TILES ----------------

// Candidate selection card for Step 1
@Composable
fun CandidateCard(
    candidate: Candidate,
    isHighContrast: Boolean,
    themeCardBg: Color,
    themeText: Color,
    themeSubText: Color,
    dimensions: DimensionsConfig,
    primaryColor: Color,
    imageVersion: Int = 0,
    onClick: () -> Unit
) {
    val hasCustomColor = !candidate.colorType.isNullOrBlank() && candidate.colorType?.lowercase() != "white"
    val partyColor = getCandidatePrimaryColor(candidate, primaryColor)
    val cardBg = when {
        isHighContrast -> themeCardBg
        hasCustomColor -> partyColor
        else -> themeCardBg
    }

    val cardTxt = when {
        isHighContrast -> themeText
        hasCustomColor -> getContrastingTextColor(partyColor)
        else -> themeText
    }

    val cardSubTxt = when {
        isHighContrast -> themeSubText
        hasCustomColor -> getContrastingSubTextColor(partyColor)
        else -> themeSubText
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.intendenteCardHeightDp.dp)
            .shadow(if (isHighContrast) 0.dp else 2.dp, RoundedCornerShape(4.dp))
            .border(
                width = if (isHighContrast) 2.dp else 1.dp,
                color = if (isHighContrast) Color(0xFFFFD700) else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(4.dp)
            )
            .alpha(if (candidate.works) 1f else 0.55f)
            .clickable(enabled = candidate.works, onClick = onClick)
            .testTag("candidate_intendente_${candidate.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Party identifier
            Text(
                text = if (candidate.works) candidate.party else "${candidate.party} (BLOQUEADO)",
                color = if (!candidate.works && !isHighContrast && !hasCustomColor) Color.Gray else cardSubTxt,
                fontSize = dimensions.partyNameTextSizeSp.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Main row including avatar and list identifier
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CandidateAvatar(
                    name = candidate.name,
                    id = candidate.id,
                    imageVersion = imageVersion,
                    modifier = Modifier
                        .size(dimensions.avatarSizeIntendenteDp.dp)
                        .padding(start = 4.dp)
                )

                // List block right
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = "LISTA",
                        fontSize = (dimensions.partyNameTextSizeSp).sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = cardSubTxt
                    )
                    Text(
                        text = candidate.list,
                        fontSize = dimensions.listNumberTextSizeSp.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isHighContrast) Color(0xFFFFD700)
                                else if (hasCustomColor) getContrastingTextColor(partyColor)
                                else Color(0xFFDC2626),
                        lineHeight = (dimensions.listNumberTextSizeSp + 2).sp
                    )
                    Text(
                        text = candidate.initials,
                        fontSize = (dimensions.partyNameTextSizeSp + 1).sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = cardSubTxt
                    )
                }
            }

            // Name footer
            Text(
                text = candidate.name,
                color = cardTxt,
                fontSize = dimensions.candidateNameTextSizeSp.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Junta selection list card for Step 2
@Composable
fun JuntaListCard(
    candidate: Candidate,
    isHighContrast: Boolean,
    themeCardBg: Color,
    themeText: Color,
    themeSubText: Color,
    dimensions: DimensionsConfig,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val hasCustomColor = !candidate.colorType.isNullOrBlank() && candidate.colorType?.lowercase() != "white"
    val partyColor = getCandidatePrimaryColor(candidate, primaryColor)
    val cardBg = when {
        isHighContrast -> themeCardBg
        hasCustomColor -> partyColor
        else -> themeCardBg
    }

    val cardTxt = when {
        isHighContrast -> themeText
        hasCustomColor -> getContrastingTextColor(partyColor)
        else -> themeText
    }

    val cardSubTxt = when {
        isHighContrast -> themeSubText
        hasCustomColor -> getContrastingSubTextColor(partyColor)
        else -> themeSubText
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.juntaCardHeightDp.dp)
            .shadow(if (isHighContrast) 0.dp else 2.dp, RoundedCornerShape(4.dp))
            .border(
                width = if (isHighContrast) 2.dp else 1.dp,
                color = if (isHighContrast) Color(0xFFFFD700) else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(4.dp)
            )
            .alpha(if (candidate.works) 1f else 0.55f)
            .clickable(enabled = candidate.works, onClick = onClick)
            .testTag("junta_list_${candidate.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (candidate.works) "LISTA  ${candidate.list}" else "LISTA  ${candidate.list}  (BLOQUEADO)",
                color = if (isHighContrast) Color(0xFFFFD700)
                        else if (hasCustomColor) getContrastingTextColor(partyColor)
                        else Color(0xFF1E3A8A),
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = (dimensions.candidateNameTextSizeSp + 3).sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = candidate.party,
                color = cardTxt,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = dimensions.candidateNameTextSizeSp.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = candidate.initials,
                color = cardSubTxt,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = dimensions.partyNameTextSizeSp.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Concejal option selection card inside Step 3
@Composable
fun ConcejalCard(
    candidate: Candidate,
    isHighContrast: Boolean,
    themeCardBg: Color,
    themeText: Color,
    dimensions: DimensionsConfig,
    primaryColor: Color = Color(0xFFDC2626),
    imageVersion: Int = 0,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.concejalCardHeightDp.dp)
            .shadow(if (isHighContrast) 0.dp else 1.dp, RoundedCornerShape(4.dp))
            .border(
                width = if (isHighContrast) 2.dp else 1.dp,
                color = if (isHighContrast) Color(0xFFFFD700) else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .testTag("concejal_option_${candidate.optionId}"),
        colors = CardDefaults.cardColors(containerColor = themeCardBg),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CandidateAvatar(
                    name = candidate.name,
                    id = candidate.id,
                    imageVersion = imageVersion,
                    modifier = Modifier
                        .size(dimensions.avatarSizeConcejalDp.dp)
                        .padding(start = 2.dp)
                )

                Surface(
                    color = if (isHighContrast) Color.Black else primaryColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (isHighContrast) Color(0xFFFFD700) else primaryColor.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Opción",
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighContrast) Color(0xFFFFD700) else primaryColor
                        )
                        Text(
                            text = candidate.optionId?.toString() ?: "",
                            fontSize = dimensions.optionNumberTextSizeSp.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isHighContrast) Color(0xFFFFD700) else primaryColor,
                            lineHeight = dimensions.optionNumberTextSizeSp.sp
                        )
                    }
                }
            }

            Text(
                text = candidate.name,
                color = themeText,
                fontSize = dimensions.candidateNameTextSizeSp.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Full-length or Grid Blank vote election card
@Composable
fun VotoBlancoCard(
    isHighContrast: Boolean,
    themeCardBg: Color,
    themeText: Color,
    label: String = "VOTO EN BLANCO",
    height: androidx.compose.ui.unit.Dp = 72.dp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(if (isHighContrast) 0.dp else 2.dp, RoundedCornerShape(4.dp))
            .border(
                width = if (isHighContrast) 2.dp else 1.dp,
                color = if (isHighContrast) Color(0xFFFFD700) else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .testTag("voto_blanco_card"),
        colors = CardDefaults.cardColors(containerColor = themeCardBg),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = themeText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp
            )
        }
    }
}
