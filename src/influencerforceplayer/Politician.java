package influencerforceplayer;

import battlecode.common.*;

import javax.swing.*;
import java.util.*;

public class Politician extends Unit {
    static boolean attack = false;
    static MapLocation attackLoc;
    static final int DISTANCE_TO_EDGE = 3;
    public Politician(RobotController rc) {
        super(rc);
    }

    public MapLocation slandererEdge = null;
    public Direction slandererWallDir = null;
    public Direction roam = null;
    public int flagMessage = 0;

    public void run() throws GameActionException {
        super.run();

        getEnlightenmentCenterLoc(myTeam);
        int sensingRadius = RobotType.POLITICIAN.sensorRadiusSquared;
        int actionRadius = RobotType.POLITICIAN.actionRadiusSquared;

        //if sense enemy muckraker, ATTACK!
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            rc.empower(actionRadius);
        }
        if (targetEnemyECLoc!=null)
            tryMove(rc.getLocation().directionTo(targetEnemyECLoc));
        //Converted Politician. Doesn't have message because it doesn't know the enlightenment center id.
        if (enlightenmentCenterSpawnedFromMessage == null) {
            // look around for nearby allies to see if they have the enemy EC location set
            //targetEnemyECLoc = scanForEnemyEC();
        }

        //politician protect wall
        if (slandererEdge == null) {
            slandererEdge = scanForEdge();
        }

        if (slandererWallDir == null || slandererWallDir == Direction.CENTER) {
            RobotInfo[] robotInfos = rc.senseNearbyRobots();

            for (RobotInfo robotInfo : robotInfos) {
                //ALl slanderers appear as politicians!
                if (robotInfo.getType() == RobotType.POLITICIAN && robotInfo.getTeam() == myTeam) {
                    slandererWallDir = getEdgeDirection(robotInfo.getLocation());
                    if (slandererWallDir != Direction.CENTER) {
                        break;
                    }
                }
            }

            if (slandererEdge != null && rc.canMove(rc.getLocation().directionTo(slandererEdge))) {
                rc.move(rc.getLocation().directionTo(slandererEdge));
            }
        }

        if (slandererWallDir != null) {
            if (!onDefenseLine()) {
//                nav.bugTwo(slandererEdge);
                if (rc.canMove(slandererWallDir)) {
                    rc.move(slandererWallDir);
                }
            } else {
//                letAllyOut(sensingRadius);
                //On defense line
                if (roam == null) {
                    roam = rc.getLocation().directionTo(slandererEdge).rotateLeft().rotateLeft();
                }

                MapLocation nextLoc = rc.getLocation().add(roam);
                //Change directions if at the end of the map
                if (rc.isReady() && (!rc.onTheMap(nextLoc)
                        || (rc.canSenseLocation(nextLoc)) && rc.senseRobotAtLocation(nextLoc) != null)) {
                    roam = roam.opposite();
                }

                if (rc.canMove(roam)) {
                    rc.move(roam);
                } else {
                    return; //don't move in random dir
                }
            }

            boolean aroundSlanderer = false;

            //if sense our slanderer, stop
//            RobotInfo[] ourSlanderer = rc.senseNearbyRobots(sensingRadius, myTeam);
//            if (ourSlanderer.length != 0) {
//                for (RobotInfo bot : ourSlanderer) {
//                    if (bot.type == RobotType.SLANDERER) {
//                        aroundSlanderer = true;
//                        break;
//                    }
//                }
//            }

            //if not, go there
//            if (!aroundSlanderer) {
//                if (rc.getLocation().distanceSquaredTo(slandererEdge) > RobotType.POLITICIAN.sensorRadiusSquared - 4 && roam == null) {
//                    nav.bugTwo(slandererEdge);
//                } else {
//                    //slandererWall = findWallDirection(slandererEdge);
//                    slandererWall = rc.getLocation().directionTo(slandererEdge);
//                    // move perpendicular to wall to defend slanderers
//                    if (roam == null) {
//                        roam = slandererWall.rotateLeft().rotateLeft();
//                    }
//                    MapLocation myCurLoc = rc.getLocation();
//                    if (rc.canMove(roam) && rc.canMove(myCurLoc.directionTo(myCurLoc.add(roam)))) {
////                    System.out.println("now moving " + roam);
//                        //attack();
//                        nav.bugTwo(roamTarget(slandererEdge, roam));
//                    } else {
//                        System.out.println("change direction to " + roam.opposite());
//                        roam = roam.opposite();
//                    }
//
//                }
//            }
        }
        /*else {
            System.out.println(targetEnemyECLoc);
            // Goto enemy Enlightenment Center if receives attack signal
            if (targetEnemyECLoc != null) {
                tryMove(rc.getLocation().directionTo(targetEnemyECLoc));
            }
            attack();
        }*/

