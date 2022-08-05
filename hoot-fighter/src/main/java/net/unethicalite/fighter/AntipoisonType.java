package net.unethicalite.fighter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AntipoisonType
{
	ANTIPOISON(179, 177, 175, 2446),
	SUPER_ANTIPOISON(185, 183, 181, 2448),
	ANTIDOTE_PLUS(5949, 5947, 5945, 5943),
	ANTIDOTE_PLUS_PLUS(5958, 5956, 5954, 5952),
	ANTI_VENOM(12911, 12909, 12907, 12905),
	ANTI_VENOM_PLUS(12919, 12917, 12915, 12913),
	SANFEW_SERUM(10931, 10929, 10927, 10925);

	private final int dose1;
	private final int dose2;
	private final int dose3;
	private final int dose4;
}
