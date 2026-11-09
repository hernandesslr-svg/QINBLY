# Padronização das ações (curtir, comentar, repostar, salvar, enviar, seguir)

## O que mudou
- **Um só conjunto de botões** (`QinblyPostActionBar`) em todas as telas: início, vídeo em tela cheia, perfil, perfil público e visualizadores. Mesma ordem (curtir, comentar, repostar, salvar, enviar), mesmo ícone de 24dp, mesmo contador, mesma área de toque.
- **Um só botão de seguir** (`QinblyFollowButton`): "Seguir" azul preenchido, "Seguindo" com contorno. Usado no feed, vídeo em tela cheia, perfil público (versão grande) e lista de seguidores/seguindo.
- **Estado compartilhado** (`QinblyEngagement`): curtir/seguir/salvar/repostar feitos em uma tela aparecem nas outras na hora. Curtir e seguir são otimistas e voltam atrás se o servidor falhar. A contagem de curtidas não duplica depois de recarregar o feed.
- Marcações de curtida/seguindo valem por conta (trocar de conta não herda as da anterior).
- Link de compartilhamento unificado: `https://qinbly.com/post/<id>` (antes havia `qinbly.com` e `qinbly.com`). **Ajuste `QINBLY_POST_LINK_BASE` para o seu domínio real.**
- Contadores falsos "0" ao lado de repostar, salvar e enviar foram removidos (não representavam nada).
- Perfil e perfil público agora usam o diálogo de compartilhar do feed (enviar por mensagem/copiar link) em vez do compartilhamento de texto cru.

## Servidor
- A Edge Function `qinbly-api` agora devolve `likedByMe` e `isFollowing` por publicação e `followers/following/isFollowing` no perfil. **É preciso fazer o deploy de novo** para o coração e o "Seguindo" virem corretos após reabrir o app.

## Limites conhecidos
- **Repostar e salvar ainda são locais** (neste aparelho). Para virar rede social de verdade, precisam de tabela e rotas no servidor (contagem de reposts, aba "Salvos" sincronizada).
- Nada disto foi compilado nem testado em aparelho; foi conferido por leitura e balanceamento de chaves.

## Rodada 2: visualizador, card, comentários, salvar/repostar, links

- **Visualizador único** (`QinblyPostViewer`): é a tela cheia do Início (botões laterais, menu "mais", barra azul do vídeo, seguir, legenda). Agora também abre as publicações do Perfil e do Perfil público, para fotos e vídeos (fotos com galeria e toque duplo para curtir). A tela de foto de perfil continua separada, pois não é uma publicação.
- **Card de publicação único** (`QinblyPostCard`): o card do feed foi extraído e é usado no Início e nas abas Publicações/Vídeos dos dois Perfis (mesmo cabeçalho, menu, mídia, enquete, legenda e barra de ações). A aba "Fotos" continua em grade de miniaturas (padrão de rede social) e abre o mesmo visualizador.
- **Comentários únicos** (`QinblyCommentsDialog`): mesma tela, campo, emojis e teclado em todo lugar. Carrega, envia e exclui sozinho (botão de lixeira em comentários seus ou em publicações suas) e atualiza o número do botão sem recarregar o feed inteiro. Corrige também o comentário aberto a partir da tela cheia, que não carregava a lista.
- **Salvar e repostar no servidor:** novas tabelas `post_saves` e `post_reposts` (`SUPABASE/04_saves_reposts.sql`), rotas `POST /api/posts/:id/save` e `/repost`, e `savedByMe`, `repostedByMe` e `reposts` em cada publicação. O botão de repostar agora mostra o contador.
- **Link compartilhado abre a publicação:** nova rota `GET /api/posts/:id`; o app trata `https://qinbly.com/post/<id>` e `qinbly://post/<id>`, e mensagens com esse link ganham o botão "Abrir publicação".

## Para publicar
1. Rodar `SUPABASE/04_saves_reposts.sql` no SQL Editor.
2. `supabase functions deploy qinbly-api`.
3. Adicionar ao `AndroidManifest.xml` (não vem neste pacote), dentro da `MainActivity`, para links externos abrirem o app:

```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="https" android:host="qinbly.com" android:pathPrefix="/post/" />
    <data android:scheme="qinbly" android:host="post" />
</intent-filter>
```
(Para o link `https` abrir direto no app, o domínio também precisa publicar `/.well-known/assetlinks.json`.)

## Ainda sem teste real
Nada foi compilado nem executado em Android. Compile no Android Studio e valide: curtir/seguir/salvar/repostar entre telas e após fechar e reabrir o app, comentários, vídeo/foto nos três visualizadores e compartilhamento por mensagem.

## Rodada 3

- **Enquetes nos perfis:** votar agora recarrega o perfil (`reloadKey`) em `ProfilePage` e `PublicProfilePage`.
- **Foto de perfil:** um único `QinblyPhotoViewer` (zoom por pinça, toque duplo, controles, menu salvar/compartilhar e, no perfil público, denunciar) nos dois perfis. As telas de edição de perfil continuam separadas.
- **Meus salvos:** nova rota `GET /api/me/saved` (precisa publicar `qinbly-api` de novo), `QinblyApi.getSavedPosts` e a aba "Salvos" do seu perfil com os cards padrão. Ao remover o salvo, o item some da lista na hora.

## Rodada 4 — cara de rede social

- **Hashtags e menções:** `#tag` e `@usuario` nas legendas viram links (`QinblyRichText`). Hashtag abre a tela da tag (`QinblyTagHost`) com os cards padrão; menção abre o perfil.
- **Explorar:** a busca agora mostra "Em alta" (hashtags mais usadas) e "Pessoas para seguir" quando está vazia; digitar `#algo` abre a hashtag.
- **Feed:** o filtro "Tendências" agora ordena por engajamento (antes não fazia nada). "Perto de mim" foi removido porque não existe localização no app.
- **Recuperar senha:** link "Esqueci minha senha" na tela de login, via `POST /api/auth/forgot` (o e-mail é enviado pelo Supabase Auth).
- **Servidor novo:** `/api/hashtags/trending`, `/api/hashtags/:tag`, `/api/users/suggested`, `/api/auth/forgot` — é preciso publicar `qinbly-api` de novo.
- **Para o e-mail de recuperação funcionar:** configure em Supabase → Authentication → URL Configuration a URL de redirecionamento e o template do e-mail.

## Rodada 5 — varredura

- **Menu "⋮" unificado:** havia duas cópias idênticas do menu da publicação dentro de `QinblyPostCard`; agora é um só (`QinblyPostMenu`).
- **Dono da publicação:** Editar legenda (novo, `PATCH /api/posts/:id`), Copiar link (antes só aparecia para os outros) e Excluir com confirmação.
- **Servidor:** publicar `qinbly-api` de novo.

## Rodada 6 — recursos sociais adicionais

- **Comentários:** respostas em fio (um nível), curtir comentário com contador, "Respondendo a @…", horário real.
- **Reações:** removidas a pedido do dono; só existe o botão de curtir normal.
- **Fixar (até 3) e arquivar:** no menu ⋮ das suas publicações; fixadas ficam no topo do perfil com 📌; nova aba "Arquivo" (só você vê).
- **Menções e respostas notificam** (`mention`, `reply`). Notificações novas também aparecem como notificação do sistema (enquanto o app está aberto/em memória; push com o app fechado exige Firebase — veja abaixo).
- **Clipes:** novo filtro "Clipes" no feed abre os vídeos em tela cheia, um por vez, deslizando na vertical.
- **Servidor:** publicar `qinbly-api` e rodar `SUPABASE/05_social_features.sql`.
- **Projeto:** `SUPABASE/00_base_schema.sql` (só banco novo), `SUPABASE/README.md` e `android-scaffold/` (manifest, gradle, res). Faltam do projeto original: as 25 imagens `qinbly_frame_*` e os ícones.

### Push com o app fechado (não incluído)
Precisa de um projeto Firebase seu: `google-services.json`, dependência `firebase-messaging`, um serviço que receba o token, uma tabela `device_tokens` e a função enviando via FCM. Sem a sua conta Firebase não dá para criar de forma funcional.

## Rodada 7 — "Seguir/Seguindo" igual em todas as telas

- **Causa:** as listas de pessoas (busca, "Pessoas para seguir", seguidores/seguindo) não recebiam do servidor se você já segue a pessoa (assumiam "não"), e a lista do perfil público nem tinha botão.
- **Correção:** o servidor agora devolve `isFollowing` em busca, seguidores e seguindo; o app aprende esse estado de qualquer resposta do servidor (feed, hashtag, salvos, busca, listas, perfis) em `QinblyEngagement.learnFollows`, e todas as telas usam o mesmo botão (`QinblyFollowButton`) lendo o mesmo estado. As listas de seguidores/seguindo (do seu perfil e dos perfis públicos) agora mostram avatar e botão de seguir.
- Publicar `qinbly-api` de novo.

## Rodada 8 — vídeo no feed

- Removido o botão de tela cheia que ficava sobre o vídeo (tocar no vídeo, fora do botão de play, continua abrindo o visualizador).
- Laterais de vídeos verticais agora usam o próprio vídeo desfocado como fundo (Android 12+; abaixo disso fica fundo preto), sem a faixa cinza.
- Barra de progresso fina e botão de som enquanto o vídeo toca.

## Rodada 9 — perfil sem "piscar" ao trocar de aba

- O seu perfil guardava tudo só dentro da tela; ao sair e voltar, a tela recomeçava vazia (foto padrão, contadores zerados, carregando) até a rede responder.
- Agora o último perfil carregado fica em memória (`QinblyCache.own`) e um resumo (foto, capa, contadores, moldura) também no aparelho. Ao voltar para a aba, a tela já abre com tudo e só atualiza por trás. É apagado ao sair/trocar de conta.

