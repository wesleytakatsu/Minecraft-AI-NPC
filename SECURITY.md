# Security policy

## Supported versions

The project is an early prototype. There is no stable release and no security support guarantee.

## Reporting

Please **do not post secrets or security exploit details in a public issue**. Contact the maintainer through the GitHub repository profile or use GitHub private vulnerability reporting if enabled.

## Secrets

- Keep future LLM/JEV API credentials in local configuration or environment variables.
- Do not commit `.env`, credentials, stack traces containing tokens, or private user content.
- Never authorize model output to run arbitrary shell commands, console commands or unvalidated Minecraft actions.

## Known limitations

v0.0.1 has no network-based AI integrations and does not create or control NPC entities. Chat prefix handling is rule-based and should be tested with other mods that modify player chat.
