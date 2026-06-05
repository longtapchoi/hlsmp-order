package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class CollectItemsManager {
   private final Order plugin;
   private FileConfiguration collectItemsConfig;
   private File collectItemsFile;

   public CollectItemsManager(Order plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   private void loadConfig() {
      File guiFolder = new File(this.plugin.getDataFolder(), "gui");
      if (!guiFolder.exists()) {
         guiFolder.mkdirs();
      }

      this.collectItemsFile = new File(guiFolder, "collect-items.yml");
      if (!this.collectItemsFile.exists()) {
         this.plugin.saveResource("gui/collect-items.yml", false);
      }

      this.collectItemsConfig = YamlConfiguration.loadConfiguration(this.collectItemsFile);
   }

   public String getCollectItemsTitle() {
      return this.collectItemsConfig.getString("collect-items.title", "&8ᴏʀᴅᴇʀs -> Collect Items");
   }

   public int getCollectItemsRows() {
      return this.collectItemsConfig.getInt("collect-items.rows", 6);
   }

   public Map<String, Object> getCollectItemsItems() {
      Map<String, Object> items = new HashMap();
      ConfigurationSection itemsSection = this.collectItemsConfig.getConfigurationSection("items");
      if (itemsSection != null) {
         int prevSlot = itemsSection.getInt("previous-page-slot", 45);
         items.put("previous-page-slot", prevSlot);
         int nextSlot = itemsSection.getInt("next-page-slot", 53);
         items.put("next-page-slot", nextSlot);
         if (itemsSection.contains("previous-page")) {
            ConfigurationSection prevSection = itemsSection.getConfigurationSection("previous-page");
            if (prevSection != null) {
               Map<String, Object> prevPage = new HashMap();
               prevPage.put("material", prevSection.getString("material", "ARROW"));
               prevPage.put("displayname", prevSection.getString("displayname", "&#00fc88ᴘʀᴇᴠɪᴏᴜꜱ"));
               prevPage.put("lore", prevSection.getStringList("lore"));
               items.put("previous-page", prevPage);
            }
         }

         if (itemsSection.contains("next-page")) {
            ConfigurationSection nextSection = itemsSection.getConfigurationSection("next-page");
            if (nextSection != null) {
               Map<String, Object> nextPage = new HashMap();
               nextPage.put("material", nextSection.getString("material", "ARROW"));
               nextPage.put("displayname", nextSection.getString("displayname", "&#00fc88ɴᴇxᴛ"));
               nextPage.put("lore", nextSection.getStringList("lore"));
               items.put("next-page", nextPage);
            }
         }

         if (itemsSection.contains("drop-loot")) {
            ConfigurationSection dropSection = itemsSection.getConfigurationSection("drop-loot");
            if (dropSection != null) {
               Map<String, Object> dropLoot = new HashMap();
               dropLoot.put("material", dropSection.getString("material", "DISPENSER"));
               dropLoot.put("slot", dropSection.getInt("slot", 49));
               dropLoot.put("name", dropSection.getString("name", "&#00fc88ᴅʀᴏᴘ ʟᴏᴏᴛ"));
               dropLoot.put("lore", dropSection.getStringList("lore"));
               items.put("drop-loot", dropLoot);
            }
         }
      }

      return items;
   }

   public void reloadConfig() {
      this.loadConfig();
   }
}
