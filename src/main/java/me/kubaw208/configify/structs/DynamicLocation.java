package me.kubaw208.configify.structs;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Class representing a location without an assigned world.
 * Allows storing coordinates and rotation that can be applied to any world later.
 */
@Getter
public class DynamicLocation {

    private final double x, y, z;
    private final float yaw, pitch;

    /**
     * Creates a location at point (0,0,0).
     */
    public DynamicLocation() {
        this(0, 0, 0);
    }

    /**
     * Creates a location with the given coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public DynamicLocation(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    /**
     * Creates a location with the given coordinates and rotation.
     *
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @param yaw   horizontal rotation
     * @param pitch vertical rotation
     */
    public DynamicLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Transforms into a {@link Location} object in the given world.
     *
     * @param worldName world name
     * @return {@link Location} object
     */
    public Location inWorld(String worldName) {
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    /**
     * Transforms into a {@link Location} object in the given world.
     *
     * @param world world object
     * @return {@link Location} object
     */
    public Location inWorld(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

}