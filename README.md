# Anti-Spam

App Android que bloqueia automaticamente ligações de números que não estão nos seus contatos —
sem tocar, sem notificação nenhuma aparecendo.

## O que faz

- **Ligações de desconhecidos** são rejeitadas na hora (via `CallScreeningService`, sem precisar
  virar o discador padrão do aparelho).
- Lista de **ligações bloqueadas**, com horário e número.
- Toggle pra ligar/desligar o bloqueio.
- Ação "permitir este número" pra liberar um número específico direto da lista.
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

O desenho da arquitetura (por que `CallScreeningService`, modelo de dados, etc.) está
documentado em [`docs/superpowers/specs/`](docs/superpowers/specs/) — inclui também o histórico
de features que foram tentadas e removidas (bloqueio de SMS, seleção de chip em dual-SIM), com o
motivo de cada remoção.
