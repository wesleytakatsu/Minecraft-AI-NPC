# Continuous integration and development releases

The workflow is [`.github/workflows/build.yml`](../.github/workflows/build.yml).

## Events

- **Pull requests:** compile and run tests, validate the remapped JAR, and smoke-test Fabric server startup. Save a temporary Actions artifact only. **No GitHub Release**.
- **Every push/merge to `main`:** perform the exact same verification. **Only after it passes**, publish one GitHub **pre-release** with an installable mod JAR and SHA-256 checksum file.
- **Failed tests or server startup:** the release job does not run.
- Re-running a workflow for a commit that already has a pre-release leaves its original artifacts unchanged.

## Checks performed

1. Java 17 / Gradle 8.12.1: `gradle --no-daemon --stacktrace clean build`.
2. Existing JUnit tests (currently `ChatRouterTest`).
3. Validate that the remapped, non-dev production JAR contains `fabric.mod.json` identifying `minecraft_ai_npc` and Minecraft 1.20.1.
4. Run a **real headless Minecraft Fabric 1.20.1 server** on the GitHub-hosted Linux runner using `gradle runServer`. Fail unless the initialization log for Minecraft AI NPC and the server's **Done** startup line are both observed.
5. Store JUnit XML/HTML reports, smoke-test logs and the validated JAR as temporary workflow artifacts.

**Scope limitation:** the headless smoke test proves only that the mod initializes successfully on a dedicated server. It does not test user chat or world interactions. GameTests to validate spawning, movement, mining, inventories and NPC tasks should be added as those systems are implemented.

## Releases

- Tag: `dev-<first 12 hexadecimal characters of the git commit SHA>`.
- Example tag: `dev-a1b2c3d4e5f6`.
- Release type: **pre-release**, explicitly **not Latest** and not a stable `vX.Y.Z`.
- Assets: `minecraft-ai-npc-<mod_version>-dev-<SHA>.jar`, plus `SHA256SUMS.txt`.
- The mod's internal version remains the value in `gradle.properties`. A commit-specific JAR name does not alter the Fabric metadata.

See the list of [GitHub Releases](https://github.com/wesleytakatsu/Minecraft-AI-NPC/releases).

By default, no personal token is required: the publisher uses the workflow's `GITHUB_TOKEN`, scoped to `contents: write` for the release job. A repository secret named `RELEASE_TOKEN` can optionally replace this token when broader workflow-related authorization is needed. Never commit tokens or print them in logs.

### GitHub restriction: older commits that changed workflows

GitHub may reject `gh release create --target <older-SHA>` with **HTTP 403: Resource not accessible by integration** even when `GITHUB_TOKEN` shows `contents: write`, if the targeted commit differs from the default branch in `.github/workflows/`. This was observed while multiple workflow-editing commits were pushed before earlier CI runs finished. A subsequent release targeting the **current main HEAD** succeeded with `GITHUB_TOKEN`.

If you need to guarantee a separate release for **every commit**, including superseded workflow-editing commits, create an optional **fine-grained personal access token** restricted to **this repository** with **Contents: Read and write** and **Workflows: Read and write** permissions; store it as the Actions secret `RELEASE_TOKEN` under **Settings → Secrets and variables → Actions**. Give it an expiration and rotate it periodically. An appropriately scoped GitHub App installation token with both permissions is another alternative. The workflow will prefer `RELEASE_TOKEN` if configured; otherwise it falls back to the default token.

For ordinary source-only commits, the default `GITHUB_TOKEN` is normally sufficient. Never silently change the release target to `main` when building an older SHA: that would mislabel the binary. Also inspect **Settings → Actions → General → Workflow permissions** and organizational policies if you get an unrelated 403.

GitHub API documentation for this restriction: https://docs.github.com/en/rest/releases/releases#create-a-release

### Releases versus workflow artifacts

The validated JAR is available for up to 30 days as an Actions artifact. Development pre-release attachments are published as GitHub Release assets and are not governed by the Actions artifact retention period. Every commit on `main` creates a separate development pre-release, so this can increase the number of pre-releases over time. Stable version tags remain a future, separately controlled process.

## Running checks locally

```bash
gradle clean build
bash scripts/ci-smoke-server.sh
```

The smoke script creates an isolated server under `run/`, binds it to `127.0.0.1`, and writes `run/eula.txt` with `eula=true`. Running a Minecraft server is subject to the [Minecraft EULA](https://www.minecraft.net/eula). By running the smoke script you are accepting that EULA for this isolated test server. Review the terms first. The `run/` directory and runtime logs are excluded from version control.

A complete in-game test suite is still on the roadmap.
