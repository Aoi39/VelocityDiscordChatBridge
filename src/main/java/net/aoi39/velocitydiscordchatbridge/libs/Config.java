package net.aoi39.velocitydiscordchatbridge.libs;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.plugin.Plugin;
import net.aoi39.velocitydiscordchatbridge.VelocityDiscordChatBridge;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Config {
    private final VelocityDiscordChatBridge plugin;
    private final Path dataDirectory;

    public static Boolean discordBotEnableDiscordBot;
    public static String discordBotToken;
    public static String discordBotGuildId;
    public static String discordBotChatBridgeChannelId;
    public static String discordBotPlayerListChannelId;
    public static boolean discordBotEnableWhitelistCommand;
    public static String discordBotWhitelistableRoleId;

    public static boolean chatBridgeShowBotMessages;
    public static boolean chatBridgeEnableServerNamePrefix;
    public static String chatBridgeServerNamePrefixColor;
    public static boolean chatBridgeNotifyJoinAndLeave;
    public static String chatBridgeNotifyJoinMessageColor;
    public static String chatBridgeNotifyLeaveMessageColor;
    public static String chatBridgeNotifyJoinAndLeaveIcon;
    public static boolean chatBridgeNotifyServerStartupAndShutdown;

    public static int playerListPlayerListUpdateInterval;
    public static String playerListPlayerListMessageColor;
    public static List<String> playerListPlayerListOrder;

    public static String systemLanguage;

    public Config(VelocityDiscordChatBridge plugin, Path dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = dataDirectory.resolve("velocity-discordchatbridge.toml").toFile();
        if (!configFile.exists()) {
            try {
                Files.createDirectories(dataDirectory);
                Files.copy(VelocityDiscordChatBridge.class.getResourceAsStream("/velocity-discordchatbridge.toml"), dataDirectory.resolve("velocity-discordchatbridge.toml"));
                VelocityDiscordChatBridge.getLogger().info("Success generate config");
            } catch (Exception e) {
                VelocityDiscordChatBridge.getLogger().error("Failed to generate config file\n{}", e.getMessage());
                System.exit(1);
            }
        }
        Toml config = new Toml().read(configFile);
        updateConfig(config.getString("System.configVersion"));
        discordBotEnableDiscordBot = config.getBoolean("DiscordBot.enableDiscordBot");
        discordBotToken = config.getString("DiscordBot.token");
        discordBotGuildId = config.getString("DiscordBot.guildId");
        discordBotChatBridgeChannelId = config.getString("DiscordBot.chatBridgeChannelId");
        discordBotPlayerListChannelId = config.getString("DiscordBot.playerListChannelId");
        discordBotEnableWhitelistCommand = config.getBoolean("DiscordBot.enableWhitelistCommand");
        discordBotWhitelistableRoleId = config.getString("DiscordBot.whitelistableRoleId");
        chatBridgeShowBotMessages = config.getBoolean("ChatBridge.showBotMessages");
        chatBridgeEnableServerNamePrefix = config.getBoolean("ChatBridge.enableServerNamePrefix");
        chatBridgeServerNamePrefixColor = config.getString("ChatBridge.serverNamePrefixColor");
        chatBridgeNotifyJoinAndLeave = config.getBoolean("ChatBridge.notifyJoinAndLeave");
        chatBridgeNotifyJoinMessageColor = config.getString("ChatBridge.notifyJoinMessageColor");
        chatBridgeNotifyLeaveMessageColor = config.getString("ChatBridge.notifyLeaveMessageColor");
        chatBridgeNotifyJoinAndLeaveIcon = config.getString("ChatBridge.notifyJoinAndLeaveIcon");
        chatBridgeNotifyServerStartupAndShutdown = config.getBoolean("ChatBridge.notifyServerStartupAndShutdown");
        playerListPlayerListUpdateInterval = config.getLong("PlayerList.playerListUpdateInterval").intValue();
        playerListPlayerListMessageColor = config.getString("PlayerList.playerListMessageColor");
        playerListPlayerListOrder = config.getList("PlayerList.playerListOrder");
        systemLanguage = config.getString("System.language");
        VelocityDiscordChatBridge.getLogger().info("Success config load!");
    }

    private void updateConfig(String configVersion) {
        if (!configVersion.equals(plugin.getClass().getAnnotation(Plugin.class).version())) {
            try {
                Files.move(dataDirectory.resolve("velocity-discordchatbridge.toml"), dataDirectory.resolve("velocity-discordchatbridge-" + configVersion + ".toml"));
                VelocityDiscordChatBridge.getLogger().info("Regenerated due to different versions of config and plugin(The original config has been renamed to velocity-discordchatbridge-{}.toml)", plugin.getClass().getAnnotation(Plugin.class).version());
            } catch (Exception e) {
                VelocityDiscordChatBridge.getLogger().error("Failed to update config file\n{}", e.getMessage());
            }
        }
    }

}
