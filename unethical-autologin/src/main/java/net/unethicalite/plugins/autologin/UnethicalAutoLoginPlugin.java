package net.unethicalite.plugins.autologin;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.World;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.events.LoginStateChanged;
import net.unethicalite.api.events.WorldHopped;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import org.jboss.aerogear.security.otp.Totp;
import org.pf4j.Extension;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@PluginDescriptor(name = "Unethical Auto Login", enabledByDefault = false)
@Extension
public class UnethicalAutoLoginPlugin extends Plugin
{
	@Inject
	private UnethicalAutoLoginConfig config;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private Client client;

	@Inject
	private WorldService worldService;

	@Inject
	private ConfigManager configManager;

	@Provides
	public UnethicalAutoLoginConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalAutoLoginConfig.class);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGIN_SCREEN && client.getLoginIndex() == 0)
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

	@Subscribe
	private void onWorldHopped(WorldHopped e)
	{
		if (config.lastWorld())
		{
			configManager.setConfiguration("hootautologin", "world", e.getWorldId());
		}
	}

	private void login()
	{
		client.setUsername(config.username());
		client.setPassword(config.password());
		World selectedWorld = Worlds.getFirst(config.world());
		if (selectedWorld != null)
		{
			client.changeWorld(selectedWorld);
		}

		Keyboard.sendEnter();
		Keyboard.sendEnter();
	}

	private void enterAuth()
	{
		client.setOtp(new Totp(config.auth()).now());
		Keyboard.sendEnter();
	}
}
