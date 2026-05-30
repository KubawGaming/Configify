package me.kubaw208.configify.annotations;

import java.lang.annotation.*;

/**
 * Helper annotation used to indicate an associated class or annotation.
 * Can be utilized by processors or validation mechanisms to check for the presence of specific annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckClass {

    /**
     * The annotation class to be checked.
     *
     * @return The annotation class.
     */
    Class<? extends Annotation> value();

}