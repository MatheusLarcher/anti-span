# Anti-Spam — Design (bloqueio de ligação e SMS)

- **Status:** aprovado pelo usuário em 2026-07-08 — SMS via Opção B (Notification Listener), ligação via `CallScreeningService`
- **Data:** 2026-07-08
- **Atualização (2026-07-15):** o bloqueio de SMS (seções 3.3, 4.2-4.4 na parte de SMS, item 2 da
  seção 5, itens de SMS/notificação da seção 6) foi **removido do app** — testado em aparelho
  real, não bloqueava de forma confiável. O app hoje só bloqueia ligação; este documento fica
  como registro histórico de por que a Opção B foi escolhida na época, não como descrição do
  app atual. Ver `git log` para o commit da remoção.

## 1. Contexto e objetivo

App Android pessoal para bloquear automaticamente, sem tocar e sem notificação:
- **Ligações** de números que não estão nos contatos.
- **SMS** de números que não estão nos contatos.

Ligações e SMS bloqueados devem continuar visíveis dentro do próprio app: ligações em uma lista simples, SMS em formato de "conversa" (chat) por número. O app tem 2 interruptores independentes para ligar/desligar o bloqueio de ligação e de SMS.

## 2. Escopo

Dentro do escopo (v1):
- Bloqueio silencioso de ligação de não-contato (sem tocar, sem notificação, sem aparecer no log de chamadas do sistema).
- Supressão da notificação de SMS de não-contato (ver seção 3.3 sobre a garantia desse bloqueio).
- Lista de ligações bloqueadas.
- Lista de SMS bloqueados em formato de chat, por número.
- Toggle "Bloquear ligações desconhecidas" e toggle "Bloquear SMS desconhecidos", independentes.
- Ação "permitir este número" a partir de um item bloqueado (ligação ou SMS).
- Tela de configuração inicial guiando as permissões/papéis especiais necessários.

Fora do escopo, ver seção 8.

## 3. Decisões arquiteturais

### 3.1 Plataforma: Android nativo

Bloqueio silencioso de ligação/SMS de terceiros exige APIs que só existem no Android (`CallScreeningService`, notificações de outros apps). Confirmado implicitamente pelo usuário (pediu geração de APK).

### 3.2 Bloqueio de ligações — `CallScreeningService` (aprovado)

API oficial do Android (desde Android 10 / API 29) para apps que não são o discador padrão. O app solicita o papel `RoleManager.ROLE_CALL_SCREENING` (via `RoleManager.createRequestRoleIntent`); uma vez concedido, o sistema chama `onScreenCall()` do nosso serviço para cada ligação recebida, antes de tocar. Não interfere no discador/app de telefone padrão do usuário — coexistem.

Resposta pra número desconhecido (toggle ligado): `CallResponse` com `setDisallowCall(true)`, `setRejectCall(true)`, `setSkipNotification(true)`, `setSkipCallLog(true)` — não toca, não notifica, não aparece nem no log de chamadas do sistema (fica só na lista do nosso app).

### 3.3 Bloqueio de SMS — Opção B (escolhida pelo usuário)

O Android não tem uma API de "SMS screening" para apps de terceiros equivalente à de ligação — só o **app padrão de SMS** recebe a mensagem antes de qualquer notificação ser criada. Virar o app padrão de SMS (Opção A) daria garantia de 100%, mas trocaria o app de mensagens principal do usuário e derrubaria recursos de RCS. **O usuário optou pela Opção B**: manter o app de SMS atual (Google Messages / Samsung Messages / outro) como está, aceitando uma garantia de bloqueio um pouco mais fraca (não 100%, mas próxima disso na prática) em troca de nenhuma disrupção no app de mensagens.

**Arquitetura de duas partes** (pra Opção B ser o mais robusta possível):

1. **`SmsReceivedReceiver`** (`BroadcastReceiver`, ação `SMS_RECEIVED_ACTION`, permissão `RECEIVE_SMS`) — recebida por qualquer app com essa permissão, depois que o app de SMS padrão já processou a mensagem. Extrai remetente e corpo direto do PDU (fonte confiável — não depende de como a notificação é formatada). Se o remetente é desconhecido (não é contato nem está em `allowed_numbers`) e o toggle de SMS está ligado: grava em `blocked_sms` e registra o número numa cache curta em memória (TTL ~10s) pra correlação com a notificação.
2. **`SmsNotificationListenerService`** (`NotificationListenerService`, exige a permissão especial "Acesso a notificações", concedida manualmente pelo usuário nas Configurações do sistema) — a cada notificação nova, checa se veio do pacote do app de SMS padrão *atual* (consultado dinamicamente via `Telephony.Sms.getDefaultSmsPackage`, então continua funcionando mesmo se o usuário trocar de app de SMS depois). Se o remetente bate com uma entrada recente da cache marcada como desconhecida, cancela a notificação (`cancelNotification`) imediatamente.

