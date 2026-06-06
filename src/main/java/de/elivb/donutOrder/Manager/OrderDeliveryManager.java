package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class OrderDeliveryManager {
   private final Order plugin;
   private final OrderManager orderManager;
   private final Economy economy;

   public OrderDeliveryManager(Order plugin, OrderManager orderManager) {
      this.plugin = plugin;
      this.orderManager = orderManager;
      this.economy = orderManager.getEconomy();
   }

   public DeliveryResult deliverItems(Player player, OrderItem order, int amount, List<ItemStack> itemsFromGUI) {
      if (amount <= 0) {
         return new DeliveryResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("error"));
      } else if (order.isExpired()) {
         return new DeliveryResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("expired"));
      } else if (!order.isActive()) {
         return new DeliveryResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("order-not-available"));
      } else {
         int remainingAmount = order.getRemainingAmount();
         if (amount > remainingAmount) {
            return new DeliveryResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("too-many-items"));
         } else {
            order.addDeliveredAmount(amount);
            this.orderManager.updateOrderItem(order);
            double reward = (double)amount * order.getPricePerItem();
            if (this.economy != null) {
               this.economy.depositPlayer(player, reward);
               Map<String, String> rewardPlaceholders = new HashMap();
               rewardPlaceholders.put("%reward%", this.orderManager.formatCurrency(reward));
               this.plugin.getLangManager().sendMessage(player, "reward-messages", rewardPlaceholders);
               // Actionbar: +số tiền$ màu xanh lá đậm
               String actionbarText = "§2+" + this.orderManager.formatCurrency(reward) + "$";
               player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionbarText));
               // Âm thanh nhặt exp
               player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }

            this.notifyOrderCreator(order, player.getName(), amount, reward);
            if (order.isFullyDelivered() && order.getCreator() != null) {
               Player creator = Bukkit.getPlayer(order.getCreator());
               if (creator != null && creator.isOnline()) {
                  this.plugin.getLangManager().sendMessage(creator, "order-finished");
               }
            }

            return new DeliveryResult(true, "");
         }
      }
   }

   public PaymentResult makePayment(Player player, OrderItem order, double amount) {
      if (amount <= (double)0.0F) {
         return new PaymentResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("error"));
      } else if (order.isExpired()) {
         return new PaymentResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("expired"));
      } else if (!order.isActive()) {
         return new PaymentResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("order-not-available"));
      } else {
         double remainingPayment = order.getRemainingPayment();
         if (amount > remainingPayment) {
            return new PaymentResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("error"));
         } else if (this.economy == null) {
            return new PaymentResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("error"));
         } else if (!this.economy.has(player, amount)) {
            return new PaymentResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("no-money"));
         } else {
            this.economy.withdrawPlayer(player, amount);
            order.addPaidAmount(amount);
            this.orderManager.updateOrderItem(order);
            if (order.getCreator() != null) {
               Player creator = Bukkit.getPlayer(order.getCreator());
               if (creator != null && creator.isOnline()) {
                  this.economy.depositPlayer(creator, amount);
               }
            }

            return new PaymentResult(true, "");
         }
      }
   }

   public CancelResult cancelOrder(Player player, OrderItem order) {
      if (!player.getUniqueId().equals(order.getCreator()) && !player.hasPermission("order.admin")) {
         return new CancelResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("no-permission"));
      } else if (order.isExpired()) {
         return new CancelResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("expired"));
      } else if (!order.isActive()) {
         return new CancelResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("order-not-available"));
      } else if (order.getDeliveredAmount() > 0) {
         return new CancelResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("error"));
      } else {
         double paidAmount = order.getPaidAmount();
         if (paidAmount > (double)0.0F && this.economy != null && order.getCreator() != null) {
            Player creator = Bukkit.getPlayer(order.getCreator());
            if (creator != null && creator.isOnline()) {
               this.economy.depositPlayer(creator, paidAmount);
               Map<String, String> refundPlaceholders = new HashMap();
               refundPlaceholders.put("%amount%", this.orderManager.formatCurrency(paidAmount));
               this.plugin.getLangManager().sendMessage(player, "refund-success", refundPlaceholders);
            }
         }

         order.setActive(false);
         this.orderManager.updateOrderItem(order);
         return new CancelResult(true, this.plugin.getLangManager().getMessageWithoutPrefix("order-cancelled-success"));
      }
   }

   public CollectResult collectItems(Player player, OrderItem order) {
      if (!player.getUniqueId().equals(order.getCreator()) && !player.hasPermission("order.admin")) {
         return new CollectResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("no-permission"));
      } else {
         int deliveredAmount = order.getDeliveredAmount();
         if (deliveredAmount <= 0) {
            return new CollectResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("no-items-to-collect"));
         } else {
            int freeSlots = 0;

            for(ItemStack item : player.getInventory().getContents()) {
               if (item == null || item.getType() == Material.AIR) {
                  ++freeSlots;
               }
            }

            int neededSlots = (int)Math.ceil((double)deliveredAmount / (double)64.0F);
            if (freeSlots < neededSlots) {
               return new CollectResult(false, this.plugin.getLangManager().getMessageWithoutPrefix("inventory-full"));
            } else {
               Material material = order.getMaterial();

               int stackSize;
               for(int remaining = deliveredAmount; remaining > 0; remaining -= stackSize) {
                  stackSize = Math.min(remaining, 64);
                  player.getInventory().addItem(new ItemStack[]{new ItemStack(material, stackSize)});
               }

               order.setDeliveredAmount(0);
               this.orderManager.updateOrderItem(order);
               Map<String, String> collectPlaceholders = new HashMap();
               collectPlaceholders.put("%amount%", String.valueOf(deliveredAmount));
               collectPlaceholders.put("%item%", this.getItemName(order));
               this.plugin.getLangManager().sendMessage(player, "items-collected", collectPlaceholders);
               return new CollectResult(true, "");
            }
         }
      }
   }

   private void notifyOrderCreator(OrderItem order, String delivererName, int amount, double reward) {
      if (order.getCreator() != null) {
         Player creator = Bukkit.getPlayer(order.getCreator());
         if (creator != null && creator.isOnline()) {
            Map<String, String> deliveredPlaceholders = new HashMap();
            deliveredPlaceholders.put("%player%", delivererName);
            deliveredPlaceholders.put("%amount%", String.valueOf(amount));
            deliveredPlaceholders.put("%item%", this.getItemName(order));
            this.plugin.getLangManager().sendMessage(creator, "delivered-user", deliveredPlaceholders);
         }
      }

   }

   private String getItemName(OrderItem order) {
      return order.isSpecialItem() ? order.getName() : this.formatMaterialName(order.getMaterial().name());
   }

   private String getCreatorName(OrderItem order) {
      if (order.getCreator() == null) {
         return "";
      } else {
         Player creator = Bukkit.getPlayer(order.getCreator());
         return creator != null ? creator.getName() : Bukkit.getOfflinePlayer(order.getCreator()).getName();
      }
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

   public static class CancelResult {
      private final boolean success;
      private final String message;

      public CancelResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }

      public boolean isSuccess() { return this.success; }
      public String getMessage() { return this.message; }
   }

   public static class CollectResult {
      private final boolean success;
      private final String message;

      public CollectResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }

      public boolean isSuccess() { return this.success; }
      public String getMessage() { return this.message; }
   }

   public static class DeliveryResult {
      private final boolean success;
      private final String message;

      public DeliveryResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }

      public boolean isSuccess() { return this.success; }
      public String getMessage() { return this.message; }
   }

   public static class PaymentResult {
      private final boolean success;
      private final String message;

      public PaymentResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }

      public boolean isSuccess() { return this.success; }
      public String getMessage() { return this.message; }
   }
}
