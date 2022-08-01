package net.unethicalite.plugins.zulrah;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import net.unethicalite.plugins.zulrah.data.Constants;
import net.unethicalite.plugins.zulrah.data.Gear;
import net.unethicalite.plugins.zulrah.data.Rotation;
import net.unethicalite.plugins.zulrah.data.RotationNode;
import net.unethicalite.plugins.zulrah.data.RotationTree;
import net.unethicalite.plugins.zulrah.data.ZulrahType;
import net.unethicalite.plugins.zulrah.framework.ZulrahTask;
import net.unethicalite.plugins.zulrah.tasks.AttackZulrah;
import net.unethicalite.plugins.zulrah.tasks.AvoidAttack;
import net.unethicalite.plugins.zulrah.tasks.Eating;
import net.unethicalite.plugins.zulrah.tasks.EnterZulrah;
import net.unethicalite.plugins.zulrah.tasks.JadPhase;
import net.unethicalite.plugins.zulrah.tasks.SwitchGear;
import net.unethicalite.plugins.zulrah.tasks.TogglePrayers;
import net.unethicalite.plugins.zulrah.tasks.Traverse;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(name = "Unethical Zulrah", enabledByDefault = false)
@Slf4j
public class UnethicalZulrahPlugin extends LoopedPlugin
{
	public RotationNode node;
	public WorldPoint origin;
	public String rotationName = "None";
	private RotationTree tree;
	private RotationNode firstPhase;
	private RotationNode secondPhase;
	private RotationNode thirdPhase;
	private RotationNode fourthPhase;
	private int zulrahHp = -1;
	private int zulrahAttacks = 0;

	private final Task[] tasks = new Task[]
			{
					new TogglePrayers(),
					new Eating(),
					new Traverse(),
					new SwitchGear(),
					new JadPhase(),
					new AttackZulrah(),
					new AvoidAttack(),
					new EnterZulrah()
			};

	@Inject
	private UnethicalZulrahConfig config;

	public static boolean atZulrah()
	{
		return Static.getClient().isInInstancedRegion();
	}

	@Override
	protected void startUp()
	{
		RotationNode root = new RotationNode(null, Rotation.INITIAL);
		thirdPhase = root.add(Rotation.GREEN_EAST_NE);

		thirdPhase.add(Rotation.MAGMA_CENTER_NW)
				.add(Rotation.TANZ_WEST_W)
				.add(Rotation.GREEN_SOUTH_W)
				.add(Rotation.TANZ_EAST_E)
				.add(Rotation.GREEN_CENTER_W)
				.add(Rotation.GREEN_WEST_W)
				.add(Rotation.TANZ_CENTER_E)
				.add(Rotation.JAD_PHASE_E)
				.add(Rotation.TANZ_CENTER_NE);

		fourthPhase = root.add(Rotation.TANZ_EAST_NE);
		fourthPhase.add(Rotation.GREEN_SOUTH_W)
				.add(Rotation.TANZ_WEST_W)
				.add(Rotation.MAGMA_CENTER_E)
				.add(Rotation.GREEN_EAST_E)
				.add(Rotation.GREEN_SOUTH_E_W)
				.add(Rotation.GREEN_SOUTH_W)
				.add(Rotation.TANZ_WEST_W)
				.add(Rotation.GREEN_CENTER_E)
				.add(Rotation.TANZ_CENTER_E)
				.add(Rotation.JAD_PHASE_E)
				.add(Rotation.TANZ_CENTER_NE);

		firstPhase = root.add(Rotation.MAGMA_CENTER_NE)
				.add(Rotation.TANZ_CENTER_E);

		firstPhase.add(Rotation.GREEN_SOUTH_W)
				.add(Rotation.MAGMA_CENTER_W)
				.add(Rotation.TANZ_WEST_W)
				.add(Rotation.GREEN_SOUTH_E)
				.add(Rotation.TANZ_SOUTH_E_CW)
				.add(Rotation.TANZ_SOUTH_CW)
				.add(Rotation.JAD_PHASE_W)
				.add(Rotation.MAGMA_CENTER_NE);

		secondPhase = firstPhase.add(Rotation.GREEN_WEST_W)
				.add(Rotation.TANZ_SOUTH_W)
				.add(Rotation.MAGMA_CENTER_W)
				.add(Rotation.GREEN_EAST_CE)
				.add(Rotation.TANZ_SOUTH_CW)
				.add(Rotation.JAD_PHASE_W)
				.add(Rotation.MAGMA_CENTER_NE);

		tree = new RotationTree(root);

		List<String> rangeGearNames = Arrays.stream(config.rangeGearNames().split(","))
				.collect(Collectors.toList());
		List<String> mageGearNames = Arrays.stream(config.mageGearNames().split(","))
				.collect(Collectors.toList());

		Gear rangeGear = Gear.generateSetup(rangeGearNames);
		Gear mageGear = Gear.generateSetup(mageGearNames);

		ZulrahType.setMagePhaseGear(rangeGear);
		ZulrahType.setRangedMeleePhaseGear(mageGear);
	}

