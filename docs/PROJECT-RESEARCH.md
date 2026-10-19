# Technical research: Minecraft NPC and autonomous-agent projects

Research date: **2026-10-19**. This is a source-code review, **not** a runtime compatibility certification. No upstream code or binary is imported into this repository.

## Executive decision

For a **Fabric 1.20.1 / Java 17 / server-controlled companion**, investigate **Fabric Carpet + SecondBrainEngine** first, while maintaining an implementation-independent `NpcController`. Use **AltoClef's task decomposition**, **AI-Player's navigation recovery**, and **Voyager's verified skill-memory pattern** as architecture references. Keep **Mineflayer** as a separately deployed fallback experiment, not the default runtime. High-frequency combat, escape and movement stay deterministic; LLMs are for conversation and clarification, JEV for ambiguous high-level intents.

### Suitability at a glance

| Source | Useful subsystem | MC 1.20.1 fit | Upstream license | Integration posture |
| --- | --- | --- | --- | --- |
| [Fabric Carpet](https://github.com/gnembon/fabric-carpet) | Fake player and player actions | 1.20.1 version used by SecondBrainEngine | MIT | Candidate runtime dependency |
| [SecondBrainEngine](https://github.com/sailex428/SecondBrainEngine) | Server-side Automatone + AltoClef + Carpet | Has explicit `versions/1.20.1` and active Stonecutter target | LGPL-3.0 | **First proof-of-concept**; audit before reuse |
| [SecondBrain](https://github.com/sailex428/SecondBrain) | NPC lifecycle, conversations and configurable LLM backends | Has explicit `versions/1.20.1` | LGPL-3.0 | Design reference, not bundled |
| [AltoClef](https://github.com/gaucho-matrero/altoclef) | Task/subtask chain and item acquisition | Original project targets older MC (1.18.x) | MIT | Study concepts; do not depend on original for 1.20.1 |
| [Baritone](https://github.com/cabaletta/baritone) | A* pathing, block-breaking and movement goals | Fabric 1.20.1 build published | LGPL-3.0 | Client-side reference; not directly a server-NPC controller |
| [AI-Player](https://github.com/shasankp000/AI-Player) | Server navigation, NLP, planner and provider adapters | Current branch `26.3`, **not** 1.20.1 | GPL-2.0 | Study ideas; **do not copy GPL code into BSD project** |
| [Mineflayer](https://github.com/PrismarineJS/mineflayer) | Bot protocol, inventory, crafting, mining | Supports the 1.20 family | MIT | Alternative external-bot backend |
| [Voyager](https://github.com/MineDojo/Voyager) | Skill memory, task reflection and feedback | Independent Python/Node environment; not native Fabric | MIT | Later autonomy and skill design |

Licenses are the **reported repository licenses**, not legal clearance for bundling individual files or transitive dependencies. LGPL integration/distribution requires a specific review of combined works, notices, licenses and replacement/source obligations. Upstream code added to our repository retains its obligations; our own BSD-3-Clause license does not erase them.

## 1. SecondBrainEngine — strongest candidate for actual NPC behavior

Sources:
- [README and example spawner](https://github.com/sailex428/SecondBrainEngine)
- [1.20.1 version file](https://github.com/sailex428/SecondBrainEngine/blob/main/versions/1.20.1/gradle.properties)
- [Build configuration](https://github.com/sailex428/SecondBrainEngine/blob/main/build.gradle.kts)
- [Task runner](https://github.com/sailex428/SecondBrainEngine/blob/main/src/main/java/me/sailex/altoclef/tasksystem/TaskRunner.java)
- [Task abstraction](https://github.com/sailex428/SecondBrainEngine/blob/main/src/main/java/me/sailex/altoclef/tasksystem/Task.java)
- [Controller](https://github.com/sailex428/SecondBrainEngine/blob/main/src/main/java/me/sailex/altoclef/AltoClefController.java)

### Verified from the source

- The engine integrates Carpet Fake Players, server-adapted Automatone navigation, and AltoClef task orchestration.
- The `versions/1.20.1/gradle.properties` file specifies: **Minecraft 1.20.1**, Yarn build **10**, Fabric API **0.92.6**, Carpet **1.4.112**. The root Stonecutter configuration selects 1.20.1; main module uses Java 17 for MC <= 1.20.5.
- The README gives a concrete flow: `NPCSpawner.spawn(profile, server, pos, callback)`; obtain per-NPC Baritone via `BaritoneAPI.getProvider().getBaritone(npc)`; construct `AltoClefController(automatone)`; call `controller.runUserTask(new ConstructIronGolemTask())`.
- `Task` has lifecycle methods `onStart`, `onTick`, `onStop`, `isFinished`, `isEqual`, nested task chaining and human-readable debug state.
- `TaskRunner` chooses an active **task chain with maximum priority** every tick, interrupting the previous chain as necessary.
- `AltoClefController` contains survival-related chains (food, hostile mobs, world survival, unstuck) in addition to user tasks and trackers.
- Confirmed available task classes: `FollowPlayerTask`, `GetToBlockTask`, `DestroyBlockTask`, `CraftInTableTask`, `PickupFromContainerTask`, `StoreInContainerTask`, `SmeltInFurnaceTask`, `MineAndCollectTask`, `DodgeProjectilesTask` and `ConstructIronGolemTask`.

### Useful design patterns

1. A user goal is **not** a single blocking action. It becomes a parent `Task` that returns child tasks until `isFinished` is true.
2. Separate *goal priorities*: escape hazards > defensive survival > assigned tasks > idle/follow flavor behaviors.
3. `FollowPlayerTask` tracks the target player's last known position, then chooses `GetToEntityTask` or `GetToBlockTask`; avoid infinitely chasing a player no longer located.
4. Maintain a public `status()` that exposes the *current task tree*, not just generic `RUNNING`.

### Code-audit warnings

- **Potential inventory duplication bug** in [`PickupFromContainerTask`](https://github.com/sailex428/SecondBrainEngine/blob/main/src/main/java/me/sailex/altoclef/tasks/container/PickupFromContainerTask.java): the code invokes `playerInventory.insertStack(new ItemStack(stack.getItem()))` as though checking capacity, then invokes `insertStack(toMove)` for the actual requested amount. The first call already mutates the inventory; it appears to award a free single item. Verify with an integration test before trusting the implementation.
- Container tasks directly manipulate `LootableContainerBlockEntity` and inventory stacks instead of following the ordinary screen-handler click path. This may bypass intended interaction semantics or protection plugins. Do not enable NPC access to arbitrary containers without explicit authorization and conservation tests.
- The presence of a task class does **not** prove correct behavior on a real dedicated server (pathfinding, hunger, lifecycle and mixins need runtime tests).
- Engine and Carpet versions differ from our Fabric API dependency; inspect compatibility rather than assuming that matching Minecraft/Yarn alone is sufficient.

## 2. SecondBrain — NPC + LLM application layer

Sources:
- [README](https://github.com/sailex428/SecondBrain)
- [1.20.1 dependency versions](https://github.com/sailex428/SecondBrain/blob/main/versions/1.20.1/gradle.properties)
- [NPC lifecycle manager](https://github.com/sailex428/SecondBrain/blob/main/src/main/java/me/sailex/secondbrain/common/NPCService.kt)
- [LLM client interface](https://github.com/sailex428/SecondBrain/blob/main/src/main/java/me/sailex/secondbrain/llm/LLMClient.java)
- [Event handler](https://github.com/sailex428/SecondBrain/blob/main/src/main/java/me/sailex/secondbrain/event/NPCEventHandler.kt)
- [Chat listener](https://github.com/sailex428/SecondBrain/blob/main/src/main/java/me/sailex/secondbrain/listener/ChatMessageListener.kt)

Useful:
- `NPC` stores entity, controller, LLM client, conversation history, context, event handler and configuration.
- `NPCService` persists configurations/conversations, registers entities by UUID, provides shutdown/cleanup, and limits active NPC count (10 in current code). Use a persistent stable UUID per companion.
- Distinct LLM clients for Ollama, OpenAI and Player2 demonstrate provider separation.
- Event handling uses a bounded queue (10) and a single worker to avoid concurrency overload, but `DiscardPolicy` silently drops work; our queue should reject explicitly and notify the player.
- Incoming LLM responses are parsed as JSON with `command` and `message`. The failure path attempts to remove code fences and reparse; this is a sign that schema validation matters.
- **Avoid its broad chat listener policy:** it forwards **every player's chat message to every NPC**. Our `@nexus` prefix, owner permissions and opt-in processing will prevent needless tokens and reduce privacy exposure.
- **Do not directly execute model-generated command strings.** Convert to a strict `Intent` enum plus checked arguments and a dedicated command dispatcher. LLM tool instructions are untrusted.

## 3. AltoClef — item dependencies and task trees

Sources:
- [README](https://github.com/gaucho-matrero/altoclef)
- [Example extension](https://github.com/gaucho-matrero/altoclef-example)

- Original AltoClef was archived in 2024, targets older Minecraft versions, and is **client-side**. The server-oriented derivative in SecondBrainEngine is more relevant to us.
- The original demonstrates obtaining hundreds of items by decomposing goals into mining, smelting, crafting, fighting, and container actions; its Butler system includes remote commands with access controls.
- Particularly valuable concepts: a task catalogue for intermediate crafting resources; per-task start/tick/stop; task equality preventing repeated resets; fallback when a target block is unreachable; committing to a chosen target to avoid *ping-pong* navigation between equally close candidates.
- **Our design:** use the Minecraft RecipeManager for authoritative recipes, then map dependencies to typed resource acquisition actions rather than letting the LLM invent recipes.

## 4. Baritone — pathfinding and movement goals

Sources:
- [README/API example](https://github.com/cabaletta/baritone)
- [Features](https://github.com/cabaletta/baritone/blob/1.21.4/FEATURES.md)

- Proven conceptual model: `Goal` interface; `GoalXZ` / `GoalBlock` target forms; adjustable path cost; mining, placement and real locomotion.
- `BaritoneAPI.getProvider().getPrimaryBaritone()` is designed for the player's **client context**. It is **not** proof that dropping the upstream Baritone Fabric jar into a dedicated server makes a Fake Player navigable.
- SecondBrainEngine adapts the Automatone lineage to run navigation server-side. That adaptation is the relevant integration candidate.
- Add our own destination tolerance, replanning deadline, visited/blocked-target cache, chunk/permission checks and success verification.

## 5. AI-Player — lessons about server navigation and ambiguous language

Sources:
- [README](https://github.com/shasankp000/AI-Player)
- [Custom LLM providers](https://github.com/shasankp000/AI-Player/blob/26.3/CUSTOM_PROVIDERS.md)
- [NavigationService](https://github.com/shasankp000/AI-Player/blob/26.3/src/main/java/net/shasankp000/PathFinding/NavigationService.java)
- [ReplanProgressTracker](https://github.com/shasankp000/AI-Player/blob/26.3/src/main/java/net/shasankp000/PathFinding/ReplanProgressTracker.java)
- [HybridPlanner](https://github.com/shasankp000/AI-Player/blob/26.3/src/main/java/net/shasankp000/GameAI/planner/HybridPlanner.java)

- Current mainline (`26.3`) is **not** our pinned MC 1.20.1 branch. Study architecture without attempting to copy current sources directly.
- Uses Carpet-like fake-player creation; supports customizable OpenAI-compatible providers; stores clarification context separately; implements event-driven autonomous goals and a hybrid planner.
- Its server navigation provides a very useful recovery design: sessions per bot UUID; moves off-thread requests back onto `server.execute`; detects stalls and limits recovery attempts; allows temporary penalties and bounded planning work.
- The current navigation source shows numerical guards such as 20-tick stall windows, 3 recovery attempts, 200-tick penalties and ~2 ms planning budget (implementation-specific, **not recommended defaults**).
- The project README openly notes continuing server-side navigation/physics difficulties. Treat pathfinding reliability as a product requirement, not an implementation detail.
- GPL-2.0 has different redistribution obligations from our BSD-3-Clause project. Do **not** transplant code.

## 6. Mineflayer + pathfinder + collectblock — a fallback, not a dependency

Sources:
- [Mineflayer](https://github.com/PrismarineJS/mineflayer)
- [Pathfinder plugin](https://github.com/PrismarineJS/mineflayer-pathfinder)
- [CollectBlock plugin](https://github.com/PrismarineJS/mineflayer-collectblock)

- The JavaScript bot joins a server via the Minecraft protocol without opening a graphical client. Inventory, block knowledge, digging, crafting, chests and combat APIs already exist.
- `mineflayer-pathfinder` supports configurable movement costs, dynamic goals, break/place while pathing, path recalculation and `goto(goal)` completion.
- `mineflayer-collectblock` packages locate > path > equip tool > dig > collect drops and optional chest overflow.
- **Advantages:** rapid prototype of high-level missions; truly interacts over the network like a normal player; useful cross-check against our server NPC behavior.
- **Trade-offs:** separate Node process/service; additional account/authentication concerns on online-mode servers; the bot consumes a player connection; client protocol behavior rather than native Fabric integration.
- Potential future adapter: `ExternalBotNpcController` via a separate authenticated process, leaving the Fabric command/mission core unchanged.

## 7. Voyager — verified, reusable skills

Sources:
- [README](https://github.com/MineDojo/Voyager)
- [SkillManager](https://github.com/MineDojo/Voyager/blob/main/voyager/agents/skill.py)
- [CriticAgent](https://github.com/MineDojo/Voyager/blob/main/voyager/agents/critic.py)
- [Published skill checkpoints](https://github.com/MineDojo/Voyager/blob/main/skill_library/README.md)

Three separable ideas:
1. Skill library: name, description, reusable implementation, success preconditions. Start with hand-written Java skills and metadata; do not generate and execute arbitrary JavaScript in the server.
2. Record task successes and failures with world state snapshots and logs. This makes future planning less repetitive.
3. Verify that a task's effects happened. Voyager uses a critic LLM; **our primary verification should be deterministic** (positions, inventory deltas, block states, recipe outputs). Reserve LLM review for genuinely ambiguous tasks.
4. Skill retrieval can initially use a simple command registry without a vector DB. Add embeddings only if skill count justifies it.

## 8. Proposed core interfaces — design, not implementation

```java
interface NpcController {
    CompletableFuture<ActionResult> moveTo(BlockPos destination, int tolerance);
    CompletableFuture<ActionResult> breakBlock(BlockPos position);
    CompletableFuture<ActionResult> interact(BlockPos position);
    void stopImmediately();
    NpcSnapshot snapshot();
}

interface Skill {
    SkillId id();
    ValidationResult validate(SkillContext context, SkillArguments args);
    void start(SkillContext context);
    SkillResult tick(SkillContext context);
    void cancel(SkillContext context);
}

interface IntentClassifier {
    CompletableFuture<IntentResult> classify(String message, List<IntentId> allowed);
}

interface ConversationModel {
    CompletableFuture<String> reply(ConversationContext context);
}
```

These are **illustrative API shapes**, not compiler-tested signatures. `NpcController` futures must be completed by the server-side execution loop, and canceling must never leave a tool held down or navigation still active.

Typed action results should include `SUCCESS`, `UNREACHABLE`, `NO_TOOL`, `NO_PERMISSION`, `BLOCKED`, `TIMEOUT`, `CANCELLED`, and diagnostic text. Distinguish `FAILED` from `PAUSED`; preserve counts of successfully collected resources.

## 9. Implementation recommendation and acceptance tests

### Spike A (first): validate SecondBrainEngine in a disposable MC 1.20.1 server

- [ ] Launch server with compatible Carpet + SecondBrainEngine; document **exact version matrix**, dependencies and mixin errors.
- [ ] Spawn one Fake Player with a stable UUID; verify visibility to a regular client.
- [ ] Navigate 10 blocks, around a 2-block obstacle and across a small height difference.
- [ ] Break exactly one stone block using valid mining semantics, tool durability and correct drop collection.
- [ ] Interrupt midway; verify immediate stop and no repeated actions.
- [ ] Restart server and verify no ghost players, stale task controllers or duplicated inventory.
- [ ] Test a shallow cave/lava edge, stuck movement and unreachable target.
- [ ] Profile server tick cost and memory; ensure no blocking AI calls.

**Go/no-go:** if the engine fails fundamental locomotion/safety tests or requires major fork maintenance, fall back to **Carpet + a minimal custom navigation/controller** for this MVP, or test Mineflayer as an external execution backend.

### Spike B: reliable tasks

- [ ] `FollowPlayer`: dynamic target, distance tolerance, player logout and dimension change.
- [ ] `DigTunnel`: bounded geometry, permitted blocks, correct tool, air/liquid hazards, verified block removal.
- [ ] `Stop`: hard priority; works without JEV/LLM and while a remote request is pending.
- [ ] `MissionStatus`: active step, processed/total blocks and last error.

### Spike C: adversarial tests before chest access

- [ ] Transfer 1/32/64 items; verify **before/after total item conservation** across source and target.
- [ ] Test full inventory, full chest, stacked identical items, hoppers, double chests, owner denial and protected areas.
- [ ] Test no duplicated/vanished items, delayed chunk loading and cancellation during transfer.
- [ ] Do not adopt upstream `PickupFromContainerTask` unchanged without resolving the suspicious preliminary `insertStack`.

### Spike D: AI

- [ ] Recognize `@nexus status`, `pare`, `me siga` offline and for free.
- [ ] Distinguish questions from orders: "você sabe minerar?" must not mine.
- [ ] Add optional JEV for ambiguous **allowed** intents and optional LLM for conversation/clarifications.
- [ ] Reject malformed/malicious model output; enforce per-player permissions, budgets and timeouts.

## Decision log

**Chosen for next experiment:** Carpet / SecondBrainEngine server-side spike.

**Chosen as core architectural pattern:** task tree + independent mission state + deterministic skill verification and safety priorities.

**Rejected for the first MVP:** running a heavyweight LLM every Minecraft tick; dynamically executing model-generated source; depending on client-only Baritone for a server NPC; copying GPL sources; unrestricted container inventories; starting a separate Mineflayer account for each companion.

**Still open:** Carpet/engine runtime compatibility with our exact Fabric Loader/API build; long-term maintainability of LGPL dependencies; whether we prefer binary dependency, separated optional module or an independently implemented server controller.
