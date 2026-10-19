# Development guide

## Requirements

- JDK **17**
- Gradle **8.12.1** (installed locally; **the official Gradle wrapper has not been committed yet**)
- An internet connection for the initial Fabric/Minecraft dependency resolution
- Git and an editor such as IntelliJ IDEA

The CI workflow installs the same JDK and Gradle versions automatically.

## Commands

```bash
gradle build
gradle test
gradle runClient
gradle runServer
```

On the first `runServer`, accept Minecraft's EULA in the generated server run directory before restarting.

## CI and pre-releases

The GitHub Actions workflow runs `gradle clean build`, checks the production JAR, starts a headless Fabric server and publishes a pre-release for every passing `main` commit. Details: [CI and releases](CI-RELEASES.md).

To run the server-startup smoke test locally (note that this writes `eula=true` into the test server; read the Minecraft EULA first):

```bash
bash scripts/ci-smoke-server.sh
```

## Smoke test

1. Start Minecraft 1.20.1 with the Fabric development environment.
2. Join a local/integrated or dedicated server running this mod and Fabric API.
3. Run `/nexus status` — expect the v0.0.1 prototype message.
4. Say `@nexus oi` — expect a private, rule-based reply.
5. Say `@nexusbot oi` or ordinary chat — it should not be intercepted.
6. Verify that another player does not receive the `@nexus` message.

**No NPC should spawn yet.** This is expected for v0.0.1.

## Build details

Versions are centralized in `gradle.properties`. The mod ID is `minecraft_ai_npc`; source package is `dev.wesleytakatsu.minecraftainpc`.

`processResources` expands the mod version into `fabric.mod.json`. The release JAR will appear in `build/libs/` after a successful build.

## Dependencies and licensing

Carpet, SecondBrainEngine, Automatone and AltoClef are **research candidates**, not project dependencies. Before adding code or distributing a bundled binary, review their licenses and loader compatibility.

## Working rules

- Keep game mechanics on the Minecraft server thread.
- No real API secrets in git, Issues or CI output.
- Add unit tests for pure Java components and integration tests for world-dependent skills.
- State explicitly in documentation which features are experimental or unimplemented.
- PRs should describe how a behavior can be tested in Minecraft.
