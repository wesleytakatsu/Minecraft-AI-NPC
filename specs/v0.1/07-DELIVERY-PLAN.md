# SPEC v0.1 — Plano executável de implementação

**Status:** PLANEJADO | **Ponto de partida:** v0.0.1 já com GitHub Actions funcionando.

## Sequência de entregas

| Etapa | Entrega técnica | Dependências | Definição de pronto |
| --- | --- | --- | --- |
| P0 — Fundação | Review das specs, Gradle wrapper oficial, testes de contratos e issue tracking | Nenhuma | `gradle build`, docs aprovadas, interfaces iniciais compilam |
| P1 — Spike do backend | Carpet + SecondBrainEngine em branch isolada, matriz de versões/atribuições | P0 | Relatório GO/NO-GO e demonstração Fake Player/mineração sob regras vanilla |
| P2 — Núcleo do NPC | `NpcController`, spawn/remove, owner UUID, ciclo de vida e persistência | P1 | T-01, T-02, T-13, T-18 |
| P3 — Missão e movimentação | Mission FSM, FOLLOW, COME_HERE, STOP e STATUS | P2 | T-04, T-05, T-06, T-12 |
| P4 — Escavação | DIG_TUNNEL 1x2, limites, ferramentas, drops, lava, cancelamento | P3 | T-07..T-11 |
| P5 — Chat e IA | Roteamento local, ACL, conversa LLM opcional e JEV opcional com mocks | P2/P3 em paralelo | T-03, T-14..T-17 |
| P6 — Qualidade/release | Fabric GameTests, CI, documentação de instalação, license notices, release notes corretas | P1..P5 | Todos T-01..T-20 e revisão de segurança |

### P0: preparação

- [ ] Fixar versões no lock de dependências e instalar o **Gradle Wrapper oficial** (não gerar script manual).
- [ ] Criar interfaces `NpcController`, `MissionManager`, `Skill` e objetos de resposta como Java interfaces/classes sem comportamento.
- [ ] Introduzir testes de parser de intenção, ACL e máquina de estados (inicialmente mocks).
- [ ] Criar branch de prova de conceito; evitar push de dependências não revisadas na main.

### P1: decisão técnica

- [ ] Executar [02-BACKEND-SPIKE.md](02-BACKEND-SPIKE.md).
- [ ] Verificar Carpet/SecondBrainEngine/Automatone/AltoClef e licenças (LGPL/MIT).
- [ ] Gravar resultado GO/NO-GO com logs, versões e evidências.
- [ ] Se NO-GO, definir fallback específico e abrir ADR, mantendo contratos estáveis.

### P2 + P3: companheiro mínimo

- [ ] Comandos `/nexus spawn`, `/nexus remove` e proprietário.
- [ ] Um NPC com UUID fixo, session cleanup e status verdadeiro.
- [ ] `/nexus handover` com transferência conservativa de uma picareta da mão principal.
- [ ] FOLLOW, COME_HERE, STOP sem model call.
- [ ] Persistência de NPC/mission snapshot; restart coerente.

### P4: tarefa útil

- [ ] Implementar túnel baseado em posições determinísticas e verificadas.
- [ ] Reusar **somente** API de quebra/locomoção validada do backend; não editar mundo por comando.
- [ ] Testar ferramenta inadequada, líquidos, proteção, STOP e inventário/drops.
- [ ] Expor progresso por segmento e erro amigável em português.

### P5: IA opcional, sem dependência de gameplay

- [ ] Interface de provedor HTTP e classificador de intenção.
- [ ] Base de roteamento sem LLM para comandos óbvios.
- [ ] Adaptador LLM compatível com OpenAI para conversa, com chave/configuração privadas.
- [ ] Adaptador JEV para escolha dentre intenções permitidas.
- [ ] Teste com mock e fail-closed em timeout, JSON inválido e dúvida.
- [ ] Teste manual (opt-in) com provedor real; não anunciar suporte não verificado.

### P6: aceitação

- [ ] Integrar GameTest Fabric na versão **1.20.1** e CI.
- [ ] Todos os requisitos no [produto](00-PRODUCT.md) com testes traçáveis.
- [ ] Atualizar README do estado atual (nunca prometer feature inacabada).
- [ ] Atualizar changelog, riscos conhecidos, licenças, setup e release notes.
- [ ] Publicar pre-release aprovada, pedir validação manual, então criar tag estável `v0.1.0` quando todos os gates estiverem verdes.

## Divisão inicial de GitHub Issues

Já existem:
- [#1 — Fake Player / backend](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/1) ⇒ P1/P2.
- [#2 — Missões e cancelamento](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/2) ⇒ P3.
- [#3 — LLM e JEV](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/3) ⇒ P5.

Issues adicionais já criadas:
- [#4 — Interfaces e contratos](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/4) ⇒ P0.
- [#5 — Persistência, um NPC e propriedade](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/5) ⇒ P2.
- [#6 — Seguir e aproximar com navegação](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/6) ⇒ P3.
- [#7 — Túnel 1x2 e mineração real](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/7) ⇒ P4.
- [#8 — GameTests integrados ao CI](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/8) ⇒ P6.
- [#9 — Revisão e release estável v0.1.0](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/9) ⇒ P6.
- [#10 — Entrega de ferramenta sem duplicação](https://github.com/wesleytakatsu/Minecraft-AI-NPC/issues/10) ⇒ P2.

**Chat ACL** permanece na #3 e na #5, com testes T-03. Limitar PRs ao escopo de uma issue ou etapa. PR que muda comportamento atualiza as specs e testes no mesmo commit.

## Critérios de passagem

- P0 → P1: projeto compila e interfaces estão explícitas.
- P1 → P2: GO documentado ou fallback formal.
- P2 → P3: personagem não duplica nem atravessa mundos em restart.
- P3 → P4: STOP confiável e navegação não entra em loop infinito.
- P4 → P5: núcleo útil offline; sem regressões de segurança.
- P5 → P6: provider desligado ainda permite jogar; uso remoto limitado e mockado.
- P6 → estável: testes reais e licença revisada, sem features declaradas apenas em README.

## Não fazer agora

Mobs inimigos inteligentes, múltiplos NPCs, crafting, baús, mineração por minério e memória vetorial. Eles exigem módulos futuros e não devem comprometer a primeira entrega.
