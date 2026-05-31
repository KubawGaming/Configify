package me.kubaw208.configify.configs;

import me.kubaw208.configify.annotations.AutoRegisterConfig;

/**
 * Marker class for Singleton-type configurations.
 * Extending this class suggests that the plugin uses only one instance of this configuration.
 * <p>
 * Classes extending this class can be automatically registered using
 * the {@link AutoRegisterConfig} annotation.
 */
public abstract class SingletonConfig extends AbstractConfig {}