package ai.siher.app

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Violet = Color(0xFF8B2CF5)
private val Blue = Color(0xFF3B82F6)
private val Panel = Color(0xFF14102A)
private val Muted = Color(0xFF9A92C9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiherScreen(log: List<Pair<Boolean, String>>, onCommand: (String) -> Unit, onMic: () -> Unit) {
    var q by remember { mutableStateOf("") }
    var about by remember { mutableStateOf(false) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Violet)) {
        Column(
            Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF07040F), Color(0xFF1B0B3F), Color(0xFF07040F))))
                .systemBarsPadding().padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { about = true }) { Icon(Icons.Filled.Menu, "Меню", tint = Violet) }
                Text(
                    buildAnnotatedString {
                        append("Siher "); withStyle(SpanStyle(color = Violet)) { append("AI") }
                    },
                    Modifier.weight(1f), color = Color.White, fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(onClick = {}) { Icon(Icons.Filled.Search, "Поиск", tint = Color.White) }
            }
            OutlinedTextField(
                q, { q = it }, Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(30.dp),
                placeholder = { Text("Поиск…", color = Muted) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Muted) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onCommand(q); q = "" }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Violet, unfocusedBorderColor = Color(0xFF2A2250),
                    unfocusedContainerColor = Panel, focusedContainerColor = Panel
                )
            )
            Spacer(Modifier.height(16.dp))
            Star()
            Text("Привет!", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text("Чем я могу помочь?", color = Muted, fontSize = 19.sp)
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(log) { (me, text) ->
                    Box(Modifier.fillMaxWidth(), contentAlignment = if (me) Alignment.CenterEnd else Alignment.CenterStart) {
                        Text(
                            text, color = Color.White,
                            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                                .background(if (me) Violet else Panel).padding(12.dp, 8.dp)
                        )
                    }
                }
            }
            Box(
                Modifier.padding(vertical = 20.dp).size(96.dp).clip(CircleShape)
                    .border(4.dp, Violet, CircleShape).clickable(onClick = onMic),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Mic, "Говорить", tint = Color.White, modifier = Modifier.size(42.dp)) }
        }
        if (about) AlertDialog(
            onDismissRequest = { about = false },
            confirmButton = { TextButton({ about = false }) { Text("Закрыть") } },
            title = { Text("Siher AI") },
            text = { Text("Версия 1.0 · Android 10 и выше\n\nПогода, новости, запуск приложений, звонки.\n\nРазрешения: уведомления, местоположение, микрофон, контакты.") }
        )
    }
}

@Composable
private fun Star() {
    Canvas(Modifier.size(150.dp)) {
        val w = size.width; val h = size.height
        val p = Path().apply {
            moveTo(w / 2, 0f); quadraticBezierTo(w * .56f, h * .42f, w, h / 2)
            quadraticBezierTo(w * .56f, h * .58f, w / 2, h)
            quadraticBezierTo(w * .44f, h * .58f, 0f, h / 2)
            quadraticBezierTo(w * .44f, h * .42f, w / 2, 0f); close()
        }
        drawPath(p, Brush.linearGradient(listOf(Violet, Blue), Offset(0f, 0f), Offset(w, h)))
    }
}
