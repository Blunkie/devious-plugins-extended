package net.unethicalite.plugins.birdhouses.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.plugins.PluginStoppedException;
import net.unethicalite.plugins.birdhouses.BirdHousesConfig;
import net.unethicalite.plugins.birdhouses.BirdHousesPlugin;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GatherTools extends BirdHouseTask
{
    private static final WorldPoint FOSSIL_ISLAND_CHEST_POINT = new WorldPoint(3742, 3805, 0);

    @Inject
    private BirdHousesConfig config;

	public GatherTools(BirdHousesPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return Inventory.isFull() || !getRequiredItems().isEmpty();
	}

	@Override
	public int execute()
	{
        if (Movement.isWalking())
        {
            return -1;
        }

        if (Bank.isOpen())
        {
            Item unneededItem = Inventory.getFirst(item -> !getTools().contains(item.getId())
                    && getInventorySetup().getOrDefault(item.getId(), 0) < Inventory.getCount(true, item.getId()));
            if (unneededItem != null)
            {
                Bank.depositAll(unneededItem.getId());
                return -2;
            }

            for (Map.Entry<Integer, Integer> entry : getRequiredItems().entrySet())
            {
                int itemId = entry.getKey();
                int quantity = entry.getValue();
                if (Inventory.getCount(true, itemId) < quantity)
                {
                    Bank.withdraw(itemId, quantity, Bank.WithdrawMode.ITEM);
                    return -3;
                }
            }

            return -1;
        }

        Player local = Players.getLocal();
        if (local.distanceTo(FOSSIL_ISLAND_CHEST_POINT) > 10)
        {
            Movement.walkTo(FOSSIL_ISLAND_CHEST_POINT.dx(-1));
            return -3;
        }

        TileObject chest = TileObjects.getFirstAt(FOSSIL_ISLAND_CHEST_POINT, obj -> obj.hasAction("Collect"));
        if (chest == null)
        {
            log.error("Bank chest not found, is it unlocked?");
            throw new PluginStoppedException();
        }

        if (!Reachable.isInteractable(chest))
        {
            Movement.walkTo(FOSSIL_ISLAND_CHEST_POINT.dx(-1));
            return -3;
        }

        chest.interact("Use");
		return -1;
	}

    private Map<Integer, Integer> getInventorySetup()
    {
        return Map.of(
                config.type().getItemId(), 4,
                config.type().getLogItemId(), 4,
                config.seedType().getItemId(), config.seedType().getQuantity() * 4
        );
    }

    private Map<Integer, Integer> getRequiredItems()
    {
        Map<Integer, Integer> out = new HashMap<>();

        int logs = getRequiredLogs();
        if (logs > 0)
        {
            out.put(config.type().getLogItemId(), logs);
        }

        int seeds = getRequiredSeeds();
        if (seeds > 0)
        {
            out.put(config.seedType().getItemId(), seeds);
        }

        for (Integer toolId : getTools()) {
            if (!Inventory.contains(toolId))
            {
                out.put(toolId, 1);
            }
        }

        return out;
    }

    private int getRequiredLogs()
    {
        return getAvailableBirdHouses().size()
                - Inventory.getCount(config.type().getLogItemId())
                - Inventory.getCount(config.type().getItemId());
    }

    private int getRequiredSeeds()
    {
        return (getAvailableBirdHouses().size() * config.seedType().getQuantity())
                    - Inventory.getCount(true, config.seedType().getItemId());
    }

    @Override
    public boolean inject() {
        return true;
    }
}
