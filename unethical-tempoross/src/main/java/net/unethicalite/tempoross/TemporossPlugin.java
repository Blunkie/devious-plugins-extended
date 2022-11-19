package net.unethicalite.tempoross;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.plugins.TaskPlugin;
import net.unethicalite.tempoross.tasks.AwaitStart;
import net.unethicalite.tempoross.tasks.ClearFire;
import net.unethicalite.tempoross.tasks.CycleState;
import net.unethicalite.tempoross.tasks.DetermineWorkArea;
import net.unethicalite.tempoross.tasks.EscapeCloud;
import net.unethicalite.tempoross.tasks.FinishGame;
import net.unethicalite.tempoross.tasks.Forfeit;
import net.unethicalite.tempoross.tasks.GatherTools;
import net.unethicalite.tempoross.tasks.HandleStates;
import net.unethicalite.tempoross.tasks.PreGame;
import net.unethicalite.tempoross.tasks.RepairMast;
import net.unethicalite.tempoross.tasks.Tether;
import net.unethicalite.tempoross.tasks.Untether;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

import static net.unethicalite.tempoross.TemporossID.ITEM_COOKED_FISH;
import static net.unethicalite.tempoross.TemporossID.ITEM_RAW_FISH;
import static net.unethicalite.tempoross.TemporossID.NPC_DOUBLE_FISH_SPOT;
import static net.unethicalite.tempoross.TemporossID.NPC_FIRE;
import static net.unethicalite.tempoross.TemporossID.NPC_VULN_WHIRLPOOL;

@Extension
@PluginDescriptor(
		name = "Unethical Tempoross",
		enabledByDefault = false
)
@Slf4j
public class TemporossPlugin extends TaskPlugin
{
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

	private final Task[] tasks =
			{
					new PreGame(this),
					new DetermineWorkArea(this),
					new FinishGame(this),
					new ClearFire(this),
					new EscapeCloud(this),
					new GatherTools(this),
					new AwaitStart(this),
					new RepairMast(this),
					new Tether(this),
					new Untether(this),
					new Forfeit(this),
					new CycleState(this),
					new HandleStates(this)
			};

	@Getter
	private final TemporossCollisionMap collisionMap = new TemporossCollisionMap(true);
	@Getter @Setter
	private int waves = 0;
	@Getter @Setter
	private boolean temporossVulnerable = false;
	@Getter @Setter
	private TemporossWorkArea workArea = null;
	@Getter @Setter
	private NPC fireToClear = null;
	@Getter @Setter
	private WorldPoint lastDestination = null;
	@Getter @Setter
	private State scriptState = State.INITIAL_CATCH;
	@Getter @Setter
	private boolean incomingWave = false;

	@Inject
	private Client client;

	public static int energy = 100;
	public static int intensity = 0;

	@Override
	protected void startUp()
	{
		scriptState = State.INITIAL_CATCH;
	}

	@Override
	public Task[] getTasks()
	{
		return tasks;
	}

	public enum State
	{
		ATTACK_TEMPOROSS(() -> energy >= 98, null),
		SECOND_FILL(() -> getAllFish() == 0, ATTACK_TEMPOROSS),
		THIRD_COOK(() -> getRawFish() == 0 || intensity >= 92, SECOND_FILL),
		THIRD_CATCH(() -> getAllFish() >= 17, THIRD_COOK),
		EMERGENCY_FILL(() -> getAllFish() == 0, THIRD_CATCH),
		INITIAL_FILL(() -> getAllFish() == 0, THIRD_CATCH),
		SECOND_COOK(() -> getRawFish() == 0, INITIAL_FILL),
		SECOND_CATCH(() -> getAllFish() >= 17, SECOND_COOK),
		INITIAL_COOK(() -> getRawFish() == 0, SECOND_CATCH),
		INITIAL_CATCH(() -> getRawFish() >= 8 || getAllFish() >= 17, INITIAL_COOK);

		final BooleanSupplier isComplete;

		@Getter
		final State next;


		State(BooleanSupplier isComplete, State next)
		{
			this.isComplete = isComplete;
			this.next = next;
		}
		boolean isCook()
		{
			return this == INITIAL_COOK || this == SECOND_COOK || this == THIRD_COOK;
		}

		public boolean isComplete(TemporossConfig config)
		{
			return isComplete.getAsBoolean() || (isCook() && !config.cook());
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		NPC doubleSpot = NPCs.getNearest(NPC_DOUBLE_FISH_SPOT);
		if (scriptState == State.INITIAL_COOK && doubleSpot != null)
		{
			scriptState = scriptState.next;
		}

		if (intensity >= 94 && scriptState == State.THIRD_COOK)
		{
			return;
		}

		if (scriptState == null)
		{
			scriptState = State.THIRD_CATCH;
		}

		NPC temporossPool = NPCs.getNearest(NPC_VULN_WHIRLPOOL);
		if ((temporossPool != null || temporossVulnerable) && scriptState != State.ATTACK_TEMPOROSS)
		{
			scriptState = State.ATTACK_TEMPOROSS;
		}

		log.debug("Current task: {}, state: {}", getCurrentTask(), scriptState);
	}

	@Subscribe
	private void onClientTick(ClientTick e)
	{
		if (Objects.equals(lastDestination, Players.getLocal().getWorldLocation()))
		{
			lastDestination = null;
			return;
		}

		WorldPoint destination = Movement.getDestination();
		if (destination != null && (lastDestination == null || !lastDestination.equals(destination)))
		{
			lastDestination = destination;
		}

		Player player = Players.getLocal();
		List<WorldPoint> path = player.getWorldLocation().pathTo(client, destination);
		List<NPC> firesBlockingPath = NPCs.getAll(x -> x.getId() == NPC_FIRE &&
			x.getWorldArea().toWorldPointList().stream().anyMatch(path::contains));
		NPC fire = firesBlockingPath.stream()
			.min(Comparator.comparing(x -> x.getWorldLocation().distanceTo(player.getWorldLocation())))
			.orElse(null);

		if (fire != null)
		{
			fireToClear = fire;
		}

		if (fireToClear != null && client.getCachedNPCs()[fireToClear.getIndex()] == null)
		{
			fireToClear = null;
		}
	}

	@Subscribe
	private void onNPCDespawned(NpcDespawned e)
	{
		if (e.getNpc() == fireToClear)
		{
			fireToClear = null;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();
		String message = event.getMessage();

		if (type == ChatMessageType.GAMEMESSAGE)
		{
			if (message.equals("<col=d30b0b>A colossal wave closes in...</col>"))
			{
				waves++;
				incomingWave = true;
			}

			if (message.contains("the rope keeps you securely") || message.contains("the wave slams into you"))
			{
				incomingWave = false;
			}

			if (message.contains("Tempoross is vulnerable!"))
			{
				temporossVulnerable = true;
			}
		}
	}

	private static int getRawFish()
	{
		return Inventory.getCount(ITEM_RAW_FISH);
	}

	private static int getCookedFish()
	{
		return Inventory.getCount(ITEM_COOKED_FISH);
	}

	private static int getAllFish()
	{
		return getRawFish() + getCookedFish();
	}

	@Provides
	TemporossConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TemporossConfig.class);
	}
}