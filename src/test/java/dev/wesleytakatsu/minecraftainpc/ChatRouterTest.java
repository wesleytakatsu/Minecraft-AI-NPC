package dev.wesleytakatsu.minecraftainpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChatRouterTest {
    @Test
    void ignoresOrdinaryChat() {
        assertNull(ChatRouter.extractRequest("bom dia"));
        assertNull(ChatRouter.extractRequest("@nexusbot oi"));
        assertNull(ChatRouter.extractRequest(null));
    }

    @Test
    void extractsOnlyAddressedText() {
        assertEquals("oi", ChatRouter.extractRequest("@nexus oi"));
        assertEquals("Ajuda", ChatRouter.extractRequest("  @NeXuS Ajuda "));
        assertEquals("", ChatRouter.extractRequest("@nexus"));
    }

    @Test
    void repliesWithoutExternalServices() {
        assertTrue(ChatRouter.reply("oi").contains("protótipo"));
        assertTrue(ChatRouter.reply("status").contains("v0.0.1"));
        assertTrue(ChatRouter.reply("cave um túnel").contains("não foram implementadas"));
    }
}
