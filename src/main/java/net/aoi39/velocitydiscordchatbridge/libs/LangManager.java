package net.aoi39.velocitydiscordchatbridge.libs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.aoi39.velocitydiscordchatbridge.VelocityDiscordChatBridge;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LangManager {
    private static JsonObject langData;

    public static void loadLangFile() {
        try (
             InputStream is = VelocityDiscordChatBridge.class.getResourceAsStream("/lang/" + Config.systemLanguage + ".json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            langData = new Gson().fromJson(reader, JsonObject.class);
            VelocityDiscordChatBridge.getLogger().info("Success load language file!");
        } catch (Exception e) {
            VelocityDiscordChatBridge.getLogger().error("Failed to load language file\n{}", e.getMessage());
            System.exit(1);
        }
    }

    public static String getMessage(String key) {
        if (langData == null) {
            return "Failed to load language file";
        }
        return langData.get(key).getAsString();
    }

}
