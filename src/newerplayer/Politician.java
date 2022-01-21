package newerplayer;

import battlecode.common.*;

import java.util.ArrayList;

public class Politician extends Unit {
    static boolean attack = false;
    static MapLocation attackLoc;
    static final int DISTANCE_TO_EDGE = 3;

    public Politician(RobotController rc) {
        super(rc);
    }

    public MapLocation slandererEdge = null;
    public Direction slandererWallDir = null;
    public Direction roam = Direction.CENTER;
    public int flagMessage = 0;
    static int distanceToEC = 4;
    static int stuckCount = 0;
    static boolean onPosition = false;
    static MapLocation whereToGo = null;

    public void run() throws GameActionException {
        super.run();

        if (distanceToEC > 10) {
            distanceToEC = 10;
        }

        if (!rc.isReady()) {
            return;
        }

        //Converted Politician. Doesn't have message because it doesn't know the enlightenment center id.
        if (enlightenmentCenterSpawnedFromMessage == null) {
            //look around for nearby allies to see if they have the enemy EC location set
            targetEnemyECLoc = scanForEnemyEC();
        }

        getEnlightenmentCenterLoc(myTeam);

        //if sense enemy muckraker, ATTACK!
        attack();

        roam2();
    }

    void roam2() throws GameActionException {
        MapLocation[] mapLocationsNearEC = new MapLocation[]{enlightenmentCenterSpawnedFromLoc.translate(-3, 0),
                enlightenmentCenterSpawnedFromLoc.translate(3, 0),
                enlightenmentCenterSpawnedFromLoc.translate(0, 3),
                enlightenmentCenterSpawnedFromLoc.translate(0, -3),
                enlightenmentCenterSpawnedFromLoc.translate(3, 3),
                enlightenmentCenterSpawnedFromLoc.translate(-3, 3),
                enlightenmentCenterSpawnedFromLoc.translate(3, -3),
                enlightenmentCenterSpawnedFromLoc.translate(-3, -3)};
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, myTeam);
        MapLocation spotsNearestToEC = null;

        for (MapLocation loc : mapLocationsNearEC) {
            if (rc.canSenseLocation(loc) && !rc.isLocationOccupied(loc)) {
                spotsNearestToEC = loc;
            }
            if (myCurrentLoc.equals(loc)) {
                onPosition = true;
                break;
            }
        }

        if (onPosition) {
            notifyNearestSpot(nearbyAllies);
            if (rc.getRoundNum() % 5 == 0) {
                comms.setFlag(Constants.NOTHING, rc.getLocation());
            }
            return;
        }

