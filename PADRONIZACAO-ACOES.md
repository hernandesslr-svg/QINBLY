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
- Editor (visual da galeria): lateral = Texto, Stickers, Formato, Recortar (só foto), Retoque (funciona), Filtros; seta abre Desenho, Espelhar e Ajustes (brilho e contraste).
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
- Filtros: Pessoas/Tudo buscam usuários; Hashtags trata o texto como hashtag; Publicações busca na legenda das publicações (rota /api/search); Vídeos foi trocado por Publicações.
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

## Limpeza do editor e da câmera
- **Fundo:** o botão saiu da lateral do editor e da câmera (por enquanto). O código do Fundo continua guardado no projeto, sem botão: como voltar está em `PADRAO-EDITOR.md` (editor) e `PADRAO-CAMERA.md` (câmera).
- **Menu Criar da faixa de conversas:** removido o caminho antigo "galeria/câmera → Novo story" (`Stories.kt`), que nenhum botão chamava mais; o botão Story abre o Criar normal.

## Mural de avisos (antes "Mural do bairro")
- Na aba do Mercado, "Mural" agora se chama **Avisos**; a tela se chama "Mural de avisos"; no menu de criar, "Aviso".
- Só texto: saíram a câmera, a foto e os tipos (Achado, Perdido, Pergunta, Evento). Aviso novo é gravado com tipo "aviso". Avisos antigos aparecem só como texto.
- Limite de **500 letras** no aviso e no comentário (era 280): no app (`Extras.kt`, com contador n/500) e no servidor (`SUPABASE/20_mural_limite_500.sql`, já aplicado). Não aumentar muito: texto gigante trava a lista.
- Continuam: avisos somem em 48 h, só para quem está perto, comentários e respostas.
- Funções de tipo/foto (`NOTICE_TAGS`, `noticeKindLabel`, `noticeTagColor`, `CameraGlyph`) ficaram no arquivo sem uso, caso voltem.

## Perto de mim: tela do anúncio (vale para todos os tipos)
Uma tela só (`ListingDetail`, em `Marketplace.kt`) para venda, doação, achado/perdido, evento e serviço:
- Foto de ponta a ponta, desliza de lado; com mais de uma foto mostra contador "1/3" e bolinhas.
- Preço, título e etiquetas (tipo, "Estado: …" quando houver, cidade, tempo). Ícones das etiquetas: `MarketGlyph`.
- "Descrição" com rótulo; anunciante (inicial + nome) e dica de segurança **sem caixa de fundo**.
- Botão fixo embaixo, acima dos botões do celular (`navigationBarsPadding`): **"Enviar mensagem"** (no anúncio da própria pessoa: "Apagar anúncio").
- Fora do esboço, não feito: compartilhar, denunciar, ver perfil do vendedor, mensagem rápida.

## Nova publicação de texto (visual novo + fundo colorido)
- Tela só de texto refeita (a enquete mantém o layout antigo): X no topo com título "Nova publicação" centralizado, autor, cartão grande com o texto e a fileira de fundos (primeiro "Aa" = **sem fundo**, depois 6 cores em degradê). Hashtags, "Marcar pessoas" e "Quem pode ver" continuam; botão **Publicar** fixo embaixo, acima dos botões do celular. A "Dica" foi removida.
- Com fundo colorido o texto fica branco, centralizado e maior (24 sp no editor, 22 sp no feed); sem fundo é o cartão claro de antes. Padrão: já abre com a primeira cor.
- Servidor: coluna `posts.text_bg` (`SUPABASE/21_texto_fundo.sql`, já aplicada) e função `qinbly-api` aceita `textBg` (1..6) só em publicação de texto e devolve `textBg` em toda publicação. Paleta única `QinblyTextBgPairs` em `MainActivity.kt` (mesma no editor e no feed).
- Feed: publicação de texto com fundo vira cartão colorido arredondado (até 8 linhas). Ainda não aplicado: tela de comentários/detalhe e grade do perfil mostram o texto sem a cor.
- Pessoas com a versão antiga do app veem a publicação como texto comum.

## Novidades: busca de publicações, rascunho, agendamento, destaques, estatísticas, avisos

- **Busca completa**: o chip "Publicações" (e o "Tudo") mostram publicações públicas cuja legenda combina com o texto (`GET /api/search` devolve `users`, `posts`, `tags`). Tocar abre a publicação nos comentários. Post de texto aparece com o fundo colorido.
- **Rascunho (só texto)**: o texto e o fundo da "Nova publicação" de texto são salvos sozinhos no aparelho (`draft_text`, `draft_text_bg`) e voltam ao reabrir, com a linha "Rascunho recuperado · Descartar". Apaga ao publicar. Não existe rascunho de foto/vídeo.
- **Agendar (só texto)**: linha "Agendar publicação" escolhe data e hora (até 90 dias); o botão vira "Agendar". O servidor grava `posts.publish_at`; antes da hora só o dono vê (RLS `posts_read` + filtro do feed) e o card mostra "Agendada para dd/MM às HH:mm".
- **Destaques**: no story próprio, menu → "Destacar no perfil" (pede nome até 24 letras) copia o story para `story_highlights`; a linha de círculos aparece nos dois perfis (`QinblyHighlightsRow`); o dono remove pela lixeira dentro do visualizador.
- **Estatísticas**: menu "⋮" da publicação própria → "Ver estatísticas" (visualizações, pessoas, curtidas, comentários, compartilhamentos, barras de 7 dias). A visualização é contada 1,2 s depois do card aparecer, uma por pessoa por dia, sem contar o dono (`post_views`).
- **Avisos com o app fechado**: `NotifyWorker.kt` (WorkManager, a cada ~15 min, só com internet) consulta notificações e mensagens novas e mostra a notificação do sistema, respeitando as chaves `setting_notice_*`. **Não é push de verdade**: o atraso pode ser de 15 min ou mais e o Android pode adiar com economia de bateria. Push real exige um projeto Firebase (google-services.json).

## Ajustes do check-up (rodada seguinte)

- **Banco**: `request_follow` e `approve_follow_request` só para quem está logado; `qinbly_open_account` (gatilho) sem acesso por API; `qinbly_can_see_author` continua aberta porque as regras de leitura (posts/stories) a usam também para visitantes. Regras de acesso com `(select auth.uid())` e índices nas chaves estrangeiras (`SUPABASE/23_seguranca_e_velocidade.sql`). Falta ligar a proteção de senha vazada no painel (Authentication → Passwords); não dá por SQL.
- **Story próprio → Quem pode ver**: escolhe Todos/Amigos/Seguidores/Perto de você e grava em `stories.audience` (regra `stories_update`, `SUPABASE/24_stories_update_proprio.sql`). "Perto" exige que o app já saiba a cidade.
- **Story de outra pessoa → Silenciar/Reativar**: vale em todos os aparelhos. Fica na tabela `story_mutes` (`SUPABASE/25_silenciar_stories.sql`, funções `story_mute_set` e `story_mute_list`, só a própria pessoa lê e grava) e também em cache local (`story_muted`) em `QinblyStoryMute`; ao abrir a bandeja o app sincroniza (na primeira vez sobe o que já estava só no celular). Silenciados vão para o fim da bandeja com aro cinza.
- **Menu "+" dos stories → Música**: escolhe uma música (seletor de músicas) e publica um story de texto com a música tocando por 24 h.
- **Câmera → Som → Gravar narração**: grava a voz (barra de gravação de voz) e entra como "Narração" no mesmo caminho do som escolhido (misturada ao vídeo gravado em seguida).
- **Fundo colorido** também no topo dos comentários e na grade do próprio perfil (o perfil de outra pessoa já usava o card).
- Removidas sobras sem uso: `NOTICE_TAGS`, `noticeKindLabel`, `noticeTagColor`, `CameraGlyph`. O recorte de fundo guardado sem botão ficou como está (decisão do dono).
- Sem mudança: Lives/Presentes (de fora a pedido), anexo "Outro" (não existe mais item que caia nele) e Clipes sem vídeo (aviso e volta).

## Editor de foto: aba "Fundo" e "Recortar pessoa" removidos (pedido do dono)

- Saiu do editor todo o painel "Fundo" (Efeitos, Cores, Degradês, Cenários, Galeria e Recortar) e o botão "Recortar pessoa / Salva nas suas figurinhas". Saíram também os estados e miniaturas desse painel e a função de recortar a pessoa (`fxPersonCutout`).
- O editor continua com Retoque e HDR (`QinblyFxSpec(retouch, hdr)`). Criar figurinha pela galeria (Figurinhas) continua igual, só sem a opção de tirar o fundo.
- Ficou no código, porque a câmera usa: o desfoque de fundo do botão "Fundo" da câmera (`fxReplaceBackground`, `fxPersonMask`) e a dependência `segmentation-selfie`.

## Agendar publicação também em foto e vídeo; limpeza de "em breve"

- **Agendar publicação** agora existe na tela "Nova publicação" de foto/vídeo (linha com relógio, "Remover agendamento", botão vira "Agendar"). Mesma regra do texto: de 1 min a 90 dias no futuro. No servidor (`qinbly-api` v5) o campo `scheduledAt` vale para texto, foto, álbum e vídeo (enquete não, ela já tem prazo). Não aparece quando a tela é de story.
- Sai do ar o resto de "em breve" sem uso: parâmetro `soon` das linhas de menu do story e da folha da câmera.
- Continua de fora a pedido: Lives (transmissão de verdade) e Presentes.

## "Fundo" tirado de vez (pedido do dono)

- O botão/opção "Fundo" não existe mais em lugar nenhum (nem câmera, nem editor). Saiu também o código que ficava por trás: `fxReplaceBackground`, `fxPersonMask`, cenários/degradês, os campos de fundo do `QinblyFxSpec`, o estado `bgBlurOn` da câmera e a dependência `segmentation-selfie`.
- Ficam só Retoque e HDR nos efeitos de foto. O fundo desfocado das laterais de foto/vídeo no Formato (`blurBg`) é outra coisa (preenchimento do quadro) e continua.

## Tela de entrar e criar conta (novo visual)

