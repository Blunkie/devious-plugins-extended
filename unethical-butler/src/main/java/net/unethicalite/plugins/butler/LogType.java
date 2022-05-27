package net.unethicalite.plugins.butler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LogType
{
	REGULAR(1511, 1512, 961),
	OAK(1521, 1522, 8779),
	TEAK(6333, 6334, 8781),
	MAHOGANY(6332, 8836, 8783)
	;

	private final int logId;
	private final int logNotedId;
	private final int plankNotedId;
}
