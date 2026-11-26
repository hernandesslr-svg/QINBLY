package app.qinbly.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

// Degradê do app no lugar do azul liso: roxo em cima (#7C4DFF), azul embaixo (#2F68E0).
// O botão Seguir usa o mesmo degradê da esquerda para a direita.

internal fun qinblyAccentBrush(alpha: Float = 1f): Brush =
    Brush.verticalGradient(listOf(Color(0xFF7C4DFF).copy(alpha = alpha), Color(0xFF2F68E0).copy(alpha = alpha)))

internal fun qinblyAccentBrushH(alpha: Float = 1f): Brush =
    Brush.horizontalGradient(listOf(Color(0xFF7C4DFF).copy(alpha = alpha), Color(0xFF2F68E0).copy(alpha = alpha)))

/** Botão de cor cheia com o degradê (no lugar de Button com containerColor azul). */
@Composable
internal fun QinblyGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    horizontal: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val a = if (enabled) 1f else 0.4f
    Button(
        onClick = onClick,
        modifier = modifier.background(if (horizontal) qinblyAccentBrushH(a) else qinblyAccentBrush(a), shape),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, contentColor = colors.contentColor,
            disabledContainerColor = Color.Transparent, disabledContentColor = colors.disabledContentColor
        ),
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        content = content
    )
}

/** Texto com o degradê (no lugar de Text com color azul). */
@Composable
internal fun QinblyAccentText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text = text, modifier = modifier, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = fontFamily,
        letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight,
        overflow = overflow, softWrap = softWrap, maxLines = maxLines, minLines = minLines,
        style = style.merge(TextStyle(brush = qinblyAccentBrush()))
    )
}

/** Ícone com o degradê (no lugar de Icon com tint azul). */
@Composable
internal fun QinblyAccentIcon(imageVector: ImageVector, contentDescription: String?, modifier: Modifier = Modifier) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(qinblyAccentBrush(), blendMode = BlendMode.SrcIn)
            },
        tint = Color.Black
    )
}
