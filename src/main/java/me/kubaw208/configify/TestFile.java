package me.kubaw208.configify;

import de.exlll.configlib.Comment;
import me.kubaw208.configify.annotations.AutoRegisterConfig;

public class TestFile extends SingletonConfig {

    @Comment({"test"})
    public String test = "test";

}