package me.kubaw208.configify.annotations;

import me.kubaw208.configify.configs.AbstractConfigLoader;
import me.kubaw208.configify.configs.SingletonConfig;

import java.lang.annotation.*;

/**
 * Annotation used to mark configuration classes for manual registration.
 * <p>
 * Classes marked with this annotation are not loaded automatically.
 * It's only assigned as registered. If you want load classes marked with this annotation,
 * use the {@link AbstractConfigLoader#loadAutoRegisteredConfigs()} method.
 * <p>
 * <b>Note:</b> Only classes extending {@link SingletonConfig} can be
 * registered using this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AutoRegisterConfig {

    /**
     * The name of the configuration file (e.g., "config.yml").
     *
     * @return The file name.
     */
    String fileName();

    /**
     * The path to the folder where the file should be saved, relative to the plugin's data folder.
     * If left empty, the file will be saved directly in the plugin's main folder.
     *
     * @return The file path.
     */
    String path() default "";

}