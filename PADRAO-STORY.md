# Padrão do visualizador de story

## Topo
- NÃO tem X de fechar: o voltar do celular fecha o story (1 passo). Menu aberto, lista de visualizações ou janela de confirmação fecham primeiro.
- Canto direito: três pontinhos (⋮) abrem o menu. O story pausa enquanto o menu ou qualquer janela está aberto.

## Menu dos três pontinhos
- Seu story: Salvar na galeria (não aparece em story de publicação alheia nem em story de texto), Quem pode ver (em breve), Destacar no perfil (pede o nome e guarda o story na linha de Destaques do perfil; funcional), Excluir story (confirmação).
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
- Toque pra pular/voltar é feito por DUAS ÁREAS TRANSPARENTES clicáveis (1/3 esquerda = voltar, 2/3 direita = próximo) por cima da mídia e por baixo dos botões — não por gesto no container (o container perdia o toque com vídeo/imagem por baixo).
- A barra de cima usa RELÓGIO PRÓPRIO (tempo real + `withFrameMillis`), nunca `animateTo`: com as animações do celular desligadas (escala 0 nas opções de desenvolvedor / "remover animações"), `animateTo` termina na hora, a barra enche de uma vez e o story pula até o último e fica parado. Qualquer contagem de tempo do app deve seguir isso.
- Fila de pessoas: chegou no fim dos stories de uma pessoa, o visualizador segue para a PRÓXIMA da faixa (o seu story primeiro, depois quem você segue). Tocar na esquerda no primeiro story volta para a pessoa anterior, no último story dela. Só fecha quando acabam todas.
- No SEU story, o menu de 3 pontinhos tem "Adicionar story" em primeiro lugar: fecha o visualizador e abre a câmera padrão para um story novo (mesmo fluxo do "+"). Não aparece no story dos outros nem em resposta de story coletivo.
