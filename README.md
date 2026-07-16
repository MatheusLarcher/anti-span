# Anti-Spam 📵

Cansado de ligação de telemarketing, golpe e número desconhecido te incomodando?

O **Anti-Spam** bloqueia automaticamente **toda ligação de quem não está nos seus contatos**.
O telefone nem toca, nenhuma notificação aparece — pra você, é como se a ligação nunca
tivesse existido. Quem está salvo nos seus contatos liga normalmente.

## 📥 Baixar o app

**[⬇️ Clique aqui para baixar o APK](anti-span.apk?raw=true)**

> É de graça, sem anúncio, sem cadastro e funciona 100% offline — nada sai do seu celular.

## 📲 Como instalar

1. Baixe o arquivo `anti-span.apk` pelo link acima (pode baixar direto no celular).
2. Abra o arquivo baixado (fica na pasta **Downloads**).
3. O Android vai avisar que o app não veio da Play Store — toque em **"Instalar mesmo assim"**
   (ou libere a opção "instalar apps desconhecidos" quando ele pedir).
4. Se aparecer um aviso do **Play Protect**, toque em **"Mais detalhes"** → **"Instalar mesmo
   assim"**. Esse aviso aparece porque o app não está na Play Store, é normal.
5. Abra o app e siga o passo a passo da primeira tela: são 2 permissões necessárias
   (**Contatos** e **Bloqueio de ligação**) e 1 recomendada (ignorar otimização de bateria).

Pronto! A partir daí, ligação de número desconhecido não toca mais.

## ✨ O que o app faz

- 🚫 **Bloqueia na hora** qualquer ligação de número que não está nos seus contatos — sem tocar,
  sem vibrar, sem notificação.
- 📋 **Lista de bloqueadas**: você vê dentro do app todos os números bloqueados, com data e hora.
- ✅ **Permitir um número**: bloqueou alguém que você queria receber? Um toque em
  "Permitir este número" e ele passa a ligar normalmente.
- 🎚️ **Botão de liga/desliga**: esperando uma ligação importante de um número novo (entrega,
  médico, entrevista)? Desligue o bloqueio por um momento e ligue de volta depois.

## ❓ Perguntas comuns

**Quem está nos meus contatos continua ligando normal?**
Sim. O app só bloqueia quem **não** está salvo nos seus contatos.

**A pessoa bloqueada sabe que foi bloqueada?**
Não. Pra ela, a ligação simplesmente cai (como se estivesse ocupado ou fora de área).

**O app lê minhas mensagens ou acessa a internet?**
Não. Ele só olha a sua lista de contatos pra saber quem é conhecido, e tudo fica salvo apenas
no seu celular.

**Funciona em qualquer Android?**
Funciona do Android 10 em diante.

---

## 🛠️ Para desenvolvedores

Kotlin + Jetpack Compose (Material 3), Room, DataStore. Bloqueio via `CallScreeningService`
(papel `ROLE_CALL_SCREENING`), sem virar discador padrão. `minSdk` 29.

```
./gradlew assembleDebug
```

O desenho da arquitetura está em [`docs/superpowers/specs/`](docs/superpowers/specs/) — inclui
o histórico de features tentadas e removidas (bloqueio de SMS, seleção de chip em dual-SIM) com
o motivo de cada remoção.
