package net.aoi39.velocitydiscordchatbridge;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.aoi39.velocitydiscordchatbridge.libs.Config;
import net.aoi39.velocitydiscordchatbridge.libs.JDAManager;
import net.aoi39.velocitydiscordchatbridge.libs.LangManager;
import net.aoi39.velocitydiscordchatbridge.listeners.PlayerChatListener;
import net.aoi39.velocitydiscordchatbridge.listeners.PlayerDisconnectListener;
import net.aoi39.velocitydiscordchatbridge.listeners.PlayerLoginListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "velocitydiscordchatbridge",
        name = "VelocityDiscordChatBridge",
        version = "1.0.0",
        description = "Sync Discord chat with all servers connected by Velocity",
        url = "https://github.com/Aoi39/VelocityDiscordChatBridge",
        authors = { "Aoi39" }
)
public class VelocityDiscordChatBridge {
    private static final Logger logger = LoggerFactory.getLogger("VelocityDiscordChatBridge");
    private final ProxyServer server;
    private final Path dataDirectory;
    private JDAManager jdaManager;

    public static Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    public JDAManager getJdaManager() {
        return jdaManager;
    }

    @Inject
    public VelocityDiscordChatBridge(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
        new Config(this, dataDirectory);
        LangManager.loadLangFile();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        logger.info("VelocityDiscordChatBridge initialized!");
        this.jdaManager = new JDAManager(this);
        new PlayerChatListener(this);
        new PlayerDisconnectListener(this);
        new PlayerLoginListener(this);
        if (Config.discordBotEnableDiscordBot && jdaManager.getJda() != null && !Config.discordBotPlayerListChannelId.isEmpty()) {
            this.server.getScheduler().buildTask(this, jdaManager::updatePlayerListEmbedMessage).repeat(Config.playerListPlayerListUpdateInterval, TimeUnit.MINUTES).schedule();
        }
    }

    @Subscribe
    private void onProxyShutdown(ProxyShutdownEvent event) {
        jdaManager.shutdownDiscordBot();
    }

}