- Peças em `AuthUi.kt`: `QinblyAuthField` (campo fino de 46 dp em pílula, ícone na frente, olhinho na senha), `QinblyAuthButton` (botão pílula de 46 dp, cheio ou só contorno), `QinblyGoogleMark` e `QinblyAuthLogo` (logo do app, o mesmo do ícone: anel em degradê com brilho). A tela em si continua em `MainActivity.kt` (mesma tela para entrar e criar conta, alternando por `createAccount`).
- Entrar: logo, "Bem-vindo", "Entre para continuar", campo **E-mail ou nome de usuário**, senha com olhinho, **Entrar** (azul), "Esqueceu a senha?", "OU", **Continuar com Google** (botão branco, igual ao Entrar em tamanho) e **Criar nova conta** fixo embaixo (contorno azul, acima dos botões do celular).
- Criar conta: logo, "Crie sua conta", e-mail, nome de usuário (3 a 24 letras, números, ponto ou sublinhado), senha (mínimo 8). Os campos mostram só o nome (E-mail, Nome de usuário, Senha); a regra aparece como aviso pequeno embaixo do campo só quando o que foi digitado está fora dela ("Use de 3 a 24 caracteres.", "Use só letras, números, ponto ou sublinhado.", "Mínimo de 8 caracteres."), com contorno vermelho suave; campo certo (e-mail e usuário) ganha um check verde. Peças: `QinblyAuthWarning` e os parâmetros `invalid`/`valid` do `QinblyAuthField`. **Criar conta**, "OU", **Cadastrar com Google**, aviso de Termos e Privacidade e **Já tenho uma conta** fixo embaixo.
- Sai da tela: caixa em volta do formulário, "Acesso à conta", "Voltar ao perfil" (a seta de voltar já faz isso), o texto técnico sobre `API_BASE_URL` e o quadro fixo de erro do Google. Erro do Google agora é um aviso curto (Toast) que some sozinho; erros de e-mail e senha aparecem em texto pequeno vermelho logo acima do botão.
- O "G" do botão do Google é um marcador simples. O G colorido oficial entra quando o arquivo da Google for adicionado ao app.
- **Entrar com nome de usuário**: quando o texto digitado não tem "@", o app chama a função `qinbly-login` (`SUPABASE/functions/qinbly-login/index.ts`), irmã da `qinbly-api`. Ela acha o e-mail da conta no servidor (o e-mail nunca volta para o app), faz o login na `qinbly-api` e devolve a mesma resposta. Usuário que não existe e senha errada dão a mesma mensagem ("Usuário ou senha incorretos."), para não revelar quais nomes existem. No app, `QinblyApi.request` manda o caminho `/signin/username` para essa função.

## Tela "Esqueceu a senha?" (substitui a janelinha antiga)

- Vira uma tela inteira (`QinblyForgotScreen` em `AuthUi.kt`), no mesmo estilo do login. Abre ao tocar em "Esqueceu a senha?" no login (já leva o que a pessoa digitou no campo) e a seta ou o botão de voltar do celular voltam para o login.
- Antes de enviar: ícone de cadeado, "Esqueceu a senha?", campo **E-mail ou nome de usuário**, **Enviar link** e duas dicas ("Não lembra do nome de usuário? ... entre usando o seu e-mail." e "Criou a conta com o Google? ..."). Erro de digitação aparece como aviso pequeno embaixo do campo.
- Depois de enviar: ícone de e-mail verde, "Confira seu e-mail", dica de spam e lixeira, "Enviar de novo" (liberado depois de 60 segundos, com contagem), e **Voltar para entrar**.
- A mensagem é sempre a mesma exista conta ou não ("Se existir uma conta com esses dados..."), para ninguém descobrir quem tem cadastro.
- Com e-mail, o app usa `/api/auth/forgot` (como antes). Com nome de usuário, usa a função `qinbly-login` (versão 2, `action = "forgot"`): ela acha o e-mail da conta e manda o link do Supabase para lá; o e-mail nunca volta para o app.
- O link do e-mail é o padrão do Supabase e abre a página do endereço configurado em Authentication, URL Configuration, "Site URL". O app ainda não recebe o link por dentro, então a página de criar a senha nova depende dessa configuração.

## Nome de usuário inicial curto

- Conta nova criada sem nome escolhido (principalmente pelo Google) recebe `user_` + 8 letras e números (ex.: `user_66b473ea`), em vez de 32. É o padrão da coluna `profiles.username` (`SUPABASE/26_username_curto_padrao.sql`, já aplicado). A pessoa troca depois em Editar perfil (3 a 24 caracteres).
- Contas que já existiam não mudam.

## Perfil: letras do cabeçalho um degrau menores

- Nome de exibição: de `headlineSmall` para `bodyLarge` (negrito), numa linha só, com "…" se for muito comprido. @usuário: de `bodyMedium` para `bodySmall`. "Adicione uma descrição ao seu perfil": agora `bodyMedium`. Contadores (Publicações, Seguindo, Seguidores), abas e bio não mudaram.

## Perfil sem conta

- Sem conta entrada (nome de usuário vazio), o botão "Editar perfil" e o toque na foto vazia abrem direto a tela de entrar/criar conta (`onAuth`), em vez da edição.

## Editar perfil: categorias coloridas e transparentes

- Só as cores mudaram (layout, foto e campos iguais). Cada uma das 40 categorias tem uma cor própria, sem repetir (`categoryColors`, mesma ordem de `availableCategories`; se entrar categoria nova, acrescentar uma cor nova ao fim da lista).
- Fundo transparente (16% da cor) e contorno de 1 dp (55%); selecionada fica com fundo 50%, contorno cheio de 2 dp e um check na frente do nome. O texto continua na cor padrão do tema.

## Perfil: botão Editar perfil no tema branco

- No tema branco o botão "Editar perfil" fica azul bem clarinho (`#E7F0FD`), sem contorno, com texto e lápis em `#1160D0`. Antes parecia lilás/rosa. No tema escuro continua o cinza de antes.
- Tons de "branco" do tema claro (em `QiblyTheme`): fundo `#FBF9FF`, cartões `#FFFFFF`, campos/áreas suaves `#F3F0FA`, bordas `#E9E4F5`. Esses tons eram levemente lilás (herança do roxo antigo da marca) e foram trocados pelo cinza gelo.
- Tons atuais do tema claro (branco fosco, "Fosco 2" aprovado no esboço): fundo e cartões `#F6F6F7` (mesma cor, tudo numa cor só), áreas suaves `#E4E6EB`, bordas `#DADADD`. O tema escuro não mudou.

## Botão de contorno (ModernUi.kt) respeita cores passadas

- O app tem o seu próprio `OutlinedButton` em `ModernUi.kt` (pílula preenchida: cinza no escuro, lilás `#EFE9FB` no claro) e antes ele ignorava o parâmetro `colors`. Por isso o "Editar perfil" azul clarinho não aparecia. Agora, se quem chama passar `colors` de propósito, vale a cor passada; sem isso, continua o padrão de antes. Os outros botões de contorno do tema branco seguem lilás até pedirem para mudar.

## Feed, contas privadas e Comunidades (diagnóstico de 17/11)

- O feed (`GET /api/posts`) mostra tudo o que o banco libera para quem está olhando: publicações públicas de perfis não privados, as suas e as de perfis privados que você segue. Nada é filtrado no app além disso.
- Perfil com "conta privada" (`profiles.is_private`) esconde as publicações de quem não o segue (regra `qinbly_can_see_author`). Em 17/11, `ana_rose` é privada: as 6 publicações mais novas do banco são dela e só aparecem para quem a segue.
- Corrigido: Comunidades dava 500 por recursão infinita nas regras (migração 27, `SUPABASE/27_corrige_recursao_comunidades.sql`).
- Ainda aberto: com a sessão vencida (401 no heartbeat/caixa de entrada), contar visualização (`/view`) dá 500 por falta de permissão. O feed continua carregando como visitante.

## Feed com sessão vencida

- `GET /api/posts` aceita visitante: com o token vencido ele responde 200 só com publicações públicas (o app não via 401 e nunca renovava a sessão). Resultado: feed mostrando só perfis públicos e nenhuma publicação de conta privada que a pessoa segue.
- `QinblyApi.getPosts` agora chama `/api/auth/me` antes (se der 401, o app renova o token sozinho) e só então busca o feed com o token novo.

## Teclado: faixa de sugestões nos campos de texto

- Padrão do app: todo campo de texto precisa manter a palavra que o teclado está montando (a "composição" do `TextFieldValue`), senão o teclado esconde a faixa de sugestões (todo → todos, todas).
- Causa do sumiço no Editar perfil (Nome, Nome de usuário, Bio): `qinblySanitized` (Extras.kt) refazia o `TextFieldValue` a cada letra, sem a composição. Agora, se o filtro não mudou nada no texto, devolve o valor original com a composição; só refaz quando precisa descartar um caractere (ex.: maiúscula no nome de usuário).
- Regra para campos novos: ao limitar/filtrar o texto, copiar `value.composition` junto (como em `Extras.kt` nos comentários) ou devolver o próprio valor quando nada mudou. Não usar `KeyboardType.Password/VisiblePassword` nem desligar `autoCorrect` em campo de texto comum.

## Marcar pessoas (tela cheia)
- Ao tocar em "Marcar pessoas" na publicação abre uma tela cheia (`QinblyTagPeopleDialog`, em `ChatExtras.kt`), não mais a janelinha.
- Topo: X (fecha sem salvar), título "Marcar pessoas" e "Pronto" (salva). Embaixo: busca "Buscar pessoa" e as abas **Seguindo** e **Seguidores** (não existe aba "Amigos").
- Cada linha: foto (ou inicial), nome com o selo de verificado (`QinblyVerifiedBadge`, sem escrever "verificado"), @usuário e círculo à direita; marcado = círculo azul com check branco.
- Não há chips/marcações azuis embaixo da busca nem contador; o limite é de 10 pessoas (ao passar, aparece "Você pode marcar até 10 pessoas.").
- Digitando 2+ letras, filtra a aba atual e acrescenta o resultado da busca geral. Devolve os @ sem o "@".

## Fundo do texto: cor livre e início sem cor
- Ao entrar em "Texto" a opção marcada é sempre "Aa" (sem cor); o fundo do rascunho não é mais restaurado, só o texto.
- Depois dos 6 fundos da paleta há a **bolinha de cor livre** (`QinblyColorWheelDot`, em `ChatExtras.kt`). A bolinha é sempre colorida (arco-íris) e nunca muda de cor; quando a cor livre está em uso ela ganha só o contorno de "selecionada".
- Segurar a bolinha e girar o dedo abre a roda de cores por cima; a cor aparece ao vivo no painel do texto e vale ao soltar.
- Gravação: `text_bg` agora é inteiro; 0 = sem fundo, 1..6 = paleta, 16777216 + RGB = cor livre (SUPABASE/28_texto_fundo_cor_livre.sql). O gateway só aceita 1..6 ao criar, então o app cria a publicação com 1 e logo em seguida grava a cor livre direto na própria publicação (regra `posts_update_own`).

