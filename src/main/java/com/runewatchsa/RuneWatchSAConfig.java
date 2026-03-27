package com.runewatchsa;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("runewatchsa")
public interface RuneWatchSAConfig extends Config
{
    @ConfigItem(
        keyName = "notifyOnTrade",
        name = "Notify on Trade",
        description = "Notify you when you trade a player on the RuneWatch SA list",
        position = 1
    )
    default boolean notifyOnTrade()
    {
        return true;
    }

    @ConfigItem(
        keyName = "notifyOnRadius",
        name = "Notify on Radius",
        description = "Notify you when a player on the RuneWatch SA list is nearby",
        position = 2
    )
    default boolean notifyOnRadius()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showSidebarIcon",
        name = "Exibir Ícone Lateral",
        description = "Mostra ou esconde o ícone do RuneWatch SA no menu lateral do RuneLite",
        position = 3
    )
    default boolean showSidebarIcon()
    {
        return true;
    }
}
