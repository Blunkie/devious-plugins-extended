package net.unethicalite.plugins.zulrah.data;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class RotationNode
{
	private final List<RotationNode> children = new ArrayList<>();
	private final RotationNode parent;
	private final Rotation rotation;

	public RotationNode(RotationNode parent, Rotation rotation)
	{
		this.parent = parent;
		this.rotation = rotation;
	}

	public RotationNode add(Rotation child)
	{
		RotationNode cn = new RotationNode(this, child);
		this.children.add(cn);

		return cn;
	}
}
