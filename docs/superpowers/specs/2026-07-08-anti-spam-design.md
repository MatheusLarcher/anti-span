# Anti-Spam — Design (bloqueio de ligação e SMS)

- **Status:** rascunho, aguardando revisão do usuário
- **Data:** 2026-07-08

## 1. Contexto e objetivo

App Android pessoal para bloquear automaticamente, sem tocar e sem notificação:
- **Ligações** de números que não estão nos contatos.
- **SMS** de números que não estão nos contatos.

Ligações e SMS bloqueados devem continuar visíveis dentro do próprio app: ligações em uma lista simples, SMS em formato de "conversa" (chat) por número. O app tem 2 interruptores independentes para ligar/desligar o bloqueio de ligação e de SMS.

## 2. Escopo

Dentro do escopo (v1):
- Bloqueio silencioso de ligação de não-contato (sem tocar, sem notificação, sem aparecer no log de chamadas do sistema).
- Bloqueio silencioso de SMS de não-contato (sem notificação).
- Lista de ligações bloqueadas.
- Lista de SMS bloqueados em formato de chat, por número.
- Toggle "Bloquear ligações desconhecidas" e toggle "Bloquear SMS desconhecidos", independentes.
- Ação "permitir este número" a partir de um item bloqueado (ligação ou SMS).
- Tela de configuração inicial guiando as permissões/papéis especiais necessários.

Fora do escopo, ver seção 8.

## 3. Decisões arquiteturais

### 3.1 Plataforma: Android nativo

Bloqueio silencioso de ligação/SMS de terceiros exige APIs que só existem no Android (`CallScreeningService`, papel de app padrão de SMS). O iOS não expõe nada equivalente para apps de terceiros. **Premissa assumida:** o usuário está no Android (baseado no nome do repo e no toolchain Android já configurado no ambiente). Não houve confirmação explícita — ver seção 10.

### 3.2 Bloqueio de ligações — `CallScreeningService`

API oficial do Android (desde Android 10 / API 29) para apps que não são o discador padrão. O app solicita o papel `RoleManager.ROLE_CALL_SCREENING` (via `RoleManager.createRequestRoleIntent`); uma vez concedido, o sistema chama `onScreenCall()` do nosso serviço para cada ligação recebida, antes de tocar. Não interfere no discador/app de telefone padrão do usuário — coexistem.

### 3.3 Bloqueio de SMS — decisão principal

O Android não tem uma API de "SMS screening" para apps de terceiros equivalente à de ligação. Só a **app padrão de SMS** (`RoleManager.ROLE_SMS`) recebe a broadcast `SMS_DELIVER_ACTION` antes de qualquer outra coisa acontecer (gravação no provider, notificação). Qualquer outro app só recebe `SMS_RECEIVED_ACTION` **depois** que o app padrão já processou a mensagem — ou seja, depois que a notificação já teria sido criada.

Duas abordagens foram avaliadas:

**Opção A — o app vira o app padrão de SMS do sistema (recomendada, adotada neste design).**
Controle total: mensagens de não-contato nunca chegam a gerar notificação nem a ser gravadas no provider de SMS do sistema, porque é o próprio app quem decide isso. Mensagens de contato são gravadas normalmente no provider (`content://sms`) e notificadas pelo próprio app (para não perder a experiência normal de "cheguei uma SMS"). Efeito colateral: o app passa a ser o app de mensagens principal do usuário (troca o Google Messages/Samsung Messages padrão).

**Opção B — `NotificationListenerService` "escutando" o app de SMS atual.**
O app pede a permissão especial de acesso a notificações, detecta quando o app de SMS atual posta uma notificação de número desconhecido e cancela na hora (`cancelNotification`). Não troca o app de mensagens do usuário. Porém: (1) existe uma janela pequena entre a notificação ser criada e ser cancelada — som/vibração pode disparar antes do cancelamento; (2) a mensagem continua gravada normalmente no app de SMS real (não é removida, só a notificação é suprimida); (3) depende do formato de notificação de cada fabricante/app de SMS, podendo falhar silenciosamente após atualizações; (4) alguns fabricantes (Xiaomi, Samsung) matam serviços de fundo agressivamente, o que pode atrasar o cancelamento.

