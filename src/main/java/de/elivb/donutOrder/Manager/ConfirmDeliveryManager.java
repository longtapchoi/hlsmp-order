package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfirmDeliveryManager {
   private final Order plugin;
   private FileConfiguration confirmDeliveryConfig;
   private File confirmDeliveryFile;

   public ConfirmDeliveryManager(Order plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   private void loadConfig() {
      File guiFolder = new File(this.plugin.getDataFolder(), "gui");
      if (!guiFolder.exists()) {
         guiFolder.mkdirs();
      }

      this.confirmDeliveryFile = new File(guiFolder, "confirm-delivery.yml");
      if (!this.confirmDeliveryFile.exists()) {
         this.plugin.saveResource("gui/confirm-delivery.yml", false);
      }

      this.confirmDeliveryConfig = YamlConfiguration.loadConfiguration(this.confirmDeliveryFile);
   }

   public String getConfirmDeliveryTitle() {
      return this.confirmDeliveryConfig.getString("confirm-delivery.title", "&8ᴏʀᴅᴇʀs -> Confirm Delivery");
   }

   public int getConfirmDeliveryRows() {
      int rows = this.confirmDeliveryConfig.getInt("confirm-delivery.rows", 4);
      return Math.max(1, Math.min(6, rows));
   }

   public Map<String, Object> getConfirmDeliveryItems() {
      Map<String, Object> items = new HashMap();
      ConfigurationSection itemsSection = this.confirmDeliveryConfig.getConfigurationSection("items");
      if (itemsSection != null) {
         if (itemsSection.contains("cancel")) {
            ConfigurationSection cancelSection = itemsSection.getConfigurationSection("cancel");
            if (cancelSection != null) {
               Map<String, Object> cancel = new HashMap();
               cancel.put("material", cancelSection.getString("material", "RED_STAINED_GLASS_PANE"));
               cancel.put("slot", this.validateSlot(cancelSection.getInt("slot", 30), 4));
               cancel.put("name", cancelSection.getString("name", "&#fc0404ᴄᴀɴᴄᴇʟ"));
               cancel.put("lore", cancelSection.getStringList("lore"));
               items.put("cancel", cancel);
            }
         }

         if (itemsSection.contains("confirm")) {
            ConfigurationSection confirmSection = itemsSection.getConfigurationSection("confirm");
            if (confirmSection != null) {
               Map<String, Object> confirm = new HashMap();
               confirm.put("material", confirmSection.getString("material", "LIME_STAINED_GLASS_PANE"));
               confirm.put("slot", this.validateSlot(confirmSection.getInt("slot", 32), 4));
               confirm.put("name", confirmSection.getString("name", "&#04fc04ᴄᴏɴғɪʀᴍ"));
               confirm.put("lore", confirmSection.getStringList("lore"));
               items.put("confirm", confirm);
            }
         }

         if (itemsSection.contains("source-item")) {
            ConfigurationSection sourceSection = itemsSection.getConfigurationSection("source-item");
            if (sourceSection != null) {
               Map<String, Object> sourceItem = new HashMap();
               sourceItem.put("slot", this.validateSlot(sourceSection.getInt("slot", 31), 4));
               items.put("source-item", sourceItem);
            }
         }
      }

      return items;
   }

   private int validateSlot(int slot, int rows) {
      int maxSlot = rows * 9 - 1;
      return slot >= 0 && slot <= maxSlot ? slot : maxSlot;
   }

   public void reloadConfig() {
      this.loadConfig();
   }
}
