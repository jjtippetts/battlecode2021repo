package newerplayer;

import battlecode.common.*;

public class Slanderer extends Unit {

    public Slanderer(RobotController rc) {
        super(rc);
    }

    Direction myWall=Direction.CENTER;
    MapLocation edgeLoc;
    MapLocation myLoc=rc.getLocation();
    int flagMessage=0;
    static int stuckCount = 0;

    public void run() throws GameActionException {
        super.run();

        if (enlightenmentCenterSpawnedFromMessage!=null &&
                enlightenmentCenterSpawnedFromMessage.id == Constants.FOUND_ENEMY_EC) {
            targetEnemyECLoc = enlightenmentCenterSpawnedFromMessage.location;
        }

        //If slanderer turns into Politician, attack enemy ec.
        if (rc.getType() == RobotType.POLITICIAN && targetEnemyECLoc != null) {
            attack();
            tryMove(rc.getLocation().directionTo(targetEnemyECLoc));
        }

        //Eight safe positions next to EC for slanderers to stay
        MapLocation[] safeSpots = {enlightenmentCenterSpawnedFromLoc.translate(1, 1),
        enlightenmentCenterSpawnedFromLoc.translate(-1, 1),
        enlightenmentCenterSpawnedFromLoc.translate(1, -1),
        enlightenmentCenterSpawnedFromLoc.translate(-1, -1),
        enlightenmentCenterSpawnedFromLoc.translate(2, 2),
        enlightenmentCenterSpawnedFromLoc.translate(2, -2),
        enlightenmentCenterSpawnedFromLoc.translate(-2, 2),
        enlightenmentCenterSpawnedFromLoc.translate(-2, -2)};

        MapLocation openSpot = null;

        //If on safe spot already, don't do anything. If not, move to one.
        for (MapLocation safeSpot : safeSpots) {
            if (rc.canSenseLocation(safeSpot) && !rc.isLocationOccupied(safeSpot)) {
                openSpot = safeSpot;
            }
            if (myCurrentLoc.equals(safeSpot)) {
                return;
            }
        }

        if (openSpot != null) {
            nav.bugTwo(openSpot);
        }
    }
}