## Rodada 10 — fotos de perfil sem atraso em TODAS as telas

- **Causa raiz:** `QiblyAccountAvatar` (usado no feed, comentários, listas, mensagens…) baixava o perfil completo da pessoa toda vez que aparecia e começava mostrando a silhueta; por isso a foto "aparecia depois".
- **Correção:** `QinblyAvatarCache` — cache único (memória reativa + aparelho) de foto e moldura. Qualquer tela já abre com a foto certa; o perfil só é buscado se o cache tiver mais de 10 min e uma vez por pessoa (não por cartão). Trocar a foto de perfil invalida o cache; sair/trocar de conta limpa tudo.
- **Mensagens:** a lista de conversas também fica em cache (`QinblyCache.inbox`) e abre na hora.
- **Removido:** conversas de demonstração falsas (Carol, Júlia, Marina, Ana) que apareciam na lista de mensagens.

## Rodada 11 — uma única foto de perfil em todo o app

- **Causa:** cada perfil tem duas imagens no banco (`avatar_path` e `cover_path`). O cabeçalho do perfil lia a de capa; publicações, listas, busca, mensagens e notificações liam a de avatar. Quando as duas divergiam (fotos antigas, ou um upload que falhou), apareciam fotos diferentes.
- **Correção (servidor, vale para todos os usuários):** `canonicalizePhotos` em `qinbly-api` — toda resposta que traz perfil passa a usar a foto enquadrada no editor (`cover_path`) também como avatar. Não é preciso migrar dados.
- **Ao trocar a foto:** o app descarta o cache de fotos e recarrega; outras pessoas passam a ver a nova foto em até ~10 minutos (validade do cache de avatar) ou ao reabrir o app.
- Publicar `qinbly-api` de novo.

## Rodada 12 — só uma foto de perfil (a do cabeçalho)

- O editor de foto deixou de enviar uma segunda imagem de avatar; o servidor grava a mesma foto em `cover_path` e `avatar_path` (nunca divergem) e continua devolvendo a foto do cabeçalho como avatar em todas as respostas.
- Rodar `SUPABASE/06_single_profile_photo.sql` uma vez: copia a foto do cabeçalho para onde as duas colunas estavam diferentes (corrige também a lista de bloqueados).
- Publicar `qinbly-api` de novo.

## Rodada 13 — altura máxima das fotos no feed

- Fotos no cartão agora têm altura máxima 4:5 (a mesma do vídeo). Fotos mais altas aparecem inteiras, sem cortar, com a própria foto desfocada nas laterais (Android 12+; abaixo disso, fundo preto). Os botões de curtir/comentar ficam sempre visíveis. Tocar na foto continua abrindo o visualizador com a foto em tamanho cheio.

## Rodada 14 — comunidades reais e limpeza do perfil
- Aba **Comunidades**: botão "Criar comunidade" (nome 3–40, descrição ≤300, ícone), listas Minhas/Em alta/Descobrir vindas do servidor, Entrar/Participando com atualização imediata, tela da comunidade com publicações dos membros, excluir publicação (autor ou dono) e excluir comunidade (dono).
- Removida a aba **Arquivo** do perfil e o item "Arquivar" do menu; publicações antes arquivadas voltam a aparecer.
- Para funcionar: rodar `SUPABASE/07_communities.sql` e publicar de novo a função `qinbly-api`.

## Rodada 15 — Editar publicação completo
- Menu da publicação: "Editar" (sem "Arquivar"). A janela edita legenda e também troca foto, troca vídeo ou remove a mídia (enquetes e publicações com várias fotos editam só a legenda).
- Servidor: `PATCH /api/posts/:id` aceita `mediaFileId` + `mediaType` (mesma validação da criação) ou `removeMedia`. Republicar `qinbly-api`.

## Rodada 16 — respostas de comentários recolhidas
- Respostas ficam escondidas; cada comentário com respostas mostra "Ver N respostas ▾" / "Ocultar respostas ▴" (azul) ao lado de "Responder".
- Responder a um comentário principal: sem marcação. Responder a uma resposta: começa com @nome, em azul e clicável.

## Rodada 17 — menu do comentário
- Lixeira removida; ficou só o coração. Segurar o comentário abre: Copiar, Editar comentário (próprio), Excluir (próprio ou dono da publicação), Ver perfil e Denunciar comentário (de outras pessoas).
- Rodar `SUPABASE/08_comment_edit.sql` e republicar `qinbly-api` (nova rota `PATCH /api/comments/:id`).

## Rodada 18 — nova tela de compartilhar
- "Enviar para": prévia da publicação (miniatura, autor, legenda), busca em formato de pílula, pessoas em grade com foto (cache global) e marca de seleção azul, campo de mensagem opcional, botão azul "Enviar", e atalhos redondos "Copiar link" e "Outros apps" quando ninguém está selecionado.

## Rodada 19 — tela de Mensagens
- Removido o filtro "Grupos". Adicionada busca "Buscar conversas", filtros em pílulas azuis (contador de não lidas em "Não lidas"), conversas com horário real, destaque azul e texto em negrito nas não lidas, avatar 60dp.

## Rodada 20 — visualizador de fotos limpo
- Ao abrir uma foto, só aparece a imagem e a seta de voltar (sem botões laterais, legenda, foto de perfil ou seguir). Salvar/compartilhar/denunciar ficam no menu ⋮ do canto superior direito. Vídeos continuam com os controles.

## Rodada 21 — molduras centralizadas
- `qinblyNormalizedFrame` agora centraliza a moldura pelo centro do "buraco" transparente (onde fica a foto) em vez da caixa que inclui flor/laço/coroa; se o buraco não for detectado, volta ao comportamento anterior (caixa total).

## Rodada 22 — notificações
- Sino do topo: ícone de contorno e contador azul com anel. Tela de notificações: foto de perfil real (cache global) com selo do tipo (curtida, seguidor, comentário, menção), nome em negrito, seções "Novas" e "Anteriores", destaque azul nas não lidas, "Marcar como lidas", filtros em pílulas azuis e tela vazia no azul do app.

## Rodada 23 — varredura geral
- Removidos botões sem função: filtro/atalho "Lives", "Live" no menu de criar e na câmera, "Adicionar som" e "Seu Momento" no editor, e itens de configuração "Seus pedidos", "Idioma", "Central de ajuda", "Qualidade de vídeo", "Downloads", "Contas bloqueadas" duplicado e a seção Lives.
- Chat: balões azul (você) e cinza (outra pessoa), separador de dia, agrupamento por remetente, rolagem até a última mensagem, seletor de emojis real, botão enviar azul; removidos câmera/foto/áudio que não faziam nada.

## Rodada 24 — perfis
- Números do perfil (próprio e de outras pessoas): Publicações, Seguindo e Seguidores em texto limpo (o ícone branco quase invisível saiu).
- Perfil de outras pessoas: topo mostra @usuário; removidas as abas vazias Republicações/Curtidas; menu ⋮ com Copiar @, Compartilhar perfil, Denunciar perfil (envia denúncia de verdade) e Bloquear perfil (com confirmação).

## Rodada 25 — limpeza geral de botões sem função
- Removidos 40 itens de Configurações que só mostravam "precisa ser conectada ao serviço", a categoria vazia "Atividade e histórico" e os botões de chamada de voz/vídeo do chat. Texto das opções de fonte agora legível em tema claro.
- Pendente de decisão: interruptores de Configurações (feed, câmera, mensagens, amigos, acessibilidade, moderação) só guardam a preferência no aparelho e ainda não alteram o comportamento do app.

## Rodada 26 — configurações que funcionam
- Removidas as categorias e interruptores que só guardavam a preferência sem efeito (Privacidade, Publicações, Mensagens, Amigos, Moderação, Conteúdo, Lives, reprodução automática, economia de dados etc.).
- Agora funcionam de verdade: Notificações (liga/desliga geral e por tipo: curtidas, comentários/menções, seguidores), Vídeos (iniciar sem som, repetir), Tela (tema escuro, fontes), Acessibilidade (Texto maior em todo o app, Reduzir animações das molduras), Dados e segurança (cache, baixar dados, contas bloqueadas, excluir conta).

## Rodada 27 — Visual moderno
- Nova publicação (mídia): prévia grande arredondada, legenda em cartão com contador, dica de #/@, linhas mortas (Marcar pessoas/Localização/Música/Público) removidas, botão Publicar azul.
- Texto/Enquete: cabeçalho com avatar, cartão de texto com contador, opções numeradas, duração em chips, botão pílula.
- Tela de sucesso: corrigido contraste (fundo claro, azul).
- Editar publicação: cartão de legenda com contador, botões em pílula, Salvar azul.
- Todos os diálogos: cantos 28dp e cor do tema.

## Rodada 28 — Nome do app em um só lugar
- `QinblyBrand.Name` (MainActivity.kt) é usado em todos os textos visíveis. Para renomear: troque esse valor e `app_name` em `android-scaffold/app/src/main/res/values/strings.xml`.
- Não mudam sozinhos: links (qinbly.com), pacote `app.qinbly.android`, nomes de classes.

## Rodada 29 — Cabeçalho do feed (opção A)
- Topo: nome do app + busca + sino; abas Descobrir/Seguindo logo abaixo; "1 online" removido.
- Filtros (pílulas menores, sem ícone): Para você, Vídeos, Fotos, Enquetes, Textos. Saíram Novidades/Tendências/Clipes.
- Nome centralizado na barra de cima (busca e sino continuam à direita).
- Filtros do feed (Para você, Vídeos, Fotos, Enquetes, Textos) removidos; cabeçalho só com nome, busca, sino e abas.
- Card da publicação: legenda acima da mídia. Barra inferior: botão Criar em círculo azul preenchido. Corrigido parâmetros duplicados em AlertDialog (shape/containerColor).

