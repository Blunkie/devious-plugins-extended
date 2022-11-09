package net.unethicalite.plugins.cooker.tasks;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.plugins.cooker.CookerPlugin;

@RequiredArgsConstructor
public abstract class CookerTask implements Task
{
	@Delegate
	private final CookerPlugin context;

	protected int taskCooldown;
}
