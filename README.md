# Minecraft AI NPC

An AI-powered NPC companion mod for **Minecraft Java Edition 1.20.1**, built with **Fabric**.

> **Status: early prototype (v0.0.1).** The mod currently registers a basic chat prefix and diagnostic commands. **There is no spawned NPC, pathfinding, LLM connection, or JEV integration yet.** Those are planned features, not released functionality.

## Vision

Build a survival-friendly intelligent companion that talks to players using a chat prefix, interprets natural-language requests, and completes real in-game tasks such as following, collecting resources, mining, crafting, and eventually defending players.

We prioritize **reliable execution** over AI controlling every game tick:

- **Java:** deterministic mechanics, combat rules, inventory, recipes, pathfinding and safety limits.
- **JEV (planned):** interpret ambiguous player intent and choose from allowed high-level actions.
- **LLMs (planned):** natural conversation and clarification of incomplete requests.

## Current prototype

After installing the mod on Fabric 1.20.1 with Fabric API, use:

- `/nexus` or `/nexus help` — display available commands.
- `/nexus status` — report the prototype's current state.
- `@nexus oi`, `@nexus ajuda`, or `@nexus status` — receive simple rule-based chat replies.

Messages beginning with `@nexus` are intercepted server-side and answered privately. **This is only a chat-routing proof of concept, not an AI chatbot.** Other chat messages behave normally.

## Planned milestones

| Milestone | Scope |
| --- | --- |
| v0.0.1 | Fabric project and chat-command skeleton |
| v0.1 | Fake player, follow, come here, stop, simple tunneling, initial LLM/JEV adapters |
| v0.2 | Tree cutting, inventory, chests |
| v0.3 | Crafting and resource dependencies |
| v0.4 | Advanced mining and exploration |
| v0.5 | Survival and combat |
| v0.6 | More intelligent hostile mobs |
| v0.7+ | Long-term memory, advanced autonomy, multiple companions |

See [ROADMAP](docs/ROADMAP.md) for acceptance criteria and scope boundaries.

## Development

- Minecraft **1.20.1**
- **Fabric Loader 0.16.14**
- **Fabric API 0.92.12+1.20.1**
- **Java 17**
- **Gradle 8.12.1** (local installation required until an official wrapper is checked in)

```bash
gradle build
gradle runServer
```

See [Development Guide](docs/DEVELOPMENT.md) for setup details and [Architecture](docs/ARCHITECTURE.md) for technical decisions.

## Documentation

- [Architecture and design principles](docs/ARCHITECTURE.md)
- [Technical research: existing agent and NPC projects](docs/PROJECT-RESEARCH.md)
- [Development setup](docs/DEVELOPMENT.md)
- [Roadmap](docs/ROADMAP.md)
- [AI message and task protocol](docs/AI-PROTOCOL.md)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)

## License and attribution

Copyright (c) 2026, **Wesley Sieiro Takatsu de Araujo**.

Distributed under the [BSD 3-Clause License](LICENSE). Reusers must preserve the original copyright and license notices under its terms. Do not imply that the original author endorses derivative projects without permission.

Minecraft is a trademark of Mojang Studios/Microsoft. This is an independent community project and is not affiliated with or endorsed by Mojang Studios or Microsoft.
