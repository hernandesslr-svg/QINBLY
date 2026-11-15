package app.qinbly.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TermsBlocks = listOf(
    "Quem pode usar" to "O QINBLY é para pessoas com 13 anos ou mais. Você é responsável pela sua conta e pelo que publica.",
    "O que você publica" to "Fotos, vídeos, textos, stories, comentários e mensagens continuam sendo seus. Ao publicar, você permite que o QINBLY mostre esse conteúdo para as pessoas que você escolher (por exemplo, no feed ou nos seus stories).",
    "O que não é permitido" to "Assédio, ameaças, discurso de ódio, conteúdo sexual envolvendo menores, golpes, spam, se passar por outra pessoa e qualquer coisa ilegal. Conteúdos assim podem ser removidos e contas podem ser suspensas.",
    "Denúncias e bloqueios" to "Qualquer pessoa pode bloquear outra conta e denunciar publicações, comentários, mensagens e perfis. As denúncias são analisadas pela equipe do QINBLY.",
    "Stories" to "Stories somem sozinhos depois de 6, 12 ou 24 horas, conforme a escolha de quem publicou. Mesmo assim, outra pessoa pode ter feito uma captura de tela.",
    "Sua conta" to "Você pode excluir sua conta a qualquer momento em Configurações. Isso apaga seu perfil, publicações, comentários, curtidas, mensagens e arquivos, e não dá para desfazer.",
    "Mudanças" to "Estes termos podem mudar. Quando mudarem de forma importante, o app avisa."
)

private val PrivacyBlocks = listOf(
    "O que o QINBLY guarda" to "Seu e-mail, nome de usuário, nome, bio e fotos de perfil; suas publicações, stories, comentários, curtidas, mensagens e quem você segue; e a hora da última vez que você esteve online (se essa opção estiver ligada).",
    "Localização" to "Se você permitir, o app usa sua localização para o Perto de mim, o Mural do bairro e o Mercado. Guardamos a sua cidade no perfil, e cada aviso do Mural guarda a posição onde foi publicado. Você pode desligar a localização e apagar esses dados em Configurações > Localização.",
    "Câmera, microfone, fotos e contatos" to "Só são usados quando você usa o recurso: tirar foto, gravar vídeo ou voz, escolher mídia da galeria ou enviar um contato em uma conversa. O app não envia sua lista de contatos para o servidor.",
    "Quem vê o que" to "Publicações aparecem no feed. Em Privacidade você decide quem vê seus stories, quem pode falar com você e quem pode comentar. Mensagens diretas só são visíveis para quem participa da conversa.",
    "Onde ficam os dados" to "Os dados ficam em um serviço de banco de dados e armazenamento na nuvem (Supabase). Não vendemos seus dados.",
    "Seus direitos" to "Você pode baixar seus dados e excluir a conta em Configurações > Dados e armazenamento e Conta. Também pode pedir correção ou esclarecimento pelo contato indicado no app.",
    "Crianças" to "O QINBLY não é feito para menores de 13 anos. Se descobrirmos uma conta assim, ela será removida."
)

/** Termos de uso e Política de privacidade lidos dentro do app (sem depender de site). */
@Composable
internal fun QinblyLegalScreen(kind: String, onDismiss: () -> Unit) {
    val terms = kind == "terms"
    val blocks = if (terms) TermsBlocks else PrivacyBlocks
    QinblyFullScreen(if (terms) "Termos de uso" else "Política de privacidade", onDismiss, background = QiblyTheme.background, contentColor = QiblyTheme.text) {
        Text("Texto-base do app. Antes de publicar na loja, ele deve ser revisado por um advogado e publicado também em um endereço na internet.", color = QiblyTheme.secondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 14.dp))
        blocks.forEach { (title, body) ->
            Column(Modifier.padding(bottom = 14.dp)) {
                Text(title, color = QiblyTheme.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(body, color = QiblyTheme.text, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
