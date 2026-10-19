package dev.wesleytakatsu.minecraftainpc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecraftAiNpcMod implements ModInitializer {
    public static final String MOD_ID = "minecraft_ai_npc";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String TAG = "[Nexus] ";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("nexus")
                .executes(context -> sendInfo(context.getSource(), help()))
                .then(CommandManager.literal("help")
                    .executes(context -> sendInfo(context.getSource(), help())))
                .then(CommandManager.literal("status")
                    .executes(context -> sendInfo(context.getSource(), ChatRouter.status())))
            )
        );

        // Intercept only messages explicitly addressed to the NPC.
        // No LLM/JEV network calls or NPC entities exist in this first milestone.
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String request = ChatRouter.extractRequest(message.getContent().getString());
            if (request == null) {
                return true;
            }

            sender.sendMessage(Text.literal(TAG + ChatRouter.reply(request)), false);
            return false; // Do not broadcast @nexus messages to other players.
        });

        LOGGER.info("Minecraft AI NPC initialized (chat prototype, no NPC or AI integrations).");
    }

    private static String help() {
        return "Commands: /nexus status, /nexus help; chat: @nexus oi, @nexus ajuda, @nexus status.";
    }

    private static int sendInfo(net.minecraft.server.command.ServerCommandSource source, String message) {
        source.sendFeedback(() -> Text.literal(TAG + message), false);
        return 1;
    }
}
