# Anti-Spam — Design (seleção de chip para bloqueio, dual-SIM)

- **Status:** aprovado pelo usuário em 2026-07-08
- **Data:** 2026-07-08

## 1. Contexto e objetivo

Hoje o bloqueio de ligação e de SMS roda igual em qualquer chip do aparelho — sem tratamento
diferenciado para dual-SIM (decisão original documentada em
`2026-07-08-anti-spam-design.md`, seção 7). Pedido novo do usuário: em aparelhos com 2+ chips
ativos, poder escolher em qual chip cada bloqueio deve atuar — ligação e SMS de forma
independente (podem ser chips diferentes).

## 2. Escopo

Dentro do escopo:
- Detecção de quantos chips o aparelho tem, sem exigir permissão nova, e ocultação completa
  dessa funcionalidade em aparelhos de chip único.
- Permissão `READ_PHONE_STATE` pedida sob demanda (só quando fizer sentido — nunca no
  checklist obrigatório de onboarding).
- 2 seletores independentes em Ajustes: chip para bloqueio de ligação, chip para bloqueio de
  SMS. Opções: **Todos os chips** (padrão, comportamento atual) ou um chip específico.
- Ligação/SMS chegando por um chip não selecionado passa direto (tratado como conhecido/
  permitido por esse filtro), sem entrar na lista de bloqueados.
- Fallback automático pra "Todos os chips" se o chip salvo deixar de existir (troca/remoção
  de SIM) ou se a permissão for revogada depois.

Fora do escopo:
- Rotear/bloquear chamadas ou SMS de **saída** (o app só trata entrada, que é o que já existe).
- Qualquer tratamento especial de eSIM além do que `SubscriptionManager` já expõe (tratado
  igual a chip físico).
- Testar em várias marcas/fabricantes — validação final fica restrita a 1 aparelho real
  dual-SIM disponível, mesma ressalva já existente no spec original para itens sensíveis a
  fabricante.

## 3. Decisões de design

### 3.1 Checar "tem dual-SIM" sem pedir permissão

`TelephonyManager.getPhoneCount()` informa quantos slots de chip o hardware tem, sem exigir
nenhuma permissão perigosa. Se retornar 1, a seção inteira de seleção de chip fica **oculta**
em Ajustes — no aparelho de chip único (a grande maioria dos casos) nada muda visualmente e
ninguém é incomodado com pedido de permissão novo.

### 3.2 `READ_PHONE_STATE` sob demanda, fora do onboarding obrigatório

Só quando o aparelho tem 2+ slots é que a seção aparece em Ajustes, com um botão "Ativar" pra
conceder `READ_PHONE_STATE` (necessário pra listar os chips ativos e mostrar nome/operadora de
cada um via `SubscriptionManager.getActiveSubscriptionInfoList()`). Sem essa permissão
concedida, a seção mostra só uma explicação + botão de ativar — o bloqueio continua
funcionando em "Todos os chips" (comportamento atual) nesse meio tempo.

Essa permissão **não** entra no checklist obrigatório de onboarding nem afeta o banner de
"proteção incompleta" — é um refinamento opcional, não um requisito pro bloqueio funcionar.

### 3.3 Identificar o chip de uma ligação

`CallScreeningService.onScreenCall` recebe `Call.Details`, que expõe `getAccountHandle()`
(`PhoneAccountHandle`). `SubscriptionManager.getSubscriptionId(PhoneAccountHandle)` (API 28+,
dentro do nosso `minSdk` 29) resolve pra qual `subscriptionId` aquele handle corresponde.
Compara-se esse valor com o `subscriptionId` salvo na preferência de "chip para ligação".

### 3.4 Identificar o chip de um SMS

O broadcast `SMS_RECEIVED_ACTION` carrega o `subscriptionId` da linha que recebeu a mensagem
em aparelhos dual-SIM. O nome exato da extra e o comportamento em diferentes versões do
Android **precisam ser confirmados contra o SDK instalado (compileSdk 34) durante a
implementação**, em vez de assumidos de memória.

### 3.5 Modelo de dados

DataStore, 2 chaves novas (Int, opcionais):
- `call_blocking_subscription_id`
- `sms_blocking_subscription_id`

Ausência da chave = "Todos os chips" (não usamos valor sentinela tipo `-1`). Escolher "Todos os
chips" de volta remove a chave.

### 3.6 Telas / UX

Ajustes ganha uma nova seção "Chip para bloqueio" (só visível se `getPhoneCount() > 1`):
- Sem `READ_PHONE_STATE`: texto explicando o que é a funcionalidade + botão "Ativar".
- Com permissão: 2 seletores (ligação / SMS), cada um com "Todos os chips" + uma linha por
  chip ativo (nome via `SubscriptionInfo.getDisplayName()`, com fallback pro número do slot,
  tipo "Chip 1"/"Chip 2", se o nome vier vazio).

## 4. Casos de borda

- **Aparelho de chip único** → seção não aparece, zero mudança de comportamento, nenhuma
  permissão nova pedida.
- **Chip salvo é removido/trocado depois** → some da lista de chips ativos → tratado
  automaticamente como "Todos os chips" de novo (sem travar, sem crash).
- **Chamada/SMS cujo chip não dá pra identificar** (raro — ex. conta VoIP sem SIM real) →
  tratado como "chip diferente do selecionado", ou seja, passa sem ser bloqueado por esse
  filtro específico. Decisão consciente: prioriza nunca bloquear por engano o chip que o
  usuário pediu explicitamente pra deixar de fora, mesmo que isso signifique deixar passar uma
  exceção rara sem chip identificável (o risco inverso — bloquear o chip "errado" — é pior
  porque contradiz uma escolha explícita do usuário).
- **`READ_PHONE_STATE` revogada depois de já ter escolhido um chip específico** → sem permissão
  pra confirmar se o chip ainda existe, mesma lógica de fail-safe: trata como "Todos os chips"
  até a permissão voltar.

## 5. Plano de testes / validação

- **Emulador** (chip único): confirmar que a seção fica oculta e que não há nenhuma regressão
  no bloqueio atual (continua valendo pra "todos os chips" implicitamente).
- **Aparelho real dual-SIM** (fora do emulador, único jeito de testar de verdade): detecção
  correta dos 2 chips e seus nomes; seleção específica por chip pra ligação e pra SMS
  (independentes); ligação/SMS chegando pelo chip **não** selecionado passa ilesa e não entra
  na lista; fallback ao remover fisicamente um dos chips; revogar `READ_PHONE_STATE` depois de
  configurado não trava a proteção.

## 6. Riscos e decisões confirmadas

- Opção "Todos os chips" mantida ao lado da seleção específica — confirmado pelo usuário
  (2026-07-08).
- Seletor de ligação e de SMS são independentes, podem apontar pra chips diferentes — pedido
  explícito do usuário.
- Nome exato da extra de `subscriptionId` no broadcast de SMS e requisitos de permissão de
  cada API usada aqui precisam ser confirmados contra o SDK instalado durante a implementação
  (não assumidos de memória) — se algum não se comportar como esperado, o plano de
  implementação registra o ajuste necessário como tarefa, sem mudar o desenho geral.
- Teste real só é possível em 1 aparelho dual-SIM disponível — comportamento pode variar em
  outros fabricantes.

## 7. Próximos passos

Aprovado — seguir pro plano de implementação (skill `planejar`).
