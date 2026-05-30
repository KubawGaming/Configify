package me.kubaw208.configify.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AutoRegisterConfig {

    String fileName();

    String path() default "";

}