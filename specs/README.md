# Especificações do Minecraft AI NPC

Este diretório é a **fonte de verdade para o comportamento esperado**, os critérios de aceite e as decisões de engenharia. A implementação pode ser diferente dos exemplos, mas não pode violar os contratos sem uma mudança de spec documentada.

**Estado atual (2026-10-19):** v0.0.1 existente (chat e comandos determinísticos). As especificações da v0.1 são **planejadas, não implementadas**. A pesquisa em [docs/PROJECT-RESEARCH.md](../docs/PROJECT-RESEARCH.md) revisou o código de SecondBrain, SecondBrainEngine, Carpet, AltoClef, Baritone, AI-Player, Mineflayer e Voyager.

## Versões e documentos

| Especificação | Conteúdo |
| --- | --- |
| [v0.1/00-PRODUCT.md](v0.1/00-PRODUCT.md) | Produto, comandos, escopo, requisitos identificados |
| [v0.1/01-ARCHITECTURE.md](v0.1/01-ARCHITECTURE.md) | Componentes, threads, limites, interfaces |
| [v0.1/02-BACKEND-SPIKE.md](v0.1/02-BACKEND-SPIKE.md) | Prova de viabilidade Carpet + SecondBrainEngine e decisões de fallback |
| [v0.1/03-CHAT-AND-AI.md](v0.1/03-CHAT-AND-AI.md) | Prefixo, autorização, roteamento, JEV e LLM |
| [v0.1/04-MISSIONS-AND-SKILLS.md](v0.1/04-MISSIONS-AND-SKILLS.md) | Máquina de estados, ações físicas e túnel |
| [v0.1/05-PERSISTENCE-CONFIG-SECURITY.md](v0.1/05-PERSISTENCE-CONFIG-SECURITY.md) | Configuração, dados persistentes, ameaças |
| [v0.1/06-TEST-PLAN.md](v0.1/06-TEST-PLAN.md) | Testes automatizados, manuais, desempenho e release gates |
| [v0.1/07-DELIVERY-PLAN.md](v0.1/07-DELIVERY-PLAN.md) | Ordem de implementação, marcos, riscos e responsabilidades |
| [v0.1/08-DECISIONS.md](v0.1/08-DECISIONS.md) | ADRs e decisões ainda condicionais |
| [v0.1/schemas/intent-result.schema.json](v0.1/schemas/intent-result.schema.json) | Esquema JSON para resultado normalizado da classificação |
| [v0.1/schemas/mission-snapshot.schema.json](v0.1/schemas/mission-snapshot.schema.json) | Esquema JSON para status persistível/observável de missão |

## Como manter specs

1. Uma **mudança funcional** precisa atualizar primeiro ou junto a respectiva spec, os testes e a issue.
2. Os códigos de requisito `FR-xx`, `NFR-xx` e teste `T-xx` são identificadores estáveis; não os renumere ao editar.
3. Marque `PROPOSTO`, `VALIDADO`, `IMPLEMENTADO` ou `OBSOLETO` com evidências de testes; não declare implementado por existir uma classe ou por passar `gradle build`.
4. Novas versões ganham um diretório novo (`specs/v0.2/`), registrando diferenças; não reescreva silenciosamente o contrato da v0.1 após release estável.
5. Atualize [docs/ROADMAP.md](../docs/ROADMAP.md) para progresso geral, mas use estes documentos para critérios precisos.
6. Questões sem resposta são registradas em [08-DECISIONS.md](v0.1/08-DECISIONS.md) até um experimento encerrá-las.
7. Documente licenças e a origem de trechos antes de importar dependências ou código de terceiros.

**Política de release:** `main` gera pré-releases automáticas após testes; `v0.1.0` estável só após todos os critérios obrigatórios de [06-TEST-PLAN.md](v0.1/06-TEST-PLAN.md).
