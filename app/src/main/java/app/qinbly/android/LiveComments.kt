package app.qinbly.android

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Comentários da live de [host] (o @ de quem transmite): busca os novos a cada 3 segundos e mostra os últimos.
 * Mudar [refreshKey] (depois de enviar um comentário) puxa na hora.
 */
@Composable
internal fun QinblyLiveCommentsList(host: String, refreshKey: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val token = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).getString("token", null) }
    var comments by remember(host) { mutableStateOf<List<QinblyApi.LiveComment>>(emptyList()) }
    LaunchedEffect(host, refreshKey) {
        while (true) {
            val after = comments.lastOrNull()?.createdAt ?: ""
            val fresh = try { withContext(Dispatchers.IO) { QinblyApi.getLiveComments(token, host, after) } } catch (_: Exception) { emptyList() }
            val known = comments.map { it.id }.toSet()
            val add = fresh.filter { it.id !in known }
            if (add.isNotEmpty()) comments = (comments + add).takeLast(40)
            delay(3000)
        }
    }
    if (comments.isEmpty()) return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        comments.takeLast(7).forEach { c ->
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(c.displayName.ifBlank { c.username } + "  ") }
                    append(c.body)
                },
                color = Color.White, fontSize = 13.sp,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x66000000)).padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

/** Envia o comentário numa thread e, ao terminar, avisa [onSent] (thread principal) ou [onError]. */
internal fun qinblySendLiveComment(context: Context, host: String, text: String, onSent: () -> Unit, onError: (String) -> Unit) {
    val token = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).getString("token", null)
    if (token.isNullOrBlank()) { onError("Entre na sua conta para comentar."); return }
    val main = android.os.Handler(android.os.Looper.getMainLooper())
    Thread {
        try {
            QinblyApi.sendLiveComment(token, host, text)
            main.post { onSent() }
        } catch (e: Exception) {
            main.post { onError(e.message ?: "Não foi possível comentar.") }
        }
    }.start()
}
