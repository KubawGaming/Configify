package me.kubaw208.configify.configs;

import de.exlll.configlib.Configuration;
import me.kubaw208.configify.structs.DynamicLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.Duration;
import java.util.Objects;

/**
 * Abstract base class that all configuration classes should extend.
 * Utilizes the {@link Configuration} annotation from the ConfigLib library
 * so you don't need to use {@link Configuration} annotation after extending this class.
 */
@Configuration
public abstract class AbstractConfig {

    /**
     * Returns the world with the given name or the first available world if the given one does not exist.
     *
     * @param world world name
     * @return {@link World} object
     */
    protected World getWorldOrDefault(String world) {
        return Objects.requireNonNullElse(Bukkit.getWorld(world), Bukkit.getWorlds().get(0));
    }

    /**
     * Creates a {@link Location} object with default coordinates (0,0,0).
     *
     * @param world world name
     * @return {@link Location} object
     */
    protected Location getLocation(String world) {
        return getLocation(world, 0, 0, 0, 0, 0);
    }

    /**
     * Creates a {@link Location} object with the given coordinates.
     *
     * @param world world name
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @return {@link Location} object
     */
    protected Location getLocation(String world, double x, double y, double z) {
        return getLocation(world, x, y, z, 0, 0);
    }

    /**
     * Creates a {@link Location} object with the given coordinates and rotation.
     *
     * @param world world name
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @param yaw   horizontal rotation
     * @param pitch vertical rotation
     * @return {@link Location} object
     */
    protected Location getLocation(String world, double x, double y, double z, float yaw, float pitch) {
        return new Location(getWorldOrDefault(world), x, y, z, yaw, pitch);
    }

    /**
     * Creates a {@link Title} object from the given components.
     *
     * @param title    title
     * @param subtitle subtitle
     * @param fadeIn   fade in time (ms)
     * @param stay     stay time (ms)
     * @param fadeOut  fade out time (ms)
     * @return {@link Title} object
     */
    protected Title title(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        return Title.title(title, subtitle, Title.Times.of(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut)));
    }

    /**
     * Creates a {@link Title} object from the given text.
     *
     * @param title    title
     * @param subtitle subtitle
     * @param fadeIn   fade in time (ms)
     * @param stay     stay time (ms)
     * @param fadeOut  fade out time (ms)
     * @return {@link Title} object
     */
    protected Title title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return title(Component.text(title), Component.text(subtitle), fadeIn, stay, fadeOut);
    }

}