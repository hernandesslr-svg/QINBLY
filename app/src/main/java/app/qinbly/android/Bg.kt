package app.qinbly.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background as foundationBackground
import androidx.compose.foundation.border as foundationBorder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp

// Todo fundo e contorno que usava a cor do app (QinblyBrand.Accent, com ou sem transparência)
// passa a sair com o degradê roxo -> azul, sem precisar mexer em cada tela.

private fun qinblyIsAccent(color: Color): Boolean =
    color.alpha > 0f && (color.toArgb() and 0xFFFFFF) == (QinblyBrand.Accent.toArgb() and 0xFFFFFF)

internal fun Modifier.background(color: Color, shape: Shape = RectangleShape): Modifier =
    if (qinblyIsAccent(color)) foundationBackground(qinblyAccentBrush(color.alpha), shape)
    else foundationBackground(color, shape)

internal fun Modifier.background(brush: Brush, shape: Shape = RectangleShape, alpha: Float = 1f): Modifier =
    foundationBackground(brush, shape, alpha)

internal fun Modifier.border(width: Dp, color: Color, shape: Shape = RectangleShape): Modifier =
    if (qinblyIsAccent(color)) foundationBorder(width, qinblyAccentBrush(color.alpha), shape)
    else foundationBorder(width, color, shape)

internal fun Modifier.border(width: Dp, brush: Brush, shape: Shape): Modifier =
    foundationBorder(width, brush, shape)

internal fun Modifier.border(border: BorderStroke, shape: Shape = RectangleShape): Modifier = foundationBorder(border, shape)
