package com.butteredapples;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum BookEnum implements StringRepresentable {
    EMPTY("empty"),
    NORMAL("normal"),
    ENCHANTED("enchanted");

    private final String name;

    BookEnum(String string){
        this.name = string;
    }

    public String toString() {
        return this.name;
    }


    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
