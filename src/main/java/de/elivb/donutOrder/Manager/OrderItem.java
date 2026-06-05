package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import de.elivb.donutOrder.utils.ColorUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OrderItem {
   private UUID orderUuid;
   private final String name;
   private final List<String> description;
   private final double pricePerItem;
   private final Material material;
   private final String itemId;
   private final List<String> commands;
   private final UUID creator;
   private final Date creationDate;
   private Date expireDate;
   private Date deletionDate;
   private boolean active;
   private boolean expiredNotified;
   private String itemType;
   private String subType;
   private int requestedAmount;
   private int deliveredAmount;
   private int collectedAmount;
   private double paidAmount;
   private int deliveryTime;
   private boolean wasFullyDelivered;

   public OrderItem(String name, List<String> description, double pricePerItem, Material material, String itemId, List<String> commands, UUID creator, int requestedAmount) {
      this(name, description, pricePerItem, material, itemId, commands, creator, (String)null, (String)null, requestedAmount);
   }

   public OrderItem(String name, List<String> description, double pricePerItem, Material material, String itemId, List<String> commands, UUID creator, String itemType, String subType, int requestedAmount) {
      this.orderUuid = null;
      this.name = name;
      this.description = (List<String>)(description != null ? description : new ArrayList());
      this.pricePerItem = pricePerItem;
      this.material = material;
      this.itemId = itemId;
      this.commands = (List<String>)(commands != null ? commands : new ArrayList());
      this.creator = creator;
      this.creationDate = new Date();
      this.requestedAmount = requestedAmount;
      this.deliveredAmount = 0;
      this.collectedAmount = 0;
      this.paidAmount = pricePerItem * (double)requestedAmount;
      int expireSeconds = Order.getInstance().getConfig().getInt("expire-time-seconds", 604800);
      int deletionSeconds = Order.getInstance().getConfig().getInt("deletion-time-seconds", 604800);
      this.deliveryTime = expireSeconds / 3600;
      this.expireDate = new Date(System.currentTimeMillis() + (long)expireSeconds * 1000L);
      this.deletionDate = new Date(System.currentTimeMillis() + (long)(expireSeconds + deletionSeconds) * 1000L);
      this.active = true;
      this.itemType = itemType;
      this.subType = subType;
      this.wasFullyDelivered = false;
      this.expiredNotified = false;
   }

   public UUID getOrderUuid() {
      return this.orderUuid;
   }

   public void setOrderUuid(UUID orderUuid) {
      this.orderUuid = orderUuid;
   }

   public String getName() {
      return this.name;
   }

   public List<String> getDescription() {
      return new ArrayList(this.description);
   }

   public double getPricePerItem() {
      return this.pricePerItem;
   }

   public double getTotalPrice() {
      return this.pricePerItem * (double)this.requestedAmount;
   }

   public Material getMaterial() {
      return this.material;
   }

   public String getItemId() {
      return this.itemId;
   }

   public List<String> getCommands() {
      return new ArrayList(this.commands);
   }

   public UUID getCreator() {
      return this.creator;
   }

   public Date getCreationDate() {
      return this.creationDate;
   }

   public Date getExpireDate() {
      return this.expireDate;
   }

   public Date getDeletionDate() {
      return this.deletionDate;
   }

   public boolean isActive() {
      return this.active;
   }

   public boolean isExpired() {
      return (new Date()).after(this.expireDate);
   }

   public boolean isFullyDelivered() {
      return this.deliveredAmount >= this.requestedAmount;
   }

   public boolean isFullyPaid() {
      return this.paidAmount >= this.getTotalPrice();
   }

   public boolean wasFullyDelivered() {
      return this.wasFullyDelivered;
   }

   public boolean isExpiredNotified() {
      return this.expiredNotified;
   }

   public int getRequestedAmount() {
      return this.requestedAmount;
   }

   public int getDeliveredAmount() {
      return this.deliveredAmount;
   }

   public int getCollectedAmount() {
      return this.collectedAmount;
   }

   public int getRemainingAmount() {
      return Math.max(0, this.requestedAmount - this.deliveredAmount);
   }

   public int getAvailableToCollect() {
      return Math.max(0, this.deliveredAmount - this.collectedAmount);
   }

   public double getPaidAmount() {
      return this.paidAmount;
   }

   public double getRemainingPayment() {
      return Math.max((double)0.0F, this.getTotalPrice() - this.paidAmount);
   }

   public int getDeliveryTime() {
      return this.deliveryTime;
   }

   public String getItemType() {
      return this.itemType;
   }

   public String getSubType() {
      return this.subType;
   }

   public boolean isSpecialItem() {
      return this.itemType != null && !this.itemType.isEmpty();
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public void setExpireDate(Date expireDate) {
      this.expireDate = expireDate;
   }

   public void setDeletionDate(Date deletionDate) {
      this.deletionDate = deletionDate;
   }

   public void setExpiredNotified(boolean expiredNotified) {
      this.expiredNotified = expiredNotified;
   }

   public void setDeliveryTime(int hours) {
      this.deliveryTime = hours;
      this.expireDate = new Date(this.creationDate.getTime() + (long)(hours * 60 * 60) * 1000L);
   }

   public void setRequestedAmount(int amount) {
      this.requestedAmount = Math.max(0, amount);
   }

   public void setDeliveredAmount(int amount) {
      this.deliveredAmount = Math.max(0, Math.min(amount, this.requestedAmount));
   }

   public void setCollectedAmount(int amount) {
      this.collectedAmount = Math.max(0, Math.min(amount, this.deliveredAmount));
   }

   public void setPaidAmount(double amount) {
      this.paidAmount = Math.max((double)0.0F, Math.min(amount, this.getTotalPrice()));
   }

   public void setItemType(String itemType) {
      this.itemType = itemType;
   }

   public void setSubType(String subType) {
      this.subType = subType;
   }

   public void addDeliveredAmount(int amount) {
      int newDelivered = Math.min(this.requestedAmount, this.deliveredAmount + Math.max(0, amount));
      this.deliveredAmount = newDelivered;
      if (this.deliveredAmount >= this.requestedAmount) {
         this.wasFullyDelivered = true;
      }

   }

   public void addCollectedAmount(int amount) {
      this.collectedAmount = Math.min(this.deliveredAmount, this.collectedAmount + Math.max(0, amount));
   }

   public void addPaidAmount(double amount) {
      this.paidAmount = Math.min(this.getTotalPrice(), this.paidAmount + Math.max((double)0.0F, amount));
   }

   public boolean shouldBeDeleted() {
      return (new Date()).after(this.deletionDate) || this.isExpired() && this.getAvailableToCollect() == 0 && this.getRemainingAmount() == 0;
   }

   public boolean shouldNotifyExpired() {
      return this.isExpired() && !this.expiredNotified;
   }

   public boolean shouldBeInOrderView() {
      return this.isActive() && !this.isExpired() && this.getRemainingAmount() > 0;
   }

   public boolean shouldBeInYourOrders() {
      return this.isActive() && !this.isExpired() && (this.getRemainingAmount() > 0 || this.getAvailableToCollect() > 0);
   }

   public boolean isCompletelyFinished() {
      return !this.isActive() || this.wasFullyDelivered && this.getAvailableToCollect() == 0 && this.getRemainingAmount() == 0;
   }

   public boolean canCollectItems() {
      return this.isActive() && !this.isExpired() && this.getAvailableToCollect() > 0;
   }

   public String getFormattedPricePerItem(OrderManager orderManager) {
      return orderManager.formatCurrency(this.pricePerItem);
   }

   public String getFormattedTotalPrice(OrderManager orderManager) {
      return orderManager.formatCurrency(this.getTotalPrice());
   }

   public String getFormattedPaidAmount(OrderManager orderManager) {
      return orderManager.formatCurrency(this.paidAmount);
   }

   public String getFormattedRemainingPayment(OrderManager orderManager) {
      return orderManager.formatCurrency(this.getRemainingPayment());
   }

   public String getFormattedExpireTime() {
      if (this.isExpired()) {
         return "Expired";
      } else {
         long remainingMillis = this.expireDate.getTime() - System.currentTimeMillis();
         long days = remainingMillis / 86400000L;
         long hours = remainingMillis % 86400000L / 3600000L;
         long minutes = remainingMillis % 3600000L / 60000L;
         StringBuilder result = new StringBuilder();
         if (days > 0L) {
            result.append(days).append("d ");
         }

         if (hours > 0L || days > 0L) {
            result.append(hours).append("h ");
         }

         result.append(minutes).append("m");
         return result.toString().trim();
      }
   }

   public String getFormattedDeletionTime() {
      if (this.shouldBeDeleted()) {
         return "deleted";
      } else {
         long remainingMillis = this.deletionDate.getTime() - System.currentTimeMillis();
         long days = remainingMillis / 86400000L;
         long hours = remainingMillis % 86400000L / 3600000L;
         long minutes = remainingMillis % 3600000L / 60000L;
         StringBuilder result = new StringBuilder();
         if (days > 0L) {
            result.append(days).append("d ");
         }

         if (hours > 0L || days > 0L) {
            result.append(hours).append("h ");
         }

         result.append(minutes).append("m");
         return result.toString().trim();
      }
   }

   public ItemStack toItemStack(OrderManager orderManager) {
      return this.toItemStack(orderManager, (Map)null);
   }

   public ItemStack toItemStack(OrderManager orderManager, Map<?, ?> sourceItemConfig) {
      ItemStack item = this.createBaseItem();
      ItemMeta meta = item.getItemMeta();
      if (meta == null) {
         return item;
      } else {
         if (sourceItemConfig != null) {
            Map<String, String> placeholders = this.getPlaceholders(orderManager);
            if (sourceItemConfig.containsKey("name") || sourceItemConfig.containsKey("displayname")) {
               String displayName = sourceItemConfig.containsKey("name") ? sourceItemConfig.get("name").toString() : sourceItemConfig.get("displayname").toString();

               for(Map.Entry<String, String> entry : placeholders.entrySet()) {
                  displayName = displayName.replace((CharSequence)entry.getKey(), (CharSequence)entry.getValue());
               }

               meta.setDisplayName(ColorUtil.color(displayName));
            }

            if (sourceItemConfig.containsKey("lore") && sourceItemConfig.get("lore") instanceof List) {
               List<?> loreList = (List)sourceItemConfig.get("lore");
               List<String> lore = new ArrayList();
               Iterator var20 = loreList.iterator();

               label84:
               while(true) {
                  String loreLine;
                  boolean isEnchantmentLine;
                  boolean isPotionLine;
                  boolean hasEnchantment;
                  boolean hasPotion;
                  do {
                     if (!var20.hasNext()) {
                        meta.setLore(lore);
                        break label84;
                     }

                     Object line = var20.next();
                     loreLine = line.toString();
                     String rawLoreLine = loreLine.replace("&7", "").replace("&f", "").replace("&#00fc88", "").replace("&#80802a", "").replace("&#008f4d", "");
                     isEnchantmentLine = rawLoreLine.contains("%enchantment-display%");
                     isPotionLine = rawLoreLine.contains("%potion-display%");
                     String enchantmentValue = (String)placeholders.get("%enchantment-display%");
                     String potionValue = (String)placeholders.get("%potion-display%");
                     if (!isEnchantmentLine && !isPotionLine) {
                        break;
                     }

                     hasEnchantment = isEnchantmentLine && enchantmentValue != null && !enchantmentValue.isEmpty();
                     hasPotion = isPotionLine && potionValue != null && !potionValue.isEmpty();
                  } while(isEnchantmentLine && !hasEnchantment || isPotionLine && !hasPotion);

                  for(Map.Entry<String, String> entry : placeholders.entrySet()) {
                     loreLine = loreLine.replace((CharSequence)entry.getKey(), (CharSequence)entry.getValue());
                  }

                  lore.add(ColorUtil.color(loreLine));
               }
            }

            item.setItemMeta(meta);
         }

         return item;
      }
   }

   private ItemStack createBaseItem() {
      if (this.isSpecialItem() && this.itemType != null && this.subType != null) {
         String type = this.itemType.toUpperCase();
         if (type.equals("POTION")) {
            return this.createPotionItem(this.subType, 1);
         }

         if (type.equals("TIPPED_ARROW")) {
            return this.createTippedArrowItem(this.subType, 1);
         }

         if (type.equals("ENCHANTED_BOOK")) {
            return this.createEnchantedBookItem(this.subType, 1);
         }
      }

      return new ItemStack(this.material, 1);
   }

   private ItemStack createPotionItem(String subType, int amount) {
      ItemStack potion = new ItemStack(Material.POTION, amount);
      PotionMeta meta = (PotionMeta)potion.getItemMeta();
      if (meta == null) {
         return potion;
      } else {
         String cleanName = subType;
         int duration = 3600;
         int amplifier = 0;
         if (subType.startsWith("LONG_")) {
            duration = 9600;
            cleanName = subType.substring(5);
         } else if (subType.startsWith("STRONG_")) {
            amplifier = 1;
            cleanName = subType.substring(7);
         }

         PotionEffectType effectType = this.getPotionEffectType(cleanName);
         if (effectType != null) {
            meta.addCustomEffect(new PotionEffect(effectType, duration, amplifier, true, true, true), true);
         }

         potion.setItemMeta(meta);
         return potion;
      }
   }

   private ItemStack createTippedArrowItem(String subType, int amount) {
      ItemStack arrow = new ItemStack(Material.TIPPED_ARROW, amount);
      PotionMeta meta = (PotionMeta)arrow.getItemMeta();
      if (meta == null) {
         return arrow;
      } else {
         String cleanName = subType;
         int duration = 3600;
         int amplifier = 0;
         if (subType.startsWith("LONG_")) {
            duration = 9600;
            cleanName = subType.substring(5);
         } else if (subType.startsWith("STRONG_")) {
            amplifier = 1;
            cleanName = subType.substring(7);
         }

         if (cleanName.startsWith("SPLASH_")) {
            cleanName = cleanName.substring(7);
         } else if (cleanName.startsWith("LINGERING_")) {
            cleanName = cleanName.substring(10);
         }

         PotionEffectType effectType = this.getPotionEffectType(cleanName);
         if (effectType != null) {
            PotionEffect effect = new PotionEffect(effectType, duration, amplifier, true, true, true);
            meta.addCustomEffect(effect, true);
            Color color = this.getPotionColor(effectType);
            if (color != null) {
               meta.setColor(color);
            }
         }

         arrow.setItemMeta(meta);
         if (arrow.getItemMeta() instanceof PotionMeta) {
            PotionMeta finalMeta = (PotionMeta)arrow.getItemMeta();
            if (finalMeta.hasColor()) {
            }

            if (finalMeta.hasCustomEffects()) {
               for(PotionEffect var11 : finalMeta.getCustomEffects()) {
                  ;
               }
            }
         }

         return arrow;
      }
   }

   private Color getPotionColor(PotionEffectType effectType) {
      if (effectType == null) {
         return null;
      } else {
         String name = effectType.getName().toLowerCase();
         if (name.contains("fire_resistance")) {
            return Color.fromRGB(255, 69, 0);
         } else if (name.contains("strength")) {
            return Color.fromRGB(255, 0, 0);
         } else if (name.contains("speed")) {
            return Color.fromRGB(0, 191, 255);
         } else if (name.contains("slowness")) {
            return Color.fromRGB(128, 128, 128);
         } else if (name.contains("poison")) {
            return Color.fromRGB(50, 205, 50);
         } else if (name.contains("regeneration")) {
            return Color.fromRGB(255, 105, 180);
         } else if (name.contains("weakness")) {
            return Color.fromRGB(169, 169, 169);
         } else if (name.contains("night_vision")) {
            return Color.fromRGB(0, 0, 128);
         } else if (name.contains("invisibility")) {
            return Color.fromRGB(211, 211, 211);
         } else if (name.contains("water_breathing")) {
            return Color.fromRGB(30, 144, 255);
         } else if (!name.contains("healing") && !name.contains("instant_health")) {
            if (!name.contains("harming") && !name.contains("instant_damage")) {
               if (name.contains("jump")) {
                  return Color.fromRGB(124, 252, 0);
               } else if (name.contains("luck")) {
                  return Color.fromRGB(81, 195, 161);
               } else {
                  return name.contains("slow_falling") ? Color.fromRGB(240, 248, 255) : Color.fromRGB(162, 0, 255);
               }
            } else {
               return Color.fromRGB(139, 0, 0);
            }
         } else {
            return Color.fromRGB(255, 20, 147);
         }
      }
   }

   private ItemStack createEnchantedBookItem(String subType, int amount) {
      ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, amount);
      EnchantmentStorageMeta meta = (EnchantmentStorageMeta)book.getItemMeta();
      if (meta == null) {
         return book;
      } else {
         String[] parts = subType.split("_");
         int level = 1;
         String enchantName;
         if (parts.length >= 2) {
            try {
               level = Integer.parseInt(parts[parts.length - 1]);
               StringBuilder nameBuilder = new StringBuilder();

               for(int i = 0; i < parts.length - 1; ++i) {
                  if (i > 0) {
                     nameBuilder.append("_");
                  }

                  nameBuilder.append(parts[i]);
               }

               enchantName = nameBuilder.toString();
            } catch (NumberFormatException var12) {
               enchantName = subType;
            }
         } else {
            enchantName = subType;
         }

         for(Enchantment enchant : Enchantment.values()) {
            if (enchant.getKey().getKey().equalsIgnoreCase(enchantName)) {
               meta.addStoredEnchant(enchant, level, true);
               break;
            }
         }

         book.setItemMeta(meta);
         return book;
      }
   }

   private PotionEffectType getPotionEffectType(String name) {
      String upperName = name.toUpperCase();
      if (upperName.equals("FIRE_RESISTANCE")) {
         return PotionEffectType.FIRE_RESISTANCE;
      } else if (upperName.equals("STRENGTH")) {
         return PotionEffectType.STRENGTH;
      } else if (!upperName.equals("SPEED") && !upperName.equals("SWIFTNESS")) {
         if (upperName.equals("SLOWNESS")) {
            return PotionEffectType.SLOWNESS;
         } else if (upperName.equals("POISON")) {
            return PotionEffectType.POISON;
         } else if (upperName.equals("REGENERATION")) {
            return PotionEffectType.REGENERATION;
         } else if (upperName.equals("WEAKNESS")) {
            return PotionEffectType.WEAKNESS;
         } else if (upperName.equals("NIGHT_VISION")) {
            return PotionEffectType.NIGHT_VISION;
         } else if (upperName.equals("INVISIBILITY")) {
            return PotionEffectType.INVISIBILITY;
         } else if (upperName.equals("WATER_BREATHING")) {
            return PotionEffectType.WATER_BREATHING;
         } else if (!upperName.equals("HEALING") && !upperName.equals("INSTANT_HEALTH")) {
            if (upperName.equals("HARMING")) {
               return PotionEffectType.INSTANT_DAMAGE;
            } else if (!upperName.equals("JUMP") && !upperName.equals("LEAPING")) {
               if (upperName.equals("LUCK")) {
                  return PotionEffectType.LUCK;
               } else {
                  return upperName.equals("SLOW_FALLING") ? PotionEffectType.SLOW_FALLING : PotionEffectType.getByName(upperName);
               }
            } else {
               return PotionEffectType.JUMP_BOOST;
            }
         } else {
            return PotionEffectType.INSTANT_HEALTH;
         }
      } else {
         return PotionEffectType.SPEED;
      }
   }

   private Map<String, String> getPlaceholders(OrderManager orderManager) {
      Map<String, String> placeholders = new HashMap();
      String ownerName = this.creator != null ? Bukkit.getOfflinePlayer(this.creator).getName() : "Unknown";
      placeholders.put("%owner%", ownerName != null ? ownerName : "Unknown");
      placeholders.put("%requested-material%", this.formatMaterialName(this.material.name()));
      placeholders.put("%price%", orderManager.formatCurrency(this.pricePerItem));
      placeholders.put("%total-paid-amount%", orderManager.formatCurrency(this.getTotalPrice()));
      placeholders.put("%requested-amount%", String.valueOf(this.requestedAmount));
      placeholders.put("%required-amount%", String.valueOf(this.requestedAmount));
      placeholders.put("%delivered-amount%", String.valueOf(this.deliveredAmount));
      placeholders.put("%collected-amount%", String.valueOf(this.collectedAmount));
      placeholders.put("%available-to-collect%", String.valueOf(this.getAvailableToCollect()));
      placeholders.put("%paid-amount%", orderManager.formatCurrency(this.pricePerItem * (double)this.deliveredAmount));
      placeholders.put("%expire-time%", this.getFormattedExpireTime());
      String enchantmentDisplay = "";
      String potionDisplay = "";
      String potionColor = "";
      String itemDetails = "";
      if (this.isSpecialItem() && this.itemType != null && this.subType != null) {
         String type = this.itemType.toUpperCase();
         if (type.equals("ENCHANTED_BOOK")) {
            String[] parts = this.subType.split("_");
            if (parts.length >= 2) {
               String enchantName = this.formatEnchantmentName(parts);
               String level = parts[parts.length - 1];
               enchantmentDisplay = enchantName + " " + level;
               itemDetails = enchantName + " " + level;
               placeholders.put("%enchantment%", parts[0]);
               placeholders.put("%enchantment-level%", level);
            } else {
               enchantmentDisplay = this.subType;
               itemDetails = this.subType;
            }

            placeholders.put("%enchantment-display%", enchantmentDisplay);
            placeholders.put("%potion-display%", "");
         } else if (!type.equals("POTION") && !type.equals("TIPPED_ARROW")) {
            placeholders.put("%variant%", this.subType);
            placeholders.put("%enchantment-display%", "");
            placeholders.put("%potion-display%", "");
         } else {
            potionDisplay = this.formatPotionName(this.subType);
            potionColor = this.getPotionColorCode(this.subType);
            itemDetails = potionDisplay;
            placeholders.put("%potion-display%", potionDisplay);
            placeholders.put("%potion-color%", potionColor);
            placeholders.put("%potion-effect%", this.getPotionEffectName(this.subType));
            placeholders.put("%enchantment-display%", "");
         }
      } else {
         placeholders.put("%enchantment-display%", "");
         placeholders.put("%potion-display%", "");
         placeholders.put("%potion-color%", "");
         placeholders.put("%potion-effect%", "");
         placeholders.put("%variant%", "");
         placeholders.put("%enchantment%", "");
         placeholders.put("%enchantment-level%", "");
      }

      placeholders.put("%item-details%", itemDetails);
      return placeholders;
   }

   private String formatEnchantmentName(String[] parts) {
      StringBuilder name = new StringBuilder();

      for(int i = 0; i < parts.length - 1; ++i) {
         if (i > 0) {
            name.append(" ");
         }

         String part = parts[i];
         name.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
      }

      return name.toString();
   }

   private String formatPotionName(String subType) {
      String[] parts = subType.split("_");
      StringBuilder result = new StringBuilder();

      for(int i = 0; i < parts.length; ++i) {
         String part = parts[i];
         if (part.equals("LONG")) {
            result.append("Verlängert ");
         } else if (part.equals("STRONG")) {
            result.append("Stark ");
         } else if (part.equals("SPLASH")) {
            result.append("Wurf-");
         } else if (part.equals("LINGERING")) {
            result.append("Verweil-");
         } else {
            result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
            if (i < parts.length - 1) {
               result.append(" ");
            }
         }
      }

      return result.toString().trim();
   }

   private String getPotionColorCode(String subType) {
      String upper = subType.toUpperCase();
      if (upper.contains("FIRE_RESISTANCE")) {
         return "&#FF4500";
      } else if (upper.contains("STRENGTH")) {
         return "&#FF0000";
      } else if (!upper.contains("SPEED") && !upper.contains("SWIFTNESS")) {
         if (upper.contains("SLOWNESS")) {
            return "&#808080";
         } else if (upper.contains("POISON")) {
            return "&#32CD32";
         } else if (upper.contains("REGENERATION")) {
            return "&#FF69B4";
         } else if (upper.contains("WEAKNESS")) {
            return "&#A9A9A9";
         } else if (upper.contains("NIGHT_VISION")) {
            return "&#000080";
         } else if (upper.contains("INVISIBILITY")) {
            return "&#D3D3D3";
         } else if (upper.contains("WATER_BREATHING")) {
            return "&#1E90FF";
         } else if (!upper.contains("HEALING") && !upper.contains("INSTANT_HEALTH")) {
            if (upper.contains("HARMING")) {
               return "&#8B0000";
            } else if (!upper.contains("JUMP") && !upper.contains("LEAPING")) {
               if (upper.contains("LUCK")) {
                  return "&#51C3A1";
               } else {
                  return upper.contains("SLOW_FALLING") ? "&#F0F8FF" : "&#00fc88";
               }
            } else {
               return "&#7CFC00";
            }
         } else {
            return "&#FF1493";
         }
      } else {
         return "&#00BFFF";
      }
   }

   private String getPotionEffectName(String subType) {
      String[] parts = subType.split("_");
      List<String> effectParts = new ArrayList();

      for(String part : parts) {
         if (!part.equals("LONG") && !part.equals("STRONG") && !part.equals("SPLASH") && !part.equals("LINGERING")) {
            String var10001 = part.substring(0, 1).toUpperCase();
            effectParts.add(var10001 + part.substring(1).toLowerCase());
         }
      }

      return String.join(" ", effectParts);
   }

   private String formatMaterialName(String materialName) {
      String[] words = materialName.toLowerCase().split("_");
      StringBuilder formatted = new StringBuilder();

      for(String word : words) {
         if (!word.isEmpty()) {
            formatted.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
         }
      }

      return formatted.toString().trim();
   }

   public boolean shouldOrderBeVisible() {
      return this.isActive() && !this.isExpired() && (this.getRemainingAmount() > 0 || this.getAvailableToCollect() > 0);
   }

   public boolean isCompleted() {
      return !this.active || this.wasFullyDelivered && this.getAvailableToCollect() == 0;
   }
}
