package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfirmCancelManager {
   private final Order plugin;
   private FileConfiguration confirmCancelConfig;
   private File confirmCancelFile;

   public ConfirmCancelManager(Order plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   private void loadConfig() {
      File guiFolder = new File(this.plugin.getDataFolder(), "gui");
      if (!guiFolder.exists()) {
         guiFolder.mkdirs();
      }

      this.confirmCancelFile = new File(guiFolder, "confirm-cancel.yml");
      if (!this.confirmCancelFile.exists()) {
         this.plugin.saveResource("gui/confirm-cancel.yml", false);
      }

      this.confirmCancelConfig = YamlConfiguration.loadConfiguration(this.confirmCancelFile);
   }

   public String getConfirmCancelTitle() {
      return this.confirmCancelConfig.getString("confirm-cancel.title", "&8ᴏʀᴅᴇʀs -> Cancel Order");
   }

   public int getConfirmCancelRows() {
      int rows = this.confirmCancelConfig.getInt("confirm-cancel.rows", 3);
      return Math.max(1, Math.min(6, rows));
   }

   public Map<String, Object> getConfirmCancelItems() {
      Map<String, Object> items = new HashMap();
      ConfigurationSection itemsSection = this.confirmCancelConfig.getConfigurationSection("items");
      if (itemsSection != null) {
         if (itemsSection.contains("cancel")) {
            ConfigurationSection cancelSection = itemsSection.getConfigurationSection("cancel");
            if (cancelSection != null) {
               Map<String, Object> cancel = new HashMap();
               cancel.put("material", cancelSection.getString("material", "RED_STAINED_GLASS_PANE"));
               cancel.put("slot", this.validateSlot(cancelSection.getInt("slot", 11), this.getConfirmCancelRows()));
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
               confirm.put("slot", this.validateSlot(confirmSection.getInt("slot", 15), this.getConfirmCancelRows()));
               confirm.put("name", confirmSection.getString("name", "&#04fc04ᴀᴄᴄᴇᴘᴛ"));
               confirm.put("lore", confirmSection.getStringList("lore"));
               items.put("confirm", confirm);
            }
         }

         if (itemsSection.contains("source-item")) {
            ConfigurationSection sourceSection = itemsSection.getConfigurationSection("source-item");
            if (sourceSection != null) {
               Map<String, Object> sourceItem = new HashMap();
               sourceItem.put("slot", this.validateSlot(sourceSection.getInt("slot", 13), this.getConfirmCancelRows()));
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