**Adotado: Opção A.** O pedido original enfatiza "nem aparece a notificação" tanto para ligação quanto para SMS — só a Opção A garante isso de forma equivalente à ligação (decisão antes de qualquer notificação existir). A Opção B fica documentada aqui como alternativa caso o usuário prefira não trocar de app de mensagens, aceitando a garantia mais fraca.

**Consequência adicional da Opção A — perda de RCS:** o "chat avançado" do Google Messages (recibo de leitura, "digitando...", mídia em alta qualidade, envio por wi-fi/dados) é um recurso do app Google Messages amarrado ao Jibe/RCS, e só funciona no app que estiver registrado como padrão para isso. Ao tornar este app o padrão de SMS, conversas com outros usuários (mesmo Android) caem para SMS/MMS clássico nessas trocas — perde-se esses recursos. Isso vale tanto para os números bloqueados quanto (principalmente) para as conversas normais com contatos, que são o uso do dia a dia. Se isso for um problema, a Opção B evita essa perda por completo.

**Consequência aceita:** números que nunca estarão nos contatos (bancos, apps de entrega, códigos de verificação/OTP) também serão bloqueados silenciosamente na primeira vez que enviarem SMS — a mensagem não some, fica na lista do app, só não notifica. Mitigado pela ação "permitir este número" (1 toque) a partir da lista.

## 4. Arquitetura técnica

### 4.1 Stack

- Kotlin + Jetpack Compose (Material 3), telas em pt-BR.
- Room (SQLite) para dados locais — sem backend, sem nuvem, nada sai do aparelho.
- DataStore (preferências) para os toggles e a lista de números permitidos manualmente.
- `minSdk` 29 (Android 10 — requisito do `RoleManager`), `targetSdk` = versão estável mais recente disponível no toolchain no momento do build.
- Pacote sugerido: `com.larchertech.antispam` (fácil de trocar). Nome do app: "Anti-Spam".

### 4.2 Componentes exigidos no manifest

- `CallScreeningService` (permissão `BIND_SCREENING_SERVICE`, intent-filter `android.telecom.CallScreeningService`).
- `BroadcastReceiver` para `SMS_DELIVER_ACTION` (permissão `BROADCAST_SMS`) — só chega para o app padrão de SMS.
- `BroadcastReceiver` para `WAP_PUSH_DELIVER_ACTION` (permissão `BROADCAST_WAP_PUSH`, mimeType `application/vnd.wap.mms-message`) — exigido para elegibilidade ao papel de SMS padrão; tratamento de MMS em si é mínimo (ver seção 7).
- `Activity` de composição (intent-filter `ACTION_SENDTO`/`SEND`/`SEND_MULTIPLE`, esquemas `sms`/`smsto`/`mms`/`mmsto`) — exigida para elegibilidade ao papel; tela simples de responder/nova mensagem.
- `Service` para `ACTION_RESPOND_VIA_MESSAGE` (resposta rápida ao recusar ligação) — exigido para elegibilidade ao papel.
- Permissões: `READ_CONTACTS`, `RECEIVE_SMS`, `READ_SMS`, `SEND_SMS`, `RECEIVE_MMS`, `POST_NOTIFICATIONS` (Android 13+).
- Papéis solicitados em runtime via `RoleManager`: `ROLE_CALL_SCREENING`, `ROLE_SMS`.

### 4.3 Modelo de dados (Room)

- `blocked_calls`: id, número normalizado, número bruto, timestamp.
- `blocked_sms`: id, número normalizado, número bruto, corpo da mensagem, timestamp — agrupado por número na UI para formar a "conversa".
- `allowed_numbers`: números liberados manualmente pela ação "permitir este número". **Não mexe nos Contatos reais do Android** — é uma lista interna própria do app, para não poluir a agenda do usuário com números sem nome. Regra final: um número passa direto se (é contato do Android) OU (está em `allowed_numbers`).
- Preferências (DataStore): `call_blocking_enabled` (padrão true), `sms_blocking_enabled` (padrão true).

### 4.4 Fluxo de dados

**Ligação:** chega → `onScreenCall()` → se toggle desligado, resposta vazia (deixa tocar normal) → se ligado, consulta `ContactsContract.PhoneLookup` + `allowed_numbers` → conhecido: deixa passar → desconhecido: `CallResponse` com `setDisallowCall(true)`, `setRejectCall(true)`, `setSkipNotification(true)`, `setSkipCallLog(true)` + grava em `blocked_calls`.

