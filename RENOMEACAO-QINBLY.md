# Renomeação VYBORA → QINBLY (código)

Tudo que se chamava Vybora/VYBORA/vybora agora é Qinbly/QINBLY/qinbly: pacote e pasta (`app.qinbly.android`), classes (`QinblyApi` etc.), SharedPreferences (`qinbly_session`), variáveis de ambiente, classes CSS/eventos do remote-ui, projeto iOS (`ios/QINBLY`) e a função do Supabase (`qinbly-api`).

## O que você precisa fazer por causa disso
1. **Função do Supabase:** publique com o novo nome: `supabase functions deploy qinbly-api` (o app agora chama `/functions/v1/qinbly-api`). Pode apagar a antiga `vybora-api` depois.
2. **Android:** o `applicationId` mudou para `app.qinbly.android`. O Android trata como **outro app**: não atualiza por cima do antigo, e quem estava logado precisa entrar de novo. Atualize também o cliente Android do **Login com Google** (nome do pacote + SHA-1) no Google Cloud.
3. **iOS:** o bundle id virou `app.qinbly.ios`; atualize certificados/perfis no Apple Developer.
4. **Variáveis de ambiente de assinatura** renomeadas: `QINBLY_KEYSTORE_PATH`, `QINBLY_KEYSTORE_PASSWORD`, `QINBLY_KEY_ALIAS`, `QINBLY_KEY_PASSWORD`.
5. **Atualização automática (OTA):** o arquivo da release deve se chamar `qinbly-release.apk`.
6. Apague as pastas antigas ao descompactar por cima: `app/src/main/java/app/vybora`, `ios/VYBORA`, `ios/VYBORA.xcodeproj`, `SUPABASE/functions/vybora-api`.

## Recursos externos também renomeados no código
- `qinbly-production.up.railway.app`: renomeie o serviço/domínio no Railway para esse endereço (ou ajuste o endereço no código).
- `qinbly-private`: crie o bucket com esse nome (ou renomeie o atual) e atualize `S3_BUCKET`.
