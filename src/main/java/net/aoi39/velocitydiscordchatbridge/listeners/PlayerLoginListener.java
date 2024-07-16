package net.aoi39.velocitydiscordchatbridge.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.aoi39.velocitydiscordchatbridge.VelocityDiscordChatBridge;
import net.aoi39.velocitydiscordchatbridge.libs.Config;

public class PlayerLoginListener {
    private final VelocityDiscordChatBridge plugin;

    public PlayerLoginListener(VelocityDiscordChatBridge plugin) {
        this.plugin = plugin;
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Subscribe
    private void onLogin(LoginEvent event) {
        if (Config.discordBotEnableDiscordBot && plugin.getJdaManager().getJda() != null) {
            plugin.getJdaManager().loginMessage(event.getPlayer());
        }
    }

}