        tryMove(Util.randomDirection());
    }

    private void letAllyOut(int sensingRadius) throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(sensingRadius, myTeam);
        for (RobotInfo ally : nearbyAllies) {
            if (rc.canGetFlag(ally.ID)) {
                Message allyFlag = comms.decodeFlag(rc.getFlag(ally.ID));
                if (allyFlag.id == Constants.STUCK && rc.getLocation().isAdjacentTo(allyFlag.location)) {
                    for (int i = 0; i < 8; i++) {
                        if (rc.canMove(directions[i])) {
                            rc.move(directions[i]);
                        }
                    }
                }
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
                        if (msg.id == Constants.FOUND_EC) {
                            return msg.location;
                        }
                    }
                }
            }
        }
        return null;
    }

    public MapLocation scanForEdge() throws GameActionException {
        RobotInfo[] friendlyBots = rc.senseNearbyRobots(-1, myTeam);
        int flag = 0;
        Message msg = null;

        if (enlightenmentCenterSpawnedFromMessage != null &&
                enlightenmentCenterSpawnedFromMessage.id == Constants.EDGE) {
            return enlightenmentCenterSpawnedFromMessage.location;
        }

        if (friendlyBots != null) {
            for (int i = 0; i < friendlyBots.length; i++) {
                if (rc.canGetFlag(friendlyBots[i].getID())) {
                    flag = rc.getFlag(friendlyBots[i].getID());
                    if (flag != 0) {
                        msg = comms.decodeFlag(flag);
                        if (msg!=null && msg.id == Constants.EDGE) {
                            return msg.location;
                        }
                    }

                }
            }
        }
        return null;
    }

    public Direction findWallDirection(MapLocation edgeLocation) throws GameActionException {
        if (rc.canSenseLocation(edgeLocation)) {
            if (!rc.onTheMap(new MapLocation(edgeLocation.x, edgeLocation.y + 1))) {
                return Direction.NORTH;
            }
            if (!rc.onTheMap(new MapLocation(edgeLocation.x + 1, edgeLocation.y))) {
                return Direction.EAST;
            }
            if (!rc.onTheMap(new MapLocation(edgeLocation.x, edgeLocation.y - 1))) {
                return Direction.SOUTH;
            }
            return Direction.WEST;
        }
        return null;

    }

    public MapLocation roamTarget(MapLocation edgeLoc, Direction roam) throws GameActionException {
        if (roam == Direction.NORTH) {
            return new MapLocation(edgeLoc.x, edgeLoc.y + 64);
        }
        if (roam == Direction.SOUTH) {
            return new MapLocation(edgeLoc.x, edgeLoc.y - 64);
        }
        if (roam == Direction.EAST) {
            return new MapLocation(edgeLoc.x + 64, edgeLoc.y);
        }
        if (roam == Direction.WEST) {
            return new MapLocation(edgeLoc.x - 64, edgeLoc.y);
        }
        return null;
    }

    //Find the direction from politician to edge
    public Direction getEdgeDirection(MapLocation slandererLoc) throws GameActionException {
        for (int i = 0; i < 7; i += 2) {
            MapLocation senseLoc = slandererLoc.add(directions[i]);
            //Check if out of the map to know if slanderer is on the edge.
            if (slandererEdge!=null && senseLoc.isWithinDistanceSquared(rc.getLocation(), RobotType.POLITICIAN.sensorRadiusSquared) && !rc.canSenseLocation(senseLoc)
                    && (slandererLoc.x == slandererEdge.x || slandererLoc.y == slandererEdge.y)) {
                return directions[i];
            }
        }

        return Direction.CENTER; //Slanderer isn't on the edge yet
    }

    public boolean onDefenseLine() {
        if (slandererWallDir == directions[0] && rc.getLocation().y == slandererEdge.y - DISTANCE_TO_EDGE) {
            return true;
        } else if (slandererWallDir == directions[4] && rc.getLocation().y == slandererEdge.y + DISTANCE_TO_EDGE) {
            return true;
        } else if (slandererWallDir == directions[2] && rc.getLocation().x == slandererEdge.x - DISTANCE_TO_EDGE) {
            return true;
        } else if (slandererWallDir == directions[6] && rc.getLocation().x == slandererEdge.x + DISTANCE_TO_EDGE) {
            return true;
        }

        return false;
    }

}
