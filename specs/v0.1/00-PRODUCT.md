# SPEC v0.1 — Produto e requisitos

**Status:** PROPOSTO | **Alvo:** Fabric 1.20.1, Java 17 | **Prioridade:** primeiro companheiro realmente utilizável

## 1. Visão de produto

Um jogador pode instalar o mod em um servidor Fabric (ou no servidor integrado), criar **um** companheiro chamado Nexus e interagir pelo chat. Ele existe fisicamente no mundo, usa mecanismos de Minecraft, acompanha o jogador e executa uma tarefa simples com progresso e possibilidade de interrupção. **Sem conta secundária nem janela adicional de Minecraft** no backend preferencial.

### Jornada mínima

1. Jogador autorizado executa `/nexus spawn` em local seguro.
2. Aparece um personagem com nome e aparência padrão de jogador.
3. `@nexus me siga` → Nexus acompanha com distância segura.
4. `@nexus venha aqui` → desloca-se até perto de quem pediu.
5. Jogador coloca uma picareta na mão principal e usa `/nexus handover`, transferindo exatamente uma ferramenta do inventário do jogador para Nexus, sem criação de itens.
6. `@nexus cave um túnel de 10 blocos` → abre um túnel 1x2 à frente de sua orientação inicial, se for seguro.
7. `@nexus status` → mostra etapa, blocos removidos e impedimentos.
8. `@nexus pare` → interrompe sem novas quebras e preserva progresso.
9. Reiniciar o servidor → personagem e estado coerente, sem duplicações.

## 2. Escopo da v0.1

**Obrigatório (release blocker):**
- Um companheiro real por mundo/servidor, com dono explícito; spawn/despawn e estado durável.
- Comandos locais de seguimento, aproximação, parada e status; chat com `@nexus` sem vazar mensagens para outros jogadores.
- Escavação limitada, autêntica, com ferramentas e drops corretos.
- Navegação em condições comuns, interrupção rápida, diagnóstico de obstáculos, sem teleportar para fingir movimento.
- Testes de execução em mundo Minecraft, além de JUnit e smoke de inicialização.
- Rodar sem credenciais e sem internet; falhas de AI não interrompem controle local.

**Integração opcional entregue na v0.1 (sem bloquear modo offline):**
- Adaptador para **uma API de LLM compatível com OpenAI** (conversa e esclarecimento) e interface preparada para provedores adicionais.
- Adaptador JEV para classificação de intenção **somente se configurado**. Rotas conhecidas seguem locais.
- Validação dos adaptadores com mocks e pelo menos uma verificação manual usando provedor real antes de afirmar compatibilidade com ele.

**Fora do escopo:** mineração de minérios escondidos, escadas avançadas, combate deliberado, crafting geral, baús, derrubada de árvores, gerenciamento de múltiplos NPCs, skins personalizadas, geração dinâmica de código, defesa inteligente de mobs. Estas funcionalidades pertencem ao roadmap posterior.

## 3. Requisitos funcionais

