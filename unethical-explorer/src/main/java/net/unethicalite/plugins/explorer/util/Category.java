package net.unethicalite.plugins.explorer.util;

import lombok.Getter;

@Getter
public enum Category
{
    QUEST("Quest"),
    CLUE("Clue"),
    BANKS("Banks"),
    CUSTOM("Custom");

    private final String name;

    Category(String name)
    {
        this.name = name;
    }
}
