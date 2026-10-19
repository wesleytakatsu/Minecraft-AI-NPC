package dev.wesleytakatsu.minecraftainpc;

import java.util.Locale;

public final class ChatRouter {
    private static final String PREFIX = "@nexus";

    private ChatRouter() {
    }

    /**
     * Returns the content addressed to Nexus, or null for ordinary chat.
     * Matching is case-insensitive, but requires a word boundary after @nexus.
     */
    public static String extractRequest(String rawMessage) {
        if (rawMessage == null) {
            return null;
        }

        String text = rawMessage.trim();
        if (!text.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            return null;
        }

        if (text.length() > PREFIX.length()
                && !Character.isWhitespace(text.charAt(PREFIX.length()))) {
            return null;
        }

        return text.substring(PREFIX.length()).trim();
    }

    public static String status() {
        return "Prototype v0.0.1 running. NPC, LLM and JEV are not implemented yet.";
    }

    /**
     * Deterministic, offline responses. Replace with an intent router in a later milestone.
     */
    public static String reply(String request) {
        String normalized = request.strip().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "", "ajuda", "help" ->
                "Olá! Comandos disponíveis: @nexus oi, @nexus ajuda, @nexus status e /nexus status.";
            case "oi", "olá", "ola", "hello", "hi" ->
                "Olá! Ainda sou um protótipo, mas já consigo receber suas mensagens.";
            case "status" -> status();
            default ->
                "Mensagem recebida. As conversas com LLM e as missões ainda não foram implementadas.";
        };
    }
}
