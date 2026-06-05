package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataManager {
   private final Order plugin;
   private File ordersFile;
   private FileConfiguration ordersConfig;

   public DataManager(Order plugin) {
      this.plugin = plugin;
      this.initialize();
   }

   private void initialize() {
      this.ordersFile = new File(this.plugin.getDataFolder(), "orders.yml");
      if (!this.ordersFile.exists()) {
         try {
            this.plugin.getDataFolder().mkdirs();
            this.ordersFile.createNewFile();
         } catch (IOException var2) {
         }
      }

      this.ordersConfig = YamlConfiguration.loadConfiguration(this.ordersFile);
   }

   private void saveConfig() {
      try {
         this.ordersConfig.save(this.ordersFile);
      } catch (IOException var2) {
      }

   }

   public void saveOrder(OrderItem order) {
      if (order.getOrderUuid() == null) {
         order.setOrderUuid(UUID.randomUUID());
      }

      String orderUuid = order.getOrderUuid().toString();
      String path = orderUuid + ".";
      this.ordersConfig.set(path + "cancel", !order.isActive());
      this.ordersConfig.set(path + "owner-id", order.getCreator().toString());
      this.ordersConfig.set(path + "price", order.getPricePerItem());
      this.ordersConfig.set(path + "request-amount", order.getRequestedAmount());
      this.ordersConfig.set(path + "delivered-amount", order.getDeliveredAmount());
      this.ordersConfig.set(path + "amount-to-be-collected", order.getAvailableToCollect());
      this.ordersConfig.set(path + "economy-name", "Vault");
      this.ordersConfig.set(path + "expire-time", order.getExpireDate().getTime());
      this.ordersConfig.set(path + "deletion-time", order.getDeletionDate().getTime());
      this.ordersConfig.set(path + "item-id", order.getItemId());
      String requirementPath = path + "requirement.";
      if (order.isSpecialItem()) {
         this.ordersConfig.set(requirementPath + "type", order.getItemType());
         if (order.getItemType().equals("ENCHANTED_BOOK")) {
            this.ordersConfig.set(requirementPath + "enchantment", order.getSubType());
         } else if (order.getItemType().equals("POTION") || order.getItemType().equals("TIPPED_ARROW")) {
            this.ordersConfig.set(requirementPath + "effect", order.getSubType());
         }
      } else {
         this.ordersConfig.set(requirementPath + "type", "NORMAL_ITEM");
      }

      this.saveConfig();
   }

   public void updateOrder(OrderItem order) {
      if (order.getOrderUuid() == null) {
         this.saveOrder(order);
      } else {
         String orderUuid = order.getOrderUuid().toString();
         if (!this.ordersConfig.contains(orderUuid)) {
            this.saveOrder(order);
         } else {
            String path = orderUuid + ".";
            this.ordersConfig.set(path + "cancel", !order.isActive());
            this.ordersConfig.set(path + "owner-id", order.getCreator().toString());
            this.ordersConfig.set(path + "price", order.getPricePerItem());
            this.ordersConfig.set(path + "request-amount", order.getRequestedAmount());
            this.ordersConfig.set(path + "delivered-amount", order.getDeliveredAmount());
            this.ordersConfig.set(path + "amount-to-be-collected", order.getAvailableToCollect());
            this.ordersConfig.set(path + "expire-time", order.getExpireDate().getTime());
            this.ordersConfig.set(path + "deletion-time", order.getDeletionDate().getTime());
            this.ordersConfig.set(path + "item-id", order.getItemId());
            this.saveConfig();
         }
      }

   }

   public void deleteOrderByUuid(UUID orderUuid) {
      if (orderUuid != null) {
         this.ordersConfig.set(orderUuid.toString(), (Object)null);
         this.saveConfig();
      }

   }

   public List<OrderItem> loadAllOrders() {
      List<OrderItem> orders = new ArrayList();

      for(String key : this.ordersConfig.getKeys(false)) {
         if (key.length() == 36) {
            try {
               UUID orderUuid = UUID.fromString(key);
               String path = key + ".";
               boolean isCancelled = this.ordersConfig.getBoolean(path + "cancel", false);
               if (!isCancelled) {
                  String ownerIdStr = this.ordersConfig.getString(path + "owner-id");
                  if (ownerIdStr != null) {
                     UUID ownerId = UUID.fromString(ownerIdStr);
                     double price = this.ordersConfig.getDouble(path + "price");
                     int requestAmount = this.ordersConfig.getInt(path + "request-amount");
                     int deliveredAmount = this.ordersConfig.getInt(path + "delivered-amount", 0);
                     int amountToBeCollected = this.ordersConfig.getInt(path + "amount-to-be-collected", 0);
                     long expireTime = this.ordersConfig.getLong(path + "expire-time", System.currentTimeMillis() + 86400000L);
                     long deletionTime = this.ordersConfig.getLong(path + "deletion-time", expireTime + 86400000L);
                     String requirementPath = path + "requirement.";
                     String requirementType = this.ordersConfig.getString(requirementPath + "type");
                     if (requirementType != null) {
                        OrderItem orderItem;
                        if (!requirementType.equals("ENCHANTED_BOOK") && !requirementType.equals("POTION") && !requirementType.equals("TIPPED_ARROW")) {
                           if (requirementType.equals("NORMAL_ITEM")) {
                              String itemId = this.ordersConfig.getString(path + "item-id");
                              if (itemId == null) {
                                 continue;
                              }

                              try {
                                 Material material = Material.valueOf(itemId.toUpperCase());
                                 orderItem = new OrderItem(this.formatMaterialName(itemId), Arrays.asList("Normal Item"), price, material, itemId, new ArrayList(), ownerId, requestAmount);
                              } catch (IllegalArgumentException var24) {
                                 continue;
                              }
                           } else {
                              try {
                                 Material material = Material.valueOf(requirementType);
                                 orderItem = new OrderItem(this.formatMaterialName(requirementType), Arrays.asList("Normal Item"), price, material, requirementType, new ArrayList(), ownerId, requestAmount);
                              } catch (IllegalArgumentException var23) {
                                 continue;
                              }
                           }
                        } else {
                           String subType = null;
                           if (requirementType.equals("ENCHANTED_BOOK")) {
                              subType = this.ordersConfig.getString(requirementPath + "enchantment");
                           } else {
                              subType = this.ordersConfig.getString(requirementPath + "effect");
                           }

                           Material material = Material.STONE;
                           if (requirementType.equals("ENCHANTED_BOOK")) {
                              material = Material.ENCHANTED_BOOK;
                           } else if (requirementType.equals("POTION")) {
                              material = Material.POTION;
                           } else if (requirementType.equals("TIPPED_ARROW")) {
                              material = Material.TIPPED_ARROW;
                           }

                           orderItem = new OrderItem(requirementType + " " + subType, Arrays.asList("Special Item"), price, material, requirementType + "_" + subType, new ArrayList(), ownerId, requirementType, subType, requestAmount);
                        }

                        orderItem.setOrderUuid(orderUuid);
                        orderItem.setDeliveredAmount(deliveredAmount);
                        int collectedAmount = deliveredAmount - amountToBeCollected;
                        orderItem.setCollectedAmount(collectedAmount);
                        orderItem.setActive(!isCancelled);
                        orderItem.setExpireDate(new Date(expireTime));
                        orderItem.setDeletionDate(new Date(deletionTime));
                        orderItem.setDeliveryTime(this.plugin.getConfig().getInt("expire-time-seconds", 604800) / 3600);
                        orderItem.setPaidAmount((double)requestAmount * price);
                        orders.add(orderItem);
                     }
                  }
               }
            } catch (Exception var25) {
            }
         }
      }

      return orders;
   }

   public OrderItem getOrderByUuid(UUID orderUuid) {
      if (orderUuid == null) {
         return null;
      } else {
         String key = orderUuid.toString();
         if (!this.ordersConfig.contains(key)) {
            return null;
         } else {
            try {
               String path = key + ".";
               boolean isCancelled = this.ordersConfig.getBoolean(path + "cancel", false);
               if (isCancelled) {
                  return null;
               } else {
                  String ownerIdStr = this.ordersConfig.getString(path + "owner-id");
                  if (ownerIdStr == null) {
                     return null;
                  } else {
                     UUID ownerId = UUID.fromString(ownerIdStr);
                     double price = this.ordersConfig.getDouble(path + "price");
                     int requestAmount = this.ordersConfig.getInt(path + "request-amount");
                     int deliveredAmount = this.ordersConfig.getInt(path + "delivered-amount", 0);
                     int amountToBeCollected = this.ordersConfig.getInt(path + "amount-to-be-collected", 0);
                     long expireTime = this.ordersConfig.getLong(path + "expire-time", System.currentTimeMillis() + 86400000L);
                     long deletionTime = this.ordersConfig.getLong(path + "deletion-time", expireTime + 86400000L);
                     String requirementPath = path + "requirement.";
                     String requirementType = this.ordersConfig.getString(requirementPath + "type");
                     if (requirementType == null) {
                        return null;
                     } else {
                        OrderItem orderItem;
                        if (!requirementType.equals("ENCHANTED_BOOK") && !requirementType.equals("POTION") && !requirementType.equals("TIPPED_ARROW")) {
                           if (requirementType.equals("NORMAL_ITEM")) {
                              String itemId = this.ordersConfig.getString(path + "item-id");
                              if (itemId == null) {
                                 return null;
                              }

                              try {
                                 Material material = Material.valueOf(itemId.toUpperCase());
                                 orderItem = new OrderItem(this.formatMaterialName(itemId), Arrays.asList("Normal Item"), price, material, itemId, new ArrayList(), ownerId, requestAmount);
                              } catch (IllegalArgumentException var22) {
                                 return null;
                              }
                           } else {
                              try {
                                 Material material = Material.valueOf(requirementType);
                                 orderItem = new OrderItem(this.formatMaterialName(requirementType), Arrays.asList("Normal Item"), price, material, requirementType, new ArrayList(), ownerId, requestAmount);
                              } catch (IllegalArgumentException var21) {
                                 return null;
                              }
                           }
                        } else {
                           String subType = null;
                           if (requirementType.equals("ENCHANTED_BOOK")) {
                              subType = this.ordersConfig.getString(requirementPath + "enchantment");
                           } else {
                              subType = this.ordersConfig.getString(requirementPath + "effect");
                           }

                           Material material = Material.STONE;
                           if (requirementType.equals("ENCHANTED_BOOK")) {
                              material = Material.ENCHANTED_BOOK;
                           } else if (requirementType.equals("POTION")) {
                              material = Material.POTION;
                           } else if (requirementType.equals("TIPPED_ARROW")) {
                              material = Material.TIPPED_ARROW;
                           }

                           orderItem = new OrderItem(requirementType + " " + subType, Arrays.asList("Special Item"), price, material, requirementType + "_" + subType, new ArrayList(), ownerId, requirementType, subType, requestAmount);
                        }

                        orderItem.setOrderUuid(orderUuid);
                        orderItem.setDeliveredAmount(deliveredAmount);
                        orderItem.setCollectedAmount(deliveredAmount - amountToBeCollected);
                        orderItem.setActive(true);
                        orderItem.setExpireDate(new Date(expireTime));
                        orderItem.setDeletionDate(new Date(deletionTime));
                        orderItem.setDeliveryTime(this.plugin.getConfig().getInt("expire-time-seconds", 604800) / 3600);
                        orderItem.setPaidAmount((double)requestAmount * price);
                        return orderItem;
                     }
                  }
               }
            } catch (Exception var23) {
               return null;
            }
         }
      }
   }

   public List<OrderItem> getOrdersByOwner(UUID ownerId) {
      List<OrderItem> result = new ArrayList();

      for(OrderItem order : this.loadAllOrders()) {
         if (order.getCreator() != null && order.getCreator().equals(ownerId)) {
            result.add(order);
         }
      }

      return result;
   }

   public int getTotalOrderCount() {
      return this.loadAllOrders().size();
   }

   public double getTotalOrderValue() {
      double total = (double)0.0F;

      for(OrderItem order : this.loadAllOrders()) {
         if (order.isActive()) {
            total += order.getPricePerItem() * (double)order.getRequestedAmount();
         }
      }

      return total;
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

   public void close() {
      this.saveConfig();
   }
}
