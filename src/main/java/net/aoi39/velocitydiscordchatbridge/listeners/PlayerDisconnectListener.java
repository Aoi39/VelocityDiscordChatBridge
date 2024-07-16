package net.aoi39.velocitydiscordchatbridge.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import net.aoi39.velocitydiscordchatbridge.VelocityDiscordChatBridge;
import net.aoi39.velocitydiscordchatbridge.libs.Config;

public class PlayerDisconnectListener {
    private final VelocityDiscordChatBridge plugin;

    public PlayerDisconnectListener(VelocityDiscordChatBridge plugin) {
        this.plugin = plugin;
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Subscribe
    private void onDisconnect(DisconnectEvent event) {
        if (Config.discordBotEnableDiscordBot && plugin.getJdaManager().getJda() != null) {
            plugin.getJdaManager().disconnectMessage(event.getPlayer());
        }
    }

}