## Texto com fundo colorido: hashtags fora do cartão
- No feed, o cartão colorido mostra só o texto; as #hashtags do final aparecem **em cima** do cartão, na cor normal do feed e clicáveis. Na prévia dos comentários e na miniatura do perfil o cartão também mostra só o texto.
- Se a publicação for só hashtags, elas ficam dentro do cartão (para não sobrar um cartão vazio).

## Pessoas marcadas ("Com @fulano") em cima, em todas as publicações
- Padrão do app: a linha "Com @pessoas" fica logo abaixo do nome de quem publicou, **acima** da legenda/hashtags e da mídia (foto, vídeo, cartão colorido, texto, enquete). Nunca embaixo, perto dos botões. Componente: `QinblyTaggedUsersRow` (MainActivity.kt).

## Sessão vencida: renovar antes de enviar (comentários, curtidas, etc.)
- Sintoma: não dá para comentar/ver comentários ("permission denied for table comments" nos logs). Causa: o token de acesso vencia e o servidor tratava a pessoa como visitante, sem devolver 401, então o app não renovava.
- Correção (`QinblyApi.request`): antes de enviar qualquer chamada com sessão, o app lê a validade (`exp`) do token e, se já venceu (ou vence em menos de 30 s), renova com o refresh token e só então envia. Vale para todas as telas; a renovação por 401 continua como segunda rede.
- Se a renovação não for possível (sem refresh token), a chamada segue como antes e a pessoa precisa entrar de novo.

## Cabeçalho do Início some ao rolar
- No Início, o cabeçalho **inteiro** (QINBLY, busca, sino **e** as abas Descobrir, Seguindo, Em alta, Vídeos, Lives, Perto de mim) sobe junto com a rolagem do feed e volta assim que a pessoa rola um pouco para cima. Trocar de aba por deslize lateral continua funcionando.
- Implementação: `NestedScrollConnection` na coluna do `FeedPage` (`headerOffset`/`headerPx`) e o bloco do cabeçalho numa `Column` que encolhe; tocar em "Início" para ir ao topo mostra o cabeçalho de novo.

## Busca de pessoas tolerante e perfil privado
- Perfil privado **continua aparecendo** na busca e tem página pública; a privacidade só restringe publicações, stories e quem segue (regra `qinbly_can_see_author`). Nada no servidor esconde o perfil na busca.
- Caso real: pesquisar "anarose" não achava `ana_rose` ("Ana Rose") porque o texto precisava casar exatamente. Agora a busca de pessoas (`searchUsers` e `searchAll`) também tenta uma versão tolerante (4+ letras): as letras digitadas com curinga entre elas, então "anarose" acha "ana_rose" e "Ana Rose". Resultados exatos vêm primeiro.

## Cor da curtida: vermelho clássico
- O coração de curtida é **todo preenchido** em vermelho clássico `#E53935` (`QinblyBrand.Liked`); sem curtir, só o contorno. Vale para a curtida da publicação, o coração animado do toque duplo e a curtida de comentário. O rosa antigo (`#FF4F9A`) não é mais usado para curtida.

## Publicação enviada na conversa: a própria mídia, sem cartão nem link
- Ao compartilhar uma publicação por mensagem, na conversa aparece **a foto ou o vídeo da publicação**, solto (sem balão azul/cinza em volta), com cantos bem redondos (26 dp). Vídeo toca ali mesmo ao apertar play; foto abre a publicação ao tocar. Não existe mais o cartão com "Abrir publicação" nem o link comprido.
- **Embaixo da mídia** ficam a foto pequena e o nome de quem publicou (com o selo de verificado, sem escrever "verificado") e, logo abaixo, a legenda. A hora e o check ficam do lado de fora.
- Publicação só de texto: o texto aparece num bloco cinza arredondado, com o perfil embaixo. O recado escrito junto aparece **embaixo** da publicação (depois do perfil e da legenda), para ficar claro a que publicação ele se refere.
- O texto enviado continua levando o link por baixo (versões antigas e fora do app), mas ele não é mostrado. Se a publicação não estiver no cache, ela é buscada pelo id.

## Tela "Enviar para" (compartilhar publicação) e envio para comunidades
- **Enviar para:** sem o letreiro de prévia da publicação no topo; só a barra "Buscar pessoas". As conversas recentes (ou pessoas encontradas) aparecem em **lista**: foto, nome, @ e um círculo à direita. Tocar na linha marca/desmarca, dá para marcar várias.
- Com alguém marcado, o rodapé vira o botão **"Enviar (N)"** (e o campo opcional de mensagem). Sem ninguém marcado, o rodapé mostra as ações: **Copiar link, Outros apps, Seu story e Comunidade**.
- **Comunidade:** abre uma tela cheia "Comunidades" com a busca "Buscar comunidade" e a lista das comunidades em que a pessoa participa (emoji, nome, membros, círculo de seleção). Marca várias e toca em **"Enviar (N)"** (fixo embaixo, acima dos botões do celular). Não tem campo de texto. Cada envio vira uma mensagem na comunidade com o texto e o link da publicação.
- Sem comunidade nenhuma: aviso "Você ainda não está em nenhuma comunidade", com a dica de entrar em uma na aba Comunidades.

## "Seu story" na tela Enviar para: abre a tela de criar story
- Tocar em **Seu story** não publica mais direto. O app baixa a foto/vídeo da publicação e abre a **tela de criar story** (a mesma do Criar) já com a mídia carregada; a pessoa edita (texto, figurinha, música…) e só publica ao compartilhar. Sair do editor sem compartilhar não posta nada.
- O crédito "@autor" vai junto no story (marca de origem na legenda, como antes), então "Ver original" continua funcionando. A marca é limpa ao publicar, ao sair do editor ou ao sair do Criar, para não vazar para o próximo story.
- Publicação só de texto (sem foto/vídeo) continua avisando que não tem mídia para o story.
- Implementação: `QinblyStoryRequest` (pedido de navegação) + `QinblyComposeSeed.pickedUri/origin/story`; o `PostPage` aplica a mídia ao abrir.

## Pessoas marcadas: embaixo do texto, sem a palavra "Com"
- Substitui a regra anterior ("em cima de tudo"). As marcações aparecem só como **@fulano @ciclano** (sem escrever "Com"), **logo embaixo do texto da legenda e antes da mídia**: foto/vídeo = legenda, marcações, mídia; cartão colorido = hashtags, marcações, cartão; texto simples ou enquete = marcações embaixo do texto. Componente: `QinblyTaggedUsersRow`.

## Hashtags e marcações sempre embaixo do texto (inclui cartão colorido)
- Padrão final: hashtags e pessoas marcadas ficam **sempre embaixo do texto**. Foto/vídeo: legenda, hashtags, marcações, mídia. **Cartão colorido: o cartão (texto) primeiro e, logo embaixo dele, as hashtags e depois as marcações.** Texto simples/enquete: marcações embaixo do texto. Substitui as regras anteriores de "em cima".

## Perfil privado de outra pessoa (17/11)
- O perfil continua aparecendo na busca e no perfil (nome, foto, bio, contadores); só o conteúdo é privado.
- Se a conta é privada (`profiles.is_private`, lido por `QinblyApi.isProfilePrivate`) e você NÃO segue (nem é você mesmo): no lugar das publicações aparece o cadeado com "Publicações privadas" + "Siga esta conta para ver as publicações e stories.", e o botão **Mensagem** some (sobra só Seguir).
- Depois que você segue (ou o pedido é aprovado), o botão Mensagem e as publicações aparecem normalmente.

## Ícones de linha sem cor: Perto de mim e categorias do perfil (17/11)
- **Perto de mim:** as categorias (Tudo, Vendas, Doações, Achados, Eventos, Serviços, Avisos), a tela vazia (Vender, Doar, Achados, Eventos) e o menu lateral usam ícone de linha (`Icons.Outlined.*` via `EmojiIcon`) cinza, sem bolinha colorida. Na categoria selecionada o ícone acompanha a cor do texto.
- **Categorias do perfil:** cada categoria tem um ícone de linha que combina com ela (`qinblyCategoryIcon` em Extras.kt: Games = controle, Esportes/Futebol = bola, Música = nota, Carros = carro...). Aparece na tela de editar perfil (nos chips), no seu perfil e no perfil dos outros (`QinblyCategoryLine`). Categoria nova sem ícone cai no `Tag`.
- O texto salvo no banco continua igual ("Games 🎮"); o emoji é cortado só na hora de mostrar (`qinblyCategoryName`).

## Hashtags usadas mas escondidas na publicação (18/11)
- Na hora de criar/editar, as hashtags continuam num campo próprio e seguem salvas no fim da legenda (a busca e o "Em alta" continuam achando a publicação por elas).
- Depois de publicada, o bloco final de #hashtags **não aparece** em lugar nenhum: feed (foto/vídeo, texto simples, cartão colorido) e visualizador de vídeo mostram só a legenda. Perfil, comentários e chat já mostravam só a legenda.
- Ao editar a publicação, as hashtags aparecem de volta no campo para poder mudar.
- Esta regra substitui "hashtags sempre embaixo do texto" (17/11). As pessoas marcadas continuam embaixo do texto. #hashtags escritas no meio do texto continuam aparecendo.

## Perto de mim só com publicações da região + Mercado e avisos separado (18/11)
- **Perto de mim (aba do Início):** agora é um feed de publicações de quem mora na sua cidade. O botão "Cidades próximas" amplia para o mesmo estado. Usa o mesmo card, visualizador, comentários e envio do feed. A cidade vem de `profiles.city` (a pessoa só entra na lista depois que o app grava a cidade dela; isso é feito ao abrir o Perto de mim ou o Mercado e ao abrir o Início). Conta privada só aparece se você segue; suas publicações e as de quem você bloqueou não aparecem. Sem distância em km (não guardamos coordenadas).
- **Mercado e avisos:** saiu do Perto de mim e fica em **Menu (três tracinhos) > Mercado e avisos**. É a mesma tela de antes (vendas, doações, achados, eventos, serviços e o Mural de avisos), em tela cheia com seta de voltar. Pede a localização como antes.
- Busca de publicações da região: `QinblyApi.getNearbyPosts` (consulta `posts` ligada ao `profiles.city` e busca cada publicação completa).

## Mercado e avisos: cestinha no topo do Início (18/11)
- Um símbolo só (cestinha de mercado, `Icons.Outlined.ShoppingBasket`, opção N escolhida no esboço) no canto esquerdo do cabeçalho do Início, do lado oposto da busca e do sino. Toque abre o **Mercado e avisos** em tela cheia (`QinblyMarketDialog`), com tudo dentro: Tudo, Vendas, Doações, Achados, Eventos, Serviços e Avisos, e o "+" para anunciar.
- O atalho em Menu > Mercado e avisos continua existindo e abre a mesma tela.
- Ainda não existe: pontinho de "novidade" no mercadinho e a tela de quadros (Vendas/Doações/...) do esboço; por enquanto abre a tela com as categorias em cima.

