/*
 * Copyright (c) 2022, Melxin <https://github.com/melxin/>,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.unethicalite.wintertodt;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.wintertodt.utils.TimeUtils;
import org.pf4j.Extension;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.widgets.Widgets;
import static net.unethicalite.api.commons.Time.sleep;
import static net.unethicalite.api.commons.Time.sleepUntil;
import java.time.Instant;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MessageNode;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Singleton
@Extension
@PluginDescriptor(
	name = "mWintertodt",
	description = "does wintertodt",
	enabledByDefault = false,
	tags =
		{
			"wintertodt",
			"minigame",
			"firemaking",
			"woodcutting",
			"fletching",
			"smithing",
			"rewards"
		}
)
@Slf4j
public class mWintertodtPlugin extends LoopedPlugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private mWintertodtOverlay mWintertodtOverlay;

	@Inject
	private mWintertodtConfig config;

	@Provides
	private mWintertodtConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(mWintertodtConfig.class);
	}

	@Getter(AccessLevel.PACKAGE)
	private State currentState;

	@Getter(AccessLevel.PACKAGE)
	private int won;

	@Getter(AccessLevel.PACKAGE)
	private int lost;

	@Getter(AccessLevel.PACKAGE)
	private boolean scriptStarted;

	private Instant scriptStartTime;
	protected String getTimeRunning()
	{
		return scriptStartTime != null ? TimeUtils.getTimeBetween(scriptStartTime, Instant.now()) : "";
	}

	@Override
	protected void startUp()
	{
		this.overlayManager.add(mWintertodtOverlay);
	}

	@Override
	protected void shutDown()
	{
		reset();
		this.overlayManager.remove(mWintertodtOverlay);
	}

	/**
	 * Reset/stop
	 */
	private void reset()
	{
		this.won = 0;
		this.lost = 0;
		this.scriptStartTime = null;
		this.scriptStarted = false;
	}

	@Subscribe
	public void onConfigButtonPressed(ConfigButtonClicked event)
	{
		if (!event.getGroup().contains("mwintertodt") || !event.getKey().toLowerCase().contains("start"))
		{
			return;
		}

		if (scriptStarted)
		{
			reset();
		}
		else
		{
			this.scriptStartTime = Instant.now();
			this.scriptStarted = true;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		ChatMessageType chatMessageType = chatMessage.getType();
		MessageNode messageNode = chatMessage.getMessageNode();

		if (!isInWintertodtRegion() && chatMessageType != ChatMessageType.GAMEMESSAGE && chatMessageType != ChatMessageType.SPAM)
		{
			return;
		}

		if (messageNode.getValue().startsWith("You have gained a supply crate"))
		{
			won++;
		}

		if (messageNode.getValue().startsWith("You did not earn enough points"))
		{
			lost++;
		}
	}

	private boolean isInWintertodtRegion()
	{
		if (client.getLocalPlayer() != null)
		{
			return client.getLocalPlayer().getWorldLocation().getRegionID() == 6462;
		}
		return false;
	}

	private boolean isGameStarted()
	{
		if (isInWintertodtRegion())
		{
			return !Widgets.get(396, 3).getText().contains("The Wintertodt returns");
		}
		return false;
	}

	private enum State
	{
		BANK, ENTER_WINTERTODT, EAT_FOOD, CUT_TREE, FLETCH_LOGS, FIX_BRAZIER, LIT_BRAZIER, FEED_BRAZIER, LEAVE_WINTERTODT, SLEEP
	}

	private State getState()
	{
		if (Combat.getHealthPercent() <= config.healthPercent())
		{
			if (Inventory.contains(config.foodType().getId()))
			{
				return State.EAT_FOOD;
			}
			else
			{
				if (isInWintertodtRegion())
				{
					return State.LEAVE_WINTERTODT;
				}
			}
		}

		if (!isInWintertodtRegion())
		{
			if (Inventory.getCount(config.foodType().getId()) < config.foodAmount() || Inventory.contains(ItemID.SUPPLY_CRATE))
			{
				return State.BANK;
			}
			else
			{
				return State.ENTER_WINTERTODT;
			}
		}

		if (isInWintertodtRegion())
		{
			if (isGameStarted())
			{
				TileObject brokenBrazier = TileObjects.getFirstSurrounding(client.getLocalPlayer().getWorldLocation(), 10, obj -> obj.hasAction("Fix"));
				TileObject unlitBrazier = TileObjects.getFirstSurrounding(client.getLocalPlayer().getWorldLocation(), 10, obj -> obj.hasAction("Light"));
				TileObject burningBrazier = TileObjects.getFirstSurrounding(client.getLocalPlayer().getWorldLocation(), 10, obj -> obj.hasAction("Feed") || obj.getName().startsWith("Burning brazier"));
				if (brokenBrazier != null && client.getLocalPlayer().distanceTo(brokenBrazier) <= 4)
				{
					return State.FIX_BRAZIER;
				}
				else if (unlitBrazier != null && client.getLocalPlayer().distanceTo(unlitBrazier) <= 4)
				{
					return State.LIT_BRAZIER;
				}
				else if (Inventory.getCount(ItemID.BRUMA_KINDLING) >= config.maxResources() || Inventory.contains(ItemID.BRUMA_KINDLING) && burningBrazier != null && client.getLocalPlayer().distanceTo(burningBrazier.getWorldLocation()) <= 4)
				{
					return State.FEED_BRAZIER;
				}
				else if (Inventory.contains(ItemID.BRUMA_ROOT))
				{
					return State.FLETCH_LOGS;
				}
				else
				{
					return State.CUT_TREE;
				}
			}
			else if (Inventory.getCount(config.foodType().getId()) < config.foodAmount() || Inventory.contains(ItemID.SUPPLY_CRATE))
			{
				return State.LEAVE_WINTERTODT;
			}
		}
		return State.SLEEP;
	}

	@Override
	protected int loop()
	{
		if (!scriptStarted)
		{
			return -1;
		}

		// Stop when on login screen or level is reached..
		if (client.getGameState() == GameState.LOGIN_SCREEN
			|| client.getBoostedSkillLevel(Skill.FIREMAKING) >= config.destinationLevel())
		{
			if (scriptStarted)
			{
				scriptStarted = false;
			}
			return -1;
		}

		currentState = getState();
		switch (currentState)
		{
			case BANK:
				TileObject bank = TileObjects.getFirstSurrounding(client.getLocalPlayer().getWorldLocation(), 10, obj -> obj.hasAction("Bank") || obj.getName().startsWith("Collect"));
				if (Bank.isOpen())
				{
					sleep(1000);
					Bank.depositAllExcept(item -> item != null && item.getName().equals(config.foodType().getName()) || item.getName().endsWith("axe") || item.getName().equals("Knife") || item.getName().equals("Hammer") || item.getName().equals("Tinderbox"));
					sleep(1500);

					if (Bank.getFirst(config.foodType().getId()) == null)
					{
						reset();
						return -1;
					}

					int foodAmountToWithdraw = config.foodAmount() - Inventory.getCount(config.foodType().getId());
					if (foodAmountToWithdraw > 0)
					{
						Bank.withdraw(config.foodType().getId(), foodAmountToWithdraw, Bank.WithdrawMode.ITEM);
						sleepUntil(() -> Inventory.getCount(config.foodType().getId()) == config.foodAmount(), 3000);

						Widget enterAmountWidget = Widgets.get(WidgetInfo.CHATBOX_TITLE);
						if (enterAmountWidget != null && enterAmountWidget.getText().startsWith("Enter amount"))
						{
							Keyboard.type(0);
							Keyboard.sendEnter();
							sleep(1500);
						}
					}

					Bank.close();
				}
				else if (bank != null)
				{
					bank.interact("Bank");
					sleepUntil(() -> Bank.isOpen(), 1000);
				}
				else
				{
					Walker.walkTo(new WorldPoint(1640, 3944, 0));
					sleepUntil(() -> bank != null, 800);
				}
				break;

			case ENTER_WINTERTODT:
				if (!isInWintertodtRegion())
				{
					Walker.walkTo(new WorldPoint(1630, 3962, 0));
					TileObject door = TileObjects.getFirstSurrounding(client.getLocalPlayer().getWorldLocation(), 10, obj -> obj.getName().startsWith("Door") && obj.hasAction("Enter"));
					sleepUntil(() -> door != null, 1000);

					if (door != null)
					{
						door.interact("Enter");
						sleepUntil(this::isInWintertodtRegion, 5000);
					}
				}

				sleep(2000);
				Walker.walkTo(config.brazierLocation().getWorldPoint());
				break;

			case EAT_FOOD:
				FoodType foodType = config.foodType();
				Inventory.getFirst(foodType.getId()).interact(foodType.getAction());
				break;

			case CUT_TREE:
				if (!client.getLocalPlayer().isAnimating() && isInWintertodtRegion())
				{
					TileObject tree = TileObjects.getFirstSurrounding(config.brazierLocation().getWorldPoint(), 10, obj -> obj.hasAction("Chop") || obj.getName().startsWith("Bruma roots"));
					if (tree != null)
					{
						tree.interact("Chop");
					}
					else
					{
						Walker.walkTo(config.brazierLocation().getWorldPoint());
						sleepUntil(() -> tree != null, 800);
					}
				}
				break;

			case FLETCH_LOGS:
				if (!client.getLocalPlayer().isAnimating() && isInWintertodtRegion()
					|| Inventory.getCount(ItemID.BRUMA_ROOT) >= config.maxResources() && client.getLocalPlayer().getAnimation() != AnimationID.FLETCHING_BOW_CUTTING)
				{
					Inventory.getFirst(ItemID.KNIFE).useOn(Inventory.getFirst(ItemID.BRUMA_ROOT));
					sleep(500);
					sleepUntil(() -> !Inventory.contains(ItemID.BRUMA_ROOT) || client.getLocalPlayer().getAnimation() != AnimationID.FLETCHING_BOW_CUTTING, 5500);
				}
				break;

			case FIX_BRAZIER:
				TileObject brokenBrazier = TileObjects.getFirstSurrounding(client.getLocalPlayer().getWorldLocation(), 10, obj -> obj.hasAction("Fix"));
				if (brokenBrazier != null)
				{
					brokenBrazier.interact("Fix");
					sleepUntil(() -> brokenBrazier == null, 3000);
				}
				break;

			case LIT_BRAZIER:
				TileObject unlitBrazier = TileObjects.getFirstSurrounding(client.getLocalPlayer().getWorldLocation(), 10, obj -> obj.hasAction("Light"));
				if (unlitBrazier != null)
				{
					unlitBrazier.interact("Light");
					sleepUntil(() -> unlitBrazier == null, 3000);
				}
				break;

			case FEED_BRAZIER:
				TileObject burningBrazier = TileObjects.getFirstSurrounding(client.getLocalPlayer().getWorldLocation(), 10, obj -> obj.hasAction("Feed") || obj.getName().startsWith("Burning brazier"));
				if (!client.getLocalPlayer().isAnimating() && isInWintertodtRegion())
				{
					if (burningBrazier != null)
					{
						burningBrazier.interact("Feed");
					}
					sleep(500);
					sleepUntil(() -> burningBrazier == null || !Inventory.contains(ItemID.BRUMA_KINDLING), 5500);
				}
				break;

			case LEAVE_WINTERTODT:
				if (isInWintertodtRegion())
				{
					TileObject door = TileObjects.getFirstSurrounding(client.getLocalPlayer().getWorldLocation(), 10, obj -> obj.getName().startsWith("Door") && obj.hasAction("Enter"));
					if (door != null)
					{
						door.interact("Enter");
						Widget skipDialog = Widgets.get(WidgetInfo.DIALOG_OPTION_OPTION1);
						sleepUntil(() -> !isInWintertodtRegion() || skipDialog != null && skipDialog.isVisible(), 5000);
						if (skipDialog != null && skipDialog.isVisible())
						{
							skipDialog.interact(MenuAction.WIDGET_CONTINUE.getId());
							sleepUntil(() -> !isInWintertodtRegion(), 1000);
						}
					}
					else
					{
						Walker.walkTo(new WorldPoint(631, 3969, 0));
						sleepUntil(() -> door != null, 800);
					}
				}
				break;

			case SLEEP:
				return 1000;
		}
		return 100;
	}
}
