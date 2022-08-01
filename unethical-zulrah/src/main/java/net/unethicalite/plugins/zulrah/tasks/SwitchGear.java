package net.unethicalite.plugins.zulrah.tasks;

import net.unethicalite.plugins.zulrah.framework.ZulrahTask;

public class SwitchGear extends ZulrahTask
{
	@Override
	public boolean validate()
	{
		return getRotation() != null && getRotation().getZulrahType().getSetup().anyUnequipped();
	}

	@Override
	public int execute()
	{
		getRotation().getZulrahType().getSetup().switchGear(50);
		return 100;
	}

	@Override
	public boolean isBlocking()
	{
		return false;
	}
}
