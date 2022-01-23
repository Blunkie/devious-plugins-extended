package dev.hoot.tempoross;

import dev.hoot.api.coords.SceneArea;
import dev.hoot.api.coords.ScenePoint;
import dev.hoot.api.entities.Players;
import dev.hoot.api.entities.TileObjects;
import dev.hoot.api.scene.Tiles;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.TileObject;

@RequiredArgsConstructor
@Getter
public enum TemporossWorkArea {
    EAST(
            new SceneArea(71, 44, 6, 10, 0),
            new ScenePoint(60, 33, 0),
            new ScenePoint(77, 47, 0),
            new ScenePoint(77, 48, 0),
            new ScenePoint(77, 51, 0),
            new ScenePoint(77, 52, 0),
            new ScenePoint(76, 53, 0),
            new ScenePoint(74, 49, 0),
            new ScenePoint(59, 33, 0),
            new ScenePoint(51, 27, 0)
    ),
    WEST(
            new SceneArea(49, 44, 6, 10, 0),
            new ScenePoint(60, 62, 0),
            new ScenePoint(49, 51, 0),
            new ScenePoint(49, 50, 0),
            new ScenePoint(49, 47, 0),
            new ScenePoint(49, 46, 0),
            new ScenePoint(50, 45, 0),
            new ScenePoint(52, 49, 0),
            new ScenePoint(60, 67, 0),
            new ScenePoint(55, 73, 0)
    ),

    ;

    private final SceneArea startPoint;
    private final ScenePoint safePoint;
    private final ScenePoint bucketPoint;
    private final ScenePoint pumpPoint;
    private final ScenePoint ropePoint;
    private final ScenePoint hammerPoint;
    private final ScenePoint harpoonPoint;
    private final ScenePoint mastPoint;
    private final ScenePoint totemPoint;
    private final ScenePoint rangePoint;

    public TileObject getBucketCrate() {
        return TileObjects.getFirstAt(Tiles.getAt(bucketPoint), x -> x.hasAction("Take"));
    }

    public TileObject getPump() {
        return TileObjects.getFirstAt(Tiles.getAt(pumpPoint), x -> x.hasAction("Use"));
    }

    public TileObject getRopeCrate() {
        return TileObjects.getFirstAt(Tiles.getAt(ropePoint), x -> x.hasAction("Take"));
    }

    public TileObject getHammerCrate() {
        return TileObjects.getFirstAt(Tiles.getAt(hammerPoint), x -> x.hasAction("Take"));
    }

    public TileObject getHarpoonCrate() {
        return TileObjects.getFirstAt(Tiles.getAt(harpoonPoint), x -> x.hasAction("Take"));
    }

    public TileObject getMast() {
        return TileObjects.getFirstAt(Tiles.getAt(mastPoint), x -> x.hasAction("Tether", "Untether"));
    }

    public TileObject getTotem() {
        return TileObjects.getFirstAt(Tiles.getAt(totemPoint), x -> x.hasAction("Tether", "Untether"));
    }

    public TileObject getRange() {
        return TileObjects.getFirstAt(Tiles.getAt(rangePoint), x -> x.hasAction("Cook-at"));
    }

    public TileObject getClosestTether() {
        TileObject mast = getMast();
        TileObject totem = getTotem();
        if (mast != null && totem != null) {
            int mastDistance = mast.getWorldLocation().distanceTo(Players.getLocal().getWorldLocation());
            int totemDistance = totem.getWorldLocation().distanceTo(Players.getLocal().getWorldLocation());
            if (mastDistance < totemDistance) {
                return mast;
            }

            return totem;
        }

        if (mast != null) {
            return mast;
        }

        return totem;
    }
}