## Rodada 30 — Molduras removidas
- Removidos: botão "Molduras" do perfil, tela de escolha, desenho nas fotos (feed, perfil, perfil público), decoração do cabeçalho do perfil, configuração "Reduzir animações" e `QinblyApi.setProfileFrame`.
- O servidor (`/api/users/me/frame`) e a coluna no banco foram mantidos sem uso; podem ser apagados depois.

## Rodada 31 — Mídia preenche o card
- Vídeo do feed: `RESIZE_MODE_ZOOM` (sem faixas pretas); foto do feed e miniaturas do perfil: `ContentScale.Crop` (sem faixas). Visualizadores em tela cheia continuam mostrando a mídia inteira.

## Rodada 32 — Vídeos
- Vídeos do feed tocam sozinhos (sem som, em repetição); botão do alto-falante liga o som. O player é liberado quando o card sai da tela.
- Ao abrir um vídeo em tela cheia (feed, perfil, perfil público, hashtag), deslizar mostra só vídeos. Abrindo uma foto, a lista continua como antes.

## Rodada 33 — Separador do feed
- Faixa de 3 dp (cor `feedDividerColor`) entre as publicações do feed.

## Rodada 34 — Conversa (1ª etapa, visual)
- Título "Mensagens" some dentro da conversa; topo com seta de voltar, foto com anel azul e busca/menu.
- Balões mais compactos (cantos 18/4, texto 15sp); publicação compartilhada vira cartão (miniatura do cache do feed, autor, legenda, "Abrir publicação") em vez do link.
- Campo de mensagem preenchido sem borda, botão enviar 46dp.
- Ficam para a 2ª etapa (precisam de servidor): áudio, anexos, figurinhas, reações, "online agora"/"lida".

## Rodada 35 — Sem cartões nas publicações
- `QinblyPostCard` sem borda, sombra, cantos nem margem lateral; só a faixa de 3 dp ao final de cada publicação.

## Rodada 36 — Voltar da conversa
- Botão de voltar do celular, dentro de uma conversa, volta para a lista de mensagens (antes ia ao Início). Seta de voltar no topo da conversa já existia desde a v52.

## Rodada 37 — Pessoas para seguir nas Mensagens
- Lista de conversas termina com "Pessoas para seguir" (autores do feed que a pessoa ainda não segue; 5 e "Ver mais" até 20). Sem conversas, as sugestões ocupam a tela. Pílula "Solicitações" (sem função) removida.

