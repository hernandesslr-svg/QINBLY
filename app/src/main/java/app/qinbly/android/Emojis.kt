package app.qinbly.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Categorias de emoji. Cada uma é montada a partir de faixas do Unicode e só mostra o que o celular sabe desenhar. */
private class EmojiCategory(val icon: String, val name: String, val ranges: List<IntRange>, val extra: List<String> = emptyList())

private val flagCodes = ("AC AD AE AF AG AI AL AM AO AQ AR AS AT AU AW AX AZ BA BB BD BE BF BG BH BI BJ BL BM BN BO BQ BR BS BT BV BW BY BZ CA CC CD CF CG CH CI CK CL CM CN CO CP CR CU CV CW CX CY CZ DE DG DJ DK DM DO DZ EA EC EE EG EH ER ES ET EU FI FJ FK FM FO FR GA GB GD GE GF GG GH GI GL GM GN GP GQ GR GS GT GU GW GY HK HM HN HR HT HU IC ID IE IL IM IN IO IQ IR IS IT JE JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LI LK LR LS LT LU LV LY MA MC MD ME MF MG MH MK ML MM MN MO MP MQ MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RE RO RS RU RW SA SB SC SD SE SG SH SI SJ SK SL SM SN SO SR SS ST SV SX SY SZ TA TC TD TF TG TH TJ TK TL TM TN TO TR TT TV TW TZ UA UG UM UN US UY UZ VA VC VE VG VI VN VU WF WS XK YE YT ZA ZM ZW")
    .split(" ").map { String(Character.toChars(0x1F1E6 + (it[0] - 'A'))) + String(Character.toChars(0x1F1E6 + (it[1] - 'A'))) }

private val emojiCategories = listOf(
    EmojiCategory("😀", "Carinhas", listOf(0x1F600..0x1F644, 0x1F910..0x1F917, 0x1F920..0x1F925, 0x1F927..0x1F92F, 0x1F970..0x1F97A, 0x1F978..0x1F97A, 0x1F9D0..0x1F9D0, 0x263A..0x263A, 0x2639..0x2639, 0x1F47B..0x1F47F, 0x1F480..0x1F480, 0x1F4A9..0x1F4A9, 0x1F916..0x1F916)),
    EmojiCategory("👋", "Gestos e corpo", listOf(0x1F44B..0x1F450, 0x1F64C..0x1F64C, 0x1F64F..0x1F64F, 0x1F918..0x1F91F, 0x1F926..0x1F926, 0x1F932..0x1F932, 0x1F90C..0x1F90F, 0x270A..0x270D, 0x261D..0x261D, 0x1F590..0x1F590, 0x1F595..0x1F596, 0x1F4AA..0x1F4AA, 0x1F440..0x1F445, 0x1F9B4..0x1F9B7, 0x1F9BE..0x1F9BF, 0x1F9E0..0x1F9E0)),
    EmojiCategory("🧑", "Pessoas", listOf(0x1F466..0x1F478, 0x1F481..0x1F487, 0x1F645..0x1F64B, 0x1F930..0x1F931, 0x1F934..0x1F93A, 0x1F9D1..0x1F9DF, 0x1F46A..0x1F46F)),
    EmojiCategory("🐶", "Animais e natureza", listOf(0x1F400..0x1F43E, 0x1F980..0x1F9AE, 0x1F330..0x1F344, 0x1F490..0x1F490, 0x1F338..0x1F33F, 0x1F300..0x1F320, 0x2600..0x2604, 0x2744..0x2744, 0x26A1..0x26A1, 0x1F525..0x1F525, 0x1F4A7..0x1F4A7, 0x1F308..0x1F308, 0x1F3D4..0x1F3DF)),
    EmojiCategory("🍔", "Comida e bebida", listOf(0x1F345..0x1F37F, 0x1F32D..0x1F32F, 0x1F950..0x1F96F, 0x1F9C0..0x1F9CB, 0x2615..0x2615, 0x1F95F..0x1F96B)),
    EmojiCategory("⚽", "Atividades", listOf(0x26BD..0x26BE, 0x1F383..0x1F39F, 0x1F3A0..0x1F3CA, 0x1F3CF..0x1F3D3, 0x1F3F8..0x1F3F9, 0x1F93A..0x1F945, 0x1F947..0x1F94F, 0x1F9E9..0x1F9E9, 0x1F3AE..0x1F3AE, 0x1F9FF..0x1F9FF)),
    EmojiCategory("🚗", "Viagens e lugares", listOf(0x1F680..0x1F6C5, 0x1F3E0..0x1F3F0, 0x1F5FA..0x1F5FF, 0x1F6CB..0x1F6D2, 0x1F6D5..0x1F6D7, 0x1F6E0..0x1F6EC, 0x1F6F0..0x1F6FC, 0x26F0..0x26FA, 0x2708..0x2708, 0x2693..0x2693)),
    EmojiCategory("💡", "Objetos", listOf(0x1F4A1..0x1F4A1, 0x1F4B0..0x1F4FF, 0x1F50A..0x1F52E, 0x1F9E7..0x1F9FE, 0x1F5A5..0x1F5A8, 0x231A..0x231B, 0x2328..0x2328, 0x23F0..0x23F3, 0x260E..0x260E, 0x2709..0x2709, 0x270F..0x270F, 0x2712..0x2712, 0x1F58A..0x1F58D, 0x1F380..0x1F382, 0x1F9F0..0x1F9F6)),
    EmojiCategory("❤️", "Símbolos", listOf(0x2764..0x2764, 0x1F493..0x1F49F, 0x1F9E1..0x1F9E1, 0x1F90D..0x1F90E, 0x1F5A4..0x1F5A4, 0x1F4AF..0x1F4AF, 0x1F4A2..0x1F4A6, 0x1F4A8..0x1F4AB, 0x2705..0x2705, 0x274C..0x274C, 0x274E..0x274E, 0x2753..0x2757, 0x203C..0x203C, 0x2049..0x2049, 0x2795..0x2797, 0x2B50..0x2B50, 0x2B55..0x2B55, 0x1F534..0x1F53D, 0x26AA..0x26AB, 0x1F7E0..0x1F7EB, 0x1F500..0x1F50F, 0x2660..0x2666, 0x267B..0x267B, 0x269B..0x269B, 0x262E..0x2638, 0x2648..0x2653, 0x1F170..0x1F171, 0x1F18E..0x1F18E, 0x1F191..0x1F19A, 0x2B06..0x2B07, 0x27A1..0x27A1, 0x2B05..0x2B05, 0x1F51F..0x1F51F)),
    EmojiCategory("🏁", "Bandeiras", listOf(0x1F3F3..0x1F3F4, 0x1F6A9..0x1F6A9, 0x1F3C1..0x1F3C1, 0x1F38C..0x1F38C), flagCodes)
)

