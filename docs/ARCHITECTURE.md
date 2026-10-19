# Architecture

> Design document, not a claim that these subsystems are implemented. The versioned, more detailed source of truth for the first usable release is [specs/v0.1/01-ARCHITECTURE.md](../specs/v0.1/01-ARCHITECTURE.md), with contracts and a backend decision gate.

## Priorities

1. **Real in-game actions:** a companion should use Minecraft mechanics instead of arbitrary world edits.
2. **Low recurring cost:** do not invoke an AI model every tick, pathfinding step, or combat hit.
3. **Safety and control:** player permissions, cancellation, allowlists, protected areas, and bounded tasks.
4. **Recovery:** persist task state and verify results instead of treating a model answer as success.
5. **Replaceable integrations:** avoid coupling core tasks to one LLM/JEV provider or NPC engine.

## Planned modules (logical, initially in one Gradle project)

- `chat/`: prefix extraction, authorization, command routing and response formatting.
- `ai/`: asynchronous LLM and JEV adapters; parse and validate structured outputs.
- `mission/`: lifecycle, retries, checkpoints and task planning.
- `skills/`: deterministic actions (follow, move, dig, collect, craft, deposit).
- `world/`: inventory, recipes, locations, observed blocks and nearby threats.
- `npc/`: abstraction for the physical agent, potentially Carpet/Automatone or another backend.
- `config/`: API secrets loaded only at runtime, budgets, permissions and safety settings.

The current v0.0.1 code only has a minimal `ChatRouter` and Fabric entrypoint.

## Decision routing

1. Check for the explicit `@nexus` prefix.
2. Apply access controls and parse hard-coded priority commands (`stop`, `status`, etc.).
3. For ambiguous input, consult JEV to select an *allowed* intent (planned).
4. Ask the LLM to answer conversationally or clarify missing parameters (planned).
5. Validate any requested action against a schema and server-side rules.
6. Execute the action using deterministic Minecraft skills; verify results and report status.

Never allow an LLM response to directly call arbitrary Java functions, execute console commands or bypass block-protection policies.

## Execution policy

- Keep world state access and mutations on the Minecraft server thread.
- Perform external AI HTTP calls off-thread with deadlines and backpressure.
- Feed only bounded, relevant state snapshots to AI services.
- When external services fail, retain local status, cancellation and safety behaviors.
- Avoid using hidden/unseen ore locations as inputs in a survival-friendly agent.
- Use bounded retries, mission timeouts and explicit completion/failure states.

## Task lifecycle

`QUEUED -> PLANNING -> RUNNING -> COMPLETED`

Possible interruptions: `PAUSED`, `CANCELLED`, `FAILED`. A skill can request replanning without forgetting verified progress.

## NPC engine investigation

Our first evaluation target is **Fabric Carpet + SecondBrainEngine**, which includes an explicit Minecraft 1.20.1 version and server-adapted Automatone/AltoClef. See [source-code findings and experiment checklist](PROJECT-RESEARCH.md). Evaluate other options if its actual runtime behavior is unreliable. Check compatible versions, licenses, maintainability, correctness and dedicated/integrated server behavior **before** importing code.

Do not vendor external projects without a license review.

## Threat model (initial)

- No user API keys in chat, logs, source control or world NBT.
- Restrict which players may control the companion.
- Protect private inventories and regions; require explicit opt-in to destructive operations.
- Bound model spend, HTTP concurrency and message length.
- Assume messages from players and model outputs are untrusted data.

See [AI-PROTOCOL](AI-PROTOCOL.md) for proposed request/response shapes.
