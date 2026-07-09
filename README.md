# Anti-Spam

App Android que bloqueia automaticamente ligações e SMS de números que não estão nos seus
contatos — sem tocar, sem notificação nenhuma aparecendo.

## O que faz

- **Ligações de desconhecidos** são rejeitadas na hora (via `CallScreeningService`, sem precisar
  virar o discador padrão do aparelho).
- **SMS de desconhecidos** têm a notificação suprimida — a mensagem continua chegando normal no
  seu app de mensagens de sempre, só a notificação que não aparece.
- Lista de **ligações bloqueadas** e de **SMS bloqueados** (esse último em formato de chat, por
  número).
- Toggle independente pra ligar/desligar o bloqueio de ligação e o de SMS.
- Ação "permitir este número" pra liberar um número específico direto da lista.
- Em aparelhos com **2 ou mais chips**, dá pra escolher em qual chip cada bloqueio (ligação e
  SMS, separadamente) deve atuar.
- Onboarding guiado com checklist das permissões necessárias.

## Stack

Kotlin + Jetpack Compose (Material 3), Room (SQLite), DataStore. `minSdk` 29 (Android 10).

## Build

```
./gradlew assembleDebug
```

Gera o APK em `app/build/outputs/apk/debug/`. Instalação é direta no aparelho (sideload) — o app
não está na Play Store.

## Design

O desenho da arquitetura (por que `CallScreeningService`, por que notification-listener em vez
de virar app de SMS padrão, modelo de dados, etc.) está documentado em
[`docs/superpowers/specs/`](docs/superpowers/specs/).