## Vídeo no feed sem pausar (18/11)
- No feed (card da publicação) o vídeo toca sozinho, sem som, e **não tem mais toque para pausar nem botão de play**: tocar no vídeo abre o visualizador, e é lá dentro que a pessoa pausa. O botão de som continua. (`InlineVideoPlayer(pausable = false)` só no `QiblyPublicationVideo`; o vídeo solto no chat continua com o play.)

## Perto de mim sem os botões "Na cidade / Cidades próximas" (18/11)
- Saíram os dois botões. A lista é uma só: primeiro as publicações de quem é da sua cidade, depois as de quem é das cidades próximas (mesmo estado), sem repetir. Continua o nome da cidade no topo e puxar para baixo atualiza.

## Perto de mim: sem título da cidade, estado inteiro (18/11)
- Saiu a linha com o nome da cidade no topo; só fica a aba "Perto de mim". A lista continua: sua cidade primeiro e depois o estado inteiro (ex.: Rondônia), agora achando o estado com ou sem acento e em qualquer parte do texto da cidade. Só aparece quem já tem a cidade gravada no perfil (acontece quando a pessoa abre o Perto de mim).

## Aba com o lugar de cada pessoa + cidade nas publicações (18/11)
- A última aba do Início, no lugar de "Perto de mim", mostra o lugar de quem está usando (ex.: "Vilhena, RO"), com o estado em sigla. Sem localização/cidade guardada, continua "Perto de mim". A aba fica um pouco mais larga (peso 1,35) e sempre numa linha só.
- Abreviação para nomes grandes (`qinblyShortPlace`, limite 18 letras): Santa→Sta., Santo→Sto., São→S., Doutor→Dr.; se ainda passar, tira "do/da/dos/d'..." no fim ("Santa Luzia d'Oeste" → "Sta. Luzia"); depois vira inicial no primeiro nome ("Nova Brasilândia" → "N. Brasilândia"); por último corta com "…". "Vale do Anari, RO" e "Porto Velho, RO" não mudam.
- Nas publicações do Perto de mim, ao lado da hora aparece a cidade e o estado de quem postou ("Agora · Vilhena, RO"), com o nome da cidade inteiro (`qinblyFullPlace`). Vem de `profiles.city` (`PostSummary.authorCity`); no resto do app não aparece.

