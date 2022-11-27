package net.unethicalite.plugins.logout;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import org.pf4j.Extension;

import javax.inject.Inject;

@Extension
@PluginDescriptor(name = "Unethical Logout", description = "Logs you out in wildy if a dangerous player is near", enabledByDefault = false)
public class LogoutPlugin extends Plugin
{
	@Inject
	private Client client;

	@Subscribe
	private void onClientTick(ClientTick e)
	{
		int wildyLevel = Game.getWildyLevel();
		if (wildyLevel < 1)
		{
			return;
		}

		Player local = Players.getLocal();
		int combatLevel = local.getCombatLevel();
		Player pker = Players.getNearest(player -> player != local && isDangerousPlayer(wildyLevel, combatLevel, player));
		if (pker != null)
		{
			client.setMouseIdleTicks(Integer.MAX_VALUE);
			client.setKeyboardIdleTicks(Integer.MAX_VALUE);
		}
	}

	private boolean isDangerousPlayer(int wildyLevel, int localCombatLevel, Player player)
	{
		int playerCombatLevel = player.getCombatLevel();
		int lowerLimit = localCombatLevel - wildyLevel;
		int upperLimit = localCombatLevel + wildyLevel;
		return playerCombatLevel >= lowerLimit && playerCombatLevel <= upperLimit;
	}
}
