package app.qinbly.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/** Peças da tela de entrar e criar conta: campo fino em forma de pílula, botão pílula e logo do app. */

/** Campo fino (46 dp) com ícone na frente; senha ganha o olhinho para mostrar/esconder. */
@Composable
internal fun QinblyAuthField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var shown by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50)
    val muted = QiblyTheme.secondary
    Row(
        modifier.fillMaxWidth().height(46.dp).clip(shape)
            .background(QiblyTheme.soft).border(0.5.dp, QiblyTheme.border, shape).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = muted, modifier = Modifier.size(19.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text(hint, color = muted, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = QiblyTheme.text),
                cursorBrush = SolidColor(QinblyBrand.Accent),
                visualTransformation = if (password && !shown) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (password) Icon(
            if (shown) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = if (shown) "Esconder senha" else "Mostrar senha",
            tint = muted,
            modifier = Modifier.size(20.dp).clickable { shown = !shown }
        )
    }
}

/** Botão pílula (46 dp). Cheio quando [fill] é dado; só contorno quando [outline] é dado. */
@Composable
internal fun QinblyAuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fill: Color? = null,
    contentColor: Color = Color.White,
    outline: Color? = null,
    leading: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(50)
    var base = modifier.fillMaxWidth().height(46.dp).clip(shape)
    if (fill != null) base = base.background(fill)
    if (outline != null) base = base.border(BorderStroke(1.dp, outline), shape)
    Row(
        base.clickable(enabled = enabled && !loading, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = contentColor)
        else {
            leading?.invoke()
            if (leading != null) Box(Modifier.size(10.dp))
            Text(text, color = contentColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

/** "G" simples numa bolinha branca (marcador do botão do Google; o G oficial colorido entra depois, com o arquivo da Google). */
@Composable
internal fun QinblyGoogleMark() {
    Box(Modifier.size(22.dp).clip(CircleShape).background(Color.White).border(0.5.dp, Color(0xFFD0D0D6), CircleShape), contentAlignment = Alignment.Center) {
        Text("G", color = Color(0xFF4285F4), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

/** Logo do aplicativo (anel em degradê com brilho), o mesmo do ícone. */
@Composable
internal fun QinblyAuthLogo(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.qinbly_icon_bitmap), contentDescription = "Logo", modifier = modifier.size(140.dp))
}