private val emojiCache = HashMap<String, List<String>>()

private fun buildEmojis(cat: EmojiCategory): List<String> = emojiCache.getOrPut(cat.name) {
    val paint = android.graphics.Paint()
    val out = LinkedHashSet<String>()
    for (r in cat.ranges) for (cp in r) {
        if (cp in 0xD800..0xDFFF) continue
        var s = String(Character.toChars(cp))
        if (cp < 0x1F000) s += "️"
        if (paint.hasGlyph(s)) out.add(s)
    }
    for (e in cat.extra) if (paint.hasGlyph(e)) out.add(e)
    out.toList()
}

/** Painel de emojis por categoria: abas em cima, grade embaixo. Toque para inserir. */
@Composable
internal fun QinblyEmojiPanel(modifier: Modifier = Modifier, background: Color = Color(0xFF1F1F21), gridHeight: androidx.compose.ui.unit.Dp = 240.dp, onPick: (String) -> Unit) {
    var sel by remember { mutableStateOf(0) }
    var list by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(sel) {
        list = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { buildEmojis(emojiCategories[sel]) }
    }
    val gridState = rememberLazyGridState()
    LaunchedEffect(sel) { gridState.scrollToItem(0) }
    Column(modifier.fillMaxWidth().background(background)) {
        LazyRow(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(emojiCategories.size) { i ->
                Box(Modifier.size(38.dp).clip(CircleShape).background(if (i == sel) Color(0x44FFFFFF) else Color.Transparent).clickable { sel = i }, contentAlignment = Alignment.Center) {
                    Text(emojiCategories[i].icon, fontSize = 20.sp)
                }
            }
        }
        Text(emojiCategories[sel].name, color = Color(0xFFA9B5C8), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp))
        LazyVerticalGrid(GridCells.Fixed(8), state = gridState, modifier = Modifier.fillMaxWidth().height(gridHeight).padding(horizontal = 6.dp)) {
            items(list) { e ->
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).clickable { onPick(e) }, contentAlignment = Alignment.Center) { Text(e, fontSize = 24.sp) }
            }
        }
    }
}
