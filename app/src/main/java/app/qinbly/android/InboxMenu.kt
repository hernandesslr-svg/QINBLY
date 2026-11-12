package app.qinbly.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Balão de opções de uma conversa da lista (segurar a conversa), no mesmo estilo do menu das mensagens. */
@Composable
internal fun QinblyInboxMenu(
    anchor: Rect?, unread: Boolean, pinned: Boolean, muted: Boolean, textColor: Color, accent: Color,
    onDismiss: () -> Unit, onProfile: () -> Unit, onMarkRead: () -> Unit, onTogglePin: () -> Unit, onToggleMute: () -> Unit,
    onDelete: () -> Unit, onBlock: () -> Unit, onReport: () -> Unit
) {
    val red = Color(0xFFE5484D)
    @Composable
    fun Option(icon: ImageVector, label: String, tint: Color, labelColor: Color = textColor, onClick: () -> Unit) {
        Row(
            Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Text(label, color = labelColor, fontSize = 16.sp)
        }
    }
    QinblyDialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        BoxWithConstraints(Modifier.fillMaxSize().clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { onDismiss() }) {
            val density = LocalDensity.current
            val screenH = maxHeight
            val navPx = WindowInsets.navigationBars.getBottom(density)
            val marginPx = maxOf(navPx, with(density) { 48.dp.roundToPx() }) + with(density) { 12.dp.roundToPx() }
            val topMinPx = with(density) { 70.dp.roundToPx() }
            val sidePx = with(density) { 24.dp.roundToPx() }
            val gapPx = with(density) { 4.dp.roundToPx() }
            val anchorBottom = anchor?.bottom
            Column(
                Modifier.layout { measurable, c ->
                    val p = measurable.measure(c.copy(minHeight = 0, maxHeight = (c.maxHeight - marginPx - topMinPx).coerceAtLeast(0)))
                    layout(c.maxWidth, c.maxHeight) {
                        val desired = if (anchorBottom != null) anchorBottom.toInt() + gapPx else (c.maxHeight - p.height) / 2
                        val y = desired.coerceAtMost(c.maxHeight - p.height - marginPx).coerceAtLeast(topMinPx)
                        p.place(sidePx, y)
                    }
                }.width(270.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF1B1727)).padding(vertical = 4.dp)
            ) {
                Option(Icons.Default.Person, "Ver perfil", accent) { onDismiss(); onProfile() }
                if (unread) Option(Icons.Default.Check, "Marcar como lida", accent) { onDismiss(); onMarkRead() }
                Option(Icons.Default.PushPin, if (pinned) "Desafixar conversa" else "Fixar conversa", accent) { onDismiss(); onTogglePin() }
                Option(if (muted) Icons.Default.Notifications else Icons.Default.NotificationsOff, if (muted) "Reativar notificações" else "Silenciar notificações", textColor) { onDismiss(); onToggleMute() }
                Option(Icons.Default.Delete, "Apagar conversa", red, red) { onDismiss(); onDelete() }
                Option(Icons.Default.Block, "Bloquear", red, red) { onDismiss(); onBlock() }
                Option(Icons.Default.Flag, "Denunciar", red, red) { onDismiss(); onReport() }
            }
        }
    }
}