	public Task[] getTasks()
	{
		return tasks;
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		log.info("Task: {} | Rotation: {}", currentTask, rotationName);
		refresh();

		if (node != null)
		{
			updateRotation(node.getRotation());
			updateOrigin(origin);
		}
		else
		{
			updateRotation(null);
			updateOrigin(null);
		}
	}

	public void refresh()
	{
		if (!atZulrah())
		{
			node = null;
			origin = null;
			rotationName = "None";
			zulrahHp = -1;
			return;
		}

		// on enter
		Widget cont = Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE);
		if (cont != null && cont.getText().startsWith("The priestess rows you to Zulrah's") && Dialog.canContinue())
		{
			origin = Players.getLocal().getWorldLocation();
			node = tree.getRoot();
			return;
		}

		NPC zulrah = NPCs.getNearest(Constants.ZULRAH_NAME);
		if (zulrah != null)
		{
			if (zulrahHp == -1)
			{
				zulrahHp = zulrah.getHealthRatio();
			}

			// go back to root if we're not on a node
			if (node == null)
			{
				log.debug("Node is null");
				rotationName = "None";
				node = tree.getRoot();
				origin = Players.getLocal().getWorldLocation();
				return;
			}

			if (node.equals(firstPhase)) rotationName = "First rotation";
			if (node.equals(secondPhase)) rotationName = "Second rotation";
			if (node.equals(thirdPhase)) rotationName = "Third rotation";
			if (node.equals(fourthPhase)) rotationName = "Fourth rotation";

			List<RotationNode> children = node.getChildren();

			if (rotationName.equals("First rotation") && node.getRotation() == Rotation.TANZ_SOUTH_E_CW && zulrahAttacks >= 3)
			{
				node = children.get(0);
			}

			if (rotationName.equals("Fourth rotation") && node.getRotation() == Rotation.GREEN_SOUTH_E_W && zulrahAttacks >= 6)
			{
				node = children.get(0);
			}

			// on despawn, set new node
			if (zulrah.getAnimation() == Constants.DISAPPEAR_ANIMATION
					&& node.getRotation().getZulrahType().id() == zulrah.getId()
					&& node.getRotation().getZulrahPosition(origin).equals(zulrah.getWorldLocation()))
			{
				log.debug("Zulrah despawned, cycling node");
				zulrahAttacks = 0;

				if (children.size() == 0)
				{
					node = tree.getRoot();
					return;
				}

				node = children.get(0);
				return;
			}

			// if we're on the wrong node, go up 1 node and start traversal
			if (node.getRotation().getZulrahType().id() != zulrah.getId()
					|| !node.getRotation().getZulrahPosition(origin).equals(zulrah.getWorldLocation()))
			{
				log.debug("We are on the wrong node {} {}", node.getRotation().getZulrahPosition(origin), zulrah.getWorldLocation());
				RotationNode parent = node.getParent();
				zulrahAttacks = 0;

				if (parent == null)
				{
					return;
				}

				// check children until we found the correct rotation
				List<RotationNode> childs = new ArrayList<>(parent.getChildren());
				for (RotationNode node : childs)
				{
					if (node.getRotation().getZulrahPosition(origin).equals(zulrah.getWorldLocation())
							&& node.getRotation().getZulrahType().id() == zulrah.getId())
					{
						log.debug("Found correct node");
						this.node = node;
						return;
					}
				}
			}
		}
	}

	public void updateRotation(Rotation rotation)
	{
		for (Task task : getTasks())
		{
			if (task instanceof ZulrahTask)
			{
				((ZulrahTask) task).setRotation(rotation);
			}
		}
	}

	public void updateOrigin(WorldPoint origin)
	{
		for (Task task : getTasks())
		{
			if (task instanceof ZulrahTask)
			{
				((ZulrahTask) task).setOrigin(origin);
			}
		}
	}

	@Provides
	UnethicalZulrahConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalZulrahConfig.class);
	}

	@Getter
	private String currentTask = null;

	@Override
	protected int loop()
	{
		currentTask = null;
		for (Task task : getTasks())
		{
			if (task.validate())
			{
				currentTask = task.getClass().getSimpleName();
				if (task instanceof ZulrahTask && ((ZulrahTask) task).getRotation() != null)
				{
					log.debug("Zulrah type: {}", ((ZulrahTask) task).getRotation().getZulrahType());
				}
				int delay = task.execute();
				if (task.isBlocking())
				{
					return delay;
				}
			}
		}

		return 1000;
	}
}