## Cor própria: índigo no lugar do azul do Facebook (18/11)
- O azul de destaque (#1877F2, igual ao do Facebook) virou **índigo `#4B5BFF`**, parente do roxo do logo (`#7C4DFF`). Troca feita em todos os arquivos: botões, links, hashtags, @menções, abas, caixas de texto em foco, ícones.
- Variantes: claro `#8E99FF` (antes `#6AA6F8`), escuro `#3A49E6` (antes `#1565D8`), tema branco com botão "Editar perfil" `#EDEFFF` com texto `#3342D6`, faixa/fundo suave `#DDE1FF`/`#EEF0FF` (claro) e `#232B6B`/`#1A2054` (escuro).
- Para mudar a cor da marca de novo, é só trocar `QinblyBrand.Accent` e as variantes em `MainActivity.kt` (e os poucos arquivos que ainda têm a cor escrita direto: Capsule, Extras, Marketplace, PhotoPicker, PhotoVideoEditor, Stories, TextStory, Music, ModernUi, Collective).

## Hashtags e marcações em índigo mais claro
- `QinblyBrand.Mention` (hashtags e @marcações nas legendas, comentários e campos de texto) agora é `#5865DE`, mais clara.
- Botões (Seguir, Participar etc.) e o resto do app continuam no índigo `#4B5BFF`.
- Para mudar só a cor das hashtags e marcações, troque apenas `QinblyBrand.Mention`.

## Azul do app #2F68E0 (opção 2) e ajustes da busca
- Cor principal do app trocada de índigo `#4B5BFF` para azul `#2F68E0` em todos os arquivos (botões, ícones, hashtags, @, letreiros).
- Variantes: `AccentDeep` (`#3A72E8` escuro / `#2558C4` claro), `AccentLight` e links claros `#7FA6F2`, `AccentLine` (`#1E3A70` / `#D6E4FF`), `AccentSoft` (`#162447` / `#E8F0FF`), degradês `#1F4FB0`.
- `QinblyBrand.Mention` (hashtags e @) agora é a mesma cor principal `#2F68E0`.
- Busca: "Limpar" na cor do texto (branco no escuro); "Suas hashtags" sem fundo, sem contorno e sem contagem (3x), só a letra azul, separadas por tracinho vertical fino (7dp de cada lado).
- Logo e botão Criar continuam roxos. Para trocar o azul de novo, mexa em `QinblyBrand.Accent`.

## Rolagem da busca até o fim
- Na tela "Buscar pessoas e publicações", as duas listas (sugestões e resultados) agora somam a altura da barra de navegação do celular na folga do fim (`20.dp + WindowInsets.navigationBars`), para o último item não ficar escondido atrás dela.

## "Limpar tudo" da busca em branco
- O botão "Limpar tudo" dos recentes (topo da busca) agora usa a cor do texto (branco no escuro), igual ao "Limpar" de "Suas hashtags".
- Correção: dentro da janela da busca os insets do Compose chegam zerados, então a folga do fim usa o padrão do app `qinblyNavBarPadding()` (a mesma medida usada nas outras telas cheias) em vez de `WindowInsets.navigationBars`.

## Anel de story na foto do perfil
- Quem tem story novo (ainda não visto por você) ganha o anel azul (`QinblyBrand.Accent`) na foto: no cabeçalho das publicações (feed, vídeos) e na página do perfil, própria e de outras pessoas.
- Quando a pessoa abre e fecha o story, o anel apaga (some por completo, igual à faixa de stories). Quem você silenciou não mostra anel.
- Estado único em `QinblyStoryRings` (MainActivity.kt): atualizado quando a bandeja de stories carrega e a cada story que aparece no visualizador.
- `QiblyAccountAvatar` ganhou o parâmetro `storyRing` (padrão desligado); só as publicações ligam. Chat, notificações e listas continuam sem anel.
- Limite: só aparece anel para quem está na sua bandeja (você e quem você segue, mais stories coletivos).

## Topo do Início: cartão que desliza entre Pergunta do dia e Desafio da semana
- Saíram do topo do Descobrir a saudação ("Boa tarde, nome") e as novidades de quem você segue.
- Pergunta do dia e Desafio da semana agora são um cartão só, no tamanho de antes (altura 112dp), que desliza para o lado (`HorizontalPager`). Abre na pergunta; o desafio aparece um pedaço na beirada; dois pontinhos mostram a posição.
- Tocar na pergunta abre o Criar com #perguntadodia; tocar no desafio abre a hashtag; "Participar" abre a câmera com a hashtag do desafio (tudo como antes).
- Código em `QinblyHomeHeader` (MainActivity.kt).

## Bolinha vermelha na aba Seguindo
- A aba "Seguindo" (barra de cima do Início) mostra uma bolinha vermelha (`#E53935`, 10dp, com borda da cor do fundo) no canto do ícone quando alguém que você segue postou depois da última vez que você abriu essa aba. Posts seus não contam.
- Ao abrir a aba Seguindo, a bolinha apaga e só volta com publicação nova. O momento da última visita fica em `following_tab_seen_at` (no primeiro uso começa sem bolinha).
- Substitui o texto "N novidades de quem você segue", que saiu do topo do Descobrir.

## Correções: anel que não apagava, toque na foto e cartão do topo
- Anel de story (faixa de stories e fotos das publicações/perfil): agora vale também o que você já abriu neste aparelho (`seen_story_ids`, guardado a cada story que aparece no visualizador), não só o "visto" do servidor. Antes só marcava o último story da fila ao fechar, e o anel de quem você viu no meio ficava aceso.
- Na publicação, tocar na **foto** com anel abre o story da pessoa (`QinblyStoryRings.openRequest`); tocar no **nome** continua abrindo o perfil. Sem anel, a foto abre o perfil como antes. Em abas sem a faixa de stories (Seguindo, Em alta...) um host invisível (`QinblyStoriesRow(hostOnly = true)`) mostra o visualizador.
- Cartão Pergunta do dia/Desafio: sem pedaço do próximo na beirada (`contentPadding` 14dp nos dois lados e `pageSpacing` 16dp).

## Pesquisas, hashtags e stories vistos são de cada conta
- Antes, o histórico "Recentes" da busca, as "Suas hashtags" e os stories já vistos ficavam guardados no aparelho inteiro, então apareciam em todas as contas que entravam nele.
- Agora cada conta tem o seu: `qinbly_search_history:<usuário>`, `qinbly_hashtags_<usuário>` (arquivo próprio) e `seen_story_ids:<usuário>`. A chave vem de `qinblyAccountKey(prefs)` (usuário em minúsculas).
- Dados antigos compartilhados deixam de ser lidos; cada conta recomeça vazia.

## Check-up: o que é da conta fica só na conta
Regra: tudo que a pessoa usa ou escolhe numa conta vale só naquela conta; só o que é do aparelho (token, tema, tamanho da letra, permissões, atualização do app) é compartilhado.
- Novo `AccountPrefs.kt`: `QinblyScopedPrefs` / `.byAccount()` embrulha a sessão (`qinbly_session`) e guarda por usuário (`chave@usuário`) estas chaves: `following_*` (quem você segue, cache local), `muted_*`, `pinned_*`, `inbox_hidden_*` (conversas silenciadas, fixadas e ocultas), `draft_text*` (rascunho), `hidden_words`, `story_default_audience`, `story_muted`, `story_mute_migrated`, `market_city`, `market_recent_cities`, `qinbly_live_search_history`, `notif_seen_ids`, `bg_notified*` (avisos em segundo plano), `setting_notice_*`, `setting_notifications`, `setting_activity_status`, `setting_comments`. Todas as telas e o NotifyWorker usam `.byAccount()`.
- `qinblyAccountFile(context, base)`: arquivos próprios por conta para `qinbly_pins` (mensagem fixada no chat), `qinbly_hidden` (mensagens apagadas para mim) e `qinbly_stickers` (favoritas e recentes). Já feitos antes: histórico de busca, hashtags usadas e stories vistos.
- Dados antigos (de antes da separação) vão para a primeira conta que abrir cada chave; as outras contas começam vazias.
- Ao trocar ou sair da conta: `QinblyStoryRings.clear()` (anéis de story) e as palavras ocultas passam a ser lidas sempre da conta aberta.
- Já estavam certos: curtidas/salvos/seguindo em memória (chave por conta), cache de leituras da API (por id do usuário), cache do feed e snapshot do perfil (limpos ao trocar).
- Continuam do aparelho (de propósito): `setting_theme`, `setting_large_text`, `setting_reduce_motion`, `setting_autoplay`, `setting_save_default`, `setting_location_enabled`, `name_font_style`, `app_font_style`, `device_place`, histórico de emoji dos comentários, `permissions_asked_v2` e dados de atualização do app.

## Atualização dentro do app (sem arquivo na pasta Downloads)
- `RemoteUiActivity`: o botão "Baixar e instalar atualização" agora baixa o APK direto para uma sessão do instalador do Android (`PackageInstaller`), sem gravar arquivo em Downloads. Mostra o progresso em % na própria tela e abre a confirmação do Android ao final.
- Em Android 12+ pede instalação sem confirmação extra quando o sistema permitir (`USER_ACTION_NOT_REQUIRED`); caso contrário aparece a confirmação normal "Atualizar".
- Se a permissão "Instalar apps desconhecidos" estiver desligada, abre a tela de permissão e continua a atualização sozinha ao voltar (`pending_update_apk_url`).
- Se o instalador interno falhar por qualquer motivo, cai no caminho antigo (DownloadManager + instalador do Android).

## Tipografia refinada (opção A, leve) no app inteiro
- Escala geral das letras: `QINBLY_TEXT_SCALE = 0.9f` (MainActivity.kt), aplicada no `LocalDensity` raiz junto do "Texto grande" (que continua multiplicando por cima). Vale para todas as telas, diálogos e abas; o workflow continua somando +3sp nos `fontSize` explícitos e a escala reduz o resultado em ~10%.
- Pesos: `FontWeight.Bold` virou `SemiBold` em todo o app (nomes, números, títulos). `ExtraBold`/`Black` (logo) ficaram como estavam; o peso "negrito" escolhido pelo usuário nos stories de texto (`qinblyFontWeight`) continua Bold.
- Cinza secundário (modo escuro): `#A9B5C8` -> `#BAC4D4` em todos os pontos (QiblyTheme.secondary, Muted e usos diretos) para as letras ficarem menos apagadas.
- Para ajustar o tamanho geral depois, basta mudar `QINBLY_TEXT_SCALE` (ex.: 0.85f menor, 0.95f maior).

## Textos de apoio brancos e abas do perfil só com ícone preenchido
- Modo escuro: cinza secundário (`QiblyTheme.secondary`, `Muted` e todos os usos diretos de `0xFFBAC4D4`) virou o mesmo branco do texto principal (`#F2F4F8`). A hierarquia passa a depender só de tamanho e peso. Modo claro não mudou. Botões de seguir/participar e hashtags/@ continuam azuis (`#2F68E0`).
- Abas do perfil (próprio e público): a aba selecionada não tem mais azul, sublinhado nem borda; só o ícone troca de contorno para preenchido (grade, vídeo, foto, salvos), tudo em branco. Texto das abas igual em todas.

## Aviso de nova atualização (notificação)
- `NotifyWorker.kt`: a verificação em segundo plano (a cada ~15 min) agora também consulta a última versão no GitHub (no máximo 1x por hora, `qinbly_update_check`). Se o código da versão publicada (major*1.000.000 + minor*10.000 + patch, igual ao workflow) for maior que `BuildConfig.VERSION_CODE`, mostra uma notificação "Nova atualização disponível" uma única vez por versão (`notified_code`).
- Respeita "Notificações" e "Avisos do sistema" (`setting_notice_system`); funciona mesmo sem conta logada; não avisa com o app aberto.
- Canal próprio "Atualizações do app" (`qinbly_update`). Ao tocar, abre `RemoteUiActivity` (tela de atualização, com download e instalação por dentro do app).

## Tela de atualização no visual novo
- `RemoteUiActivity`: a tela de atualização usa o visual do app (fundo `#171719`, cartão `#262628`, botão azul `#2F68E0`, marca QINBLY roxa, texto branco). Sai o roxo/rosa antigo e o "V".
- Estados: versão disponível (instalada x disponível + novidades + "Atualizar agora"), baixando (barra e % no botão), "Você está na versão mais recente" (compara o código da versão com `BuildConfig.VERSION_CODE`, botão "Verificar de novo") e erro/sem internet (com "Tentar novamente" e link para os downloads no GitHub).
- Instalação: a sessão do instalador já pede `USER_ACTION_NOT_REQUIRED` (Android 12+), então, quando o sistema permitir, instala logo ao terminar de baixar sem a pergunta "Atualizar?". O Android só dispensa a pergunta se o QINBLY foi o instalador da versão atual e com o app fora de uso; a primeira atualização por dentro do app ainda pergunta.

## Criar abre na câmera; Texto e Enquete vão para o início
- Botão "+" (barra de baixo): não abre mais a tela de 4 cartões; abre direto a câmera do app (`createEntryMode = "camera"; tab = 2`). A tela antiga (`showCreateOptions`) ficou sem uso.
- Câmera (`PostPage`): fileira de chips agora é só **FOTO** e **VÍDEO**. No modo VÍDEO aparecem, acima dela, os tempos **15 s, 20 s, 60 s e 5 min** (limite da gravação: 15/20/60/300 s; o 10 min saiu). O chip TEXTO da câmera (texto por cima do fundo) foi removido; texto sobre a foto continua no editor, depois de tirar a foto. As variáveis `camTextMode`/`camTextEditing` ficaram, sempre desligadas.
- Galeria: continua pela miniatura embaixo à esquerda da câmera (seletor do sistema).
- Início (aba Descobrir): nova barra `QinblyHomeComposer` entre os Stories e a Pergunta do dia, com um lápis (no lugar da foto de perfil), "Escrever algo..." e o botão **Enquete**. Tocar na barra ou no lápis abre o texto longo; o botão Enquete abre a enquete (via `onCreateWith`).
- Esboços aprovados guardados em `/home/claude/esbocos/` (sessão), fora do repositório.

## Stories em bolinha (faixa do início)
- `QinblyStoryBubble` (MainActivity.kt) substitui os cartões grandes (168x306) da faixa de stories: bolinha de 88 dp com a foto de perfil (76 dp), anel de 3 dp e nome embaixo (`QinblyStoryCard` ficou sem uso).
- Anel azul = story ainda não visto; cinza escuro = silenciado; roxo apagado = já visto. Sem story: sem anel colorido.
- "Seu story": primeiro da faixa, com + branco no canto. Tocar na bolinha abre o seu story (ou o menu de criar, se você não tiver); tocar no + sempre abre o menu (Story, Texto, Música, Story coletivo, Clima do dia, Cápsula do tempo).
- O emoji do "Clima do dia" aparece num selinho no canto superior da bolinha. O selo "Coletivo" e as fotos de quem participa deixaram de aparecer na faixa (continuam no visualizador).
- Para mudar o tamanho: 88.dp (anel), 76.dp (foto) e 96.dp (largura do item) em `QinblyStoryBubble`.

## App reabre sozinho depois de atualizar
- O Android fecha o app quando termina de instalar uma atualização. Agora `UpdateRelaunchReceiver` (novo, declarado no AndroidManifest para `MY_PACKAGE_REPLACED`) reabre o `MainActivity` logo após a instalação.
- Só age se a atualização foi iniciada por dentro do app: `RemoteUiActivity` grava a marca (`qinblyMarkRelaunchAfterUpdate`, prefs `qinbly_update_relaunch`) ao confirmar o download/instalação, e ela vale por 60 minutos. Atualização feita por fora (Downloads, outro instalador) não reabre.
- Limite do Android 10+: o sistema pode bloquear abrir telas a partir do segundo plano. Se bloquear, o app mostra a notificação "QINBLY atualizado - toque para abrir" (some em 30 s). Não usa permissão de sobreposição de tela.

## Foto da câmera abria pequena no meio de um quadro largo
- Causa: o CameraX grava o JPEG "deitado" com aviso de rotação (EXIF) e o editor lê só largura x altura do arquivo (`BitmapFactory` com `inJustDecodeBounds`), que ignora o aviso. A proporção "Original" saía larga (~2:1) e a foto em pé aparecia pequena, com fundo desfocado dos lados.
- Correção: `qinblyMakeUpright(file)` (FaceFilters.kt) regrava a foto já em pé logo após `takePicture`, fora da thread principal, antes de abrir o editor (`takePhoto` em MainActivity.kt). Também cobre o espelhamento da câmera da frente. Se falhar, mantém o arquivo original.

## Foto da câmera com mais qualidade
- A foto saía com ~960x1920 px (baixa para o celular) porque a câmera usava o modo "menos demora" e a resolução padrão do CameraX.
- Agora `ImageCapture` usa `CAPTURE_MODE_MAXIMIZE_QUALITY`, `setJpegQuality(100)` e `ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY`. Se o aparelho não aceitar essa combinação (prévia + foto + vídeo juntos), o app tenta de novo no modo simples antigo (`bindUseCases(false)`), então a câmera não deixa de abrir.
- Efeito colateral: tirar a foto pode demorar uns instantes a mais.

## Teclas de volume tiram a foto na câmera
- Enquanto a câmera do app está aberta (sem o editor por cima), as teclas de aumentar e diminuir volume funcionam como o botão de tirar foto (ou começam/param a gravação, no modo vídeo). O volume do celular não muda nessa hora.
- Implementação: `QinblyCameraKeys` (MainActivity.kt) + `dispatchKeyEvent` consome a tecla só quando `active`; `PostPage` liga/desliga `active` e chama `onShutterTap()`. Fora da câmera, as teclas continuam só ligando o som dos vídeos mudos.

## Toque na foto de perfil sem quadrado transparente
- `QiblyAccountAvatar` (usada na faixa de stories e em todo o app) tinha o brilho de toque do Android num quadrado do tamanho da foto, que aparecia em volta da bolinha. Agora o toque é cortado em círculo (`clip(CircleShape)` antes do `clickable`), então o efeito fica redondo, igual à foto.

## Sem fundo/brilho transparente ao tocar, em todo o app
- Raiz do app (`QinblyApp`): `LocalIndication` = `QinblyNoIndication` (indicação vazia) e `LocalRippleConfiguration` = `null`. Assim nenhum `clickable`, botão, linha de lista, aba ou ícone desenha o brilho/quadrado transparente ao ser tocado. Vale também para diálogos e menus (herdam os locais).
- Efeito colateral: os botões não "acendem" ao toque; a resposta visual fica só na ação em si (abrir tela, trocar ícone etc.).
- Telas web do app (atualização, `RemoteUiActivity`): `-webkit-tap-highlight-color:transparent`.
- Para voltar o brilho em algum ponto específico, basta dar `indication = ripple()` naquele `clickable`.

## Filtros "de verdade" (estilo Instagram) e foto mais nítida
- Pesquisa: os filtros de foto do Instagram misturam contraste, saturação, tom quente/frio, fade (preto levantado), vinheta (bordas escuras), granulado e nitidez; não são só troca de cor.
- Novo arquivo `LookFinish.kt`: `QinblyFinish(vignette, grain, sharpen)` por filtro (`qinblyFinishOf`, reduzido pela intensidade), `qinblySharpen` (máscara de desfoque com desfoque de caixa), `qinblyApplyFinish` (nitidez, granulado e vinheta).
- 10 filtros novos no começo da lista (depois de "Original"): Nítido, Vintage suave, Quente vibrante, Luminoso, Retrô dourado, Dramático Pro, Lo-Fi, Poeirento, Lua, Tinta. Os filtros antigos (Filme, Vintage, Cinema, Noir, Polaroid, Kodak, Lomo, VHS, Dramático, Alto contraste, Prata, Sépia...) também ganharam vinheta/granulado/nitidez; os demais ganham só um pouco de nitidez.
- Foto final (`qinblyRenderEditedPhoto`): o acabamento é aplicado depois do recorte/cor e antes de texto, stickers e desenho. Prévia do editor: mostra a vinheta (nitidez e granulado só aparecem na foto final).
- Prévia de vídeo: `QinblyOriginalFilterNames` (11 nomes antigos) substitui o antigo `drop(12)`, porque a ordem da lista mudou.
- Foto da câmera: depois de tirar, além de regravar em pé, recebe nitidez (`qinblySharpenFile`, 0,55; limita a ~6 megapixels para não faltar memória).

## Anel do "Seu story" apaga depois de visto
- Antes o anel do próprio story ficava sempre azul. Agora usa a mesma regra dos outros (`QinblyStoryRings.entryUnseen`): azul enquanto há story seu não visto (um story novo acende de novo), roxo apagado depois que você abre, cinza se não há story.

## Anel de story na cor do logotipo
- `QinblyBrand.RingBrush`: degradê roxo (`Logo`) para rosa (`Pink`), o mesmo do nome QINBLY no topo. Usado no anel de story não visto: bolinhas do Início (`QinblyStoryBubble`), foto de perfil nas publicações (`QiblyAccountAvatar`) e capa do perfil (próprio e público). Visto continua roxo apagado e silenciado cinza.

## Barra do topo (menu), "+" do story centralizado e anel mais fino
- Início: o ícone de menu (três tracinhos) ocupa o lugar da cestinha e abre o mesmo menu/Configurações do restante do app (`onMenu` = `showSettings = true`). A cestinha (Mercado e avisos) passou para a direita, entre a lupa e o sino.
- Bolinha de story: o anel passou de 3 dp para 2 dp (avatar 80 dp) e o botão "+" saiu do canto inferior direito para o centro da borda de baixo; o nome "Seu story" desce um pouco (`padding top 13 dp` só quando há "+"). Anel da foto nas publicações: 2,5 dp para 2 dp.

## Tela Criar (menu do "+" do Seu story): cartões em rolagem lateral e galeria
- Saíram o cartão "Story", a lista "Mais" e a opção "Ver meu story". Os atalhos viraram cartões quadrados (82 dp, só ícone e nome) numa `LazyRow`: Texto, Música, Story coletivo, Clima do dia, Cápsula do tempo (cabem uns 4 na tela; o 5º aparece pela metade).
- Abaixo, "Galeria": grade de 3 colunas (fotos 3:4) com `loadRecentMedia` e `MediaThumb` (os mesmos do story coletivo). O primeiro quadradinho é a câmera do app. Tocar numa foto usa `qinblySetPickedMedia` e abre direto o editor padrão. Sem permissão de fotos, mostra "Permitir acesso" e a opção de escolher pelo seletor do sistema.
- Cartão "Clima do dia" removido do menu Criar (a tela do clima continua no código, sem atalho).

## Clima do dia removido do app
- Removidos: tela/seleção do clima (`QinblyMoodDialog`, `qinblyFetchMoods`, `QinblyMood` em Extras.kt), o passo "mood" do menu Criar e o emoji de clima nas bolinhas e nos cartões de story. As colunas `mood_*` da tabela `profiles` ficam no Supabase sem uso (nada foi alterado no servidor).

## Tela Criar: ajustes (título, cartões maiores, galeria acima dos botões)
- Título "Criar" + versão viraram "Novo story" centralizado (sem número de versão); "Galeria" também centralizada.
- Cartões: largura = (largura da tela - 32 - 24) / 4, para quatro ocupando a tela, altura +12 dp, ícone 30 dp.
- A grade da galeria usa `navigationBarsPadding()` para terminar acima dos botões do celular.

## Tela de atalhos (menu de três tracinhos do Início)
- O menu de três tracinhos agora abre `QinblyShortcutsScreen` (tela cheia, em lista, SEM cartões ou caixas em volta de nada; preferência do usuário para telas de configuração/atalhos). Antes abria as Configurações.
- Perfil no topo (foto + "Ver meu perfil"); EXPLORAR: Vídeos, Em alta, Lives, Perto de mim (trocam a aba do Início) e Mercado e avisos; SEUS ESPAÇOS: Comunidades, Salvos (abre o Perfil já na aba Salvos via `QinblyProfileJump`), Cápsula do tempo, Convidar amigos (compartilhar). No fim, Configurações (abre a tela de configurações de sempre; Ajuda, alternar de conta e sair continuam lá).

## Atalhos do mercado e "Achados" removido
- Tela de atalhos: o item "Mercado e avisos" saiu de EXPLORAR e virou o grupo NA SUA CIDADE com Vendas (a mesma cestinha do topo do Início), Doações, Eventos, Serviços e Avisos. Cada um abre o Mercado já na categoria (`QinblyMarketDialog(initialCategory)` / `QinblyNearbyHub(initialCategory)`). A cestinha do topo continua abrindo em "Tudo".
- "Achados" removido do Mercado: chip, subfiltro Achados/Perdidos, atalho de publicar e opção "Achado ou perdido". Anúncios antigos de achados/perdidos deixam de aparecer em "Tudo" (filtro `kind=not.in.(achado,perdido)`); nada foi apagado no servidor.

- Atualização: o "Achados" voltou ao Mercado (chip, subfiltro Achei/Perdi, publicar achado, anúncios antigos) e entrou na lista NA SUA CIDADE dos atalhos, entre Doações e Eventos (ícone de lupa, abre o Mercado em Achados).
- Na lista de atalhos, o item "Vendas" passou a se chamar "Mercado" (continua abrindo o Mercado na aba Vendas).

## Cada atalho do mercado abre só a sua tela
- `QinblyNearbyHub(single = true)`: sem a fileira de abas (Tudo, Vendas, Doações...); o título vira o nome da categoria (Mercado, Doações, Achados, Eventos, Serviços, Avisos). Achados mantém o filtro Achei/Perdi. A cestinha do topo do Início agora abre só o Mercado (Vendas). A tela "Mercado e avisos" de Configurações continua completa, com todas as abas.

## Mercado: menu de três tracinhos (lista simples)
- Menu lateral do Mercado agora é uma lista sem cartões: CATEGORIAS (Mercado, Doações, Achados, Eventos, Serviços, Avisos), SEU MERCADO (Meus anúncios, Salvos) e AJUDA (Dicas de segurança, Regras do Mercado).
- Ícones e letras brancos, títulos de grupo em cinza. Removidos "Trocar cidade", "Novo anúncio", "Voltar" e os cartões do topo.
- Tocar numa categoria troca a tela para aquela categoria.

## Padrão: tudo fica ACIMA dos botões do celular
- Toda tela cheia, menu lateral ou lista dentro de QinblyDialog (decorFitsSystemWindows = false) usa `qinblyNavBarPadding()` como espaço no fim da tela/lista. `navigationBarsPadding()` sozinho não funcionou nesses diálogos.
- Aplicado em: Atalhos do Início, menu lateral do Mercado e grade da galeria do "Novo story".
- Telas novas já nascem assim; ao mexer em tela antiga que use só `navigationBarsPadding()` dentro de diálogo, trocar.

## Cápsula do tempo removida
- Removida do app: botão no "Novo story", item nos Atalhos do Início, aviso de cápsula que chegava na tela inicial e o arquivo Capsule.kt inteiro.
- Os dados antigos no banco não foram mexidos.

## Editor da câmera: aviso ao salvar
- Ao tocar em "Salvar" no editor, o aviso ("Foto salva na galeria" / "Vídeo salvo na galeria" / erro) agora é um aviso do sistema, que aparece por cima da tela cheia. Antes usava o aviso do app, que ficava escondido atrás do editor.

## Avisos: agora aparecem em todo o app
- O aviso central do app (usado por salvar perfil, trocar foto, sair da conta, erros de rede, etc.) era guardado mas nunca desenhado. Agora cada aviso aparece como aviso do sistema, por cima de qualquer tela.
- Avisos iguais em menos de 1,5 s não se repetem.
- Telas que já tinham o próprio aviso (Stories, texto, coletivo, editor) continuam como estavam.

## Atalhos: sem coloração
- Na tela de Atalhos do Início, ícones e títulos de grupo (Explorar, Na sua cidade, Seus espaços) perderam as cores. Ícones usam a cor do texto e os títulos de grupo ficam em cinza.

## Atualização: nunca baixa APK para o celular
- Removido o caminho antigo (DownloadManager, "Download concluído" na barra de notificações e arquivo na pasta Downloads). Antes, qualquer falha no instalador interno caía nele.
- Agora a atualização é só dentro do app: baixa direto para o instalador do Android. Se a conexão falhar, tenta de novo sozinho até 3 vezes; se ainda falhar, mostra o motivo e pede para tocar em Atualizar de novo.

## Lives de verdade (LiveKit)
- Aba Lives: lista de quem está no ar atualiza a cada 8 s; "Começar uma live" transmite câmera e microfone; tocar numa live abre o vídeo.
- Pede câmera e microfone ao iniciar. Pausar desliga câmera e microfone sem sair da sala. Virar câmera e silenciar funcionam durante a live.
- Efeitos de rosto e cor só na prévia (antes de iniciar). Detalhes e configuração em LIVES-LIVEKIT.md.

## Backend próprio no Cloudflare (saindo do Supabase)
- Banco: Cloudflare D1 `qinbly` (51 tabelas espelhadas + `accounts`, `refresh_tokens`, `app_secrets`). Arquivos: R2 `qinbly-files`. Bindings `DB` e `FILES` no projeto Pages `qinbly`.
- Código em `functions/`: `_lib/` (núcleo, emulação do PostgREST, gateway da API), e rotas `functions/v1/*` (gateway + verificação + login), `rest/v1/*`, `storage/v1/*`, `auth/v1/*`. Mantêm os mesmos caminhos de antes, então o app só precisa trocar o endereço do servidor.
- Login: Google (verificado pelo token do Google) e e-mail/senha. Sessões novas: quem estava logado precisa entrar de novo uma vez.
- Fotos/vídeos: URLs públicas no mesmo formato `/storage/v1/object/public/post-media/...`.
- Limites conhecidos: não há serviço de e-mail ("esqueci a senha" não envia nada); a conta de e-mail/senha antiga não tem senha importada.
- Fotos e vídeos antigos (138 arquivos) não foram copiados: o Supabase bloqueou o acesso aos arquivos (erro 402, limite do plano gratuito). Posts antigos com foto ficam sem a imagem; o conteúdo novo é salvo no Cloudflare.

## App agora usa o Cloudflare
- O app passou a falar com `https://qinbly.pages.dev` (API, login, fotos e vídeos) em vez do Supabase. Quem estava logado precisa entrar de novo com o Google uma vez.

## Foto/vídeo salvo na galeria
- No app, o aviso é só "Foto salva na galeria" / "Vídeo salvo na galeria" (sem nome do app).
- O arquivo salvo na galeria do celular pode levar o nome do app: `qinbly-<número>.jpg`/`.mp4`, na pasta Qinbly.

## Live em tela cheia (quem transmite e quem assiste)
- Ao entrar numa live, a tela cobre tudo: some a barra QINBLY, as abas e o menu de baixo. Os botões e o campo de comentar ficam acima dos botões do celular e sobem com o teclado.
- Cápsula de cima: foto, nome e curtidas (♥). Para quem assiste, tem o botão **Seguir** / **Seguindo**. Do outro lado, as fotinhas de quem assiste com o número.
- Curtir é tocar na tela (um toque ou vários). Cada toque solta um coração que sobe e soma nas curtidas; quem transmite vê os corações dos outros. Não há mais botão de coração nem duplo toque.
- Comentários em balões foscos, com o aviso "fulano entrou na live". Campo "Diga algo…" e botão de compartilhar para quem assiste; quem transmite mantém virar câmera, microfone, pausar e encerrar.
- Sem cidade na tela. Resumo ao encerrar mostra as curtidas.
- Curtidas e entradas trafegam pelo próprio LiveKit (mensagens de dados); quem assiste agora pode enviar dados (functions/api/live.js).

## Live: "Antes de começar" com Câmera ou Jogos
- A tela virou uma folha única, sem cartões: foto + título com lápis, **O que mostrar** (Câmera | Jogos), **Quem pode assistir** (Todos | Seguidores), Comentários, Presentes e **Iniciar live** acima dos botões do celular.
- **Jogos** transmite a tela do celular no lugar da câmera. Ao tocar em Iniciar live o Android pede permissão para gravar a tela (aviso do sistema, não dá para tirar); depois da contagem, é só abrir o jogo. O microfone continua ligado. O som do jogo não vai junto. Quem assiste vê tudo que aparecer na tela, inclusive avisos.
- No modo Jogos some o botão de virar câmera; pausar esconde a imagem e silencia o microfone.
- Saiu o botão de filtro/efeitos da prévia (fica para depois).
- A contagem antes de entrar no ar passou de 3 para 5 segundos.

## Perfil reorganizado e troca de conta no @
- Cabeçalho do perfil: foto menor à esquerda (selo de verificado no canto), nome e @ ao lado. Interesses, descrição, país e data de entrada em letra pequena, alinhados à esquerda.
- Os números (Publicações, Seguindo, Seguidores) ficam soltos, sem cartão. Logo abaixo, dois botões pequenos lado a lado: **Editar perfil** e **Compartilhar perfil**.
- As abas ganharam um sublinhado fino na aba ativa e um divisor embaixo.
- **Troca de conta** agora fica no perfil: a seta ao lado do @ abre uma folha de baixo com as contas salvas (conta atual marcada), "Adicionar outra conta" e, ao segurar uma conta, a opção de remover do aparelho. A linha "Alternar entre suas contas" saiu das configurações.

## Editar perfil: categorias sem cores
- Saíram as cores diferentes de cada categoria. As escolhidas aparecem numa faixa "Escolhidas", na cor principal do app, cada uma com um X para tirar.
- As demais ficam agrupadas por assunto (Criação e mídia, Estilo de vida, Esporte e saúde, Negócios e aprender, Hobbies) em pílulas cinza claras, sem contorno e sem ícone. O limite continua em 5.

## Padrão de letras do aplicativo (todas as telas)
Fonte: Roboto (padrão do Android). Só estes tamanhos podem ser usados em `fontSize` e no tema:
| Tamanho | Peso | Uso |
|---|---|---|
| 24 | semibold | título de tela |
| 20 | semibold | nome no perfil, título de janela |
| 18 | semibold | números (seguidores, curtidas) |
| 16 | semibold/médio | nome no feed, seções, botões grandes, título de item das configurações |
| 14 | regular | texto corrido: legenda, comentário, bio, mensagem |
| 12 | regular | @, horário, local, abas, rótulos e descrições em cinza |
| 11 | regular | menor tamanho (avisos mínimos) |
- Tamanhos decorativos (contagem regressiva da live etc.) ficam fora da tabela.
- A redução geral de 10% (`QINBLY_TEXT_SCALE = 0.9`) foi removida (agora 1.0); era o que deixava muitas letras pequenas demais. "Letras grandes" nas configurações continua multiplicando por cima.
- Ao criar uma tela nova, usar um destes tamanhos (ou `MaterialTheme.typography`, que já segue a tabela).

## Configurações: saiu "Mercado e avisos"
- A linha "Mercado e avisos" foi removida da lista de Configurações. O Mercado e os Avisos continuam acessíveis pelo ícone da cestinha no início e pelo menu do Mercado; só o atalho nas configurações saiu.

## Versão web do QINBLY (formato de site, como o Instagram/Facebook na web)
- O site `https://qinbly.pages.dev` **é** a versão web do app (pasta `docs/`: `index.html`, `app.css`, `app.js`, `manifest.webmanifest`, `sw.js`, ícones). A página de download antiga virou `docs/baixar.html`, linkada em "Mais" e na tela de login. O app Android **não foi alterado**.
- Layout de site: no computador, **menu à esquerda** (Início, Buscar, Em alta, Mensagens, Notificações, Comunidades, Mercado, Salvos, Criar, Perfil, Mais), **feed no centro** (stories + abas Descobrir/Seguindo/Em alta/Vídeos) e **coluna à direita** (sua conta com "Trocar", sugestões de quem seguir, assuntos em alta, links). Entre 700 e 1000 px o menu fica só com ícones; abaixo de 700 px (celular) vira a barra de baixo igual à do app (Início, Comunidades, Criar, Mensagens, Perfil) e a coluna direita some.
- Mesmo servidor do app (`https://qinbly.pages.dev`): mesmas contas, publicações, mensagens, comunidades e mercado. Entrar por e-mail/usuário e senha, **entrar com o Google** (Google Identity Services, mesmo cliente do app; precisa de `https://qinbly.pages.dev` em "Origens JavaScript autorizadas"), criar conta e trocar de conta (as sessões ficam só neste aparelho e renovam sozinhas).
- Telas prontas: feed, stories (ver e criar), publicar texto com fundo/fotos/vídeo, curtir/salvar/compartilhar/comentar/votar em enquete, buscar (pessoas, publicações, assuntos), assunto (#tag), notificações (com contador), comunidades (minhas/descobrir/em alta, criar, entrar/sair, conversar), mercado (categorias, cidade, detalhes, falar com o vendedor), salvos, perfil (mesmo layout do app), editar perfil e foto, mensagens de texto.
- Recursos completos também na web (mesmo servidor do app): **menu ⋯ do post** (editar legenda, fixar no perfil, privado/público, arquivar, estatísticas, excluir, repostar, compartilhar, denunciar, bloquear); **comentários** com curtir, responder, apagar e denunciar; **criar** texto/foto/vídeo/**enquete** (várias respostas, duração), **agendar**, **marcar pessoas** e visibilidade; **stories** de foto, vídeo e **texto**, ver quem viu, destaque, apagar e responder, **notas**; **destaques** no perfil, fixados primeiro, aba Salvos, seguidores/seguindo, menu do perfil (bloquear, denunciar, copiar link); **seguir conta privada** (pedido); **mensagens** com foto, documento, áudio gravado, figurinha e enquete (chat e comunidades); **mercado** com **anunciar**, Meus anúncios, Salvos e excluir; **lives** (assistir, curtir, comentar e transmitir por câmera ou tela/jogo, via LiveKit); **Configurações** (conta, privacidade, conteúdo com palavras ocultas e arquivados, tela com tema escuro/claro, acessibilidade com texto maior e menos animações, dados, verificação, ajuda).
- Segunda leva na web: **mensagens completas** (tocar na mensagem abre o menu: reagir com emoji, responder com citação, copiar, encaminhar, cancelar envio, apagar para mim, fixar no topo, salvar figurinha, denunciar; publicações compartilhadas aparecem como cartão; pesquisar na conversa; mídia, links e documentos; fixar, silenciar, limpar, apagar conversa, bloquear e denunciar; emojis, câmera e localização; separador de dias); **caixa de entrada** com Todas, Não lidas e Solicitações, busca e conversas fixadas; **compartilhar publicação** (enviar para conversas e comunidades com recado, copiar link, outros apps); menu do post com **Ocultar** e **Copiar link**, marcados ("Com @..."); **Home** com "Escrever algo...", Pergunta do dia e Desafio da semana; aba **Perto de mim** (localização, publicações da cidade e **Mural de avisos** com comentários); **notificações** com abas, pedidos de seguir (aceitar/recusar), "Seguir de volta", marcar como lidas e ajustes; **stories** com público (todos, seguidores, amigos, perto) e duração, ver quem viu e as respostas; **comunidades** com menu (compartilhar, silenciar, limpar, sair, excluir) e ações nas mensagens; **comentários** editáveis e com figurinha; perfil com abas Fotos e Vídeos; **esqueci a senha** e checagem de nome de usuário livre; Configurações novas (Atividade/online, Localização, Fontes, Stories); presença online.
- Só no app Android (o navegador não permite ou o servidor não tem): editor de foto/vídeo, filtros de câmera 3D, música e colagem no story, stories colaborativos, enviar contato, mensagem de voz nos comentários e denunciar comunidade (o servidor só aceita denúncia de post, comentário, usuário e mensagem).
- Letras: mesma escala do app (24/20/18/16/14/12/11).

## Onda 3 — visual igual ao app (web)
- Paleta do app: escuro #171719 / #262628 / borda #2E2E30; claro #F6F6F7 / #E4E6EB / #DADADD. Fonte base 16px.
- Cabeçalho do celular: menu à esquerda, logo QINBLY em degradê roxo→rosa, busca/mercado/sino à direita.
- Abas do feed com ícone sobre o texto e linha de 3px; "Perto de mim" mostra a cidade. Stories só na aba Descobrir.
- Stories de 88px com anel roxo→rosa e "+" roxo; posts de tela inteira com avatar de 52px e anel de story.
- Barra de ações: curtir (vermelho), comentar, repostar, salvar (amarelo), enviar. Barra de baixo com botão Criar branco com "+" em degradê.
- "Instalar o app": no Android abre o instalador direto (ou baixa o APK); só no iPhone aparece a dica de Compartilhar, porque o iOS não permite instalar com um toque.
- Perfil (próprio e de outras pessoas) refeito igual ao app: próprio = menu no canto, foto 76px + nome e @ com seta, categorias/descrição, país e "Entrou em", estatísticas Publicações/Seguindo/Seguidores, botões "Editar perfil" e "Compartilhar perfil", destaques e abas Publicações/Vídeos/Fotos/Salvos. De outra pessoa = foto grande centralizada, nome e @ no meio, botões "Seguir" e "Mensagem". Publicações e Vídeos aparecem como lista de posts; Fotos em grade de 3.

## Apagou, apagou (não volta)
- "Apagar para mim", "Limpar conversa" e "Apagar conversa" nas mensagens diretas agora ficam guardados no servidor (tabela `message_hides`), não só no navegador. O servidor passa a não devolver mais essas mensagens a quem apagou, em qualquer aparelho. A outra pessoa continua vendo.
- "Cancelar envio" e "Excluir publicação" já apagam de verdade no banco; agora a mensagem/publicação some da tela na hora e fica marcada para não reaparecer numa atualização que já estava a caminho.
- O service worker (`sw.js`, cache v6) deixou de guardar respostas da API; só guarda a casca do app. Antes, uma falha de rede podia mostrar uma lista antiga com o que já foi apagado.
- Aplicativo Android: "Apagar para mim", "Limpar conversa" e "Apagar conversa" agora também gravam no servidor (`message_hides`) e não voltam mais; antes só valiam no aparelho (a tela de limpar dizia que as mensagens voltavam). Excluir publicação tira a publicação também das cópias guardadas (feed, meu perfil, perfis abertos e a cópia offline).
- Menu dos três tracinhos do site agora é a tela cheia "Atalhos", igual ao app: seta de voltar, seu perfil, e os blocos EXPLORAR (Vídeos, Em alta, Lives, Perto de mim), NA SUA CIDADE (Mercado, Doações, Achados, Eventos, Serviços, Avisos) e SEUS ESPAÇOS (Comunidades, Salvos, Convidar amigos, Transmitir ao vivo), mais Configurações, Trocar de conta, Instalar o app e Sair.

## Comentários e respostas (site e app)
- Comentário: avatar de 36, nome e horário na mesma linha, texto 15, "Responder" pequeno e cinza, coração pequeno à direita com o número.
- Respostas recuadas (avatar 26, texto 14), sem linha lateral e sem tracinho. Fechadas por padrão: "Ver N respostas ▾". Ao abrir, mostram 3; embaixo ficam lado a lado "Ver mais N ▾" (de 10 em 10) e "Ocultar ▴". Ao responder alguém, todas as respostas aparecem.
- Todos os comentários ficam na mesma lista, sem "ver mais comentários" e sem botões de ordenar.

## Notificações do app (som)
- Canal novo `qinbly_social_v2` (importância alta, som padrão de notificação, vibração e luz). O canal antigo podia estar mudo e o Android não deixa o app alterar um canal já criado.
- A permissão de notificação (Android 13+) volta a ser pedida, no máximo uma vez por dia, enquanto não for liberada. Antes só era pedida na primeira abertura, no meio de várias outras.
- Limite conhecido: com o app fechado, os avisos chegam por consulta periódica (no melhor caso a cada ~15 min). Aviso na hora, com o app fechado, só com push de verdade (Firebase).

## Foto deitada com barras pretas (editor do app)
- Causa: o editor lia a foto sem aplicar a rotação (EXIF) do celular. A prévia (Coil) mostrava em pé, mas o arquivo final saía deitado e encaixado numa moldura 4:5 com barras pretas dentro da própria imagem.
- Correção: toda leitura de foto do editor passa por `qinblyUpright` (proporção, ajuste/recorte, girar/espelhar, imagem final e foto de comunidade). A proporção "Original" agora segue a foto de verdade.
- Publicações antigas já salvas assim continuam deitadas (o defeito está gravado no arquivo): é preciso apagar e publicar de novo.

## Aviso de atualização do app
- Já existia: o app consulta a última versão no GitHub e avisa uma vez por versão nova. Ajustes: canal novo `qinbly_update_v2` (importância alta, som e vibração), texto com o número da versão, checagem a cada 10 min (antes 1 h), checagem imediata ao abrir o app e aviso também com o app aberto (antes só com o app fechado).
- Limites: o aviso só chega depois que a pessoa instalou uma versão que já tem esse recurso; com o app fechado depende do Android acordar o app (~15 min ou mais, bateria). Aviso instantâneo exige push (Firebase).

## Ícone das notificações
- A barra de status mostrava o "i" genérico do Android. Agora usa `ic_stat_qinbly` (silhueta branca do logo: anel com estrela) nas notificações de novidades, mensagens, atualização e "QINBLY atualizado".

## Tocar na notificação abre direto no assunto (app)
- A notificação do sistema agora leva o tipo, a publicação e o @ da pessoa. Ao tocar: mensagem abre a conversa; curtida, marcação e menção abrem a publicação; comentário e resposta abrem a publicação já com os comentários; seguidor e "aceitou seu pedido" abrem o perfil; pedido de seguir, verificação e avisos abrem a lista de notificações; atualização abre a tela de atualizar.
- Cada aviso tem seu próprio código de toque (antes todos usavam o mesmo e um podia sobrescrever o outro).
- A lista de Notificações dentro do app usa o mesmo caminho (antes curtida e comentário abriam só o perfil).

## Barra de botões da publicação (app e site)
- Ordem: curtir, comentar, repostar, enviar, e o salvar na ponta direita.
- Curtir: coração cheio e vermelho quando curtido. Salvar: marcador cheio e amarelo quando salvo.
- Repostar: duas setas em círculo (redondinho).
- Enviar: avião de papel largo, com bico e asas arredondados (desenho próprio, igual no app e no site).
- Site: `sw.js` passou para `qinbly-web-v11` para o navegador pegar a mudança.

## Avisos com o app fechado (serviço em segundo plano)
- Antes: só uma tarefa agendada (a cada ~15 min, quando o Android deixava; em Samsung quase nunca com o app fechado). Agora: `QinblyNotifyService`, serviço em primeiro plano com aviso discreto fixo ("Recebendo seus avisos", sem som, recolhido), que consulta novidades e versão nova do app a cada ~40 s.
- Liga ao abrir o app, ao entrar numa conta e ao ligar o celular/atualizar o app (`QinblyBootReceiver`). Para sozinho se sair da conta ou desligar "Notificações".
- O app pede uma vez, depois das notificações, para rodar sem economia de bateria (sem isso o Android pode dormir o serviço). Recusou: pergunta de novo só depois de 3 dias.
- Continua a tarefa agendada como reserva. Push instantâneo de verdade (Firebase/FCM) segue possível no futuro; exige projeto Firebase.

### Ajuste do serviço de avisos (celular de tela apagada)
- A espera entre consultas deixou de ser um `sleep` (que dorme junto com o celular). Agora cada consulta segura o processador acordado só por ela e agenda a próxima com `AlarmManager.setAndAllowWhileIdle`.
- O aviso fixo mostra "Última verificação às HH:mm" (ou "Sem conexão com o servidor às HH:mm"): serve de prova de que o serviço está vivo.
- Se o Android recusar ligar o serviço fixo, ele se desliga sem derrubar o app.

## Sem conta: perfil só com "Entrar" e aviso nas ações (app)
- Perfil sem conta: só o botão grande "Entrar" e o aviso "Entre para ver seu perfil" (sem contadores, abas, editar nem compartilhar).
- Qualquer ação que exige conta (curtir, comentar, salvar, repostar, seguir, enviar, mensagem, criar...) agora abre um aviso por baixo da tela: "Entre para curtir/comentar/..." com botão grande "Entrar" e "Agora não". É feito num ponto só: toda mensagem "Entre na sua conta para..." é trocada por esse aviso.
- "Entrar" abre a tela de entrar/criar conta; ao entrar (ou cancelar) a pessoa volta para a aba onde estava.
- Site: já exige login para abrir, então não precisa do aviso.

## Avisos: somem ao abrir, sem aviso duplicado com o app aberto
- Ao abrir/voltar pro app (`onResume`) os avisos de curtida, comentário, seguidor e mensagem saem da barra (`qinblyClearSocialNotifications`); o aviso fixo do serviço e o de atualização ficam.
- Com o app aberto não se cria mais aviso na barra (só o contador do sino); com o app fechado quem avisa é o serviço.
- Aviso fixo do serviço trocou de id (7002 → 7010) porque colidia com o aviso de "app reiniciado após atualizar".

## Aviso fixo do serviço: texto parado
- O aviso fixo ("QINBLY – Recebendo seus avisos") não é mais atualizado a cada consulta (a hora mudando fazia ele reaparecer toda hora). Ficou com texto fixo, importância mínima e oculto na tela de bloqueio. O Android exige um aviso fixo enquanto o serviço roda; o jeito de sumir de vez é o push do Firebase.

## Velocidade: fotos e feed
- Servidor: fotos (até 6 MB) passam a sair do cache da borda da Cloudflare (`caches.default`) em vez de consultar o R2 a cada abertura. Vídeos seguem direto do R2.
- App: `getPosts` não faz mais a chamada extra `/api/auth/me` antes de buscar o feed (o `request()` já renova token vencido). Uma ida a menos ao servidor em toda abertura/atualização do feed.
