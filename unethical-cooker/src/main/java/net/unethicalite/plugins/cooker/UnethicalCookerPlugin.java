package net.unethicalite.plugins.cooker;

import com.google.inject.Provides;
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
public class UnethicalCookerPlugin extends TaskPlugin
{
	private final Task[] tasks =
			{
					new Cook()
			};

	@Override
	public Task[] getTasks()
	{
		return tasks;
	}

	@Provides
	UnethicalCookerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalCookerConfig.class);
	}
}
