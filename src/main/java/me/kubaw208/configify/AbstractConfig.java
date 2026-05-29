package me.kubaw208.configify;

import de.exlll.configlib.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Implementable by configs.
 */
@Configuration
public abstract class AbstractConfig {

    public World getWorld(String world) {
        return Bukkit.getWorld(world);
    }

}