## Rodada 38 — Início renovado
- Linha única de abas com ícone em cima: Descobrir (estrela de 4 pontas), Seguindo, Em alta, Vídeos, Lives e Perto de mim (as duas últimas mostram "em breve").
- Descobrir: saudação por horário, Pergunta do dia (gira por dia, abre texto com #perguntadodia) e Desafio da semana (gira por semana; toque abre a hashtag, Participar abre a câmera com a hashtag). Texto inicial vai por `QinblyComposeSeed`.
- Em alta: período (Hoje/Semana/Mês), assuntos em alta e ranking por curtidas+comentários+reposts; sem servidor.
- Listas de perguntas/desafios ficam em `qinblyDailyQuestions` e `qinblyWeeklyChallenges` (MainActivity.kt).

## Rodada 39 — Aba Lives (v57)
- A aba **Lives** do cabeçalho abre `QinblyLivesPage`: barra fina azul "Começar uma live" (aviso "em breve"), "Ao vivo agora" (estado vazio "Ninguém ao vivo agora.") e "Agendadas" (vazio).
- Card de live: tarja "AO VIVO" + espectadores. Sala `QinblyLiveRoom`: nome com coração vermelho + número de curtidas, bolinha vermelha ao lado dos espectadores, duplo toque curte, botões pequenos 📌/📊 (abrem e fecham), campo de comentário + presente + compartilhar (avisos "em breve").
- A lista de lives ainda é vazia: transmissão real exige serviço de vídeo + tabelas no servidor. Nada compilado/testado.

## Rodada 40 — Botão de som dos vídeos (v58)
- `InlineVideoPlayer`: o botão de som deixou de usar emoji 🔇/🔊; agora é uma bolinha de vidro translúcida (30dp, borda fina) com ícone VolumeOff/VolumeUp branco.

## Rodada 41 — Curtir com dois toques em todo o app (v59)
- Novo `QinblyHeartBurst` (coração vermelho que cresce e some no centro). Duplo toque curte (só curte, não descurte) na foto e no vídeo do feed (`QinblyPostCard`), nas fotos do visualizador (`QinblyViewerImages`) e nos vídeos do visualizador (`QinblyFullscreenVideo(onDoubleTap)`). Toque simples continua abrindo o visualizador / pausando.

## Rodada 42 — Volume do celular liga o som do vídeo (v60)
- `MainActivity.dispatchKeyEvent` conta toques em "aumentar volume" (`QinblyVolumeKey.upTicks`); `InlineVideoPlayer` mudo observa o contador e liga o som (`muted=false`, `volume=1f`). Funciona mesmo com o volume já no máximo.
- (v61) Vale para as duas teclas: aumentar **e** diminuir o volume ligam o som dos vídeos mudos.

## Rodada 43 — Barra de ações da publicação (v62)
- `QinblyPostActionBar` (horizontal): curtir, comentar, republicar e enviar agrupados à esquerda (espaço de 8dp entre botões, sem divisores) e só o salvar na ponta direita. A versão vertical (sobre o vídeo) não mudou.

## Rodada 44 — Seguir some depois de seguir (v63)
- Novo `QinblyCardFollow` no cabeçalho das publicações: ao tocar em Seguir aparece "Seguindo" por ~1,5 s e o botão some; quem já é seguido não mostra o botão. Se deixar de seguir em outro lugar, "Seguir" volta. Listas de sugestões e perfil continuam mostrando Seguir/Seguindo normalmente.

## Rodada 45 — Ícones da barra de status visíveis (v64)
- `MainActivity.setContent`: hora, bateria e sinal (barra de cima) e ícones da barra de baixo passam a ser escuros quando o fundo do app é claro e claros no tema escuro (`isAppearanceLightStatusBars/NavigationBars` conforme `QiblyTheme.background`). Antes ficavam brancos sobre fundo branco.

## Rodada 46 — Editar perfil + @usuário único (v65)
- Tela Editar perfil renovada: campos claros com contorno fino (sem fundo escuro/ícones/setinha), foto com câmera no meio e link "Alterar foto" embaixo (nome e @ saíram de baixo da foto), seção "SOBRE VOCÊ" com Nome, Nome de usuário, **Bio** (agora logo após o usuário, antes ficava no fim) e País; categorias em chips que se encaixam (`FlowRow`, sem emojis, contador "N de 5"). A coluna agora **rola** (antes bio e parte das categorias ficavam cortadas).
- @usuário único: o app checa a disponibilidade enquanto digita (`QinblyApi.isUsernameAvailable` → `GET /api/users/username-available?u=`), mostra "✓ disponível" ou "Esse nome de usuário já está em uso", e bloqueia o Salvar. O servidor também devolve 409 com a mesma mensagem se o banco recusar (a coluna já é UNIQUE).
- **É preciso reimplantar a função `qinbly-api`** (nova rota + tratamento do erro 409). Nome de exibição pode repetir (só o @usuário é único).

## Rodada 47 — Menu "Criar" (v66)
- O botão Criar agora abre uma folha de baixo (Dialog) com 4 cartões 2x2 — Texto, Galeria, Câmera, Enquete — cada um com ícone de linha azul (`QinblyCreateIcons`, desenhados em 24x24) sobre círculo azul clarinho. Tocar fora fecha. Substitui o Popup com bolhas e símbolos de texto (✎ ▣ ◎ ▤).

## Rodada 48 — Começar uma live (v67)
- "Começar uma live" abre `QinblyLiveHost`: **Antes de começar** (título, Todos/Seguidores, Comentários, Presentes, "Iniciar live") → contagem 3-2-1 → **No ar** (foto/curtidas, cronômetro, barra de baixo: comentário, virar câmera, microfone, pausa ⏸ e X vermelho que encerra com confirmação + resumo). Pausar mostra "Live pausada" + "Retomar".
- Bolinha vermelha: **removida** da sala de quem assiste; na tela de quem transmite fica vermelha ao vivo, **cinza ao pausar** e volta a acender ao retomar.
- É só interface: não há câmera nem transmissão real ainda (avisa "Prévia" ao entrar no ar). Depende do serviço de vídeo + servidor.

## Rodada 49 — Deslizar troca as abas de cima (v68)
- Removido o gesto global que trocava a barra de baixo (Início → Mensagens → Lives → Avisos → Perfil). A barra de baixo agora só muda por toque.
- No Início, deslizar para os lados troca as abas do cabeçalho na ordem Descobrir, Seguindo, Em alta, Vídeos, Lives, Perto de mim (a última mostra "em breve" e não muda). Carrosséis de fotos dentro das publicações continuam tendo prioridade no próprio deslize.

## Rodada 50 — Mensagens estilo TikTok (v69)
- Título "Mensagens" centralizado, lupa no canto direito (abre a busca de pessoas); barra "Buscar conversas" removida.
- No lugar da barra: faixa horizontal com **Criar** (sua foto + "+" azul; toque mostra "Notas e stories: em breve") e atalhos para as pessoas com quem você conversa. Sem anel de story: stories/notas com frase e música ainda dependem do servidor e de um catálogo de músicas licenciado.
- "Atividade" fica só no sino de notificações (a aba Mensagens já mostra somente conversas e "Pessoas para seguir").

## Rodada 51 — Notificações (v70)
- Ícone de três tracinhos no topo abre "Configurações das notificações" (folha de baixo): Silenciar tudo, Curtidas, Comentários e menções, Novos seguidores, Avisos do QINBLY. Usam as mesmas chaves já lidas pelo aviso do sistema (`setting_notifications`, `setting_notice_likes/comments/followers`) + nova `setting_notice_system`.
- Linhas "começou a seguir você" ganham o botão **Seguir de volta** (vira "Seguindo"); `QinblyFollowButton` ganhou `idleLabel`.
- Mostra as 5 primeiras notificações; o resto abre para baixo na barrinha "▾ Mais notificações (N)" / "▴ Mostrar menos".
- Nova seção **Seguir de volta** no fim ("Pessoas que seguem você e que você não segue ainda."): até 6 pessoas e "Ver todos (N)"; vem de `getConnections(seuUsuario, "followers")` sem quem você já segue (lista fixada ao abrir).
- (v71) Menções ganharam interruptor próprio ("Menções", chave `setting_notice_mentions`) nas configurações das notificações; "Comentários" agora cobre só comentários e respostas. O servidor já cria a notificação "mencionou você" quando alguém marca @usuário em publicação (legenda) ou comentário.

## Rodada 52 (v72) — Notificações e Mensagens em azul
- Sino maior (ícone 32dp, toque 48dp); contador azul (redondo 1 dígito, pílula 2+ dígitos, "99+"), borda da cor do fundo.
- Contador do sino zera ao abrir a tela de notificações (ids vistos em `notif_seen_ids`); as novas continuam com fundo azul claro até serem tocadas.
- Notificações novas: sem bolinha; fundo azul claro (12%) + hora azul; ao tocar vira branca. "Seguir de volta" segue azul.
- Menção: texto "em uma publicação"/"em um comentário" (servidor grava `mention_comment` para comentários, com fallback para `mention`); tocar abre a publicação (`QinblyDeepLink.postId`). **Requer redeploy de `qinbly-api`.**
- Mensagens: sem vermelho. Hora na linha do nome; bolinha azul com a quantidade na linha da mensagem; fundo azul claro e negrito nas novas; ao abrir a conversa vira branca e o contador da aba (azul, borda) diminui.

## Rodada 53 (v73) — Tema escuro azul-noite + Configurações reorganizadas
- Tema escuro: fundo #10151E, cartões #1A2230, campos/blocos #222C3D, bordas #2A3446, texto #F2F4F8, secundário #A9B5C8 (antes preto puro). Textos/ícones fixos em preto (e dialogs/folhas brancas) passaram a usar `QiblyTheme.text/card/soft/border`; comentários e `Muted` também seguem o tema. Telas de câmera/galeria seguem claras.
- Configurações: Conta (compartilhar perfil, contas bloqueadas, excluir conta); Notificações (Silenciar tudo, curtidas, comentários e respostas, menções, seguidores, avisos do app); Vídeos; Tela (tema + fontes, sob um só cabeçalho); Acessibilidade (texto maior + reduzir animações, ligado ao coração); Dados e armazenamento (limpar cache com tamanho, baixar dados); Convidar amigos (novo); Ajuda e suporte; Alternar contas; Sair.
- Removidos: seção morta "Gerenciamento de contas", "Dispositivos conectados", "Segurança e permissões", duplicatas entre Conta e Dados, variáveis sem uso. Ícones de configurações agora em azul (antes brancos, invisíveis no tema claro).

## Rodada 54 (v74) — Ícones das configurações
- Um ícone Material (Outlined, linha fina, azul, em quadradinho azul claro) para cada opção de Configurações: Conta, Notificações, Vídeos, Tela, Acessibilidade, Dados, Convidar amigos, Ajuda, Alternar contas, Sair; e cada opção interna (compartilhar, bloqueio, lixeira, sino cortado, coração, balão, @, pessoa+, megafone, volume mudo, repetir, lua, etiqueta, fonte, tamanho de texto, câmera lenta, vassoura, download, bandeira, info, atualização). Sem emojis. Requer material-icons-extended (já no projeto).

## Rodada 55 (v75) — Estilo C nos ícones das configurações
- Ícones só como símbolo (sem quadradinho/fundo), linha fina (Outlined, 27dp) na cor do texto do tema. Cada opção com símbolo próprio (Novos seguidores = GroupAdd; Modo escuro = Brightness4; Tela = lua).

## Rodada 56 (v76) — Economia de dados e status online
- Vídeos > "Economia de dados" (`setting_data_saver`): vídeos do feed só carregam ao tocar em play.
- Conta > "Mostrar que estou online" (`setting_activity_status`, padrão ligado): desligado, o app não envia presença e o contador de online some para a própria pessoa.
- Ficam para depois (dependem de servidor/serviços externos): conta privada (coluna + pedidos de seguir), live real (serviço de vídeo), stories/notas com música (catálogo licenciado), chat etapa 2, Perto de mim.

## Rodada 57 (v77) — Ícone padrão de figurinhas/emojis
- Novo `QinblyCreateIcons.sticker` (carinha com "+" no canto, linha fina). É o ícone padrão para qualquer botão de emojis/figurinhas: barra de mensagens e campo de comentários das publicações. Regra de padronização: todo novo lugar com emojis/figurinhas deve usar esse símbolo (nunca emoji como ícone).
- Painel de figurinhas, clipe de anexos, câmera e microfone nas mensagens/comunidade seguem aprovados em esboço, ainda não aplicados (dependem de servidor).

## Rodada 58 (v78) — Barra de mensagem, criar comunidade com foto, folha Criar mais alta
- Mensagens: carinha/figurinha (ícone padrão) agora à ESQUERDA dentro da caixa; à direita clipe (painel com Documento, Galeria, Áudio, Localização, Contato, Enquete) e câmera; botão azul vira microfone quando a caixa está vazia e "enviar" quando há texto. Anexos/câmera/áudio ainda mostram aviso ("chega junto com a atualização do servidor"): o envio depende do servidor.
- Criar comunidade: sem seletor de ícones/emoji; círculo para escolher foto (prévia local, upload depende do servidor), contador do nome (x/40), campos arredondados.
- Folha "Criar" (botão +): sobe com padding da barra de navegação + 56dp, para não ficar sob os botões do celular.

## Rodada 59 (v79) — Criar comunidade em tela inteira
- `QinblyCreateCommunityDialog` agora é uma tela inteira (opção B): topo "Nova comunidade" com voltar, foto grande (112dp, prévia local), campos NOME (x/40) e DESCRIÇÃO com textos de exemplo, aviso de comunidade aberta e botão azul grande "Criar comunidade" fixo embaixo (respeita barra de navegação e teclado).

## Rodada 60 (v80) — Comunidade estilo grupo de WhatsApp
- Lista de comunidades: círculo azul com a letra inicial do nome no lugar do emoji (`QinblyCommunityAvatar`; vira foto quando o servidor guardar fotos).
- Por dentro: topo com voltar, avatar, nome e membros (toque abre "Dados da comunidade"), menu ⋮ (Dados, Silenciar/Reativar avisos, Limpar conversa [só na tela, `community_cleared_<id>`], Denunciar comunidade, Sair/Excluir). Descrição fixada em barra; balões (outros à esquerda com nome colorido, seus à direita), separadores Hoje/Ontem/data, hora, excluir própria mensagem (dono exclui qualquer); atualização automática a cada 6 s.
- Barra de mensagem igual à das conversas (figurinha/emoji à esquerda; clipe e câmera à direita; microfone/enviar). Quem ainda não entrou vê "Entrar na comunidade".
- Dados da comunidade: avatar grande, membros, atalhos Silenciar/Compartilhar, descrição, dono + quem já falou, Sair/Excluir/Denunciar. Lista completa de membros, foto, anexos e áudio dependem do servidor.
- Denúncia usa `reportContent("community", id, motivo)`; se o servidor ainda não aceitar o tipo "community", mostra o erro.

## Rodada 61 (v81) — Figurinhas (stickers) no lugar dos emojis
- Regra de padronização: o botão com o ícone F abre SEMPRE o painel de figurinhas (`QinblyStickerPanel`: painel azul clarinho, grade de 4 colunas, primeiro quadrado "+" para criar). Não há mais teclado de emojis dentro do app (o celular já tem o do teclado).
- 12 figurinhas desenhadas em vetor (`QinblyStickers`: happy, heart, like, love, cool, meh, wow, cry, star, fire, ok, party). Uma figurinha viaja como texto `::sticker:ID::` e aparece grande em: conversas (balão), comunidades (balão) e comentários das publicações. Na lista de conversas aparece como "Figurinha". Tocar na figurinha envia na hora.
- Valendo em: Mensagens, Comunidade e Comentários. Criar figurinhas das pessoas (foto → figurinha) depende do servidor: o "+" mostra aviso por enquanto.

## Rodada 62 (v82) — Câmera nova + vídeos do feed (só o em foco toca)
- Câmera redesenhada (esboço aprovado): topo com X e pílula "Adicionar som" (folha: Do seu celular [escolhe o áudio e mostra o nome; a mistura com o vídeo depende do servidor], Gravar narração e Sons do Qinbly = "Em breve"); sem aba LIVE.
- Lateral à direita, recolhível: Flash, Temporizador (Desligado/3/5/10 s com contagem na tela), Colagem*, Formato (9:16/4:3/1:1, recorte real via ViewPort do CameraX), Retoque*, Filtros (mostra/esconde a fileira de filtros) e setinha que abre: Grade, Espelhar (foto frontal), Velocidade*, Mãos livres (vídeo começa após a contagem), Exposição (slider), Modo noite*, HDR*, Estabilizar*, Qualidade (HD/FHD/UHD), Fundo*. Aberta, a lista rola e esmaece perto da linha do texto. (*) = aparece apagado e mostra "em breve".
- Zoom só com o gesto de pinça (indicador "1.8x" some sozinho). Botão de virar câmera desceu para a direita do botão de gravar; galeria à esquerda, bem afastada.
- Botão de gravar: toque = foto, segurar = gravar (anel azul de progresso, contador, solte para parar). Chips 10 min / 60 s / 15 s definem o limite; FOTO; TEXTO abre a tela de texto.
- Galeria: abre o seletor nativo do Android com fotos E vídeos (PickVisualMedia.ImageAndVideo). A miniatura mostra a última foto/vídeo quando o app já tem permissão de leitura; senão, ícone.
- Filtros de cor (Original, P&B, Sépia, Vintage, Quente, Frio, Vívido, Suave, Contraste, Desbotado, Noir, Rosado) em fileira rolável para o lado, com nome, e slider de Intensidade. Valem na prévia (camada de cor da PreviewView), no editor e na foto exportada (`qinblyFilterMatrix`, usada também por `createAdjustedImage`).
- Efeitos animados (Corações, Confete, Neve, Brilho, Bolhas, Chuva, Fogo): partículas desenhadas por cima da câmera e do editor. Ainda NÃO ficam gravados no arquivo final; não seguem o rosto.
- Feed: `QinblyVideoFocus` — só o vídeo mais visível (>= 40%) toca e recebe som; os que saem da tela pausam; o botão de volume liga o som só do vídeo em foco e o som ligado vale para o próximo; vídeo em tela cheia pausa os do feed; app em segundo plano pausa.

## Rodada 63 (v83) — Nova publicação e Escolher capa
- Tela "Nova publicação" redesenhada: só voltar + título (sem "Editar", "Editar vídeo", "Voltar à edição"); miniatura 96×140 (play + duração) ao lado da legenda (contador 0 / 2200); botão "Escolher capa" (só vídeo); atalhos #novidade, #qinbly, #diadia e @ marcar; lista com ícones de linha: Marcar pessoas e Quem pode ver (em breve), Permitir comentários, Permitir compartilhar, Permitir que salvem, Salvar no meu celular (copia para a galeria, pasta Qinbly, Android 10+). Botão "Publicar" mantido embaixo.
- Nova tela "Escolher capa": X + título, prévia 9:16 com etiqueta "Capa", faixa com 10 momentos do vídeo (toque/arraste), botão "Usar esta capa". Sem escolher da galeria.
- Limites: capa e opções (comentários/compartilhar/salvar) ainda não são guardadas no servidor; a capa só aparece na prévia local.

## Rodada 64 (v84) — Sem tela "Publicado!"
- Removida a tela "Publicado!" (botões "Ver publicação"/"Criar outra publicação"). Ao publicar com sucesso, o app limpa o rascunho, mostra o aviso "Publicado" e volta ao Início. Em caso de erro, continua na tela de publicar com a mensagem.

## Rodada 65 (v85) — Tela "Editar publicação"
- A janelinha virou tela cheia: voltar + título + "Salvar" no topo; miniatura da mídia ao lado da legenda (2200 caracteres); atalhos de hashtag/@; interruptores (comentários, compartilhar, permitir que salvem); botão "Salvar alterações"; "Excluir publicação" pequeno e em vermelho, afastado, com confirmação.
- Removidos "Trocar foto", "Trocar vídeo" e "Remover": a mídia não muda depois de publicar.
- Só a legenda é salva (editPostFull). Interruptores são só visuais até o servidor guardar esses dados.

## Rodada 66 (v86) — Menu "Criar" acima dos botões do celular
- A folha "Criar" subiu: margem de baixo de 56 para 96 dp, para os quatro cartões ficarem acima dos botões de navegação do celular.

## Rodada 67 (v87) — Teclado no chat
- A conversa agora sobe junto com o teclado (imePadding), para a caixa "Mensagem…" e o texto digitado ficarem visíveis. Com o teclado aberto numa conversa, a barra de baixo do app se esconde (volta ao fechar o teclado) para não deixar um vão.

## Rodada 68 (v88) — Teclado padrão em todo o app
- Regra do app: toda tela com campo de texto sobe junto com o teclado, com a caixa de digitar colada nele. Aplicado na área principal (imePadding no conteúdo do Scaffold) e a barra de baixo se esconde com o teclado aberto, em qualquer aba. No chat, a margem de baixo da caixa cai de 16 para 2 dp com o teclado aberto.
- Telas em janela própria (diálogos/telas cheias) precisam do próprio imePadding; ao criar uma nova com campo de texto, incluir.

## Rodada 69 (v89) — Ícone do aplicativo
- Novo ícone adaptativo (Android 8+): fundo em degradê índigo (#4A2BD6 → #1B1456), nome "QINBLY" em branco e barra colorida embaixo (amarelo, laranja, roxo, verde-água). Camada da frente em PNG por densidade (mipmap-*/ic_launcher_foreground.png), camada monocromática para o tema de ícones do Android 13+, fundo em drawable/ic_launcher_background.xml, mipmap-anydpi-v26/ic_launcher(.xml e _round.xml). Manifest passou a apontar para @mipmap/ic_launcher e ic_launcher_round.
- O nome fica dentro da zona segura (64% da largura), por isso aparece menor que no esboço. Imagem de 512 px para a Play Store: QINBLY-icone-512.png.
- Se o projeto de verdade já tiver pasta res com ícones, copiar estas pastas para lá e conferir o android:icon do manifest real.

## Rodada 70 (v90) — "Não foi possível reproduzir este vídeo"
- Causa provável: todo vídeo do feed era preparado ao aparecer na lista, mesmo fora de foco; vários decodificadores ao mesmo tempo (e rede oscilando) geravam o erro.
- Agora: o vídeo só é preparado quando está em foco; ao sair de foco, guarda a posição, para e solta o decodificador (o primeiro quadro volta a aparecer). Se der erro, tenta de novo até 3 vezes (0,7 s, 1,4 s, 2,1 s) antes de mostrar a mensagem. O botão de play no erro continua funcionando.
- Se continuar acontecendo em um vídeo específico, a causa pode ser o arquivo (formato/codec) ou o link do servidor; mandar o vídeo/log.

## Rodada 71 (v91) — Cursor ao tocar nas hashtags
- Ao tocar em #novidade, #qinbly, #diadia ou @ na Nova publicação (e em Editar publicação), o cursor agora vai para o fim do texto, depois da hashtag. Antes ele ficava onde estava e a hashtag aparecia na frente. A legenda usa TextFieldValue com a posição do cursor guardada (captionCursor / textCursor).

## Rodada 72 (v92) — Vão entre a caixa de digitar e o teclado
- Causa: com o teclado aberto, a margem da barra do sistema (Scaffold) era somada à altura do teclado, mais 10 dp de margem do chat. Agora, com teclado aberto, o conteúdo usa só a margem do topo + a altura do teclado (imePadding), a margem de baixo do chat vai a 0 e a caixa tem 4 dp de folga.

## Rodada 73 (v93) — Editor da galeria no visual da câmera
- Depois de escolher foto/vídeo na galeria, a tela de edição segue o visual da câmera nova: X no canto, lista lateral só com edição (Texto, Stickers, Formato, Retoque "em breve", Filtros; seta abre Desenho), fileira de filtros em bolinhas (com intensidade) e efeitos animados embaixo, botão "Avançar" azul.
- Removidos: Compartilhar, Efeitos (que só trocava de filtro), Editar vídeo, Camadas e o botão de voltar redondo escuro. Não há "Adicionar som" nem opções de câmera (flash, temporizador, grade, velocidade etc.).
- O fundo é a mídia escolhida. A prévia do filtro é aproximada (Contraste não aparece na prévia, mas vale no arquivo final de foto); os efeitos animados só aparecem na prévia.

## Rodada 74 (v94) — Editor novo só no caminho da galeria
- O visual novo do editor (X, lateral só de edição de foto, filtros em bolinhas, "Avançar" azul) vale somente quando a pessoa entra por Criar > Galeria (initialMode == "gallery"). O editor depois de tirar foto/vídeo na câmera voltou ao visual anterior.

## Rodada 75 (v95) — Recortar, Formato, Espelhar, Ajustes e foto final com as edições
- Editor (visual da galeria): lateral = Texto, Stickers, Formato, Recortar (só foto), Retoque (em breve), Filtros; seta abre Desenho, Espelhar e Ajustes (brilho e contraste).
- Recortar: tela própria com quadro, grade, zoom e arrasto com os dedos, proporções (Original, 1:1, 4:5, 9:16, 16:9), girar 90° e botão "Usar recorte". Formato continua trocando só a proporção (centralizada).
- Espelhar e Girar geram uma cópia da foto (cache) que passa a ser a base da edição.
- IMPORTANTE (correção): até a v94 o filtro, o texto, o sticker e o desenho do editor só apareciam na prévia; a foto publicada era a original (createAdjustedImage/createEditorOverlayImage não eram chamadas). Agora, ao tocar em "Avançar", a foto é renderizada com recorte + filtro (com intensidade) + brilho/contraste + texto/sticker/desenho (qinblyRenderEditedPhoto) e é essa a enviada (bakedUri). Vale também para fotos vindas da câmera. Várias fotos de uma vez (álbum) seguem sem edição. Se a renderização falhar, publica a original e avisa.
- Efeitos animados continuam só na prévia. Vídeo não recebe filtro/recorte no arquivo (só prévia).

## Rodada 76 (v96) — Lateral da câmera maior e sem esmaecer
- Só na câmera: ícones 24→27 dp, nomes 10→12 sp, "Adicionar som" maior (15 sp, ícone 19 dp). A seta de "mais opções" foi para o fim da lista (desce virada para baixo; aberta, vira para cima no final). Aberta, a lista ocupa a altura toda até o fim da tela (com rolagem se precisar) e o efeito que esmaecia os itens foi removido.
- O editor da galeria não mudou (QinblyCameraSideItem ganhou o parâmetro compact = true, que mantém os tamanhos antigos).

## Rodada 77 (v97) — Lateral do editor da galeria igual à da câmera (só opções de foto)
- O editor da galeria usa o mesmo tamanho grande da lateral da câmera, com a seta no fim da lista e a lista aberta descendo até o fim da tela (com rolagem). Itens só de edição de foto: Texto, Stickers, Formato, Recortar, Retoque, Filtros e, na seta, Desenho, Espelhar e Ajustes. O editor depois da câmera segue como antes.

## Rodada 78 (v98) — Texto na câmera, fontes e cores
- Chip TEXTO da câmera não abre mais Criar > Texto: ativa `camTextMode` e abre digitação por cima da prévia ao vivo (scrim + campo central, máx. 120 caracteres).
- `QinblyTextStyleBar`: 10 fontes (Forte, Larga, Leve, Fina, Compacta, Clássica, Elegante, Máquina, Divertida, Manual) e 12 cores em fileiras com rolagem lateral, logo acima do teclado. Também aparece na ferramenta Texto do editor da galeria.
- Texto compartilhado com o editor (`editorText/editorTextFont/editorTextColor`): após tirar a foto aparece no editor e é gravado na imagem em "Avançar" (`createEditorOverlayImage` / `qinblyRenderEditedPhoto` com fonte e cor).
- Criar > Texto (post só de texto) não mudou.
- Limites: só foto (vídeo não recebe o texto), texto fixo no centro, fontes dependem das fontes do sistema do aparelho. Não compilado/testado.

## Rodada 79 (v99) — Cor por palavra no texto da foto
- Estados `editorWordColors` (índice da palavra → cor) e `editorSelWord`. `QinblyTextStyleBar` ganhou fileira de palavras ("Todas" + cada palavra); com uma palavra marcada, a cor muda só ela; com "Todas", muda o texto inteiro e limpa as cores individuais.
- `qinblyColoredText` pinta cada palavra na câmera (prévia e campo de digitação) e no editor da galeria; `createEditorOverlayImage` desenha palavra a palavra (cor e sombra por palavra), centralizando cada linha.
- Vale nas duas telas: texto da câmera (chip TEXTO, embaixo) e ferramenta Texto do editor da galeria (lateral). Não compilado/testado.

## Rodada 80 (v100) — Perto de mim, permissões e câmera
- Aba "Perto de mim" abre (clique e deslize): `QinblyNearbyPage` pede localização, mostra a cidade (LocationManager + Geocoder), "Abrir configurações" se a permissão foi bloqueada. Posts por localização dependem do servidor (ainda não guarda localização).
- Manifest: ACCESS_COARSE_LOCATION e ACCESS_FINE_LOCATION.
- Primeira abertura pede câmera, microfone, mídia, localização e notificações de uma vez (flag `permissions_asked_v1`).
- Câmera: `DisposableEffect` em `PostPage` solta a câmera (`unbindAll`) ao sair da tela Criar; antes ela continuava ligada no Activity. Não compilado/testado.

## Rodada 81 (v101) — Nova tela de busca
- `SearchUsersDialog`: campo arredondado (pílula) com placeholder "Pessoas, #hashtags, publicações" e ação Pesquisar no teclado.
- Seção "Recentes": histórico local (`qinbly_search_history`, até 10), toque preenche o campo, X remove, "Limpar tudo". Salva ao pesquisar, abrir perfil ou abrir hashtag.
- Seção "Pesquisas em alta": hashtags do servidor (`getTrendingTags`) ou, se vazio, as mais usadas no feed carregado.
- Removido "Pessoas para seguir". Novos ícones `QinblyCameraIcons.clock/trend`.
- Limite: "mais pesquisadas" de verdade exige o servidor contar as buscas; hoje usa hashtags mais usadas. Não compilado/testado.

## Rodada 82 (v102) — Busca: pesquisas em alta (termos) e hashtags no fim
- "Pesquisas em alta" agora são palavras normais (as mais repetidas nas legendas do feed carregado, sem #/@; tocar preenche e pesquisa). Fonte provisória: o servidor ainda não conta as buscas.
- "Hashtags em alta" movidas para o fim, em bolinhas (FlowRow); tocar abre as publicações da hashtag.
- Rodada 81 (v101) fica incluída: Recentes, campo em pílula, sem sugestão de pessoas. Não compilado/testado.

## Rodada 83 (v103) — Tela de busca completa
- Ordem: campo (pílula, microfone quando vazio → reconhecimento de voz do Android, pt-BR) e filtros Tudo/Pessoas/Hashtags/Vídeos; Recentes; Top 5 lives em alta; Pesquisas em alta (palavras); Explorar por assunto (Música, Comida, Viagem, Esportes, Humor, Moda); Hashtags em alta no fim. Sem "Perto de mim" e sem sugestão de pessoas.
- `QinblyTopLives`: lista com coroa ouro/prata/bronze (1º–3º) e medalha (4º–5º), foto com aro vermelho (iniciais se sem foto), nome, @, telespectadores/curtidas, selo AO VIVO; ordenação por Telespectadores ou Curtidas. `QinblyLive` ganhou `displayName` e `avatarUrl`. Lista vazia ("Ninguém ao vivo agora") até o servidor fornecer lives.
- Filtros: Pessoas/Tudo buscam usuários; Hashtags trata o texto como hashtag; Vídeos mostra "chega em breve" (busca de publicações não existe no servidor).
- Novos ícones: mic, crown, medal, eye. Não compilado/testado.

## Rodada 84 (v104) — Aba Lives: Lives em alta + Ver mais
- Aba Lives: "Começar uma live" → "Lives em alta" (top 5 com coroa ouro/prata/bronze, medalha no 4º/5º, foto com aro vermelho, ordenação Telespectadores/Curtidas) → "Ver mais lives" (só com mais de 5 no ar) → "Agendadas". A grade de cartões "Ao vivo agora" foi substituída.
- `QinblyLiveRow`, `QinblySortChips`, `QinblyTopLives` (com `onOpen`/`onSeeMore`) e `QinblyAllLives` (lista completa: Telespectadores, Curtidas, Mais recentes; do 6º em diante nas duas primeiras). Tocar numa live abre a sala.
- Sem servidor de lives a aba mostra "Ninguém ao vivo agora". Não compilado/testado.

## Rodada 85 (v105) — Pesquisa dentro da aba Lives
- Cabeçalho "Lives" com lupa na aba. A lupa abre `QinblyLiveSearch`: campo em pílula (foco automático, teclado com ação Pesquisar), histórico "Pesquisados recentemente" (`qinbly_live_search_history`, até 8, X remove) e "Ao vivo agora" quando vazio; ao digitar, lista de quem está ao vivo filtrada por nome ou @ com o trecho em azul (`qinblyHighlight`), sem coroas; "Nenhuma live encontrada com esse nome." se vazio; seta/voltar fecha.
- `QinblyLiveRow` ganhou `showPosition` e `highlight`. Sem servidor de lives a pesquisa só acha alguém quando houver lives. Não compilado/testado.
- Correção (v106): o patch do v104 removeu por engano `QinblyNearbyPage`; função restaurada.

## Rodada 86 (v107) — Auditoria geral
- Comparado com o v97: nenhuma função/classe/objeto removido; nenhuma chamada a função inexistente; nenhum membro inexistente em QinblyCameraIcons/QinblyCreateIcons/QinblyBrand/RemoteAppearance/QiblyTheme; todas as chamadas `QinblyApi.*` existem.
- Corrigidos: `@OptIn(ExperimentalLayoutApi)` em expressão (não permitido) → movido para `SearchUsersDialog`; `cont.resume(x) {}` (API experimental) → `resumeWith(Result.success(x))`; ícone `mic` duplicado; `QinblyNearbyPage` restaurada.
- Duplicatas restantes são antigas e válidas: `build` (objetos/overloads diferentes), `toggleJoin` (funções locais em telas diferentes), imports repetidos (apenas aviso).
- Não compilado/testado.

## Rodada 87 (v108) — "Method too large" em PostPage
- Erro de compilação: `PostPage` (tela Criar, ~1800 linhas) estourou o limite de 64 KB de bytecode de um método na JVM (Column/Row/Box/if são inline e viravam um método só).
- Correção sem remover nada: criado `QinblySplit { }` (composable que só chama o conteúdo) e 48 blocos grandes de UI dentro de `PostPage` (if/else, Column, Row, Box, Surface, Card com 14+ linhas, apenas em contexto composable) passaram a ser envolvidos nele; cada um vira um método separado. Nenhum comportamento mudou; LaunchedEffect/onClick/funções locais ficaram intactos.
- Não compilado/testado; se outra função grande acusar o mesmo erro, o mesmo procedimento se aplica.

## Rodada 88 (v109) — Permissões de rede social
- Manifest: + READ_CONTACTS, READ_MEDIA_AUDIO, READ_MEDIA_VISUAL_USER_SELECTED, ACCESS_MEDIA_LOCATION, BLUETOOTH_CONNECT, MODIFY_AUDIO_SETTINGS, VIBRATE, WAKE_LOCK, FOREGROUND_SERVICE; `uses-feature` (microfone, localização, GPS, bluetooth) com required=false. Localização/câmera/áudio/mídia/notificações já estavam (v100).
- Pedido na 1ª abertura (`permissions_asked_v2`, re-pergunta o que faltar): câmera, microfone, localização, contatos, fotos, vídeos, áudio, mídia selecionada (Android 14), bluetooth, notificações.
- Atenção Play Store: READ_CONTACTS exige declaração/política de privacidade e o app ainda não tem recurso "achar amigos" que a use; se não for usar, o Google pode questionar.

## Rodada 89 (v110) — Editor único (galeria e câmera)
- O editor novo da galeria (X no canto, ferramentas com nome na lateral, Filtros/Ajustes com bolhas, Avançar azul) passa a valer também para foto/vídeo feitos na câmera: `newEditorChrome = initialMode == "gallery" || "camera"` substitui as condições `initialMode == "gallery"` do editor.
- `leaveEditor()`: na galeria volta para a galeria; na câmera descarta a foto/vídeo (limpa texto, sticker, desenho, mídia) e volta à câmera; vale para o X e para o botão voltar do celular.
- O editor antigo (botões redondos sem nome, "Editar vídeo", Avançar branco) ficou sem uso no código (branch else). Em vídeo: Recortar/Espelhar/Ajustes só aparecem em foto; filtros e texto em vídeo continuam só na prévia (não gravados no arquivo). Não compilado/testado.

## Rodada 90 (v111) — Vídeo de fotos (botão "Editar" no editor da galeria)
- Editor da galeria: novo item **Editar** na lateral, acima de Texto (só para foto). Abre a tela cheia `QinblyPhotoVideoEditor` (arquivo novo `PhotoVideoEditor.kt`).
- Tela: voltar (cinza) e continuar (azul, redondo) no topo; prévia 9:16 grande (toque = tocar/pausar); play/pausa branco; desfazer/refazer; linha do tempo rolável com régua e linha branca; "+" no fim.
- Ferramentas (valem para a foto sob a linha branca): Adicionar (galeria, até 20), Duração (1–10 s), Transição (Nenhuma, Zoom, Deslizar, Surgir), Filtros (os 12 filtros do app + "Aplicar em todas"), Ordem (mover ‹ ›), Excluir.
- Continuar: grava cada foto com o filtro (9:16) e junta tudo em MP4 720x1280, 30 fps, sem áudio, com Media3 Transformer. O MP4 segue o fluxo normal de vídeo (publicar).
- build.gradle: + media3-transformer, media3-effect, media3-common 1.5.1.
- Pontes em MainActivity.kt: `qinblyPvFilterNames`, `qinblyPvFilterMatrix`, `qinblyPvBake`. Ícone `QinblyCameraIcons.editor`.
- Não compilado nem testado aqui. Texto e som no vídeo de fotos ainda não existem (som depende de mistura de áudio).

## Rodada 91 (v112) — Hashtags padrão do app
- Hashtags e @menções ficam azuis enquanto a pessoa digita (`QinblyTagTransformation`): legenda, texto, enquete, editar publicação, comentários, editar comentário e comentários da live.
- Tela de publicar / editar / texto / enquete: legenda e hashtags em campos separados (`QinblyLegendField` + `QinblyHashtagField`). Hashtag digitada e fechada com espaço vai sozinha para o campo de hashtags (bolinhas azuis com ×, até 30). Sugestões com contagem e "Suas últimas hashtags". No servidor a legenda continua junta: legenda, linha em branco, hashtags.
- Feed (foto): legenda em cima da foto, hashtags azuis logo abaixo; com mais de 4, mostra 4 + "mais/menos" (`QinblyFeedCaption`).
- Visualizador de vídeo: legenda sobre o vídeo com "mais" que mostra as hashtags em azul-claro e "menos" (`QinblyViewerCaption`).
- Pesquisa: "Hashtags mais usadas" (top 5, com contagem de publicações) e "Suas hashtags" (histórico local com "usou N×" e Limpar).
- Página da hashtag: título azul e contagem curta ("12,4 mil publicações").
- Histórico local: `QinblyHashtagStore` (SharedPreferences `qinbly_hashtags`), gravado ao publicar/editar.
- Contagens globais vêm de `/api/hashtags/trending` (precisa do deploy do `qinbly-api`); sem servidor usa as hashtags do feed carregado.
- Não compilado nem testado aqui.

## Rodada 92 (v113) — Ícones da lateral mais abaixo
- Lateral da câmera e do editor de foto: ícones descidos 36 dp (câmera 52→88 dp, editor 56→92 dp).

## Rodada 93 (v114) — Rolagem do feed com vídeo + correção de função duplicada
- Corrigido erro de compilação do v112: `qinblyCount` estava declarada duas vezes (a antiga foi removida; vale a nova: 912, 3,1 mil, 1,2 mi).
- Feed: a lista não volta mais ao topo quando o número de publicações muda (era `LaunchedEffect(refreshKey, displayedPosts.size) { scrollToItem(0) }`); agora só ao atualizar o feed ou trocar de aba.
- Vídeo no feed: o foco (`isFocused`) usa `derivedStateOf`, então só recompõe quando muda de fato; e o mapa de visibilidade só é escrito quando a fração muda 3% ou mais.
- Não compilado nem testado aqui; a causa exata do travamento não foi reproduzida.

## Rodada 94 (v115) — Filtros de cor próprios
- `QinblyColorFilterNames` agora: Original, Aurora, Pôr do sol, Neon, Filme, Pastel, Noir, Oceano, Vintage, Sonho, Dourado, Cinema.
- Novo helper `qinblyLook(sat, con, rm, gm, bm, ro, go, bo)` monta cada look (saturação, contraste, ganho e deslocamento por canal). Câmera ao vivo, editor, foto salva e editor de vídeo de fotos usam a mesma `qinblyFilterMatrix`, então o padrão vale em todo o app.
- Miniatura da bolinha (`qinblyFilterSwatch`) usa cena neutra (céu claro, pele, chão bege): acabou o visual todo verde.
- Tintas do vídeo (`editorFilterTint`) e o atalho "Efeitos" atualizados para os novos nomes.
- Filtros de rosto (36 desenhos + MediaPipe) ficam para a próxima rodada.

## Rodada 95 (v116) — Filtros de rosto próprios
- Novo arquivo `FaceFilters.kt`: 12 filtros desenhados em código (Cachorro, Coroa, Gato, Coelho, Óculos de sol, Estrelas, Urso, Palhaço, Festa, Bigode, Flores, Anjo). Um único desenho (`qinblyDrawFaceFilter`) serve para a prévia ao vivo, a miniatura da bolinha e a foto salva.
- Detecção de rosto com ML Kit (`com.google.mlkit:face-detection:16.1.7`, modelo embutido no app; não usei MediaPipe porque exige baixar arquivo de modelo). O filtro acompanha a posição e a inclinação dos olhos.
- Câmera: bolinhas dos filtros de rosto depois dos efeitos animados; toque de novo desliga. `ImageAnalysis` entra no mesmo grupo da câmera; se o aparelho não aceitar 4 usos juntos, a câmera abre normal, sem filtro de rosto.
- Foto: o filtro é gravado na foto ao tirar (`qinblyBakeFaceFilter`). Vídeo gravado AINDA NÃO leva o filtro de rosto.
- Live: a tela agora abre a câmera de verdade (pede permissão), o botão "Virar câmera" funciona e o botão de efeitos (estrelinha, lado direito) abre filtros de cor + rosto. A transmissão em si continua dependendo do servidor.
- Padrão: qualquer tela nova com câmera deve usar `QinblyFaceCameraPreview` e a lista `QinblyFaceFilterNames`.

## Rodada 96 (v117) — Botões laterais maiores
- `QinblyCameraSideItem` (usado na câmera e no editor de foto/vídeo): ícone 27→33 dp, texto 12→14 sp, espaço ícone/texto 7→8 dp. Como é um item só, vale para todas as laterais do app.

## Rodada 97 (v118) — Barra de mensagem com ícones maiores
- As duas barras de mensagem (conversa direta e conversa de comunidade): carinha 26→30 dp (botão 40→44), clipe e câmera 24→28 dp (botões 38→42) e ambos 10 dp mais para dentro, microfone/enviar 24→26 e 23→25 dp, bolinha azul 46→50 dp.
- Áudio de verdade (gravar e enviar): em negociação, depende de mudança no servidor (categoria de arquivo de áudio).

## Rodada 98 — Permissões não apareciam no celular
- Causa: o manifesto de `android-scaffold/` é só um modelo; o manifesto real do repositório (`app/src/main/AndroidManifest.xml`) não recebia as permissões novas. Entregue `AndroidManifest.xml` completo (21 permissões, localização e contatos incluídos) para substituir/mesclar no repositório real.
- Regra daqui para frente: toda permissão nova tem que ser conferida no manifesto REAL, não só no modelo.
- v119: o manifesto completo agora também vai no caminho real do projeto, `app/src/main/AndroidManifest.xml`, dentro do zip. Ao copiar o projeto por cima do repositório, ele já substitui o manifesto antigo. Conferir antes se o manifesto antigo tinha algo a mais.

## Rodada 99 (v120) — Mensagem de voz nas conversas
- Novo arquivo `VoiceMessages.kt`: gravador (`QinblyVoiceRecorder`, .m4a AAC), barra de gravação (`QinblyVoiceBar`: lixeira, ponto vermelho, tempo, ondas, seta de enviar; limite de 5 min; menos de 1 s cancela com aviso) e balão de voz (`QinblyAudioBubble`: play/pausa, ondas com progresso, duração; só um áudio toca por vez).
- Conversa direta (`MessagesPage`): o microfone, com a caixa vazia, pede a permissão e começa a gravar; a seta sobe o arquivo e envia. Conversa de COMUNIDADE ainda mostra o aviso antigo (precisa de rota própria no servidor).
- API: `QinblyApi.sendVoiceMessage` (sobe como categoria `message_audio` e envia o corpo `qinbly:audio:<arquivo>:<segundos>`); `MessageSummary` ganhou `audioUrl` e `audioSeconds`.
- Servidor (`SUPABASE/functions/qinbly-api/index.ts`): categoria `message_audio` (audio/mp4, aac, mpeg, ogg, webm, 3gpp; até 10 MB), conferência no `complete`, mensagens devolvem `audioUrl`/`audioSeconds` e corpo "Mensagem de voz" (também na lista de conversas), e o envio só aceita áudio da própria pasta do usuário. É PRECISO publicar: `supabase functions deploy qinbly-api`.

## Rodada 100 — Botões que eram só enfeite agora funcionam
- Conversa direta, painel de anexos: **Galeria** (escolhe e envia foto), **Localização** (pede permissão e envia link do Maps com a posição), **Contato** (seletor de contatos do sistema; envia nome e telefone), **Áudio** (começa a gravar mensagem de voz). Ícone de **câmera** da barra tira a foto e envia.
- Servidor: nova categoria `message_media` (imagens), corpo `qinbly:img:<arquivo>`, `imageUrl` nas mensagens, prévia "Foto" na caixa de entrada. Exige `supabase functions deploy qinbly-api`.
- App: `QinblyApi.sendImageMessage`, `MessageSummary.imageUrl`, bolha de imagem.
- Câmera: **Estabilizar** liga a estabilização de vídeo do CameraX quando o aparelho suporta (senão avisa).
- Ainda sem função: Documento, Enquete no chat, mic/anexos do chat de comunidade, criar figurinhas, comentários/presentes/compartilhar na live, notas e stories, Colagem, Retoque, Velocidade, Modo noite, HDR, Fundo, Marcar pessoas, Quem pode ver, busca de vídeos, mixagem de som.

## Rodada 101 — Mais botões ativados
- Chat de comunidade: **Galeria**, **câmera**, **Localização**, **Contato** e **Áudio** (microfone e anexo) funcionam, iguais às mensagens diretas. Servidor: mensagens de comunidade aceitam `qinbly:img:` e `qinbly:audio:` (validação do caminho do dono) e devolvem `imageUrl`/`audioUrl`/`audioSeconds`.
- Câmera: **Modo noite** sobe a exposição (2/3 do máximo do aparelho); avisa se o aparelho não permite.
- Live: **Compartilhar** abre o compartilhamento do Android.
- Publicar: **Quem pode ver** alterna Todos / Só eu; servidor grava `visibility = private` (aparece só no seu perfil). Usa a restrição já removida pelo SQL 05.
- Continuam sem função: Documento, Enquete no chat, figurinhas próprias, comentários e presentes da live, notas e stories, Colagem, Retoque, Velocidade, HDR, Fundo, Marcar pessoas, busca de vídeos, mixagem de som.

## Rodada 102 — Busca de vídeos, Retoque, Fundo, HDR, Colagem, Velocidade e som
- **Busca de vídeos** (filtro "Vídeos"): lista em grade os vídeos das publicações recentes; o texto digitado filtra por legenda ou @. Toque abre o visualizador, com comentar e compartilhar.
- Novo arquivo `PhotoEffects.kt` (só fotos): **Retoque** (suaviza a pele dentro dos rostos achados pelo ML Kit), **Fundo** (desfoca o fundo, pessoa nítida, segmentação de pessoa do ML Kit) e **HDR** (clareia sombras, comprime brilhos, +12% de cor — realce, não HDR de sensor). Na câmera são liga/desliga e valem para as fotos tiradas; no editor de foto viram botões que aplicam na hora (Retoque, HDR, Fundo). Se não achar rosto/pessoa, avisa e mantém a foto.
- Novo arquivo `Collage.kt`: **Colagem** na câmera alterna Desligada → 2 fotos → 4 fotos; cada disparo guarda uma foto e, ao completar, monta 1080x1920 (2 fotos uma sobre a outra, ou grade 2x2) e abre o editor.
- Novo arquivo `VideoSpeedSound.kt` (Media3 Transformer, já no projeto): **Velocidade** (0,5x / 2x / 3x) e **mistura do som escolhido** com o vídeo gravado (o som é cortado no tamanho do vídeo; o áudio original continua junto). Só vale para vídeos gravados na câmera, não para os da galeria.
- `app/build.gradle.kts`: nova dependência `com.google.mlkit:segmentation-selfie:16.0.0-beta6`.
- Continuam sem função (precisam de servidor ou serviço externo): Documento, Enquete no chat, figurinhas próprias, comentários/presentes da live, transmissão ao vivo, notas e stories, Marcar pessoas.

## Rodada 103 — Lote 1: Documento, Enquete no chat, Marcar pessoas (tudo pronto; falta rodar SQL 09 e fazer o deploy)
- **SQL novo `SUPABASE/09_chat_extras.sql`**: tabelas `chat_polls`, `chat_poll_votes`, `post_tags` (com RLS) e liberação do tipo de notificação `tag`.
- **Documento (DM e comunidade)**: categoria `message_file` (PDF, Word, Excel, PowerPoint, TXT, CSV, ZIP; até 25 MB), marca `qinbly:file:<arquivo>:<nome codificado>`, bolha com ícone + nome que abre o arquivo. Não precisa de tabela.
- **Enquete no chat (DM e comunidade)**: `POST /api/chat-polls` cria; mensagem `qinbly:poll:<id>`; `POST /api/chat-polls/:id/vote` vota/troca o voto; mensagens devolvem `poll {question, options, votes, myVote}`. Bolha com barras de porcentagem.
- **Marcar pessoas**: tela de busca (até 10), `POST /api/posts` aceita `taggedUsernames`, grava `post_tags`, avisa cada pessoa (notificação `tag`, abre a publicação) e os posts trazem `taggedUsers`; o card mostra "Com @fulano".
- Servidor: helper único `chatBodyDto` (voz, foto, documento, enquete) usado nas mensagens diretas e nas de comunidade; `validateChatToken` confere dono do arquivo/enquete antes de gravar.
- Novo arquivo `ChatExtras.kt` (bolhas e janelas de enquete e marcação).

## Rodada 104 — Lote 2: figurinhas próprias e comentários da live (pronto; falta rodar SQL 10 e fazer o deploy)
- **SQL novo `SUPABASE/10_stickers_live.sql`**: tabelas `user_stickers` e `live_comments` com RLS.
- **Figurinhas próprias**: o "+" do painel de figurinhas (conversa direta e comunidade) abre "Criar figurinha": escolhe uma foto, corta em quadrado 512x512 (PNG), opção "Tirar o fundo" (recorte da pessoa pelo ML Kit; sem pessoa usa a foto inteira) ou, sem recorte, cantos arredondados com borda branca. Sobe na categoria `sticker` (PNG/WebP, até 2 MB) e vai para `user_stickers` (até 60 por pessoa). A faixa "Minhas figurinhas" aparece acima do painel: toque envia, segurar exclui. Mensagem: `qinbly:sticker:<arquivo>`; a bolha mostra a imagem 120 dp. Nos comentários de publicações continuam só as figurinhas prontas.
- **Comentários da live** (a live é o @ de quem transmite): `GET/POST /api/lives/:host/comments` (o GET aceita `?after=<data ISO>`); no app, a sala da live e a tela de quem transmite buscam novos a cada 3 s e mostram os últimos 7 sobre o vídeo; enviar atualiza na hora. Presentes continuam sem função (dinheiro/loja).
- Novos arquivos: `CustomStickers.kt`, `LiveComments.kt`; `PhotoEffects.kt` ganhou `fxPersonCutout`.

## Rodada 105 — Lote 3: notas e stories (pronto; falta rodar SQL 11 e fazer o deploy)
- **SQL novo `SUPABASE/11_stories_notes.sql`**: tabelas `stories` (foto ou vídeo, legenda até 200, expira em 24 h), `story_views` e `notes` (texto até 60, uma por pessoa, expira em 24 h), com RLS (quem viu só o dono do story vê).
- **Servidor**: upload categoria `story_media` (foto até 25 MB, vídeo até 100 MB); `GET /api/stories` (bandeja: você + quem você segue, com story novo antes), `POST /api/stories`, `DELETE /api/stories/:id`, `POST /api/stories/:id/view`, `GET /api/stories/:id/views` (só o dono), `POST/DELETE /api/notes`.
- **App** (`Stories.kt`): na faixa do topo das mensagens, "Criar" abre o menu (story da galeria, story com foto tirada agora, escrever/mudar/apagar nota, ver meu story). Quem tem story ganha um aro (colorido = novo, cinza = já visto) e a nota aparece num balãozinho acima da foto (cada item reserva 20 dp de altura para alinhar). Visualizador em tela cheia: barras de progresso, toque à direita avança e à esquerda volta, passa sozinho (6 s foto; vídeo até o fim, máx. 20 s); nos seus stories mostra quantos viram, lista de quem viu e excluir.
- Visual novo na faixa (aro e balão de nota) feito sem esboço prévio: ajustar se não agradar.
- Ainda sem função: Presentes e transmissão de verdade da live (dinheiro/serviço externo).
