# Changelog

Mudanças do mod, da mais nova para a mais antiga. Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/).

## [Não lançado]

### Adicionado
- **Etapa 1: cerimônia, grimório de fogo e barra de mana** (#2).
  - Regras puras de sorteio, chamado, mana, conjuração, queimadura e quests.
  - Núcleo: registros, raiz de comandos, configs, rede e helpers de teste; aba criativa com o altar e o ovo do sacerdote.
  - Motor de quests por datapack, com progresso, eventos e comandos.
  - Grimório: elemento fogo, chamado, cerimônia do sacerdote no altar e grimório flutuante; tela de escolha livre de elemento com um card por elemento; conquista com a imagem do grimório ao recebê-lo; grimório no HUD.
  - Cerimônia: grimório cresce nas estantes, voa mais devagar até o jogador e some ao chegar.
  - Grimório flutuante cresce e encolhe com brilho e fica escondido em primeira pessoa.
  - Habilidades: mana, conjuração na tecla Z e bola de fogo com acúmulos de queimadura; HUD de mana à esquerda da hotbar com recarga do slot Z; bola de fogo explode no impacto sem quebrar blocos e some no fim do alcance.
  - VFX: runtime próprio para partículas do Blockbench; explosão da bola de fogo e brilho do grimório.
  - Mundo: Torre do Grimório, vila inicial no spawn e zona segura (sem patrulhas, phantoms nem raids); todo jogador nasce na praça da vila, com raio de spawn 4; torre e igreja refeitas a partir de construções da comunidade; grimórios da torre embaralhados, girando em volta do centro e intocáveis, sem o de cinco folhas; trilha do chamado fica até o jogador chegar na torre.
  - Sacerdote com modelo próprio (#3), que fica no seu posto.
- **Modelos 3D GeckoLib** gerados por script no Blockbench (#3, PR #1): grimório, bola de fogo, magos, espadas, vassoura, grifo, altar, construções, manto, estandarte, forma demônio e poses do jogador.
- Run `client2` no Gradle para testar multiplayer por LAN com um segundo jogador (`Dev2`).

### Corrigido
- Conjuração negada para espectadores e mortos, falhas reportadas e ticks de queimadura corrigidos.
- Chamado conta só a vida perdida e fica privado; cerimônia sem magia ficou segura.
- Vila inicial garantida, sem patrulhas e phantoms, mantendo o baú bônus.
- Ícone do grimório virado com o trevo da capa para a frente.
- Trilha do chamado aponta para a torre, não para o início da estrutura.

### Documentação
- ADRs da arquitetura da etapa 1, partículas do Blockbench com runtime próprio (0004) e construções baseadas em builds da comunidade (0005).
- HUD de mana, ícones de GUI, tela de escolha de elemento e sincronização de attachments no fluxo de magia.
