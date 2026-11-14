package app.qinbly.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties

// Versões modernas dos componentes antigos do Material. Como estão no mesmo pacote, valem em todo o app
// sem mudar cada tela: menu de 3 pontinhos arredondado, campos e botões em pílula.

/** Lista que abre ao lado do botão (3 pontinhos): cantos bem arredondados, fundo do tema e borda fina. */
@Composable
internal fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.DropdownMenu(
        expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier, offset = offset,
        scrollState = scrollState, properties = properties,
        shape = RoundedCornerShape(18.dp),
        containerColor = if (QiblyTheme.dark) Color(0xFF262628) else Color.White,
        tonalElevation = 0.dp, shadowElevation = 8.dp,
        border = BorderStroke(0.5.dp, if (QiblyTheme.dark) Color(0xFF3C3C3E) else Color(0xFFE2DCF2)),
        content = content
    )
}

/** Linha do menu: cantos redondos e um pouco mais de espaço. */
@Composable
internal fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    interactionSource: MutableInteractionSource? = null
) {
    androidx.compose.material3.DropdownMenuItem(
        text = text, onClick = onClick, modifier = modifier.padding(horizontal = 4.dp).clip(RoundedCornerShape(12.dp)),
        leadingIcon = leadingIcon, trailingIcon = trailingIcon, enabled = enabled, colors = colors,
        contentPadding = contentPadding, interactionSource = interactionSource
    )
}

/** Campo de texto em pílula: sem a borda quadrada; só destaca em roxo quando está em uso. */
@Composable
internal fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = RoundedCornerShape(26.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = QiblyTheme.soft, unfocusedContainerColor = QiblyTheme.soft,
        focusedBorderColor = Color(0xFF1877F2), unfocusedBorderColor = Color.Transparent,
        focusedTextColor = QiblyTheme.text, unfocusedTextColor = QiblyTheme.text,
        focusedPlaceholderColor = QiblyTheme.secondary, unfocusedPlaceholderColor = QiblyTheme.secondary,
        focusedLabelColor = Color(0xFF6AA6F8), unfocusedLabelColor = QiblyTheme.secondary,
        cursorColor = Color(0xFF6AA6F8)
    )
) {
    androidx.compose.material3.OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = modifier, enabled = enabled, readOnly = readOnly,
        textStyle = textStyle, label = label, placeholder = placeholder, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
        prefix = prefix, suffix = suffix, supportingText = supportingText, isError = isError,
        visualTransformation = visualTransformation, keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        singleLine = singleLine, maxLines = maxLines, minLines = minLines, interactionSource = interactionSource,
        shape = RoundedCornerShape(26.dp), colors = colors
    )
}

/** Botão de contorno vira pílula preenchida (cinza-roxo), no mesmo lugar de antes. */
@Composable
internal fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick, modifier = modifier, enabled = enabled, shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (QiblyTheme.dark) Color(0xFF2C2C2E) else Color(0xFFEFE9FB),
            contentColor = QiblyTheme.text,
            disabledContainerColor = if (QiblyTheme.dark) Color(0xFF232325) else Color(0xFFF4F0FB),
            disabledContentColor = QiblyTheme.secondary
        ),
        elevation = null, border = null, contentPadding = contentPadding, interactionSource = interactionSource, content = content
    )
}

/** Escolha de país: tela inteira, com busca. */
@Composable
internal fun QinblyCountrySheet(options: List<String>, selected: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val shown = options.filter { query.isBlank() || it.contains(query.trim(), ignoreCase = true) }
    QinblyFullScreen("País", onDismiss, background = QiblyTheme.background, contentColor = QiblyTheme.text, scroll = false) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp)).background(QiblyTheme.soft).padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (query.isEmpty()) Text("Buscar país", color = QiblyTheme.secondary, fontSize = 15.sp)
            BasicTextField(query, { query = it.take(40) }, singleLine = true, textStyle = TextStyle(color = QiblyTheme.text, fontSize = 15.sp), cursorBrush = SolidColor(QiblyTheme.text), modifier = Modifier.fillMaxWidth())
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(top = 6.dp, start = 16.dp, end = 16.dp)) {
            if (query.isBlank()) item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onPick("") }.padding(horizontal = 8.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Não informar", color = QiblyTheme.text, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    if (selected.isBlank()) QinblyCheckIcon(Color(0xFF63D68D), 21.dp)
                }
            }
            items(shown, key = { it }) { country ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onPick(country) }.padding(horizontal = 8.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(country, color = QiblyTheme.text, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    if (country == selected) QinblyCheckIcon(Color(0xFF63D68D), 21.dp)
                }
            }
        }
    }
}

/** Ícone da linha do menu escolhido pelo texto; ações destrutivas ficam vermelhas. */
@Composable
internal fun QinblyMenuIcon(label: String) {
    val l = label.lowercase()
    val red = l.contains("exclu") || l.contains("denunc") || l.contains("bloquear") || l.contains("ocultar")
    val icon = when {
        l.contains("salvar") -> androidx.compose.material.icons.Icons.Outlined.Download
        l.contains("compartilh") -> androidx.compose.material.icons.Icons.Outlined.Share
        l.contains("denunc") -> androidx.compose.material.icons.Icons.Outlined.Flag
        l.contains("copiar") -> androidx.compose.material.icons.Icons.Outlined.ContentCopy
        l.contains("editar") -> androidx.compose.material.icons.Icons.Outlined.Edit
        l.contains("perfil") && l.contains("ver") -> androidx.compose.material.icons.Icons.Outlined.Person
        l.contains("fixar") -> androidx.compose.material.icons.Icons.Outlined.PushPin
        l.contains("exclu") -> androidx.compose.material.icons.Icons.Outlined.DeleteOutline
        l.contains("ocultar") -> androidx.compose.material.icons.Icons.Outlined.VisibilityOff
        l.contains("bloquear") -> androidx.compose.material.icons.Icons.Outlined.Block
        else -> androidx.compose.material.icons.Icons.Outlined.MoreHoriz
    }
    androidx.compose.material3.Icon(icon, contentDescription = null, tint = if (red) Color(0xFFE5484D) else QiblyTheme.secondary, modifier = Modifier.size(20.dp))
}
