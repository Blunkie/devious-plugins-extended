package dev.hoot.interactiontester;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("interactiontester")
public interface InteractionTesterConfig extends Config {
    @ConfigItem(
            keyName = "loop",
            name = "Loop timeout value",
            description = ""
    )
    default int loopTimeOut() {
        return 1000;
    }

    @ConfigItem(
            keyName = "entity",
            name = "Entity type",
            description = ""
    )
    default InteractEntity entity() {
        return InteractEntity.NPC;
    }

    @ConfigItem(
            keyName = "entityId",
            name = "Entity ID",
            description = ""
    )
    default int entityId() {
        return -1;
    }

    @ConfigItem(
            keyName = "entityName",
            name = "Entity name",
            description = ""
    )
    default String entityName() {
        return "Goblin";
    }

    @ConfigItem(
            keyName = "action",
            name = "Action",
            description = ""
    )
    default String action() {
        return "Attack";
    }

    @ConfigItem(
            keyName = "scanRadius",
            name = "Entity scan radius",
            description = ""
    )
    default int scanRadius() {
        return 15;
    }
}
