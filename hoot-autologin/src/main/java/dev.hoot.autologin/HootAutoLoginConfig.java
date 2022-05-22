package dev.hoot.autologin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hootautologin")
public interface HootAutoLoginConfig extends Config
{
	@ConfigItem(
			keyName = "username",
			name = "Username",
			description = "Username",
			position = 0
	)
	default String username()
	{
		return "Username";
	}

	@ConfigItem(
			keyName = "password",
			name = "Password",
			description = "Password",
			secret = true,
			position = 1
	)
	default String password()
	{
		return "Password";
	}

	@ConfigItem(
			keyName = "auth",
			name = "Authenticator",
			description = "Authenticator",
			secret = true,
			position = 2
	)
	default String auth()
	{
		return "Authenticator";
	}

	@ConfigItem(
			keyName = "world",
			name = "World",
			description = "World Selector",
			position = 3
	)
	default int world()
	{
		return 0;
	}
}
