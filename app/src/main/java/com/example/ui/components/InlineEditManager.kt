package com.example.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioRed

data class InlineEditRequest(
    val fieldName: String,
    val currentValue: String,
    val onSave: (String) -> Unit
)

class InlineEditManager {
    var activeRequest by mutableStateOf<InlineEditRequest?>(null)
        private set

    fun requestEdit(fieldName: String, currentValue: String, onSave: (String) -> Unit) {
        activeRequest = InlineEditRequest(fieldName, currentValue, onSave)
    }

    fun dismiss() {
        activeRequest = null
    }
}

val LocalInlineEditManager = staticCompositionLocalOf<InlineEditManager?> { null }

/**
 * Safe, reusable inline double-tap editing modifier.
 * - Single click triggers [onSingleClick] if provided (e.g. for card navigation).
 * - Double tap triggers the Material 3 edit dialog to modify and persist the value.
 */
fun Modifier.editableDoubleTap(
    currentValue: String,
    label: String = "Edit Value",
    fieldName: String = label,
    onSingleClick: (() -> Unit)? = null,
    onSave: (String) -> Unit
): Modifier = composed {
    val manager = LocalInlineEditManager.current
    val title = if (fieldName.isNotBlank()) fieldName else label
    this.pointerInput(title, currentValue) {
        detectTapGestures(
            onTap = {
                onSingleClick?.invoke()
            },
            onDoubleTap = {
                manager?.requestEdit(
                    fieldName = title,
                    currentValue = currentValue,
                    onSave = onSave
                )
            }
        )
    }
}

fun Modifier.editableDoubleTap(
    fieldName: String,
    currentValue: String,
    onSingleClick: (() -> Unit)? = null,
    onSave: (String) -> Unit
): Modifier = editableDoubleTap(
    currentValue = currentValue,
    label = fieldName,
    fieldName = fieldName,
    onSingleClick = onSingleClick,
    onSave = onSave
)

@Composable
fun InlineEditDialogHost(manager: InlineEditManager) {
    val request = manager.activeRequest ?: return
    var textValue by remember(request) { mutableStateOf(request.currentValue) }

    AlertDialog(
        onDismissRequest = { manager.dismiss() },
        modifier = Modifier.testTag("inline_edit_dialog"),
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Edit ${request.fieldName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F0F0F)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Double-tap editing will update Compose state and permanently persist locally.",
                    fontSize = 12.sp,
                    color = Color(0xFF606060),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text(request.fieldName) },
                    singleLine = !request.fieldName.contains("Description", ignoreCase = true) && !request.fieldName.contains("Headline", ignoreCase = true),
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inline_edit_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F0F0F),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFF0F0F0F),
                        cursorColor = StudioRed
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    request.onSave(textValue)
                    manager.dismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F0F0F)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("inline_edit_save_button")
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { manager.dismiss() },
                modifier = Modifier.testTag("inline_edit_cancel_button")
            ) {
                Text("Cancel", color = Color(0xFF606060), fontWeight = FontWeight.Medium)
            }
        }
    )
}
