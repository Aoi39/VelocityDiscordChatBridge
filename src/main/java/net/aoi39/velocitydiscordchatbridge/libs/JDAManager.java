package net.aoi39.velocitydiscordchatbridge.libs;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.aoi39.velocitydiscordchatbridge.VelocityDiscordChatBridge;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JDAManager extends ListenerAdapter {
    private final VelocityDiscordChatBridge plugin;
    private JDA jda;

    public JDAManager(VelocityDiscordChatBridge plugin) {
        this.plugin = plugin;
        this.startDiscordBot();
    }

    public JDA getJda() {
        return jda;
    }

    private void startDiscordBot() {
        try {
            if (Config.discordBotEnableDiscordBot && jda == null) {
                if (Config.discordBotToken.isEmpty()) {
                    VelocityDiscordChatBridge.getLogger().error("Specify a token to enable discordBot");
                    System.exit(1);
                }
                if (Config.discordBotGuildId.isEmpty()) {
                    VelocityDiscordChatBridge.getLogger().info("Specify a guildId to enable discordBot");
                    System.exit(1);
                }
                VelocityDiscordChatBridge.getLogger().info("Starting DiscordBot...");
                jda = JDABuilder.createDefault(Config.discordBotToken)
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .addEventListeners(this)
                        .build()
                        .awaitReady();

                Guild guild = jda.getGuildById(Config.discordBotGuildId);
                if (guild == null) {
                    VelocityDiscordChatBridge.getLogger().warn("The specified server could not be found");
                } else {
                    if (Config.discordBotEnableWhitelistCommand) {
                        Optional<PluginContainer> velocityWhitelist = plugin.getServer().getPluginManager().getPlugin("velocitywhitelist");
                        if (velocityWhitelist.isEmpty()) {
                            VelocityDiscordChatBridge.getLogger().warn("VelocityWhitelist plugin not found");
                        } else {
                            guild.updateCommands()
                                    .addCommands(
                                            Commands.slash("whitelist", "whitelist")
                                                    .addSubcommands(
                                                            new SubcommandData("add", "Add user to whitelist")
                                                                    .addOptions(
                                                                            new OptionData(OptionType.STRING, "user", "Users to be added to whitelist", true)
                                                                    ),
                                                            new SubcommandData("remove", "Remove user from whitelist")
                                                                    .addOptions(
                                                                            new OptionData(OptionType.STRING, "user", "Users to be removed from whitelist", true)
                                                                    ),
                                                            new SubcommandData("list", "Display a list of whitelists")
                                                    )
                                    ).queue();
                        }
                    }
                    if (!Config.discordBotChatBridgeChannelId.isEmpty() && Config.chatBridgeNotifyServerStartupAndShutdown) {
                        MessageEmbed embed = new EmbedBuilder()
                                .setAuthor(LangManager.getMessage("serverHasStarted"))
                                .setColor(0x00ff00)
                                .build();
                        jda.getTextChannelById(Config.discordBotChatBridgeChannelId).sendMessageEmbeds(embed).queue();
                    }
                }
            }
        } catch (Exception e) {
            VelocityDiscordChatBridge.getLogger().error("Failed to start DiscordBot\n{}", e.getMessage());
        }
    }

    public void shutdownDiscordBot() {
        if (jda != null) {
            if (!Config.discordBotChatBridgeChannelId.isEmpty() && Config.chatBridgeNotifyServerStartupAndShutdown) {
                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor(LangManager.getMessage("serverHasStopped"))
                        .setColor(0xff0000)
                        .build();
                jda.getTextChannelById(Config.discordBotChatBridgeChannelId).sendMessageEmbeds(embed).complete();
            }
            if (!Config.discordBotPlayerListChannelId.isEmpty()) {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle(LangManager.getMessage("playerList"))
                        .setDescription(LangManager.getMessage("serverHasStopped"))
                        .setFooter(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")))
                        .setColor(0xff0000)
                        .build();
                TextChannel channel = jda.getTextChannelById(Config.discordBotPlayerListChannelId);
                List<Message> messages = channel.getHistory().retrievePast(1).complete();
                if (messages.isEmpty()) {
                    channel.sendMessageEmbeds(embed).complete();
                } else {
                    if (messages.get(0).getAuthor().equals(jda.getSelfUser())) {
                        messages.get(0).editMessageEmbeds(embed).complete();
                    }
                }
            }
            jda.shutdown();
        }
    }

    public void sendChatBridgeChannel(String message) {
        jda.getTextChannelById(Config.discordBotChatBridgeChannelId).sendMessage(message).queue();
    }

    private String formatMessage(Message message) {
        String text = message.getContentDisplay();

        if (!message.getAttachments().isEmpty()) {
            text = "(" + LangManager.getMessage("attachment") + ") " + text;
        }

        return text;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (jda != null && !event.getAuthor().equals(jda.getSelfUser()) && !Config.discordBotChatBridgeChannelId.isEmpty() && event.getChannel().getId().equals(Config.discordBotChatBridgeChannelId)) {
            if (Config.chatBridgeShowBotMessages && event.getAuthor().isBot()) {
                return;
            }
            Component message = Component.text("[Discord]").color(TextColor.fromHexString(Config.chatBridgeServerNamePrefixColor)).append(Component.text("<" + (event.getMember().getNickname() == null ? event.getAuthor().getEffectiveName() : event.getMember().getNickname()) + "> " + formatMessage(event.getMessage())).color(TextColor.color(255, 255, 255)));
            for (RegisteredServer server : plugin.getServer().getAllServers()) {
                server.sendMessage(message);
            }
        }
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
           if (event.getGuild().getId().equals(Config.discordBotGuildId)) {
               Role whitelistableRole = null;
               if (!Config.discordBotWhitelistableRoleId.isEmpty()) {
                   whitelistableRole = event.getGuild().getRoleById(Config.discordBotWhitelistableRoleId);
               }
               if (event.getName().equals("whitelist")) {
                   if (whitelistableRole != null && !event.getMember().getRoles().contains(whitelistableRole)) {
                       event.reply(LangManager.getMessage("notPermitted")).setEphemeral(true).queue();
                       return;
                   }
                   try {
                       Class<?> whitelistManager = Class.forName("net.aoi39.velocitywhitelist.libs.WhitelistManager");
                       if (event.getSubcommandName().equals("add")) {
                           Method method = whitelistManager.getDeclaredMethod("addWhitelist", String.class);
                           Object response = method.invoke(null, event.getOption("user").getAsString());
                           if (response.equals(200)) {
                               event.reply(LangManager.getMessage("addedPlayerToTheWhitelist").replace("{username}", event.getOption("user").getAsString())).queue();
                               return;
                           }
                           if (response.equals(404)) {
                               event.reply(LangManager.getMessage("playerNotFound")).queue();
                               return;
                           }
                           if (response.equals(409)) {
                               event.reply(LangManager.getMessage("playerHaveAlreadyBeenAdded")).queue();
                               return;
                           }
                           event.reply(LangManager.getMessage("anErrorHasOccurred") + "\nCode: " + response).queue();
                           return;
                       }
                       if (event.getSubcommandName().equals("remove")) {
                           Method method = whitelistManager.getDeclaredMethod("removeWhitelist", String.class);
                           Object response = method.invoke(null, event.getOption("user").getAsString());
                           if (response.equals(200)) {
                               event.reply(LangManager.getMessage("removedPlayerFromTheWhitelist").replace("{username}", event.getOption("user").getAsString())).queue();
                               return;
                           }
                           if (response.equals(404)) {
                               event.reply(LangManager.getMessage("playerNotFound")).queue();
                               return;
                           }
                           event.reply(LangManager.getMessage("anErrorHasOccurred") + "\nCode: " + response).queue();
                           return;
                       }
                       if (event.getSubcommandName().equals("list")) {
                           EmbedBuilder embedBuilder = new EmbedBuilder()
                                   .setTitle("Whitelist")
                                   .setColor(0x00ff00);
                           List<String> whitelistUserNames = (List<String>) whitelistManager.getDeclaredField("whitelistUserNames").get(null);
                           whitelistUserNames.sort(Comparator.naturalOrder());
                           for (int i = 0; i <whitelistUserNames.size(); i += 10) {
                               embedBuilder.addField("", whitelistUserNames.subList(i, Math.min(i + 10, whitelistUserNames.size())).stream().map(s -> "`" + s + "`").collect(Collectors.joining("\n")), true);
                           }
                           event.replyEmbeds(embedBuilder.build()).queue();
                       }
                   } catch (Exception e) {
                       VelocityDiscordChatBridge.getLogger().error(e.getMessage());
                   }
               }
           } 
        });
    }

    public void loginMessage(Player player) {
        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(LangManager.getMessage("playerJoinedTheGame").replace("{username}", player.getUsername()), null, Config.chatBridgeNotifyJoinAndLeaveIcon.replace("{username}", player.getUsername()).replace("{uuid}", String.valueOf(player.getUniqueId())))
                .setColor(Integer.parseInt(Config.chatBridgeNotifyJoinMessageColor.substring(1), 16))
                .build();
        jda.getTextChannelById(Config.discordBotChatBridgeChannelId).sendMessageEmbeds(embed).queue();
    }

    public void disconnectMessage(Player player) {
        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(LangManager.getMessage("playerLeftTheGame").replace("{username}", player.getUsername()), null, Config.chatBridgeNotifyJoinAndLeaveIcon.replace("{username}", player.getUsername()).replace("{uuid}", String.valueOf(player.getUniqueId())))
                .setColor(Integer.parseInt(Config.chatBridgeNotifyLeaveMessageColor.substring(1), 16))
                .build();
        jda.getTextChannelById(Config.discordBotChatBridgeChannelId).sendMessageEmbeds(embed).queue();
    }

    public void updatePlayerListEmbedMessage() {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(LangManager.getMessage("playerList"))
                .setFooter(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")))
                .setColor(Integer.parseInt(Config.playerListPlayerListMessageColor.substring(1), 16));
        List<RegisteredServer> servers = plugin.getServer().getAllServers().stream()
                .sorted(Comparator.comparingInt(server -> {
                    int index = Config.playerListPlayerListOrder.indexOf(server.getServerInfo().getName());
                    return index == -1 ? Integer.MAX_VALUE : index;
                })).toList();
        for (RegisteredServer server : servers) {
            embedBuilder.addField(server.getServerInfo().getName(), String.join(", ", server.getPlayersConnected().stream().map(Player::getUsername).toList()), false);
        }
        TextChannel channel = jda.getTextChannelById(Config.discordBotPlayerListChannelId);
        channel.getHistory().retrievePast(1).queue(messages -> {
            if (messages.isEmpty()) {
                channel.sendMessageEmbeds(embedBuilder.build()).queue();
            } else {
                if (messages.get(0).getAuthor().equals(jda.getSelfUser())) {
                    messages.get(0).editMessageEmbeds(embedBuilder.build()).queue();
                }
            }
        });
    }

}