**Garantia real:** existe uma janela pequena (tipicamente imperceptível — poucos milissegundos) entre a notificação ser criada e ser cancelada. Som/vibração podem disparar antes do cancelamento em alguns aparelhos.

**Limitação aceita:** notificações agrupadas pelo fabricante (ex. "3 novas mensagens" numa notificação só) não são canceladas individualmente no v1.

**Efeito colateral positivo da Opção B:** como a mensagem continua sendo gravada normalmente pelo app de SMS de verdade (não somos nós que decidimos gravar ou não), um código de verificação/OTP de número desconhecido nunca "some" — na pior hipótese (notificação suprimida), ele ainda está no app de mensagens padrão, além de aparecer também na lista deste app.

## 4. Arquitetura técnica

### 4.1 Stack

- Kotlin + Jetpack Compose (Material 3), telas em pt-BR.
- Room (SQLite) para dados locais — sem backend, sem nuvem, nada sai do aparelho.
- DataStore (preferências) para os toggles e a lista de números permitidos manualmente.
- `minSdk` 29 (Android 10 — requisito do `RoleManager`), `targetSdk` = versão estável mais recente disponível no toolchain no momento do build.
- Pacote sugerido: `com.larchertech.antispam` (fácil de trocar). Nome do app: "Anti-Spam".

### 4.2 Componentes exigidos no manifest

- `CallScreeningService` (permissão `BIND_SCREENING_SERVICE`, intent-filter `android.telecom.CallScreeningService`).
- `BroadcastReceiver` para `SMS_RECEIVED_ACTION` (permissão `RECEIVE_SMS`).
- `NotificationListenerService` (permissão `BIND_NOTIFICATION_LISTENER_SERVICE`) — acesso concedido manualmente pelo usuário em Configurações > Apps > Acesso especial > Acesso a notificações; o app abre esse diálogo direto no onboarding.
- Permissões: `READ_CONTACTS`, `RECEIVE_SMS`.
- Papel solicitado em runtime via `RoleManager`: `ROLE_CALL_SCREENING` (SMS não usa papel do sistema nessa opção).

Nota: por não virar app padrão de SMS, **não** precisamos de `SEND_SMS`, `READ_SMS`, `RECEIVE_MMS`, receiver de `WAP_PUSH_DELIVER_ACTION`, tela de composição nem serviço de resposta rápida — a Opção B é bem mais enxuta em permissões que a Opção A.

### 4.3 Modelo de dados (Room)

- `blocked_calls`: id, número normalizado, número bruto, timestamp.
- `blocked_sms`: id, número normalizado, número bruto, corpo da mensagem, timestamp — agrupado por número na UI para formar a "conversa".
- `allowed_numbers`: números liberados manualmente pela ação "permitir este número". **Não mexe nos Contatos reais do Android** — é uma lista interna própria do app. Regra final: um número passa direto se (é contato do Android) OU (está em `allowed_numbers`).
- Preferências (DataStore): `call_blocking_enabled` (padrão true), `sms_blocking_enabled` (padrão true).
- Cache de correlação notificação↔mensagem: apenas em memória (não persistida no Room), TTL curto (~10s).

### 4.4 Fluxo de dados

**Ligação:** chega → `onScreenCall()` → se toggle desligado, resposta vazia (deixa tocar normal) → se ligado, consulta `ContactsContract.PhoneLookup` + `allowed_numbers` → conhecido: deixa passar → desconhecido: rejeita conforme 3.2 + grava em `blocked_calls`.

**SMS:** chega `SMS_RECEIVED_ACTION` → `SmsReceivedReceiver` extrai remetente/corpo → se toggle desligado ou número conhecido: não faz nada (mensagem já está normalmente no app de SMS do usuário) → se desconhecido e toggle ligado: grava em `blocked_sms` + marca na cache de correlação. Em paralelo, `SmsNotificationListenerService` detecta a notificação do app de SMS padrão e, se o remetente estiver na cache como desconhecido, cancela a notificação.

## 5. Telas / UX

