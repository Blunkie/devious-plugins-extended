package net.unethicalite.plugins.bankpin;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

import javax.inject.Inject;

@Extension
@PluginDescriptor(
		name = "Unethical Bankpin",
		enabledByDefault = false
)
public class UnethicalBankPinPlugin extends Plugin
{
	private static final WidgetInfo[] BANK_PIN_NUMBERS =
			{
					WidgetInfo.BANK_PIN_10,
					WidgetInfo.BANK_PIN_1,
					WidgetInfo.BANK_PIN_2,
					WidgetInfo.BANK_PIN_3,
					WidgetInfo.BANK_PIN_4,
					WidgetInfo.BANK_PIN_5,
					WidgetInfo.BANK_PIN_6,
					WidgetInfo.BANK_PIN_7,
					WidgetInfo.BANK_PIN_8,
					WidgetInfo.BANK_PIN_9,
			};

	@Inject
	private UnethicalBankPinConfig config;

	@Inject
	private Client client;

	@Subscribe
	private void onScriptEvent(ScriptPostFired e)
	{
		if (e.getScriptId() != 683)
		{
			return;
		}

		Widget bankPinContainer = Widgets.get(WidgetInfo.BANK_PIN_CONTAINER);
		if (!Widgets.isVisible(bankPinContainer))
		{
			return;
		}

		String pin = config.pin();
		if (!pin.matches("\\d{4}"))
		{
			return;
		}

		String[] pinSplit = pin.split("");
		Widget first = Widgets.get(WidgetInfo.BANK_PIN_FIRST_ENTERED);
		Widget second = Widgets.get(WidgetInfo.BANK_PIN_SECOND_ENTERED);
		Widget third = Widgets.get(WidgetInfo.BANK_PIN_THIRD_ENTERED);
		Widget fourth = Widgets.get(WidgetInfo.BANK_PIN_FOURTH_ENTERED);

		if (first.getText().equals("?"))
		{
			int number = Integer.parseInt(pinSplit[0]);
			clickNumber(client, number);
		}
		else if (second.getText().equals("?"))
		{
			int number = Integer.parseInt(pinSplit[1]);
			clickNumber(client, number);
		}
		else if (third.getText().equals("?"))
		{
			int number = Integer.parseInt(pinSplit[2]);
			clickNumber(client, number);
		}
		else if (fourth.getText().equals("?"))
		{
			int number = Integer.parseInt(pinSplit[3]);
			clickNumber(client, number);
		}
	}

	private static void clickNumber(Client client, int number)
	{
		for (WidgetInfo widgetInfo : BANK_PIN_NUMBERS)
		{
			Widget numberBox = Widgets.get(widgetInfo);
			if (!Widgets.isVisible(numberBox))
			{
				continue;
			}

			if (numberBox.getChildren() == null || numberBox.getChildren().length < 2)
			{
				continue;
			}

			if (numberBox.getChild(1).getText().equals(String.valueOf(number)))
			{
				client.invokeWidgetAction(1, numberBox.getChild(0).getId(), 0, -1, "Select");
				break;
			}
		}
	}

	@Provides
	UnethicalBankPinConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalBankPinConfig.class);
	}
}
