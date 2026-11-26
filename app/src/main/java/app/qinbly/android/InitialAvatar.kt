package app.qinbly.android

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight

/** Cores da foto padrão (quem ainda não colocou foto). Cada pessoa fica sempre com a mesma, escolhida pelo @. */
internal val QinblyAvatarColors = listOf(
    Color(0xFF7C4DFF), Color(0xFF2F68E0), Color(0xFFE0368C), Color(0xFF1E9E6A),
    Color(0xFFF08A24), Color(0xFF1AA3B8), Color(0xFFE5484D), Color(0xFF565BF0)
)

internal fun qinblyAvatarColor(seed: String): Color {
    val key = seed.trim().removePrefix("@").lowercase()
    var h = 0
    for (c in key) h = (h * 31 + c.code) and 0x7FFFFFFF
    return QinblyAvatarColors[h % QinblyAvatarColors.size]
}

internal fun qinblyAvatarLetter(name: String): String =
    name.trim().removePrefix("@").firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "?"

/** Foto padrão: primeira letra do nome em branco, em cima de uma cor fixa por pessoa. Ocupa todo o espaço que receber. */
@Composable
internal fun QinblyInitialAvatar(name: String, seed: String = name, modifier: Modifier = Modifier.fillMaxSize()) {
    BoxWithConstraints(modifier.background(qinblyAvatarColor(seed)), contentAlignment = Alignment.Center) {
        val size = with(LocalDensity.current) { (minOf(maxWidth, maxHeight) * 0.46f).toSp() }
        Text(qinblyAvatarLetter(name), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = size)
    }
}
