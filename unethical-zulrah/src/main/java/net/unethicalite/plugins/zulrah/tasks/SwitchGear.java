package net.unethicalite.plugins.zulrah.tasks;

import net.unethicalite.plugins.zulrah.framework.ZulrahTask;

public class SwitchGear extends ZulrahTask
{
	@Override
	public boolean validate()
	{
		return getZulrahCycle() != null && getZulrahCycle().getZulrahType().getSetup().anyUnequipped();
	}

	@Override
	public int execute()
	{
		getZulrahCycle().getZulrahType().getSetup().switchGear(50);
		return 100;
	}

	@Override
	public boolean isBlocking()
	{
		return false;
	}
}
