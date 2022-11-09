package net.unethicalite.plugins.cooker;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.plugins.TaskPlugin;
import net.unethicalite.plugins.cooker.tasks.Cook;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
		name = "Unethical Cooker",
		enabledByDefault = false
)
public class CookerPlugin extends TaskPlugin
{
	private final Task[] tasks =
			{
					new Cook(this)
			};

	@Inject
	@Getter
	private CookerConfig config;

	@Inject
	@Getter
	private Client client;

	@Override
	public Task[] getTasks()
	{
		return tasks;
	}

	@Provides
	CookerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CookerConfig.class);
	}
}
