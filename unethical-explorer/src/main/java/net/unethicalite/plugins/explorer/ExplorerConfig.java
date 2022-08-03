package net.unethicalite.plugins.explorer;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.plugins.explorer.util.Category;

@ConfigGroup("unethicalexplorer")
public interface ExplorerConfig extends Config
{

	@ConfigItem(
			keyName = "categories",
			name = "Categories",
			description = "Predefined location categories",
			position = 10
	)
	default Category category()
	{
		return Category.QUEST;
	}

	@ConfigItem(
			keyName = "bankLocations",
			name = "Banks",
			description = "Walk to the specified bank",
			position = 11,
			hidden = true,
			unhide = "categories",
			unhideValue = "BANKS"
	)
	default BankLocation bankLocation()
	{
		return BankLocation.AL_KHARID_BANK;
	}

	@ConfigItem(
			keyName = "customcoords",
			name = "Custom coords",
			description = "Walk to the specified coordinates",
			position = 15,
			hidden = true,
			unhide = "categories",
			unhideValue = "CUSTOM"
	)
	default String coords()
	{
		return "3220 3220 0";
	}

	@ConfigItem(
			keyName = "walk",
			name = "Walk to",
			description = "Walk to set coordinates",
			position = 20
	)
	default Button walk()
	{
		return new Button();
	}
}