**SMS:** chega `SMS_DELIVER_ACTION` (só o nosso app recebe, por ser o padrão) → se toggle desligado ou número conhecido: grava no provider do sistema (`content://sms`) + notificação própria normal → se desconhecido e toggle ligado: **não** grava no provider do sistema, grava só em `blocked_sms`, sem notificação.

## 5. Telas / UX

1. **Ligações bloqueadas** — lista: horário, número, botão "permitir este número".
2. **SMS bloqueados** — lista de conversas por número (preview da última mensagem + horário); ao abrir, mensagens daquele número em bolhas (estilo chat), botão "permitir este número" no topo da conversa.
3. **Ajustes** — switch "Bloquear ligações desconhecidas", switch "Bloquear SMS desconhecidos", lista de números permitidos (com opção de remover), acesso ao checklist de configuração.

## 6. Onboarding / permissões

Tela de configuração no primeiro uso (e acessível depois via Ajustes) com checklist guiado: permissão de Contatos, papel de screening de chamada, papel de app de SMS padrão, permissão de notificações (Android 13+), e recomendação de desativar otimização de bateria para o app (fabricantes como Xiaomi/Samsung derrubam serviços de fundo agressivamente). Cada item do checklist abre diretamente o diálogo do sistema correspondente. Sem esses acessos concedidos, o bloqueio não funciona — o app deve deixar isso visível (ex.: banner "proteção incompleta") em vez de falhar silenciosamente.

## 7. Casos de borda

- Toggle desligado → passa tudo normalmente, independente de ser contato.
- Número privado/sem identificação de chamada → tratado como desconhecido.
- MMS → fora do escopo de renderização de mídia no v1; MMS de contato passa direto, MMS de número bloqueado é registrado como mensagem sem mídia.
- Chip duplo (dual-SIM) → as APIs usadas (`CallScreeningService`, `ROLE_SMS`) já são agnósticas de SIM, sem tratamento especial necessário.
- Falta de permissão/papel concedido → funcionalidade correspondente fica visivelmente desativada na UI até o usuário completar o onboarding.

## 8. Fora de escopo (v1)

- Publicação na Play Store (exigiria declaração extra de permissões sensíveis — instalação é direta no aparelho).
- Lista de spam comunitária/compartilhada (tipo Truecaller).
- Backup/restore em nuvem, sincronização entre aparelhos.
- App de mensagens completo para contatos (apenas notificação + resposta mínima).
- iOS.

## 9. Plano de testes / validação

Sem considerar "pronto" só por compilar. Validação via emulador Android (simulação de ligação/SMS por `adb emu gsm call` / `sms send` ou Extended Controls) cobrindo: ligação/SMS de número de contato vs. desconhecido, com cada toggle ligado/desligado; persistência das listas após fechar/reabrir o app; estado vazio; ação "permitir este número" liberando de fato o próximo contato/SMS. Teste final em aparelho real recomendado à parte, já que comportamento de bateria/autostart varia por fabricante e não é replicável no emulador.

## 10. Riscos e premissas assumidas

As perguntas de esclarecimento feitas durante o brainstorming (confirmação de plataforma Android, e a escolha entre Opção A/B para SMS) não foram respondidas a tempo (timeout) — as decisões acima foram tomadas com a opção mais sensata para não travar o progresso, e ficam marcadas aqui para revisão explícita:

- **Plataforma Android**: assumida, não confirmada.
- **Opção A (virar app de SMS padrão)**: assumida como a que atende literalmente o pedido ("nem aparece a notificação"), mas é a mais disruptiva (troca o app de mensagens principal do usuário **e faz perder recursos de RCS/chat avançado nas conversas normais**, não só nas bloqueadas). Se isso for indesejado, a Opção B é a alternativa documentada na seção 3.3.
- **Consequência de bloquear OTP/códigos de verificação na primeira mensagem**: aceita como parte do pedido original, mitigada pela ação "permitir este número".

## 11. Próximos passos

Após aprovação deste design, segue para o plano de implementação detalhado (skill `planejar`).
