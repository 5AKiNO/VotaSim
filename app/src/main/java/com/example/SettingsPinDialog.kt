package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SettingsPinDialog(
    onDismiss: () -> Unit,
    onPinVerified: () -> Unit
) {
    val context = LocalContext.current
    var pinValue by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun verifyEnteredPin() {
        SoundPlayer.playClick()
        if (AuthManager.checkSettingsPin(context, pinValue)) {
            SoundPlayer.playPrint()
            onPinVerified()
        } else {
            errorMessage = "PIN incorrecto. Intente nuevamente."
            pinValue = ""
        }
    }

    fun onKeypadDigit(digit: String) {
        if (pinValue.length < 8) {
            SoundPlayer.playClick()
            val newPin = pinValue + digit
            pinValue = newPin
            errorMessage = null

            // Auto-verify if the length matches the stored PIN length
            val storedPin = AuthManager.getSettingsPin(context)
            if (newPin.length == storedPin.length && newPin == storedPin) {
                SoundPlayer.playPrint()
                onPinVerified()
            }
        }
    }

    fun onKeypadBackspace() {
        if (pinValue.isNotEmpty()) {
            SoundPlayer.playClick()
            pinValue = pinValue.dropLast(1)
            errorMessage = null
        }
    }

    fun onKeypadClear() {
        SoundPlayer.playClick()
        pinValue = ""
        errorMessage = null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 320.dp, max = 560.dp)
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .testTag("pin_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                val isLandscapeWide = maxWidth >= 460.dp

                if (isLandscapeWide) {
                    // Landscape 2-column layout (Left: Info & Input, Right: On-screen Keypad)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column: Header, Pin Input, Actions
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            PinDialogHeader()

                            Spacer(modifier = Modifier.height(14.dp))

                            PinDisplayDots(
                                pinLength = pinValue.length,
                                isError = errorMessage != null
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            PinInputField(
                                value = pinValue,
                                onValueChange = {
                                    pinValue = it.filter { c -> c.isDigit() }.take(8)
                                    errorMessage = null
                                },
                                isVisible = isPinVisible,
                                onToggleVisibility = { isPinVisible = !isPinVisible },
                                onDone = { verifyEnteredPin() }
                            )

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            PinDialogActionButtons(
                                onDismiss = onDismiss,
                                onSubmit = { verifyEnteredPin() },
                                isSubmitEnabled = pinValue.isNotEmpty()
                            )
                        }

                        // Right Column: Keypad
                        Column(
                            modifier = Modifier.weight(0.9f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PinKeypad(
                                onDigit = { onKeypadDigit(it) },
                                onClear = { onKeypadClear() },
                                onBackspace = { onKeypadBackspace() }
                            )
                        }
                    }
                } else {
                    // Single-column layout (Compact/Portrait)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PinDialogHeader()

                        Spacer(modifier = Modifier.height(14.dp))

                        PinDisplayDots(
                            pinLength = pinValue.length,
                            isError = errorMessage != null
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        PinInputField(
                            value = pinValue,
                            onValueChange = {
                                pinValue = it.filter { c -> c.isDigit() }.take(8)
                                errorMessage = null
                            },
                            isVisible = isPinVisible,
                            onToggleVisibility = { isPinVisible = !isPinVisible },
                            onDone = { verifyEnteredPin() }
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        PinKeypad(
                            onDigit = { onKeypadDigit(it) },
                            onClear = { onKeypadClear() },
                            onBackspace = { onKeypadBackspace() }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PinDialogActionButtons(
                            onDismiss = onDismiss,
                            onSubmit = { verifyEnteredPin() },
                            isSubmitEnabled = pinValue.isNotEmpty()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PinDialogHeader() {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "PIN de Seguridad",
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        ),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Ingrese el PIN para acceder a la configuración.",
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
private fun PinDisplayDots(pinLength: Int, isError: Boolean) {
    val totalSlots = 4
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSlots) {
            val isFilled = i < pinLength
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isError -> MaterialTheme.colorScheme.error
                            isFilled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .border(
                        width = 1.5.dp,
                        color = when {
                            isError -> MaterialTheme.colorScheme.error
                            isFilled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("PIN de seguridad", fontSize = 13.sp) },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation('•'),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = {
            TextButton(onClick = onToggleVisibility) {
                Text(
                    text = if (isVisible) "Ocultar" else "Ver",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pin_input_field")
    )
}

@Composable
private fun PinKeypad(
    onDigit: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        text = key,
                        onClick = {
                            when (key) {
                                "C" -> onClear()
                                "⌫" -> onBackspace()
                                else -> onDigit(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    val isSpecial = text == "C" || text == "⌫"
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSpecial) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .size(width = 54.dp, height = 44.dp)
            .testTag("pin_key_$text")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = if (isSpecial) 15.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (text == "C") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PinDialogActionButtons(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    isSubmitEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("pin_cancel_button")
        ) {
            Text("Cancelar")
        }
        Button(
            onClick = onSubmit,
            enabled = isSubmitEnabled,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("pin_submit_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Ingresar")
        }
    }
}
