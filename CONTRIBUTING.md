# Contributing

Thanks for helping build Minecraft AI NPC!

## Getting started

Read the [Development Guide](docs/DEVELOPMENT.md), [Architecture](docs/ARCHITECTURE.md), and [Roadmap](docs/ROADMAP.md).

1. Open an issue to describe a bug, proposed feature or integration.
2. Create a small focused branch and commit your changes.
3. Run `gradle build` locally if you have Gradle 8.12.1 installed.
4. Open a pull request with test steps and relevant logs (remove secrets).

## Guidelines

- Target Minecraft 1.20.1, Fabric and Java 17.
- Prefer deterministic mechanics over high-frequency remote AI calls.
- Keep model-generated output behind schema checks and permission gates.
- Preserve the author's BSD 3-Clause copyright and license notices.
- Do not copy external mod code without checking and documenting its license.
- Never include API keys, tokens, passwords or private world data in your PR.
- Describe which features are complete versus planned.

By submitting a contribution, you agree that it will be distributed under the project's BSD 3-Clause license.
