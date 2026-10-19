# SPEC v0.1 — Arquitetura

**Status:** PROPOSTO. O código atual só inicializa Fabric, registra `/nexus` e responde ao prefixo `@nexus` por regras.

## Fluxo

```text
Chat (@nexus) / Comandos (/nexus)
        │
        ▼
ChatGateway ──► AuthPolicy + limites + CANCEL prioritário
        │
        ▼
IntentRouter ──► regras locais ─────┐
        ├──────── JEV (opcional) ────┤
        └──────── LLM (conversa) ────┤
                                   ▼
                           MissionManager
                            │           │
                    SkillScheduler   Status/Storage
                            │
                            ▼
                     NpcController API
                      │            │
           Carpet + SecondBrainEngine    Fallback (após spike)
                       │
                       ▼
                 Minecraft Server
```

## Componentes e contratos

| Pacote planejado | Responsabilidade | Limite de confiança |
| --- | --- | --- |
| `chat` | Prefixo, roteamento, resposta ao jogador, rate-limit | Texto do usuário é não confiável |
| `auth` | Dono/op, comandos e autorização de ações | Nunca delegar a modelos |
| `ai` | `ConversationModel`, `IntentClassifier`, timeout e custo | Saída da IA sempre validada |
| `mission` | Estado de missão, progressos, resultados, prioridades | Nenhum efeito físico direto |
| `skills` | Follow, move, tunnel, stop, verificação | Executa somente objetivos validados |
| `npc` | Interface única do backend e snapshot | Implementação substituível |
| `storage` | UUID, world/dimension, checkpoints, versão do schema | Dados atômicos/versionados |
| `config` | Defaults e segredos locais | Não expor a cliente/chat/LLM |

Proposta de tipos (são contratos conceituais, **ainda não implementados nem compilados**):

```java
interface NpcController {
    NpcSnapshot snapshot();
    CompletionStage<ActionResult> moveTo(BlockPos target, int tolerance);
    CompletionStage<ActionResult> breakBlock(BlockPos pos);
    void cancelAll();  // cancela pathfinding, input e ação pendente
    void dispose();
}

interface Skill {
    ValidationResult validate(SkillContext ctx, SkillArgs args);
    void start(SkillContext ctx);
    SkillResult tick(SkillContext ctx);
    void cancel(SkillContext ctx);
}

interface IntentClassifier {
    CompletionStage<IntentResult> classify(String text, Set<Intent> allowed);
}

interface ConversationModel {
    CompletionStage<String> respond(ConversationContext context);
}
```

Os `CompletionStage` não autorizam acessar `World` em threads externas; suas conclusões/eventos devem ser serializados de volta ao servidor. `cancelAll` é idempotente e invalida respostas antigas de futures.

## Concorrência e tick

- `ServerTickEvents.END_SERVER_TICK` ou ponto equivalente chama o executor **sem bloquear** o servidor.
- Um *tick* faz no máximo uma fatia limitada do agendamento; operações longas têm estado retomável.
- Requisições HTTP executam em pool limitado, com timeout e uma solicitação pendente por NPC/conversa.
- Ao receber resposta de IA, use `server.execute(...)` para validar se o NPC ainda existe, se o usuário ainda possui permissão e se a solicitação não foi cancelada.
- Identificador monotônico por sessão ou `requestId` invalida respostas antigas após nova ordem, logout ou restart.
- Dados de posição inventário/breaking são lidos/salvos apenas da thread autoritativa; logs não contêm secrets.

## Prioridades determinísticas

1. Remoção administrativa e `STOP` explícito.
2. Falhas críticas / risco de morte / proteção de blocos.
3. Recuperação de stuck e segurança de caminho.
4. Missão de usuário (follow, move, tunnel).
5. Idle/look-at-owner.

Inspirado na seleção de `TaskChain` do [SecondBrainEngine](https://github.com/sailex428/SecondBrainEngine/blob/main/src/main/java/me/sailex/altoclef/tasksystem/TaskRunner.java), mas com STOP acima de todas as cadeias.

## Por que `NpcController` deve ser separada

Baritone é primariamente orientado a cliente; no servidor, SecondBrainEngine usa adaptação própria do Automatone. Se o engine for incompatível ou tiver bugs de inventário, trocamos o adapter, não as missões, chat, armazenamento ou testes.

**Não adotar automaticamente os controladores de baú do engine:** existe possível duplicação na `PickupFromContainerTask` observada em [docs/PROJECT-RESEARCH.md](../../docs/PROJECT-RESEARCH.md). A v0.1 não permite acesso a baús.

## Mapeamento das fontes de conhecimento

- **Carpet**: ciclo de vida de jogadores artificiais.
- **SecondBrainEngine**: navegação no servidor, tarefas e prioridades.
- **AltoClef**: decomposição em subtarefas, `isFinished()` e catálogo de recursos.
- **AI-Player**: detecção de stuck, orçamento de planejamento e replanejamento.
- **SecondBrain**: ciclo de vida de NPC/LLM e filas limitadas; rejeitar broadcast geral de chat.
- **Mineflayer**: alternativa futura por processo externo (não padrão).
- **Voyager**: skills recuperáveis e validação de resultados, sem executar código de LLM.

As escolhas condicionais estão em [02-BACKEND-SPIKE.md](02-BACKEND-SPIKE.md) e [08-DECISIONS.md](08-DECISIONS.md).
