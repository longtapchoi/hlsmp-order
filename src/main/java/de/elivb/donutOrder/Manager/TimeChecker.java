package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import org.bukkit.Bukkit;

public class TimeChecker {
   private final Order plugin;
   private final OrderManager orderManager;
   private int taskId = -1;
   private boolean running = false;
   private static final long CHECK_INTERVAL = 1200L;

   public TimeChecker(Order plugin, OrderManager orderManager) {
      this.plugin = plugin;
      this.orderManager = orderManager;
   }

   public void start() {
      if (!this.running) {
         this.running = true;
         if (this.plugin.isFolia()) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, (task) -> this.checkExpiredOrders(), 1200L, 1200L);
            this.taskId = -2;
         } else {
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::checkExpiredOrders, 1200L, 1200L);
         }
      }

   }

   public void stop() {
      if (this.running) {
         this.running = false;
         if (!this.plugin.isFolia() && this.taskId != -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
         }

         this.taskId = -1;
      }

   }

   private void checkExpiredOrders() {
      this.orderManager.checkExpiredOrders();
      if (this.plugin.getConfig().getBoolean("debug", false)) {
      }

   }

   public boolean isRunning() {
      return this.running;
   }
}