        if (spotsNearestToEC != null) {
            nav.bugTwo(spotsNearestToEC);
        } else {
            if (whereToGo != null && rc.canSenseLocation(whereToGo) && rc.isLocationOccupied(whereToGo)) {
                RobotInfo robot = rc.senseRobotAtLocation(whereToGo);
                if (robot.type == RobotType.POLITICIAN && robot.team == myTeam) {
                    whereToGo = null;
                }
            }

            if (whereToGo == null) {
                whereToGo = getNearestSpotToGoTo(nearbyAllies);
            }
            if (whereToGo != null) {
                System.out.println(whereToGo);
                nav.bugTwo(whereToGo);
            }
            //Can't use myCurrentLoc because we just moved
            if (rc.getLocation().equals(whereToGo)) {
                onPosition = true;
            }
        }
    }

    public MapLocation scanForEnemyEC() throws GameActionException {
        RobotInfo[] friendlyBots = rc.senseNearbyRobots(-1, myTeam);
        int flag = 0;
        Message msg = null;

        if (friendlyBots != null) {
            for (int i = 0; i < friendlyBots.length; i++) {
                if (rc.canGetFlag(friendlyBots[i].getID())) {
                    flag = rc.getFlag(friendlyBots[i].getID());
                    if (flag != 0) {
                        msg = comms.decodeFlag(flag);
                        if (msg.id == Constants.FOUND_ENEMY_EC) {
                            return msg.location;
                        }
                    }
                }
            }
        }
        return null;
    }

    MapLocation getNearestSpotToGoTo(RobotInfo[] nearbyAllies) throws GameActionException {
        MapLocation nearestLoc = null;
        int distance = 1000;

        //Get the nearest spot of the outer to go to relayed back from other Politicians
        for (RobotInfo ally : nearbyAllies) {
            if (ally.type.equals(RobotType.POLITICIAN) && rc.canGetFlag(ally.ID)) {
                Message decodedMessage = comms.decodeFlag(rc.getFlag(ally.ID));

                if (decodedMessage != null && decodedMessage.id == Constants.OPEN_SPOT
                        && decodedMessage.location.distanceSquaredTo(myCurrentLoc) < distance) {
                    nearestLoc = decodedMessage.location;
                    distance = decodedMessage.location.distanceSquaredTo(myCurrentLoc);
                }
            }
        }

        return nearestLoc;
    }

    void notifyNearestSpot(RobotInfo[] nearbyAllies) throws GameActionException {
        MapLocation[] mapLocations = new MapLocation[]{
                myCurrentLoc.translate(-3, 0),
                myCurrentLoc.translate(3, 0),
                myCurrentLoc.translate(0, 3),
                myCurrentLoc.translate(0, -3),
                myCurrentLoc.translate(3, 3),
                myCurrentLoc.translate(-3, 3),
                myCurrentLoc.translate(3, -3),
                myCurrentLoc.translate(-3, -3)};
        MapLocation notOccupied = null;
        int closestDistanceFromMe = 1000;
        int closestDistanceFromEC = 1000;
        MapLocation flaggedOpenPosition = null;
        ArrayList<MapLocation> notOccupiedSpots = new ArrayList<>();
        ArrayList<MapLocation> relayedSpots = new ArrayList<>();

        //Check there are open spots nearby myself
        for (MapLocation loc : mapLocations) {
            if (rc.canSenseLocation(loc) && !rc.isLocationOccupied(loc)) {
                int distanceToLoc = myCurrentLoc.distanceSquaredTo(loc);
                if (distanceToLoc < closestDistanceFromMe) {
                    closestDistanceFromMe = distanceToLoc;
                    boolean noNearbyPoli = true;
                    RobotInfo[] nearPotentialLoc = rc.senseNearbyRobots(loc, 5, myTeam);
                    System.out.println(loc + " " + nearPotentialLoc.length);
                    //To not connect with other EC lattice - Check if the spot has Politicians that are established nearby
                    for (RobotInfo near : nearPotentialLoc) {
                        if (near.type == RobotType.POLITICIAN) {
                            noNearbyPoli = false;

                        }
                    }
                    System.out.println(loc + "  ");
                    if (noNearbyPoli) {
                        notOccupied = loc;
                    }
                }
            }
        }

        //Priorities flagging nearby vacant spots over relaying other spot
        if (notOccupied != null) {
            System.out.println("NOT OCCUPIED " + notOccupied);
            comms.setFlag(Constants.OPEN_SPOT, notOccupied, Constants.NOT_OCCUPIED);
        } else {
            int type = 0;

            //Get the closest outer wall position relayed back from other outer Politicians
            for (RobotInfo ally : nearbyAllies) {
                if (ally.type.equals(RobotType.POLITICIAN) && rc.canGetFlag(ally.ID)) {
                    Message decodedMessage = comms.decodeFlag(rc.getFlag(ally.ID));
                    if (decodedMessage != null && decodedMessage.id == Constants.OPEN_SPOT
                            && ally.location.distanceSquaredTo(enlightenmentCenterSpawnedFromLoc)
                            > myCurrentLoc.distanceSquaredTo(enlightenmentCenterSpawnedFromLoc)
                            && !decodedMessage.location.equals(myCurrentLoc)) {
                        if (decodedMessage.other == Constants.NOT_OCCUPIED) {
                            notOccupiedSpots.add(decodedMessage.location);
                        } else if (decodedMessage.other == Constants.FLAGGED) {
                            relayedSpots.add(decodedMessage.location);
                        }
                    }
                }
            }

            if (!notOccupiedSpots.isEmpty()) {
                for (MapLocation spot : notOccupiedSpots) {
                    int distanceFromSpotToEC = spot.distanceSquaredTo(enlightenmentCenterSpawnedFromLoc);
                    if (distanceFromSpotToEC < closestDistanceFromEC) {
                        flaggedOpenPosition = spot;
                        closestDistanceFromEC = distanceFromSpotToEC;
                        type = Constants.NOT_OCCUPIED;
                    }
                }
            } else if (!relayedSpots.isEmpty()) {
                closestDistanceFromEC = 1001;
                for (MapLocation spot : relayedSpots) {
                    int distanceFromSpotToEC = spot.distanceSquaredTo(enlightenmentCenterSpawnedFromLoc);
                    if (distanceFromSpotToEC < closestDistanceFromEC) {
                        flaggedOpenPosition = spot;
                        closestDistanceFromEC = distanceFromSpotToEC;
                        type = Constants.FLAGGED;
                    }
                }
            }
            if (flaggedOpenPosition != null) {
                comms.setFlag(Constants.OPEN_SPOT, flaggedOpenPosition, type);
            } else {
                comms.setFlag(Constants.NOTHING, myCurrentLoc);
            }
        }
    }

}
