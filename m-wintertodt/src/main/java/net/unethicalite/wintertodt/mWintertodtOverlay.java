package net.unethicalite.wintertodt;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class mWintertodtOverlay extends OverlayPanel
{
	private final Client client;
	private final mWintertodtPlugin plugin;
	private final mWintertodtConfig config;

	@Inject
	private mWintertodtOverlay(Client client, mWintertodtPlugin plugin, mWintertodtConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.BOTTOM_LEFT);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isScriptStarted() && config.overlayEnabled())
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("mWintertodt")
				.color(Color.WHITE)
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Running: " + plugin.getTimeRunning())
				.leftColor(Color.GREEN)
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("State: " + plugin.getCurrentState())
				.leftColor(Color.YELLOW)
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Won: " + plugin.getWon())
				.leftColor(Color.GREEN)
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Lost: " + plugin.getLost())
				.leftColor(Color.RED)
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Cut: " + plugin.getLogsCut())
				.leftColor(Color.GREEN)
				.build());

			if (config.fletchingEnabled())
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left("Fletched: " + plugin.getLogsFletched())
					.leftColor(Color.GREEN)
					.build());
			}

				panelComponent.getChildren().add(LineComponent.builder()
					.left("Brazier: " + plugin.getBrazierLocation().name())
					.leftColor(Color.MAGENTA)
					.build());

			if (config.fixBrokenBrazier())
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left("Braziers fixed: " + plugin.getBraziersFixed())
					.leftColor(Color.GREEN)
					.build());
			}

			if (config.lightUnlitBrazier())
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left("Braziers lit: " + plugin.getBraziersLit())
					.leftColor(Color.GREEN)
					.build());
			}

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Food consumed: " + plugin.getFoodConsumed())
				.leftColor(Color.GREEN)
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Times banked: " + plugin.getTimesBanked())
				.leftColor(Color.GREEN)
				.build());
		}
		return super.render(graphics);
	}
}
