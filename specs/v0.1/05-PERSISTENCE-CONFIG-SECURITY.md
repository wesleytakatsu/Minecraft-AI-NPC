# SPEC v0.1 — Persistência, configuração e segurança

**Status:** PROPOSTO

## Persistência (servidor autoritativo)

Guardar por mundo (UUID estável do NPC e UUID do dono; nunca usar apenas o nome do jogador):

```text
world/data/ (via PersistentState / equivalente Fabric-Yarn 1.20.1)
  nexus_v1:
    schemaVersion
    npcUuid, ownerUuid, dimension, lastKnownPosition
    active, createdAt
    missionSnapshot (ou null)
    settings/version
```

A localização concreta é uma sugestão: usar APIs apropriadas do servidor e deixar arquivos de config separados do save. **NÃO** guardar senhas/token/URL privada em NBT, snapshots de missão ou itens.

Regras:
- UUID é único por mundo. Mesmo nome não implica mesmo NPC.
- Inventário real deve seguir o ciclo de persistência do Fake Player; não manter cópia paralela que cause duplicações.
- Em shut down, cancelar controles, persistir snapshot, salvar mapa de proprietários e fechar pools.
- Retomada após restart é manual e depende da revalidação (consultar [04-MISSIONS-AND-SKILLS.md](04-MISSIONS-AND-SKILLS.md)).
- Em dado inválido ou versão futura de schema, criar backup/quarentena e falhar de forma segura; jamais apagar sem registro.
- Spawn repetido não cria clone; remover/despawn cancela missão antes de desaparecer.

## Configuração proposta (`config/minecraft-ai-npc.json` ou equivalente)

```json
{
  "schemaVersion": 1,
  "chatPrefix": "@nexus",
  "maxCompanions": 1,
  "allowOtherPlayersToChat": false,
  "ownerCanControl": true,
  "operatorCanManage": true,
  "tunnelMaxLength": 32,
  "safeMode": true,
  "ai": {
    "enabled": false,
    "llm": { "enabled": false, "baseUrl": "", "model": "", "timeoutMs": 10000 },
    "jev": { "enabled": false, "timeoutMs": 5000 },
    "maxPendingRequests": 1
  }
}
```

Valores são **propostos**, não configuração implementada. Carregar API keys por variáveis de ambiente ou arquivo privado ignorado pelo Git; recomendar nomes `MINECRAFT_AI_NPC_LLM_API_KEY` e `MINECRAFT_AI_NPC_JEV_API_KEY`. Não imprimir segredos em diagnósticos, screenshot, log de exceção ou resposta de chat.

## Segurança / abusos

| Risco | Controle obrigatório |
| --- | --- |
| Outro jogador assume NPC | ACL por UUID do dono e permissão de operador em spawn/remove |
| Prompt injection solicita `/op` ou ações de console | Intent enum e dispatch server-side; **sem** acesso a shell/Brigadier via IA |
| Chat não solicitado gera tokens | Prefixo, opt-in e limites de taxa/filas |
| IA responde após STOP | `requestId`/generation counter + checagem de estado |
| Destruição de área/baús protegidos | Limites de geometria, validação de blocos e ACL antes de cada ação |
| Falhas de backend duplicam itens | Testes de conservação em `/nexus handover`, respeito a durabilidade; sem invocar rotinas de baú suspeitas |
| HTTP acessa destinos perigosos | URLs de provedores configuradas somente por administrador, nunca pelo chat |
| DDoS via player/NPC | 1 NPC inicial, 1 missão ativa, limite de HTTP, rate-limit e máximo 32 segmentos |
| Dados do mundo em serviço externo | Enviar apenas contexto resumido e necessário; mascarar segredos e histórico alheio |

Não prometer compatibilidade com todos os mods de proteção; **se não houver integração confiável com a política de área, recusar** a ação na área restrita ou exigir allowlist clara.

## Contratos de erro e comportamento

- Falta de chave: modo offline, sem exceções contínuas em logs.
- Provider timeout: resposta privada e status não alterado.
- Falha ao persistir: parar novas operações destrutivas e reportar diagnóstico a operador.
- Player logout: missão de follow fica pausada/interrompida; sem teleport cross-dimension.
- NPC death: não reaparecer sem opção administrativa; preservar estado de missão e dados reais.

## Licenças e distribuição

Minecraft AI NPC: BSD-3-Clause. Carpet: MIT; SecondBrainEngine: LGPL-3.0; AI-Player: GPL-2.0. Atribuição e obrigações de dependências devem ser revisadas **antes** de empacotar o engine; nossa licença BSD não altera termos de componentes terceiros. Registrar manifest de dependências e notices no PR do backend.
