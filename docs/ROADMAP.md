# Roadmap

Feature status is tracked here. **Unchecked means not implemented.** Versions describe development goals, not promised release dates.

## v0.0.1 — Repository and chat skeleton
- [x] Fabric 1.20.1 / Java 17 project structure
- [x] Basic `/nexus` help and status commands
- [x] `@nexus` prefix parsing and private deterministic replies
- [x] JUnit tests for the pure chat router
- [x] Architecture, development and contribution documentation
- [x] Initial CI build verified on GitHub Actions
- [ ] Automated headless server smoke test and pre-release publishing verified on GitHub Actions

## v0.1 — First usable companion
- [ ] Run and document the [SecondBrainEngine/Carpet compatibility spike](PROJECT-RESEARCH.md#9-implementation-recommendation-and-acceptance-tests)
- [ ] Spawn/despawn one persistent, visible Fake Player or equivalent NPC
- [ ] Follow / come here / stop commands
- [ ] Navigate around obstacles without teleport shortcuts
- [ ] Excavate a small tunnel using normal block-breaking mechanics
- [ ] Respect tool requirements, tool durability, drops, liquids and protected blocks
- [ ] Mission state, progress report and reliable cancellation
- [ ] Pluggable LLM dialogue and JEV intent classification, both optional
- [ ] Graceful behavior with missing AI credentials or network access
- [ ] Integration test world with documented reproduction steps

**Acceptance:** a player can spawn the companion, ask it to follow, order a 10-block tunnel, stop the job and get an accurate status. No special permissions, invisible breaking or model calls per tick.

## v0.2 — Gathering
- [ ] Trees, drops and replanting
- [ ] Own inventory and tool management
- [ ] Authorized chest deposit / withdrawal

## v0.3 — Crafting
- [ ] Resolve actual Minecraft recipes
- [ ] Gather ingredients from configured sources
- [ ] Crafting-table and furnace skills

## v0.4 — Advanced mining
- [ ] Safe staircase geometry
- [ ] Multi-block tunnels and torch placement
- [ ] Ore search using observed/explored areas

## v0.5 — Survival
- [ ] Health, equipment, hunger and escape rules
- [ ] Deterministic defensive combat
- [ ] Recoverable mission failures

## v0.6 — Smarter hostile mobs
- [ ] Separate feature module or feature toggle
- [ ] Deterministic perception/state-machine goals
- [ ] Performance and multiplayer compatibility tests

## v0.7+ — Expanded autonomy
- [ ] Persistent world map and social memory
- [ ] Recurring resource tasks
- [ ] Complex mission planning and optional multiple agents

## Not in the first release

General autonomous survival, multi-agent colonies, unsupported third-party recipes, fully dynamic house building, AI combat decisions every tick, and full player-account automation.
