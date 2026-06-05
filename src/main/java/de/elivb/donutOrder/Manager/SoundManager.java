package de.elivb.donutOrder.Manager;

import de.elivb.donutOrder.Order;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SoundManager {
   private final Order plugin;
   private final Map<String, SoundConfig> sounds;

   public SoundManager(Order plugin) {
      this.plugin = plugin;
      this.sounds = new HashMap();
      this.loadSounds();
   }

   private void loadSounds() {
      this.sounds.clear();
      ConfigurationSection soundsSection = this.plugin.getConfig().getConfigurationSection("sounds");
      if (soundsSection != null) {
         for(String key : soundsSection.getKeys(false)) {
            ConfigurationSection soundSection = soundsSection.getConfigurationSection(key);
            if (soundSection != null) {
               boolean enabled = soundSection.getBoolean("enabled", true);
               String soundName = soundSection.getString("sound", "UI_BUTTON_CLICK");
               float volume = (float)soundSection.getDouble("volume", (double)1.0F);
               float pitch = (float)soundSection.getDouble("pitch", (double)1.0F);
               SoundConfig config = new SoundConfig(enabled, soundName, volume, pitch);
               this.sounds.put(key.toLowerCase(), config);
            }
         }
      }

   }

   public void playSound(Player player, String soundKey) {
      if (player != null && soundKey != null) {
         SoundConfig config = (SoundConfig)this.sounds.get(soundKey.toLowerCase());
         if (config != null && config.isEnabled()) {
            Sound sound = this.getSoundFromRegistry(config.getSoundName());
            if (sound != null) {
               player.playSound(player.getLocation(), sound, config.getVolume(), config.getPitch());
            }
         }
      }

   }

   public void playSound(Location location, String soundKey) {
      if (location != null && location.getWorld() != null && soundKey != null) {
         SoundConfig config = (SoundConfig)this.sounds.get(soundKey.toLowerCase());
         if (config != null && config.isEnabled()) {
            Sound sound = this.getSoundFromRegistry(config.getSoundName());
            if (sound != null) {
               location.getWorld().playSound(location, sound, config.getVolume(), config.getPitch());
            }
         }
      }

   }

   public void playSound(Iterable<? extends Player> players, String soundKey) {
      for(Player player : players) {
         this.playSound(player, soundKey);
      }

   }

   public boolean isSoundEnabled(String soundKey) {
      SoundConfig config = (SoundConfig)this.sounds.get(soundKey.toLowerCase());
      return config != null && config.isEnabled();
   }

   public SoundConfig getSoundConfig(String soundKey) {
      return (SoundConfig)this.sounds.get(soundKey.toLowerCase());
   }

   public void reload() {
      this.plugin.reloadConfig();
      this.loadSounds();
   }

   private Sound getSoundFromRegistry(String soundName) {
      try {
         String keyName;
         if (soundName.startsWith("minecraft:")) {
            keyName = soundName.substring(9);
         } else {
            keyName = soundName;
         }

         keyName = keyName.toLowerCase().replace('_', '.');
         NamespacedKey key = NamespacedKey.minecraft(keyName);
         Sound sound = (Sound)Registry.SOUNDS.get(key);
         if (sound != null) {
            return sound;
         } else {
            keyName = soundName.toLowerCase().replace('_', '/');
            key = NamespacedKey.minecraft(keyName);
            sound = (Sound)Registry.SOUNDS.get(key);
            if (sound != null) {
               return sound;
            } else {
               keyName = soundName.toLowerCase().replace('_', ' ');
               key = NamespacedKey.minecraft(keyName);
               sound = (Sound)Registry.SOUNDS.get(key);
               if (sound != null) {
                  return sound;
               } else {
                  key = NamespacedKey.minecraft(soundName.toLowerCase());
                  sound = (Sound)Registry.SOUNDS.get(key);
                  if (sound == null) {
                  }

                  return sound;
               }
            }
         }
      } catch (Exception var5) {
         return null;
      }
   }

   public void listAllSounds() {
      int count = 0;

      for(Sound sound : Registry.SOUNDS) {
         if (count < 50) {
            Object var4 = null;
         }

         ++count;
      }

   }

   public Set<String> getConfiguredSounds() {
      return this.sounds.keySet();
   }

   public boolean isValidSound(String soundName) {
      try {
         String keyName = soundName.toLowerCase().replace('_', '.');
         NamespacedKey key = NamespacedKey.minecraft(keyName);
         return Registry.SOUNDS.get(key) != null;
      } catch (Exception var4) {
         return false;
      }
   }

   public static class SoundConfig {
      private final boolean enabled;
      private final String soundName;
      private final float volume;
      private final float pitch;

      public SoundConfig(boolean enabled, String soundName, float volume, float pitch) {
         this.enabled = enabled;
         this.soundName = soundName;
         this.volume = volume;
         this.pitch = pitch;
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      public String getSoundName() {
         return this.soundName;
      }

      public float getVolume() {
         return this.volume;
      }

      public float getPitch() {
         return this.pitch;
      }

      public String toString() {
         return "SoundConfig{enabled=" + this.enabled + ", sound=" + this.soundName + ", volume=" + this.volume + ", pitch=" + this.pitch + "}";
      }
   }
}
