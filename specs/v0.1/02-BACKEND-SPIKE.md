# SPEC v0.1 — Spike de backend Fake Player

**Status:** PENDENTE DE EXECUÇÃO | **Issue principal:** [#1](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/1)

## Hipótese

O conjunto Carpet 1.4.112 + SecondBrainEngine 1.1.8 (variante 1.20.1) pode fornecer jogador artificial, navegação server-side e tarefas AltoClef num servidor Fabric 1.20.1.

Isto é uma **hipótese de integração**, não uma dependência aprovada.

## Evidência no código upstream

- [SecondBrainEngine versions/1.20.1](https://github.com/sailex428/SecondBrainEngine/blob/main/versions/1.20.1/gradle.properties): MC 1.20.1, Yarn 10, FAPI 0.92.6, Carpet 1.4.112.
- [Exemplo de uso](https://github.com/sailex428/SecondBrainEngine): `NPCSpawner.spawn`, `BaritoneAPI.getProvider().getBaritone(npc)`, `AltoClefController.runUserTask(...)`.
- [SecondBrain 1.20.1](https://github.com/sailex428/SecondBrain/blob/main/versions/1.20.1/gradle.properties): Engine `1.20.1-1.1.8`.
- Nosso mod fixa MC 1.20.1/Yarn 10, Loader 0.16.14, FAPI 0.92.12: **confirmar matriz**, não misturar bibliotecas cegamente.
- Upstream LGPL-3.0, Carpet MIT; rever dependências transitivas, forma de ligação e arquivos de licença antes da distribuição.

## Plano do experimento (branch `spike/secondbrainengine-1201`)

1. Criar mundo e diretório de execução **descartáveis**; registrar os SHAs/tags upstream e hashes dos JARs.
2. Resolver com Gradle (não colocar JAR de terceiros no código sem licença); testar apenas combinações declaradas como compatíveis.
3. Inicializar servidor dedicado e servidor integrado (cliente Fabric), registrar warnings de mixins e loaders.
4. Criar e remover um Fake Player no local do operador. Verificar skin/nome, sync, UUID e ausência de ghost players.
5. Navegar 10 blocos, desviar de parede de 2 blocos, subir 1 degrau e permanecer próximo a um jogador em movimento.
6. Quebrar **um** bloco de pedra com picareta equipada e timing vanilla; verificar bloco removido, durabilidade, drops e inventário.
7. Repetir com ferramenta ausente, lava ao lado, bloco protegido, chunk descarregado e destino impossível.
8. Interromper durante movimento e quebra. Confirmar que controles/ações realmente param e futuras antigas não retomam.
9. Reiniciar 3 vezes com mesmo UUID e conferir jogadores, inventário e entidades duplicadas.
10. Executar ao menos 10 tentativas de navegação do cenário padrão; registrar resultados, causas de falha, tick cost e log.

## Go / no-go

**GO:** carregamento limpo MC 1.20.1; movimento sem teleport arbitrário; quebra física com drops e conservação; cancelamento eficaz; sem crash/duplicações após restart; tolerância a obstáculos documentada.

**NO-GO:** não compila/runServer; conflito estrutural de mixins; depende de cliente real; viola regras de mineração; não cancela; inventário duplica; integração exige fork extenso e difícil de manter.

**Se parcial:** registrar falhas, criar `CarpetNativeNpcController` restrito à locomoção mínima, ou isolar somente navegador do engine. Mineflayer entra como *último* fallback separado, pois exige bot externo/logon. Não trocar arquitetura de produto para contornar um erro sem registrar ADR.

## Segurança na integração

- **Não habilitar rotinas de container do engine na v0.1.** Auditar `PickupFromContainerTask` e outras antes da v0.2.
- Armazenar versões, dependências e notices num arquivo de licenças antes de distribuir binários.
- Não copiar código de AI-Player (GPL-2.0) para a base BSD-3-Clause.
- Bloquear ações físicas de NPC sem validação de permissões no nosso serviço.
- Separar qualquer shader/gui/cliente opcional: servidor deve carregar sozinho.

## Relatório obrigatório

Criar `specs/v0.1/evidence/BACKEND-SPIKE-RESULT.md` **quando rodar o experimento**, contendo: data, commits, matriz de versões, logs/links CI, casos aprovados/reprovados, decisão GO/NO-GO, riscos de licença e assinaturas de quem validou. Sem relatório, a arquitetura permanece PROPOSTA.