1. **Ligações bloqueadas** — lista: horário, número, botão "permitir este número".
2. **SMS bloqueados** — lista de conversas por número (preview da última mensagem + horário); ao abrir, mensagens daquele número em bolhas (estilo chat), botão "permitir este número" no topo da conversa.
3. **Ajustes** — switch "Bloquear ligações desconhecidas", switch "Bloquear SMS desconhecidos", lista de números permitidos (com opção de remover), acesso ao checklist de configuração.

## 6. Onboarding / permissões

Tela de configuração no primeiro uso (e acessível depois via Ajustes) com checklist guiado:
- Permissão de Contatos.
- Papel de screening de chamada (`ROLE_CALL_SCREENING`).
- Acesso a notificações (pra suprimir notificação de SMS desconhecido).
- Recomendação de desativar otimização de bateria para o app (fabricantes como Xiaomi/Samsung derrubam serviços de fundo agressivamente — isso é ainda mais importante na Opção B, já que o cancelamento da notificação depende do serviço estar rodando).

Cada item do checklist abre diretamente o diálogo do sistema correspondente. Sem esses acessos concedidos, o bloqueio correspondente não funciona — o app deve deixar isso visível (ex.: banner "proteção incompleta") em vez de falhar silenciosamente.

## 7. Casos de borda

- Toggle desligado → passa tudo normalmente, independente de ser contato.
- Número privado/sem identificação de chamada → tratado como desconhecido.
- Notificações agrupadas pelo fabricante ("3 novas mensagens") → não canceladas individualmente no v1 (limitação conhecida da Opção B).
- Usuário troca de app de SMS padrão depois → detectado dinamicamente a cada notificação, sem precisar reconfigurar nada.
- MMS de número bloqueado → a notificação é suprimida do mesmo jeito (mesmo mecanismo), mas o conteúdo (mídia) não é capturado pro app — vira um registro simples "MMS recebido" na lista, já que ler o conteúdo de MMS exigiria acesso que a Opção B deliberadamente não pede.
- Chip duplo (dual-SIM) → as APIs usadas já são agnósticas de SIM, sem tratamento especial necessário.
- Falta de permissão/acesso concedido → funcionalidade correspondente fica visivelmente desativada na UI até o usuário completar o onboarding.

## 8. Fora de escopo (v1)

- Publicação na Play Store (exigiria declaração extra de permissões sensíveis — instalação é direta no aparelho).
- Lista de spam comunitária/compartilhada (tipo Truecaller).
- Backup/restore em nuvem, sincronização entre aparelhos.
- Responder SMS pelo app, ou qualquer funcionalidade de app de mensagens completo.
- Preview de mídia de MMS bloqueado.
- iOS.

## 9. Plano de testes / validação

Sem considerar "pronto" só por compilar. Validação via emulador Android (simulação de ligação/SMS por `adb emu gsm call` / `sms send` ou Extended Controls, com um app de SMS padrão ativo no perfil de teste) cobrindo:
- Ligação/SMS de número de contato vs. desconhecido, com cada toggle ligado/desligado.
- Notificação de SMS desconhecido é de fato cancelada rapidamente; de contato, permanece normal.
- Múltiplas mensagens próximas no tempo (correlação da cache continua acertando o remetente certo).
- Persistência das listas após fechar/reabrir o app.
- Estado vazio.
- Ação "permitir este número" liberando de fato a próxima ligação/SMS daquele número.
- Acesso a notificações não concedido → UI mostra claramente que a proteção de SMS está incompleta, sem crash.

Teste final em aparelho real recomendado à parte, já que comportamento de bateria/autostart e formato de notificação variam por fabricante e não são totalmente replicáveis no emulador.

## 10. Riscos e decisões confirmadas

- **Plataforma Android:** confirmada implicitamente (usuário pediu geração de APK).
- **Ligação (`CallScreeningService`):** aprovado explicitamente pelo usuário.
- **SMS — Opção B:** escolhida explicitamente pelo usuário em vez da Opção A. Trade-off consciente: guarantee de bloqueio um pouco mais fraca (não 100%), em troca de manter o app de mensagens atual intacto (sem perder RCS).
- **Riscos residuais aceitos:** notificações agrupadas não suprimidas individualmente; dependência do serviço de notificação não ser morto em segundo plano pelo sistema/fabricante (mitigado via exclusão de otimização de bateria no onboarding); janela pequena onde a notificação pode "piscar" antes do cancelamento.

## 11. Próximos passos

Aprovado. Seguir para o plano de implementação detalhado (skill `planejar`) e, ao final, gerar o APK.
