# Padrão do visualizador de story

## Topo
- NÃO tem X de fechar: o voltar do celular fecha o story (1 passo). Menu aberto, lista de visualizações ou janela de confirmação fecham primeiro.
- Canto direito: três pontinhos (⋮) abrem o menu. O story pausa enquanto o menu ou qualquer janela está aberto.

## Menu dos três pontinhos
- Seu story: Salvar na galeria (não aparece em story de publicação alheia nem em story de texto), Quem pode ver (em breve), Destacar no perfil (em breve), Excluir story (confirmação).
- Story de outra pessoa: Silenciar @pessoa (em breve), Denunciar story (motivo, vai como denúncia da pessoa), Bloquear @pessoa (confirmação).
- Resposta de story coletivo: Apagar resposta (quem respondeu ou o dono do story), Ver perfil, Denunciar, Bloquear.
- Story de publicação de outro perfil: entram também "Ver original" e "Ver perfil de @autor" no topo do menu.
- Item que ainda não funciona aparece com "em breve" — nunca esconder.

## Visualizações (só o dono do story)
- No cantinho de baixo à ESQUERDA (como no Instagram): até 3 avatares de quem viu por último (34 dp, sobrepostos) e o texto "Visualizações", sem fundo, sem número, sem seta.
- Tocar ou arrastar pra cima (parte de baixo da tela) abre a lista completa; tocar numa pessoa da lista abre o perfil dela e o voltar reabre o story.
- Quem não é dono do story nunca vê isso.

## Compartilhar publicação no story
- Tela "Enviar para" → "Seu story": baixa a foto/vídeo da publicação e cria o story só com a mídia (sem card, sem crédito na tela).
- A legenda guarda um marcador escondido `⧉§idPublicação§@autor§video|image` (qinblyEncodeOrigin / qinblyParseOrigin em TextStory.kt). `qinblyDisplayCaption` esconde o marcador.
- "Ver original": vídeo → aba Vídeos do Início, rolada no vídeo (se não estiver na lista, abre a publicação sozinha); foto → publicação aberta sozinha. O voltar reabre o story no mesmo ponto (QinblyStoryReturn).
- Limite conhecido: o story é uma cópia da mídia e vale a audiência de quem compartilhou, não a da publicação original.

## Barra de cima e toque
- Uma barra por story. A barra enche sozinha e, quando termina, pula para o próximo story (e fecha no último). Foto/texto: 6 s. Vídeo: acompanha o vídeo de verdade (posição ÷ duração), e o vídeo pausa junto quando menu/lista/janela está aberto.
- Tocar (não arrastar) no terço esquerdo da tela volta o story; no resto da tela, pula para o próximo. Vale na tela toda; botões e campos têm prioridade.
- Toque na borda/cantinho da tela NUNCA fecha o story: a janela do visualizador tem `dismissOnClickOutside = false` (antes, um toque na margem fora da janela chamava "fechar"). Fechar = só o voltar do celular.
