package net.unethicalite.tempoross.tasks;

import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.tempoross.TemporossPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AwaitStart extends TemporossTask
{
	private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");
	private boolean started = false;

	public AwaitStart(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return !started;
	}

	@Override
	public int execute()
	{
		return -1;
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		Widget energyWidget = Widgets.get(437, 35);
		Widget essenceWidget = Widgets.get(437, 45);
		Widget intensityWidget = Widgets.get(437, 55);
		if (!Widgets.isVisible(energyWidget) || !Widgets.isVisible(essenceWidget) || !Widgets.isVisible(intensityWidget))
		{
			started = false;
			return;
		}

		Matcher energyMatcher = DIGIT_PATTERN.matcher(energyWidget.getText());
		Matcher intensityMatcher = DIGIT_PATTERN.matcher(intensityWidget.getText());
		if (!energyMatcher.find() || !intensityMatcher.find())
		{
			started = false;
			return;
		}

		started = true;

		TemporossPlugin.energy = Integer.parseInt(energyMatcher.group(0));
		TemporossPlugin.intensity = Integer.parseInt(intensityMatcher.group(0));
	}

	@Override
	public boolean subscribe()
	{
		return true;
	}
}
