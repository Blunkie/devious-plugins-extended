package net.unethicalite.fighter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AntifireType
{
    ANTIFIRE(2458, 2456, 2454, 2452),
    EXTENDED_ANTIFIRE(11957, 11955, 11953, 11951),
    SUPER_ANTIFIRE(21987, 21984, 21981, 21987),
    EXTENDED_SUPER_ANTIFIRE(22218, 22215, 22212, 22209);

    private final int dose1;
    private final int dose2;
    private final int dose3;
    private final int dose4;
}