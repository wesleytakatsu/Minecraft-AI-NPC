# SPEC v0.1 — Registro de decisões técnicas (ADRs)

**Status:** decisões propostas/condicionais até verificação em jogo.

## ADR-001 — NPC nativo do servidor (ACEITO PARA SPIKE)

**Contexto:** queremos companheiro sem outro cliente aberto. **Decisão:** testar Carpet + SecondBrainEngine com `NpcController` independente. **Alternativas:** Mineflayer, cliente Baritone, entidade custom. **Reavaliação:** após [BACKEND-SPIKE](02-BACKEND-SPIKE.md), nunca por preferência abstrata.

## ADR-002 — Código determinístico no loop de gameplay (ACEITO)

LLM serve para conversa/esclarecimento, JEV classifica pedidos ambíguos, Java executa movimento, risco, combate futuro e crafting futuro. Nenhuma decisão via rede por tick.

## ADR-003 — Prefixo opt-in `@nexus` (ACEITO)

Somente mensagens explicitamente destinadas ao NPC entram no fluxo de conversa; status, stop e comandos críticos não dependem de IA. Esse caminho economiza tokens e evita encaminhar chat geral de outros jogadores como observado no SecondBrain.

## ADR-004 — Uma missão ativa e um NPC na v0.1 (ACEITO)

Evitar concorrência e bugs de persistência; arquitetura permite generalizar depois. Nova ordem substitui outra apenas com cancelamento explícito e rollback seguro das ações pendentes.

## ADR-005 — Persistir snapshots; não auto-retomar tarefa destrutiva (ACEITO)

Desligamento transforma missão inacabada em `PAUSED`; após restart não voltar a cavar sem nova validação/ordem. Dados do mundo devem ser rechecados para impedir duplicação.

## ADR-006 — Backend e licenças (PENDENTE DE GO/NO-GO)

Reutilização do SecondBrainEngine (LGPL-3.0) deve passar por revisão de licenças e testes de compatibilidade; evitar copiar GPL-2.0 do AI-Player. Inventário e baús ficam desabilitados até resolver risco de duplicação upstream.

## ADR-007 — Primeira release `v0.1.0` é manualmente aprovada (ACEITO)

`main` gera pre-releases, mas tagging estável exige [TEST-PLAN](06-TEST-PLAN.md). Não liberar release final só por smoke de startup.

## ADR-008 — GameTest realmente executado, não só teste unitário (ACEITO)

Adicionar framework GameTest compatível com Fabric API 1.20.1, validar entidades/blocos/inventário/drops, rodar no CI. Não copiar configuração de versão recente do Loom sem teste.

## ADR-009 — Transferência mínima de picareta, sem GUI (ACEITO)

A v0.1 precisa de ferramenta real para `DIG_TUNNEL`. Para evitar introduzir baús/crafting ou dar itens gratuitos, incluir `/nexus handover`: mover exatamente uma picareta da mão do dono para o Fake Player, preservando NBT/durabilidade e verificando conservação. A GUI de inventário fica para versão futura. Issue [#10](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/10).

## Perguntas abertas e experimento para fechá-las

| Pergunta | Experimento ou evidência necessária |
| --- | --- |
| SecondBrainEngine 1.1.8 funciona com Loader/FAPI atuais? | P1 na matriz reproduzível |
| Precisaremos de cliente com mod ou basta instalar servidor? | Smoke com cliente Vanilla/Fabric e dedicado/integrado |
| O Fake Player mantém inventário/UUID ao reiniciar? | 3 restarts com inspeção no mundo |
| Comando de chat com `ALLOW_CHAT_MESSAGE` provoca aviso de assinatura? | T-03 com 2 jogadores |
| Como verificar proteção territorial? | Teste da API/mod escolhido ou bloquear ação insegura |
| O que acontece quando ferramenta quebra no meio do túnel? | T-08/T-09 com durabilidade mínima |
| Como registrar GameTests e resultado XML em 1.20.1? | P6 spike de harness GameTest |
| Qual LLM e versão da API JEV serão suportadas oficialmente? | Adapter real, testes com mock e smoke de integração opt-in |
| Qual overhead de um NPC em servidor dedicado? | Baseline reproduzível no T-19 |

## Alterações arquiteturais

Toda mudança importante deve adicionar `ADR-009`, etc., indicando data, decisão, alternativas, riscos, evidência e migração. Sem decisões ocultas em comentários de código.
