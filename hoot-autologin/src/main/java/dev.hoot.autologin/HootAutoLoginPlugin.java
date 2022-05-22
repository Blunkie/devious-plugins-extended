package dev.hoot.autologin;

import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.unethicalite.api.events.LoginStateChanged;
import dev.unethicalite.api.game.Game;
import dev.unethicalite.api.input.Keyboard;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.jboss.aerogear.security.otp.Totp;
import org.pf4j.Extension;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@PluginDescriptor(name = "Hoot Auto Login", enabledByDefault = false)
@Extension
public class HootAutoLoginPlugin extends Plugin
{
	@Inject
	private HootAutoLoginConfig config;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private Client client;

	@Inject
	private WorldService worldService;

	@Provides
	public HootAutoLoginConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HootAutoLoginConfig.class);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGIN_SCREEN && Game.getClient().getLoginIndex() == 0)
		{
			executor.schedule(() -> client.setLoginIndex(2), 2000, TimeUnit.MILLISECONDS);
			executor.schedule(this::login, 2000, TimeUnit.MILLISECONDS);
		}
	}

	@Subscribe
	private void onLoginStateChanged(LoginStateChanged e)
	{
		switch (e.getIndex())
		{
			case 2:
				login();
				break;

			case 4:
				enterAuth();
				break;
		}
	}

	private void login()
	{
		client.setUsername(config.username());
		client.setPassword(config.password());
		client.changeWorld(createWorld(config.world()));
		Keyboard.sendEnter();
		Keyboard.sendEnter();
	}

	private void enterAuth()
	{
		client.setOtp(new Totp(config.auth()).now());
		Keyboard.sendEnter();
	}

	private net.runelite.api.World createWorld(int worldId)
	{
		assert client.isClientThread();

		WorldResult worldResult = worldService.getWorlds();
		// Don't try to hop if the world doesn't exist
		World world = worldResult.findWorld(worldId);
		if (world == null)
		{
			return null;
		}

		final net.runelite.api.World rsWorld = client.createWorld();
		rsWorld.setActivity(world.getActivity());
		rsWorld.setAddress(world.getAddress());
		rsWorld.setId(world.getId());
		rsWorld.setPlayerCount(world.getPlayers());
		rsWorld.setLocation(world.getLocation());
		rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

		return rsWorld;
	}
}
