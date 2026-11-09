package app.qinbly.android

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Documento enviado numa conversa: ícone, nome e "Toque para abrir" (abre pelo navegador/visualizador do aparelho). */
@Composable
internal fun QinblyFileBubble(url: String, name: String, accent: Color, textColor: Color) {
    val context = LocalContext.current
    Row(
        Modifier.widthIn(max = 240.dp).clip(RoundedCornerShape(12.dp)).clickable {
            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { }
        }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(accent), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(name, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Toque para abrir", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
        }
    }
}

/** Enquete numa conversa: pergunta, opções com barra de porcentagem e contagem; tocar numa opção vota (ou muda o voto). */
@Composable
internal fun QinblyChatPollBubble(poll: QinblyApi.ChatPoll, accent: Color, textColor: Color, onVote: (Int) -> Unit) {
    val total = poll.votes.sum()
    Column(Modifier.widthIn(min = 200.dp, max = 260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(poll.question, color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        poll.options.forEachIndexed { i, option ->
            val count = poll.votes.getOrElse(i) { 0 }
            val share = if (total > 0) count.toFloat() / total else 0f
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(textColor.copy(alpha = 0.08f)).clickable { onVote(i) }) {
                Box(Modifier.fillMaxWidth(share.coerceIn(0f, 1f)).height(38.dp).background(accent.copy(alpha = 0.30f)))
                Row(Modifier.fillMaxWidth().heightIn(min = 38.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text((if (poll.myVote == i) "✓ " else "") + option, color = textColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(count.toString(), color = textColor.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
        Text(total.toString() + if (total == 1) " voto" else " votos", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}

/** Janela para montar a enquete: pergunta e de 2 a 6 opções. */
@Composable
internal fun QinblyPollCreateDialog(accent: Color, onDismiss: () -> Unit, onCreate: (String, List<String>) -> Unit) {
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova enquete") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(question, { question = it.take(200) }, label = { Text("Pergunta") })
                options.forEachIndexed { i, _ ->
                    OutlinedTextField(options[i], { options[i] = it.take(80) }, label = { Text("Opção " + (i + 1)) }, singleLine = true)
                }
                if (options.size < 6) TextButton(onClick = { options.add("") }) { Text("Adicionar opção", color = accent) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cleaned = options.map { it.trim() }.filter { it.isNotEmpty() }
                if (question.isNotBlank() && cleaned.size >= 2) onCreate(question.trim(), cleaned)
            }) { Text("Enviar", color = accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/** Janela "Marcar pessoas": busca por nome ou @ e escolhe até 10. Devolve os @ escolhidos (sem o "@"). */
@Composable
internal fun QinblyTagPeopleDialog(token: String?, initial: List<String>, accent: Color, onDismiss: () -> Unit, onConfirm: (List<String>) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    val chosen = remember { mutableStateListOf<String>().apply { addAll(initial) } }
    LaunchedEffect(query) {
        val q = query.trim().removePrefix("@")
        if (q.length < 2) {
            results = emptyList()
        } else {
            delay(250)
            results = try { withContext(Dispatchers.IO) { QinblyApi.searchUsers(q, token) } } catch (_: Exception) { emptyList() }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Marcar pessoas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(query, { query = it.take(30) }, label = { Text("Buscar pessoa") }, singleLine = true)
                if (chosen.isNotEmpty()) Text("Marcadas: " + chosen.joinToString(", ") { "@$it" }, color = accent, fontSize = 13.sp)
                LazyColumn(Modifier.heightIn(max = 240.dp)) {
                    items(results, key = { it.id }) { user ->
                        val on = user.username in chosen
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                if (on) chosen.remove(user.username) else if (chosen.size < 10) chosen.add(user.username)
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(user.displayName.ifBlank { user.username }, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("@" + user.username, fontSize = 12.sp, color = Color.Gray)
                            }
                            if (on) Text("✓", color = accent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(chosen.toList()) }) { Text("Pronto", color = accent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
