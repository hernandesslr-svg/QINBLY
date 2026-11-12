package app.qinbly.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

/**
 * Substitui o AlertDialog quadradão do Material por uma folha moderna que sobe de baixo
 * (padrão Material 3 Expressive / Instagram): cantos bem redondos, alça no topo, botões em pílula,
 * sempre acima dos botões do celular. Tem a mesma assinatura do AlertDialog, então todas as caixinhas
 * do app passam a usar este visual sem mudar nada nas telas.
 */
@Composable
internal fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(28.dp),
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
) {
    val nav = qinblyNavBarPadding()
    val none = remember { MutableInteractionSource() }
    QinblyDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        lift = false
    ) {
        Box(
            Modifier.fillMaxSize().background(Color(0x99000000))
                .clickable(indication = null, interactionSource = none) { if (properties.dismissOnClickOutside) onDismissRequest() }
        ) {
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 48.dp, bottom = nav + 8.dp)
                    .clip(RoundedCornerShape(32.dp)).background(containerColor)
                    .clickable(indication = null, interactionSource = none) { }
                    .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 18.dp)
                    .heightIn(max = 560.dp)
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(width = 40.dp, height = 5.dp).clip(CircleShape).background(textContentColor.copy(alpha = 0.25f)))
                Spacer(Modifier.size(16.dp))
                if (icon != null) {
                    Box(Modifier.align(Alignment.CenterHorizontally)) { CompositionLocalProvider(LocalContentColor provides iconContentColor) { icon() } }
                    Spacer(Modifier.size(10.dp))
                }
                if (title != null) {
                    CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                        ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp, fontWeight = FontWeight.Bold)) { title() }
                    }
                    Spacer(Modifier.size(10.dp))
                }
                if (text != null) {
                    Box(Modifier.weight(1f, fill = false)) {
                        CompositionLocalProvider(LocalContentColor provides textContentColor) {
                            ProvideTextStyle(MaterialTheme.typography.bodyLarge) { text() }
                        }
                    }
                }
                Spacer(Modifier.size(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    if (dismissButton != null) { dismissButton(); Spacer(Modifier.width(8.dp)) }
                    confirmButton()
                }
            }
        }
    }
}
