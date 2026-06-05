package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class EditOrderManager {
   private final Order plugin;
   private FileConfiguration editOrderConfig;
   private File editOrderFile;

   public EditOrderManager(Order plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   private void loadConfig() {
      File guiFolder = new File(this.plugin.getDataFolder(), "gui");
      if (!guiFolder.exists()) {
         guiFolder.mkdirs();
      }

      this.editOrderFile = new File(guiFolder, "edit-order.yml");
      if (!this.editOrderFile.exists()) {
         this.plugin.saveResource("gui/edit-order.yml", false);
      }

      this.editOrderConfig = YamlConfiguration.loadConfiguration(this.editOrderFile);
   }

   public String getEditOrderTitle() {
      return this.editOrderConfig.getString("edit-order.title", "&8ᴏʀᴅᴇʀs -> Edit Order");
   }

   public int getEditOrderRows() {
      return this.editOrderConfig.getInt("edit-order.rows", 3);
   }

   public Map<String, Object> getEditOrderItems() {
      Map<String, Object> items = new HashMap();
      ConfigurationSection itemsSection = this.editOrderConfig.getConfigurationSection("items");
      if (itemsSection != null) {
         if (itemsSection.contains("background")) {
            ConfigurationSection bgSection = itemsSection.getConfigurationSection("background");
            if (bgSection != null) {
               Map<String, Object> background = new HashMap();
               background.put("material", bgSection.getString("material", "BLACK_STAINED_GLASS_PANE"));
               background.put("name", bgSection.getString("name", " "));
               background.put("lore", bgSection.getStringList("lore"));
               String slotsStr = bgSection.getString("slots", "0,1,2,9,11,19,20");
               List<Integer> slots = this.parseSlots(slotsStr);
               background.put("slots", slots);
               items.put("background", background);
            }
         }

         if (itemsSection.contains("source-item")) {
            ConfigurationSection sourceSection = itemsSection.getConfigurationSection("source-item");
            if (sourceSection != null) {
               Map<String, Object> sourceItem = new HashMap();
               sourceItem.put("slot", sourceSection.getInt("slot", 10));
               items.put("source-item", sourceItem);
            }
         }

         if (itemsSection.contains("cancel-order")) {
            ConfigurationSection cancelSection = itemsSection.getConfigurationSection("cancel-order");
            if (cancelSection != null) {
               Map<String, Object> cancelOrder = new HashMap();
               cancelOrder.put("material", cancelSection.getString("material", "RED_TERRACOTTA"));
               cancelOrder.put("name", cancelSection.getString("name", "&#00fc88ᴄᴀɴᴄᴇʟ"));
               cancelOrder.put("lore", cancelSection.getStringList("lore"));
               cancelOrder.put("slot", cancelSection.getInt("slot", 13));
               items.put("cancel-order", cancelOrder);
            }
         }

         if (itemsSection.contains("collect-items")) {
            ConfigurationSection collectSection = itemsSection.getConfigurationSection("collect-items");
            if (collectSection != null) {
               Map<String, Object> collectItems = new HashMap();
               collectItems.put("material", collectSection.getString("material", "CHEST"));
               collectItems.put("name", collectSection.getString("name", "&#00fc88ᴄᴏʟʟᴇᴄᴛ"));
               collectItems.put("lore", collectSection.getStringList("lore"));
               collectItems.put("slot", collectSection.getInt("slot", 15));
               items.put("collect-items", collectItems);
            }
         }

         if (itemsSection.contains("no-collect-items")) {
            ConfigurationSection noCollectSection = itemsSection.getConfigurationSection("no-collect-items");
            if (noCollectSection != null) {
               Map<String, Object> noCollectItems = new HashMap();
               noCollectItems.put("material", noCollectSection.getString("material", "CHEST"));
               noCollectItems.put("name", noCollectSection.getString("name", "&#00fc88ɴᴏ ᴄᴏʟʟᴇᴄᴛ"));
               noCollectItems.put("lore", noCollectSection.getStringList("lore"));
               noCollectItems.put("slot", noCollectSection.getInt("slot", 15));
               items.put("no-collect-items", noCollectItems);
            }
         }

         if (itemsSection.contains("back")) {
            ConfigurationSection backSection = itemsSection.getConfigurationSection("back");
            if (backSection != null) {
               Map<String, Object> back = new HashMap();
               back.put("material", backSection.getString("material", "RED_STAINED_GLASS_PANE"));
               back.put("name", backSection.getString("name", "&#00fc88ʙᴀᴄᴋ"));
               back.put("lore", backSection.getStringList("lore"));
               back.put("slot", backSection.getInt("slot", 18));
               items.put("back", back);
            }
         }
      }

      return items;
   }

   private List<Integer> parseSlots(String slotsStr) {
      List<Integer> slots = new ArrayList();
      String[] parts = slotsStr.split(",");

      for(String part : parts) {
         try {
            slots.add(Integer.parseInt(part.trim()));
         } catch (NumberFormatException var9) {
         }
      }

      return slots;
   }

   public void reloadConfig() {
      this.loadConfig();
   }
}
