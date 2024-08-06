package net.aoi39.velocitydiscordchatbridge.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.aoi39.velocitydiscordchatbridge.VelocityDiscordChatBridge;
import net.aoi39.velocitydiscordchatbridge.libs.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class PlayerChatListener {
    private final VelocityDiscordChatBridge plugin;

    public PlayerChatListener(VelocityDiscordChatBridge plugin) {
        this.plugin = plugin;
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Subscribe
    private void onPlayerChat(PlayerChatEvent event) {
        Component message = Component.text("<" + event.getPlayer().getUsername() + "> " + event.getMessage()).color(TextColor.color(255, 255, 255));
        if (Config.chatBridgeEnableServerNamePrefix) {
            message = Component.text("[" + event.getPlayer().getCurrentServer().get().getServerInfo().getName() + "]").color(TextColor.fromHexString(Config.chatBridgeServerNamePrefixColor)).append(message);
        }
        for (RegisteredServer server : plugin.getServer().getAllServers()) {
            if (!server.getServerInfo().getName().equals(event.getPlayer().getCurrentServer().get().getServerInfo().getName())) {
                server.sendMessage(message);
            }
        }
        if (Config.discordBotEnableDiscordBot && !Config.discordBotChatBridgeChannelId.isEmpty() &&plugin.getJdaManager().getJda() != null) {
            String discordMessage = "<" + event.getPlayer().getUsername() + "> " + event.getMessage();
            if (Config.chatBridgeEnableServerNamePrefix) {
                discordMessage = "[" + event.getPlayer().getCurrentServer().get().getServerInfo().getName() + "]" + discordMessage;
            }
            plugin.getJdaManager().sendChatBridgeChannel(discordMessage);
        }

    }

}
