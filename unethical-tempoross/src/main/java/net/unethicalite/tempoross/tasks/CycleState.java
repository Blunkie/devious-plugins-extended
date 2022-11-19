package net.unethicalite.tempoross.tasks;

import net.unethicalite.tempoross.TemporossConfig;
import net.unethicalite.tempoross.TemporossPlugin;

import javax.inject.Inject;

public class CycleState extends TemporossTask
{
	@Inject
	private TemporossConfig config;

	public CycleState(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return getScriptState().isComplete(config);
	}

	@Override
	public int execute()
	{
		setScriptState(getScriptState().getNext());
		if (getScriptState() == null)
		{
			setScriptState(TemporossPlugin.State.THIRD_CATCH);
		}

		return 10;
	}
}
