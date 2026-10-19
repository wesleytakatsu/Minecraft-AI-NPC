# AI message and task protocol (proposal)

This protocol is **not implemented in v0.0.1**. It documents a stable boundary between dialogue, intent detection and action execution.

## Messages

Incoming Minecraft chat with the `@nexus` prefix is addressed to the companion.

- `@nexus oi`: conversational intent
- `@nexus venha aqui`: `COME_HERE`
- `@nexus cave um túnel de 10 blocos`: `DIG_TUNNEL`, length=10
- `@nexus pare`: local `STOP` override; never wait for an AI provider
- `@nexus você consegue minerar?`: a question, **not** a mining order

Only authorized players should be able to issue physical commands.

## Intent categories

`CHAT`, `HELP`, `STATUS`, `FOLLOW`, `COME_HERE`, `STOP`, `DIG_TUNNEL`, `UNKNOWN`.

JEV may choose among allowed intents; it should not determine block-breaking speed, crafting recipes, mob damage or low-level movement.

## Example normalized result

```json
{
  "intent": "DIG_TUNNEL",
  "arguments": {
    "length": 10,
    "width": 1,
    "height": 2
  },
  "requires_clarification": false
}
```

All fields must be validated server-side. Never directly deserialize arbitrary model output into executable Java instructions.

## Fallbacks

1. Built-in critical commands are handled locally.
2. JEV/LLM timeouts produce a friendly failure, with no world mutation.
3. Unknown intent becomes conversation/clarification, not an irreversible operation.
4. A mission executor performs tasks using approved skills and reports verified results.

## Economics and privacy

- No model call per tick or combat attack.
- Debounce repeated requests and apply per-player rate limits.
- Send the minimum necessary world snapshot (not full chat history or secret coordinates).
- Store API keys server-side outside the tracked repository.
- Provide opt-in configuration for external AI processing.