| ID | Requisito | Critério mensurável |
| --- | --- | --- |
| FR-01 | Spawn/despawn restrito | Somente operador (padrão) cria/remove o NPC; nome/UUID sem duplicação |
| FR-02 | Personagem real | NPC visível, posição sincronizada, inventário real, sem comandos `/fill` ou `/tp` para tarefas |
| FR-03 | Chat opt-in | Apenas `@nexus` (case-insensitive, fronteira de palavra) é processado; chat geral permanece normal |
| FR-04 | Autorização | Só dono/op autorizado pode emitir ordens físicas; outros podem conversar se configuração permitir |
| FR-05 | FOLLOW | `@nexus me siga` acompanha dono numa distância alvo, sem oscilações frequentes |
| FR-06 | COME_HERE | `@nexus venha aqui` chega a distância configurada ou informa bloqueio |
| FR-07 | STOP | `@nexus pare` interrompe execução imediatamente no próximo tick de servidor processado e informa estado |
| FR-08 | Túnel | `@nexus cave um túnel de 10 blocos` escava até 10 segmentos 1x2 se válido; progresso por segmento verificado |
| FR-09 | Ferramenta | Ferramenta compatível necessária; tempo, durabilidade e drops seguem regras reais; sem ferramenta informa erro |
| FR-10 | Segurança física | Não cava bedrock, container, bloco protegido ou vizinho de lava/risco conhecido sem autorização explícita |
| FR-11 | Status verdadeiro | `@nexus status` informa tarefa, estado, progresso, últimas falhas e posição (quando conhecida) |
| FR-12 | Persistência | Após restart não há bot fantasma, perda/duplicação de inventário ou missão reiniciada indevidamente |
| FR-13 | Conversa opcional | `@nexus oi` pode produzir resposta da LLM quando configurada; quando offline recebe fallback local |
| FR-14 | Decisão opcional | JEV escolhe intenções de conjunto fechado para solicitações ambíguas; a execução passa por validação Java |
| FR-15 | Desambiguação | Pergunta `você consegue minerar?` **não inicia** missão; ordens incompletas pedem parâmetro |
| FR-16 | Desligamento | Remoção/desligamento libera workers, sessões de pathfinding e estado persistente consistente |
| FR-17 | Entregar ferramenta | `/nexus handover` transfere exatamente uma picareta da mão principal do dono para o NPC, equipa-a e verifica conservação, sem GUI de baú |

## 4. Requisitos não funcionais

| ID | Requisito / política |
| --- | --- |
| NFR-01 | Sem chamadas a IA por tick, movimento individual, bloco ou ataque |
| NFR-02 | Chamadas externas são assíncronas, com timeout, fila limitada e fallback definido |
| NFR-03 | APIs, segredos e histórico não aparecem nos logs ou JAR |
| NFR-04 | Sem operações de mundo fora da thread do servidor |
| NFR-05 | Erro de missão não altera blocos adicionais após `STOP`; resultados têm razão explícita |
| NFR-06 | Compatibilidade com instalação em servidor dedicado e single-player integrado Fabric 1.20.1 |
| NFR-07 | O JAR só é anunciado como `v0.1.0` estável depois das evidências em [06-TEST-PLAN.md](06-TEST-PLAN.md) |
| NFR-08 | Compatibilidade com proteção territorial não é presumida; padrão recusa interação sem autorização quando não for possível verificá-la |
| NFR-09 | O mod deve comunicar claramente riscos físicos e não prometer mineração infalível |

## 5. Contrato de comandos da v0.1

| Entrada | Execução | Resposta esperada |
| --- | --- | --- |
| `/nexus help` | Local | Mostra ajuda atual |
| `/nexus spawn` | Local, operador | Cria companheiro se área segura e não existir |
| `/nexus remove` | Local, operador | Cancela missão e remove com limpeza |
| `/nexus status` | Local | Estado confiável |
| `/nexus handover` | Local, dono/op | Transfere uma picareta da mão principal ao NPC, se houver espaço e autorização |
| `@nexus me siga` | Local | Entra em FOLLOW |
| `@nexus venha aqui` | Local | Inicia COME_HERE |
| `@nexus pare` | Local prioritário | Cancela objetivo em execução |
| `@nexus cave um túnel de 10 blocos` | Parser local + validação | Inicia DIG_TUNNEL com `length=10` |
| `@nexus status` | Local | Resumo da missão |
| `@nexus oi` | Conversação | LLM se habilitada, fallback se não |
| `@nexus pode cavar?` | Pergunta | Responde sem iniciar escavação |

Comandos sem `@nexus` não devem acionar o companheiro, exceto `/nexus`. Textos de confirmação são exemplos, não strings fixas.

## 6. Definição de pronto

A versão v0.1.0 só é aceitável quando FR-01 a FR-12, FR-15 a FR-17 e NFR-01 a NFR-09 passarem. FR-13 e FR-14 exigem implementações opcionais **testadas com mock** e caminho real configurável; indisponibilidade de provedores nunca impede o modo offline. O bloqueio de ações desconhecidas é obrigatório.

As histórias de teste vinculadas encontram-se em [06-TEST-PLAN.md](06-TEST-PLAN.md).
