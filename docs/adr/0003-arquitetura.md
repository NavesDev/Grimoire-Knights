# 0003 Arquitetura por funcionalidade com regras puras

**Status:** aceita (2026-10-28)

**Decisão:** detalhes em [`docs/arquitetura.md`](../arquitetura.md).
- Pacotes por funcionalidade (`grimoire`, `progression`, `ability`...), com subpacotes por tipo técnico dentro; `core` para o que é geral.
- Regras do jogo em `rules/`, Java puro testado com JUnit.
- Balanceamento em config `SERVER` por funcionalidade.
- Magias e espadas: uma classe por habilidade, interface `Ability` única, registro próprio.
- Missões: motor genérico em `quest`, logo acima de `core`. Missão em JSON de datapack; tipos de objetivo e recompensa são classes registradas pelas funcionalidades donas; comportamento especial ouve eventos da missão.

**Alternativas descartadas:**
- Pacotes por tipo técnico (`block/`, `item/`, `entity/`): espalha cada sistema por vários pacotes.
- Regras dentro dos handlers: só testável por GameTest.
- Balanceamento em datapack JSON ou constantes: mais código de carga ou rebuild a cada ajuste.
- Habilidades compostas por JSON: abstração cedo demais para 3 magias por elemento.
- Missão especial em código próprio: cada nova tarefa exigiria um sistema novo.
