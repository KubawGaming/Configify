package me.kubaw208.configify;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class ConfigLoader extends AbstractConfigLoader {

    private final Path roundsDirectory = new File(plugin.getDataFolder() + "/rounds").toPath();

    public ConfigLoader(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void registerSerializers() {}

    @Override
    public ConfigLoader loadConfigs() {

        return this;
    }

    public <T extends AbstractConfig> T getRoundConfig(String round, Class<T> clazz) {
        return getConfig(clazz, roundsDirectory.resolve(round));
    }

    public <T extends AbstractConfig> boolean updateRoundConfig(String round, Class<T> clazz) {
        return updateConfig(clazz, roundsDirectory.resolve(round));
    }

    public <T extends AbstractConfig> void saveRoundConfig(String round, Class<T> clazz) {
        saveConfig(clazz, roundsDirectory.resolve(round));
    }

    public void example() {
        var testMessage = getRoundConfig("round1", TestFile.class).test;
        boolean successfully = updateRoundConfig("round1", TestFile.class);

        if(!successfully) System.out.println("Couldn't update file round1.yml!");

        saveRoundConfig("round1", TestFile.class);

        getConfig(TestFile.class);
    }

}
