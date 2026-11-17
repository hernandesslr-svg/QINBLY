package app.qinbly.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
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
    keyboardType: KeyboardType = KeyboardType.Text,
    invalid: Boolean = false,
    valid: Boolean = false
) {
    var shown by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50)
    val muted = QiblyTheme.secondary
    Row(
        modifier.fillMaxWidth().height(46.dp).clip(shape)
            .background(QiblyTheme.soft).border(if (invalid) 1.dp else 0.5.dp, if (invalid) QinblyAuthWarnColor else QiblyTheme.border, shape).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (invalid) QinblyAuthWarnColor else muted, modifier = Modifier.size(19.dp))
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
        if (valid && !password) Icon(Icons.Outlined.CheckCircle, contentDescription = "Certo", tint = Color(0xFF5DCAA5), modifier = Modifier.size(19.dp))
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

internal val QinblyAuthWarnColor = Color(0xFFF09595)

/** Aviso pequeno embaixo do campo (aparece só quando o que foi digitado está fora da regra). */
@Composable
internal fun QinblyAuthWarning(text: String) {
    Row(Modifier.fillMaxWidth().padding(start = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = QinblyAuthWarnColor, modifier = Modifier.size(15.dp))
        Text(text, color = QinblyAuthWarnColor, style = MaterialTheme.typography.labelMedium)
    }
}

/** Cartinha de dica (ícone de informação + texto). */
@Composable
private fun QinblyAuthTip(text: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(QiblyTheme.soft).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = QinblyBrand.AccentLight, modifier = Modifier.size(18.dp))
        Text(text, color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall)
    }
}

/** Tela de "Esqueceu a senha?": pede e-mail ou nome de usuário; depois de enviar, mostra "Confira seu e-mail". */
@Composable
internal fun QinblyForgotScreen(
    input: String,
    onInput: (String) -> Unit,
    busy: Boolean,
    sent: Boolean,
    error: String,
    cooldown: Int,
    onSend: () -> Unit,
    onBack: () -> Unit
) {
    val green = Color(0xFF5DCAA5)
    Column(Modifier.fillMaxSize().background(QiblyTheme.background)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 12.dp)) {
            IconButton(onClick = onBack, enabled = !busy) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text)
            }
            Column(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(if (sent) Color(0xFF12332A) else QiblyTheme.soft)
                        .border(0.5.dp, if (sent) Color(0xFF1D9E75) else QiblyTheme.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (sent) Icons.Outlined.Email else Icons.Outlined.Lock, contentDescription = null, tint = if (sent) green else QinblyBrand.AccentLight, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(if (sent) "Confira seu e-mail" else "Esqueceu a senha?", color = QiblyTheme.text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (sent) "Se existir uma conta com esses dados, enviamos o link para criar uma senha nova. Pode levar alguns minutos."
                    else "Digite seu e-mail ou nome de usuário e enviamos um link para criar uma senha nova.",
                    color = QiblyTheme.secondary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center
                )
            }
            if (!sent) {
                QinblyAuthField(
                    value = input, onValueChange = onInput, hint = "E-mail ou nome de usuário",
                    icon = Icons.Outlined.PersonOutline, enabled = !busy, keyboardType = KeyboardType.Email, invalid = error.isNotBlank()
                )
                if (error.isNotBlank()) QinblyAuthWarning(error)
                Spacer(Modifier.height(16.dp))
                QinblyAuthButton("Enviar link", onClick = onSend, enabled = !busy, loading = busy, fill = QinblyBrand.Accent)
                Spacer(Modifier.height(22.dp))
                QinblyAuthTip("Não lembra do nome de usuário? Sem problema: você também pode entrar usando o seu e-mail.")
                Spacer(Modifier.height(10.dp))
                QinblyAuthTip("Criou a conta com o Google? Volte e toque em Continuar com Google.")
            } else {
                QinblyAuthTip("Não chegou? Olhe também a caixa de spam e a lixeira.")
                Spacer(Modifier.height(22.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Ainda nada? ", color = QiblyTheme.secondary, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Enviar de novo",
                        color = if (cooldown > 0 || busy) QiblyTheme.secondary else QinblyBrand.AccentLight,
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(enabled = cooldown <= 0 && !busy, onClick = onSend)
                    )
                }
                if (cooldown > 0) Text(
                    "Você poderá pedir outro link em $cooldown segundos.",
                    color = QiblyTheme.secondary, style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
                if (error.isNotBlank()) Text(error, color = QinblyAuthWarnColor, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
        QinblyAuthButton(
            "Voltar para entrar", onClick = onBack, enabled = !busy,
            fill = if (sent) QinblyBrand.Accent else null,
            outline = if (sent) null else QinblyBrand.Accent,
            contentColor = if (sent) Color.White else QinblyBrand.AccentLight,
            modifier = Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 16.dp).navigationBarsPadding()
        )
    }
}
