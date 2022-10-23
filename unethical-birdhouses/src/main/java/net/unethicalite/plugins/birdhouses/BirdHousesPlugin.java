package net.unethicalite.plugins.birdhouses;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.unethicalite.api.account.GameAccount;
import net.unethicalite.api.commons.SpriteUtil;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.plugins.TaskScript;
import net.unethicalite.api.script.blocking_events.LoginEvent;
import net.unethicalite.client.managers.UnethicalPluginManager;
import net.unethicalite.plugins.birdhouses.model.BirdHouse;
import net.unethicalite.plugins.birdhouses.model.BirdHouseLocation;
import net.unethicalite.plugins.birdhouses.model.BirdHouseState;
import net.unethicalite.plugins.birdhouses.tasks.AwaitAndLogin;
import net.unethicalite.plugins.birdhouses.tasks.Break;
import net.unethicalite.plugins.birdhouses.tasks.GatherTools;
import net.unethicalite.plugins.birdhouses.tasks.SetupBirdHouse;
import net.unethicalite.plugins.birdhouses.tasks.WaitAtBank;
import net.unethicalite.plugins.birdhouses.tasks.WalkToBirdHouse;
import org.pf4j.Extension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(name = "Unethical Bird Houses", enabledByDefault = false)
@Slf4j
public class BirdHousesPlugin extends TaskScript
{
	private static final int FIVE_MINUTES_IN_TICKS = 500;

	private static final List<Integer> INV_SETUP_ITEMS = List.of(
			ItemID.HAMMER,
			ItemID.CHISEL
	);

	private static final List<BirdHouse> BIRD_HOUSES = List.of(
			new BirdHouse(BirdHouseLocation.MEADOW_NORTH, BirdHouseState.UNKNOWN),
			new BirdHouse(BirdHouseLocation.MEADOW_SOUTH, BirdHouseState.UNKNOWN),
			new BirdHouse(BirdHouseLocation.VALLEY_NORTH, BirdHouseState.UNKNOWN),
			new BirdHouse(BirdHouseLocation.VALLEY_SOUTH, BirdHouseState.UNKNOWN)
	);

	private final Task[] tasks =
			{
					new AwaitAndLogin(this),
					new GatherTools(this),
					new WalkToBirdHouse(this),
					new SetupBirdHouse(this),
					new WaitAtBank(this),
					new Break(this),
			};

	@Getter
	private final LoginEvent loginEvent = new LoginEvent(getBlockingEventManager());

	private Class<?> previousTask = null;

	@Inject
	private Client client;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private UnethicalPluginManager unethicalPluginManager;

	@Override
	public Task[] getTasks()
	{
		return tasks;
	}

	@Override
	public void onStart(String... args)
	{
		unethicalPluginManager.registerButton(this, NavigationButton.builder()
				.tooltip("Unethical Bird Houses")
				.icon(ImageUtil.loadImageResource(getClass(), "birdhouses.png"))
				.priority(0)
				.panel(new BirdHousesPanel(this))
				.build());

		getBlockingEventManager().remove(LoginEvent.class);

		if (client.getUsername() != null && !client.getUsername().isBlank())
		{
			Game.setGameAccount(new GameAccount(client.getUsername(), client.getPassword()));
		}

		if (Game.isLoggedIn())
		{
			for (BirdHouse birdHouse : getAvailableBirdHouses())
			{
				birdHouse.setState(BirdHouseState.fromVarpValue(Vars.getVarp(birdHouse.getVarp().getId())));
			}

			printState();
		}
	}

	public List<BirdHouse> getAvailableBirdHouses()
	{
		return BIRD_HOUSES.stream()
				.filter(b -> b.getState() != BirdHouseState.SEEDED || b.isComplete())
				.collect(Collectors.toList());
	}

	public Optional<BirdHouse> getNextBirdHouse()
	{
		return getAvailableBirdHouses().stream().findFirst();
	}

	public List<BirdHouse> getBirdHouses()
	{
		return BIRD_HOUSES;
	}

	public List<Integer> getTools()
	{
		return INV_SETUP_ITEMS;
	}

	public void printMessage(String message)
	{
		chatMessageManager.queue(QueuedMessage.builder()
				.runeLiteFormattedMessage(
						new ChatMessageBuilder()
								.img(SpriteUtil.getUnethicaliteEmojiIdx())
								.append(ChatColorType.NORMAL)
								.append("[Bird Houses] ")
								.append(ChatColorType.HIGHLIGHT)
								.append(message)
								.build()
				)
				.type(ChatMessageType.ITEM_EXAMINE)
				.build());
	}

	private void printState()
	{
		for (BirdHouse birdHouse : BIRD_HOUSES)
		{
			printMessage(birdHouse.toString());
		}
	}

	@Provides
	BirdHousesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BirdHousesConfig.class);
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (!Objects.equals(previousTask, getCurrentTask()))
		{
			previousTask = getCurrentTask();
			printMessage("Task changed: " + (previousTask == null ? "Idle" : previousTask.getSimpleName()));
		}

		int ticks = client.getTickCount();
		if (ticks % FIVE_MINUTES_IN_TICKS == 0)
		{
			printState();
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged e)
	{
		int varpId = e.getVarpId();
		for (BirdHouse birdHouse : BIRD_HOUSES)
		{
			if (birdHouse.getVarp().getId() == varpId)
			{
				birdHouse.setState(BirdHouseState.fromVarpValue(e.getValue()));
			}
		}
	}
}
