package me.kubaw208.configify.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckClass {

    Class<? extends Annotation> value();

}