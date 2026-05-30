package me.kubaw208.configify;

import de.exlll.configlib.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Abstract base class that all configuration classes should extend.
 * Utilizes the {@link Configuration} annotation from the ConfigLib library
 * so you don't need to use {@link Configuration} annotation after extending this class.
 */
@Configuration
public abstract class AbstractConfig {

    /**
     * Helper method to retrieve a Bukkit world by its name.
     *
     * @param world The name of the world.
     * @return The {@link World} object, or {@code null} if the world does not exist.
     */
    protected World getWorld(String world) {
        return Bukkit.getWorld(world);
    }

}