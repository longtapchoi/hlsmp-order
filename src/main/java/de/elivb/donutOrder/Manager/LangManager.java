package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import de.elivb.donutOrder.utils.ColorUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LangManager {
   private final Order plugin;
   private File langFile;
   private FileConfiguration langConfig;
   private String prefix;
   private boolean prefixEnabled;

   public LangManager(Order plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   private void loadConfig() {
      this.langFile = new File(this.plugin.getDataFolder(), "lang.yml");
      if (!this.langFile.exists()) {
         this.plugin.saveResource("lang.yml", false);
      }

      this.langConfig = YamlConfiguration.loadConfiguration(this.langFile);
      this.prefixEnabled = this.langConfig.getBoolean("prefix-enable", true);
      this.prefix = this.langConfig.getString("prefix", "&#00fc88ᴏʀᴅᴇʀ &7» ");
   }

   public String getMessage(String key) {
      String message = this.langConfig.getString("messages." + key);
      if (message == null) {
         return "Missing message: " + key;
      } else {
         if (this.prefixEnabled && !message.isEmpty()) {
            message = this.prefix + message;
         }

         return ColorUtil.color(message);
      }
   }

   public String getMessageWithoutPrefix(String key) {
      String message = this.langConfig.getString("messages." + key);
      return message == null ? "Missing message: " + key : ColorUtil.color(message);
   }

   public String getRawMessage(String key) {
      String message = this.langConfig.getString("messages." + key);
      return message == null ? "Missing message: " + key : ColorUtil.color(message);
   }

   public String getMessage(String key, Map<String, String> placeholders) {
      String message = this.getRawMessage(key);

      for(Map.Entry<String, String> entry : placeholders.entrySet()) {
         message = message.replace((CharSequence)entry.getKey(), (CharSequence)entry.getValue());
      }

      if (this.prefixEnabled && !message.isEmpty()) {
         message = this.prefix + message;
      }

      return ColorUtil.color(message);
   }

   public String getMessageWithoutPrefix(String key, Map<String, String> placeholders) {
      String message = this.getRawMessage(key);

      for(Map.Entry<String, String> entry : placeholders.entrySet()) {
         message = message.replace((CharSequence)entry.getKey(), (CharSequence)entry.getValue());
      }

      return ColorUtil.color(message);
   }

   public List<String> getMultilineMessage(String key) {
      String message = this.langConfig.getString(key);
      List<String> lines = new ArrayList();
      if (message == null) {
         lines.add("Missing message: " + key);
         return lines;
      } else {
         String[] split = message.split("\n");

         for(String line : split) {
            if (!line.trim().isEmpty()) {
               lines.add(ColorUtil.color(line));
            }
         }

         return lines;
      }
   }

   public List<String> getMultilineMessage(String key, Map<String, String> placeholders) {
      List<String> lines = this.getMultilineMessage(key);
      List<String> processed = new ArrayList();

      for(String line : lines) {
         String processedLine = line;

         for(Map.Entry<String, String> entry : placeholders.entrySet()) {
            processedLine = processedLine.replace((CharSequence)entry.getKey(), (CharSequence)entry.getValue());
         }

         processed.add(processedLine);
      }

      return processed;
   }

   public void sendMessage(Player player, String key) {
      player.sendMessage(this.getMessage(key));
   }

   public void sendMessageWithoutPrefix(Player player, String key) {
      player.sendMessage(this.getMessageWithoutPrefix(key));
   }

   public void sendMessage(Player player, String key, Map<String, String> placeholders) {
      player.sendMessage(this.getMessage(key, placeholders));
   }

   public void sendMessageWithoutPrefix(Player player, String key, Map<String, String> placeholders) {
      player.sendMessage(this.getMessageWithoutPrefix(key, placeholders));
   }

   public void sendMultilineMessage(Player player, String key) {
      for(String line : this.getMultilineMessage(key)) {
         player.sendMessage(line);
      }

   }

   public void sendMultilineMessage(Player player, String key, Map<String, String> placeholders) {
      for(String line : this.getMultilineMessage(key, placeholders)) {
         player.sendMessage(line);
      }

   }

   public String getPrefix() {
      return this.prefix;
   }

   public boolean isPrefixEnabled() {
      return this.prefixEnabled;
   }

   public void reload() {
      this.loadConfig();
   }
}
