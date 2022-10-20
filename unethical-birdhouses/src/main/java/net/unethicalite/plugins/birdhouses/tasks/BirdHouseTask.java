package net.unethicalite.plugins.birdhouses.tasks;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.plugins.birdhouses.BirdHousesPlugin;

@RequiredArgsConstructor
public abstract class BirdHouseTask implements Task
{
	@Delegate
	protected final BirdHousesPlugin context;
}
