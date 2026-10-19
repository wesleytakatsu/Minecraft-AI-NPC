# SPEC v0.1 — Plano de testes e critérios de release

**Status:** PROPOSTO. Ainda não existe suite de gameplay: o CI atual verifica JUnit, JAR e inicialização do servidor, **não** executa NPC/ações físicas.

## Pirâmide de testes

1. **JUnit puro** (segundos): parser, classificação local, ACL, state machine, limites de parâmetros, validador de JSON, progressos e erros.
2. **Testes de contrato com mocks**: HTTP JEV/LLM, timeout, fila, cancelamento de resposta obsoleta e comando malicioso.
3. **Fabric GameTest em servidor**: Fake Player, blocos, navegação, durabilidade, drops, persistência e segurança. Verificar como registrar/configurar o GameTest **na Fabric API 1.20.1** antes de copiar exemplos de versões novas. Documentação de referência: [Fabric GameTest API (1.20)](https://maven.fabricmc.net/docs/fabric-api-0.78.0%2B1.20/net/fabricmc/fabric/api/gametest/v1/package-summary.html), [Fabric Docs Automated Testing](https://docs.fabricmc.net/develop/automatic-testing).
4. **Teste manual com dois clientes**: chat assinado, permissões, aparência, follow e colocação/remoção do companion.
5. **Observabilidade e regressão**: logs sem secrets, medição de tick, restart, nenhuma duplicação.

## Casos obrigatórios e vínculo de requisito

| ID | Caso | Requisitos | Passa quando |
| --- | --- | --- | --- |
| T-01 | Inicialização dedicada e integrada | FR-01, NFR-06 | Sem crash ou mixin fatal; comandos disponíveis |
| T-02 | Spawn duplo e remoção | FR-01, FR-02, FR-16 | 1 NPC com UUID estável, sem fantasma |
| T-03 | Chat com 2 jogadores | FR-03, FR-04 | Chat geral intocado; `@nexus` não vaza; controle não autorizado negado |
| T-04 | Follow em campo + obstáculos | FR-05 | Segue mantendo distância, sem teleport nem loop infinito |
| T-05 | Come here | FR-06 | Alcança tolerância ou retorna `UNREACHABLE` |
| T-06 | STOP durante navegação | FR-07 | Nenhum movimento planejado continua depois do cancelamento processado |
| T-07 | Túnel 10 segmentos | FR-08, FR-09 | Exatamente corredor 1x2, sem destruição fora do alvo, progresso 10/10 |
| T-08 | Sem ferramenta ou ferramenta incompatível | FR-09 | Nenhum bloco incorretamente quebrado; `NO_TOOL` |
| T-09 | Durabilidade e drops | FR-09 | Durabilidade cai corretamente e itens só entram no inventário ao serem coletados |
| T-10 | Lava, água, bedrock e bloco protegido | FR-10 | Aborta com razão; não quebra bloco vedado |
| T-11 | STOP durante quebra | FR-07, FR-08 | Sem novas quebras após parada; progresso parcial verdadeiro |
| T-12 | Status parcial/falha | FR-11 | Contagem e erro iguais à inspeção real do mundo |
| T-13 | Restart repetido | FR-12, FR-16 | Estado consistente e sem NPC/inventário duplicado em 3 reinícios |
| T-14 | LLM/JEV offline | FR-13, FR-14 | Comandos locais funcionam sem chave/rede |
| T-15 | Mock LLM/JEV | FR-13, FR-14, FR-15 | Chat mock responde, JEV devolve intent validada, malformed JSON falha seguro |
| T-16 | Pergunta, ordem ambígua, prompt injection | FR-15 | Não altera o mundo sem ordem válida |
| T-17 | Resposta AI após cancelamento | FR-07, FR-14 | Resposta tardia nunca inicia nova missão |
| T-18 | Desligamento / lifecycle | FR-16 | Controladores e threads liberados sem seguir executando |
| T-19 | FPS/TPS e carga de 1 NPC | NFR-01, NFR-02, NFR-04 | Sem bloqueio de tick por HTTP ou loop de replanejamento permanente |
| T-20 | JAR reprodutível e release | NFR-07 | Mesmo commit testado e publicado com checksum e versão correta |

## Massas de teste / fixtures

- Mundo `flat` ou área montada em GameTest com piso de pedra e parede de 2 blocos.
- Um corredor de 10 posições 1x2 de pedra, picareta adequada e inventário inicialmente vazio.
- Cenários isolados de lava, água, baú/entidade-bloqueio, chunk descarregado, ferramenta no limite da durabilidade e pessoa não autorizada.
- Mocks HTTP: sucesso, 429, 500, conexão indisponível, JSON inválido, resposta com intent não permitida e resposta atrasada.

## Regras de evidência

Para **cada T-xx**, capturar resultado automatizado (`junit.xml` ou equivalente) ou passo manual documentado com execução/ambiente, screenshot/log sanitizado e issue associada. Sem evidência, marcar pendente mesmo que a classe esteja no projeto.

Regra para testes de bloco: utilizar snapshots de blocos antes/depois; para itens, comparar inventário + drops reais no mundo; para mission state, conferir dados persistidos. Um teste que só verifica mensagem de chat **não** comprova mineração ou navegação.

## Gate automático e gate estável

**Em PR e push de main:** JUnit + contrato mock + build + smoke de inicialização + GameTests (assim que disponíveis). Qualquer erro bloqueia publicação da pré-release do commit.

**Para tag estável `v0.1.0`:**
- Todos os testes T-01..T-20 concluídos sem falhas bloqueantes.
- T-04..T-11 repetidos em servidor real com obstáculos e limites; anexar vídeo/log quando necessário.
- Teste de integração com **um** provedor LLM e teste real do JEV *se* a compatibilidade com fornecedor for anunciada na release; sem credenciais em CI.
- Nenhuma duplicação/perda de itens observada nos casos de teste; nenhuma ação após STOP.
- Manifests de licenças/avisos de componentes, instruções e changelog atualizados.
- Release notes automatizadas **precisam deixar de dizer** que não existe NPC/IA após essas funcionalidades serem implementadas.
- Revisão manual final de um JAR da release com Minecraft 1.20.1.

### Critério de desempenho inicial (observação, não SLA universal)

Comparar tempo de tick do servidor em mundo controlado com zero e um NPC durante follow, movimento e túnel; documentar hardware, distância, chunks e medidas por percentis. Tratar travamentos, aumento contínuo de memória e grandes spikes recorrentes como bloqueantes; thresholds numéricos só depois do primeiro baseline medido.

## Como medir progresso

Planilha/tabela de execução versionada em `specs/v0.1/evidence/TEST-RESULTS.md` quando existirem testes; conservar links diretos para workflows. Não escrever `PASS` por antecipação.

## Integração com pipeline existente

O CI [`build.yml`](../../.github/workflows/build.yml) já produz pré-releases por commit na `main`. A fase de testes deve adicionar task de GameTest e upload de relatórios/logs **antes** do job de publicação, para que a release represente código executado no Minecraft, não apenas compilado.
