# SPEC v0.1 — Missões, ações e mineração

**Status:** PROPOSTO | **Issue:** [#2](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/2)

## Modelo de missão

```text
IDLE ──► QUEUED ──► PLANNING ──► RUNNING ──► COMPLETED
                            │       │
                            │       ├──► PAUSED ──► RUNNING (revalidar)
                            │       ├──► FAILED
                            │       └──► CANCELLED
                            └──► FAILED / CANCELLED
```

Estados terminais: `COMPLETED`, `FAILED`, `CANCELLED`. `STOP` faz transição idempotente para CANCELLED e cancela fisicamente ferramenta/caminho. Cancelamento de uma chamada JEV/LLM pendente invalida o token da missão. O status deve dizer **quantidade verificada**, não progresso imaginado.

Cada missão contém: `missionId`, `ownerUuid`, `npcUuid`, `type`, `state`, `args`, `startedAt`, `updatedAt`, `completedUnits`, `totalUnits`, `errorCode`, `schemaVersion`. Exemplo de snapshot no [schema](schemas/mission-snapshot.schema.json).

## Resultados tipados

`SUCCESS`, `CANCELLED`, `NO_TOOL`, `NO_PERMISSION`, `UNREACHABLE`, `UNSAFE`, `BLOCKED`, `TIMEOUT`, `CHUNK_UNLOADED`, `NPC_MISSING`, `INTERNAL_ERROR`. Não usar exceção como resultado comum. Contagem já realizada permanece registrada.

## FOLLOW

- Objetivo: manter o dono ao alcance-alvo de 2–4 blocos, sem ficar "girando" no mesmo ponto.
- Atualizar caminho se dono deslocar significativamente; não replanejar a cada tick sem necessidade.
- Se dono desconectar ou mudar de dimensão, pausar/parar e informar motivo; não teleportar.
- A opção `@nexus me siga` substitui tarefa anterior se autorizada, com cancelamento explícito.
- STOP paralisa follow imediatamente, mesmo com AI indisponível.

## COME_HERE

- Destino é posição recente do jogador autorizado na mesma dimensão; tolerância definida no skill.
- Retornar `SUCCESS` somente ao alcançar tolerância com posicionamento real.
- Se caminho impossível, devolver `UNREACHABLE` após tentativas limitadas; nunca ficar preso infinitamente.
- Evitar caminhos perigosos quando existirem alternativas; política de risco passa por código.

## DIG_TUNNEL — v0.1

O formato é fixo: **1 bloco de largura, 2 de altura, N segmentos de comprimento**, `N` no intervalo 1..32 (comando mínimo demonstrado: 10). Ângulo horizontal é discretizado em Norte/Sul/Leste/Oeste **no início** e não muda durante a execução.

**Pré-condições:**
- NPC disponível, missão autorizada, mesma dimensão, posição inicial segura.
- Ferramenta válida no inventário (equipar o que é permitido; nunca criar item).
- Verificar altura mínima, alcance, bordas/chunks e blocks protegidos/indestrutíveis.
- Sem lava/água em locais expostos que ameacem fluxo: interromper com `UNSAFE`; nunca destruir contêiner, bloco de entidade ou propriedade sem permissão.

**Passos determinísticos (para segmento `i` de 1 até `N`):**
1. Calcular os dois `BlockPos` alvo (pés/cabeça) para o segmento a partir do ponto inicial + direção cardinal congelada.
2. Planejar posição segura e alcançar distância legítima de mineração. Falhou → `UNREACHABLE`.
3. Verificar permissões, risco local, dureza, ferramenta, estado de chunk e oxigênio antes da quebra.
4. Executar quebra pelo controlador com regras vanilla: delay/dureza, ferramenta/durabilidade, drops.
5. Confirmar que os dois espaços alvo são seguros/escavados, que drops não apareceram magicamente e que não foram alterados blocos além do perímetro autorizado.
6. Atualizar `completedUnits=i` **somente após confirmação física**. Repetir até concluir.
7. Resultado terminal: `COMPLETED` apenas com 2*N espaços livres autorizados no segmento de corte; registrar blocos previamente vazios separadamente.

**Semântica de coleta:** drops podem cair no chão e serem recolhidos pelo NPC se alcançáveis; se não forem recolhidos, informar que itens permaneceram no mundo. Nunca declarar "coletei X" só porque blocos sumiram.

**STOP:** ao ser processado no tick do servidor, não inicia novas quebras nem move; interromper ações de input/timers em andamento conforme suporte do backend. O executor não deve esperar HTTP.

**Sem ferramenta:** falhar com `NO_TOOL` e não alterar blocos; não fabricar ferramentas na v0.1.

## Resiliência e persistência

- O caminho de navegação não é persistido; recalculado após restart.
- Uma missão `RUNNING` no shutdown vira `PAUSED` recuperável, com campos de progresso e efeitos reais já confirmados.
- Na inicialização, revalidar mundo, dono, NPC, ferramenta e região **antes** de retomar; default da v0.1 é **aguardar nova confirmação do dono** (`@nexus continuar` apenas se essa opção for implementada) ou reportar pausa e exigir nova missão explícita. Não executar em background silenciosamente.
- A retomada automática é **fora do escopo**; preservar o snapshot para inspeção e reaproveitamento futuro.
- Se posição ou progresso contradizem o mundo, informar falha de consistência, não inventar progresso.

## Inspirações

SecondBrainEngine `Task`/prioridades, AltoClef `isFinished()` e subtarefas, AI-Player `NavigationService` com detecção de stuck/replan, Voyager verificação de skill. Ver [docs/PROJECT-RESEARCH.md](../../docs/PROJECT-RESEARCH.md). Não copiar código GPL e não usar ações que editem o mundo diretamente sem mecanismos de jogo.
