package newerplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Muckraker extends Unit {

    ArrayList<Direction> nwCorner = new ArrayList<>(Arrays.asList(
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH
    ));
    ArrayList<Direction> neCorner = new ArrayList<>(Arrays.asList(
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST
    ));
    ArrayList<Direction> seCorner = new ArrayList<>(Arrays.asList(
            Direction.NORTH,
            Direction.WEST,
            Direction.NORTHWEST
    ));
    ArrayList<Direction> swCorner = new ArrayList<>(Arrays.asList(
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST
    ));

    public MapLocation myEnlightenmentCenter;
    public MapLocation enemyEnlightenmentCenter;
    public MapLocation[] mapCorners = new MapLocation[4];
    public Direction scoutMovingDir;
    public boolean amIScout = false;
    public boolean foundEdge = false;
    public int scoutFlag = -1; //-1 for flag hasn't been accepted by ec, 0 for has been
    public boolean settedEnemyECFlag = false;
    public boolean settedNeuralECFlag = false;
    static int surroundCounter = 10;
    static int campAtEnemyECCounter = 0;
    static boolean surround = false;
    static Direction dirToTheOtherSideOfEnemyEC = null;
    static Direction startDir;

    public Muckraker(RobotController rc) {
        super(rc);
        myEnlightenmentCenter = getEnlightenmentCenterLoc(rc.getTeam());
        System.out.println("My Enlightenment Center at " + myEnlightenmentCenter);
    }

    public void run() throws GameActionException {
        super.run();
        myCurrentLoc = rc.getLocation();

        if (startDir == null) {
            startDir = myCurrentLoc.directionTo(enlightenmentCenterSpawnedFromLoc).opposite();
        }

        if (turnCount <= 3) {
            tryMove(myCurrentLoc.directionTo(enlightenmentCenterSpawnedFromLoc));
        }
        //see if I am a scout
        if (!amIScout && rc.getInfluence() == Constants.SCOUT_COST) {
            amIScout = true;
            Direction myECDir = myCurrentLoc.directionTo(myEnlightenmentCenter);
            scoutMovingDir = myCurrentLoc.directionTo(myCurrentLoc.subtract(myECDir));
        }

        RobotInfo[] bots = rc.senseNearbyRobots();

        //sense robots then kill slanderers or set flag for finding ec
        senseRobots(bots);

        //Roam around map and collapse on enemy EC every 60 turns for 30 turns.
        if (!amIScout) {
            if (targetEnemyECLoc != null) {
                if (myCurrentLoc.isAdjacentTo(targetEnemyECLoc)) {
                    return;
                }

                if (rc.getRoundNum() % 60 == 0) {
                    surround = true;
                    surroundCounter = 60;
                    dirToTheOtherSideOfEnemyEC = null;
                }

                Direction directionToEnemyEC = myCurrentLoc.directionTo(targetEnemyECLoc);
                Direction oppositeDirectionToEnemyLoc = directionToEnemyEC.opposite();
                Direction directionToEC = myCurrentLoc.directionTo(enlightenmentCenterSpawnedFromLoc);
                //If your on the side facing your own EC, move to the other side of enemy HC during expansion.
//                if (surround && dirToTheOtherSideOfEnemyEC == null
//                        && oppositeDirectionToEnemyLoc == directionToEC || oppositeDirectionToEnemyLoc == directionToEC.rotateLeft()
//                        || oppositeDirectionToEnemyLoc == directionToEC.rotateRight()) {
//                    dirToTheOtherSideOfEnemyEC = enlightenmentCenterSpawnedFromLoc.directionTo(targetEnemyECLoc);
//                }

                if (--surroundCounter >= 30) {
//                        tryMove(directionToEnemyEC);
                    if (myCurrentLoc.isWithinDistanceSquared(targetEnemyECLoc, 64)) {
                        nav.bugTwo(targetEnemyECLoc);
                    } else {
                        tryMove(directionToEnemyEC);
                    }
                } else if (--surroundCounter >= 0 && dirToTheOtherSideOfEnemyEC != null) {
//                    tryMove(dirToTheOtherSideOfEnemyEC);
//                    System.out.println(dirToTheOtherSideOfEnemyEC);
//                    return;
                }
            }

            roam();
        }

        //The flag set was used by enlightenment center, no need to send flag again
        if (scoutFlag == ecFlag) {
            scoutFlag = 0;
        }

        // Would set flag ever fail? If not we can move this into the if above;
        if (scoutFlag == 0) {
            comms.setFlag(Constants.NOTHING, rc.getLocation());
        }

        //scout move and check map edge
        if (amIScout && !foundEdge && scoutFlag == -1) {
            if (tryMove(scoutMovingDir)) {
            } else {
                //cant move to that direction, check if reached the edge
                MapLocation possibleMapEdge = myCurrentLoc.add(scoutMovingDir);
                if (!rc.onTheMap(possibleMapEdge)) {
                    scoutFlag = comms.setFlag(2, myCurrentLoc);
                    if (rc.canGetFlag(myID)) {
                        int myFlag = rc.getFlag(myID);
                        Message decodedFlag = comms.decodeFlag(myFlag);
                        if (decodedFlag.id == 2) {
                            foundEdge = true;
                        }
                    }
                }
                tryMove(randomDirection());
            }
        } else {
            //what else should scout do?
            tryMove(randomDirection());
        }
        //if all ec found, should we spawn muckraker with higher influence and go to ec too?
    }

    private void roam() throws GameActionException {
        int closestDistance = 1000;
        Direction dir = null;

        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(myCurrentLoc, -1, myTeam);
        for (RobotInfo bot : nearbyAllies) {
            if (bot.type == RobotType.MUCKRAKER) {
                int distance = bot.location.distanceSquaredTo(myCurrentLoc);
                boolean closerToEC =  bot.location.distanceSquaredTo(enlightenmentCenterSpawnedFromLoc)
                        < myCurrentLoc.distanceSquaredTo(enlightenmentCenterSpawnedFromLoc);
                if (distance < closestDistance && closerToEC) {
                    closestDistance = distance;
                    dir = bot.location.directionTo(myCurrentLoc);
                }
            }
        }

        if (dir != null) {
            tryMove(dir);
        } else {
            MapLocation nextLoc = myCurrentLoc.add(startDir);
            if (rc.canSenseLocation(nextLoc) && !rc.onTheMap(nextLoc)) {
                if (startDir == directions[1] || startDir == directions[2] || startDir == directions[3] || startDir == directions[4]) {
                    startDir = startDir.rotateLeft().rotateLeft();
                } else {
                    startDir = startDir.rotateRight().rotateRight();
                }
            }
            tryMove(startDir);
        }
    }

    private boolean checkIfEnemyECIsSurrounded() throws GameActionException {
        if (targetEnemyECLoc != null && rc.getLocation().isAdjacentTo(targetEnemyECLoc)) {
            campAtEnemyECCounter++;
            if (campAtEnemyECCounter >= 100) {
                System.out.println("Set flag");
                comms.setFlag(Constants.ENEMY_EC_TRAPPED, rc.getLocation());
            }
            return true;
        }
        return false;
    }

    public MapLocation findCorner(MapLocation currentLoc) throws GameActionException {

        ArrayList<Direction> dirsOnMap = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Direction dir = directions[i];
            if (rc.onTheMap(currentLoc.add(dir))) {
                dirsOnMap.add(dir);
            }
        }
        if (dirsOnMap.size() == 3) {
            if (dirsOnMap.equals(nwCorner)) {
                mapCorners[0] = currentLoc;
                System.out.println("NW Corner found");
                return currentLoc;
            }
            if (dirsOnMap.equals(neCorner)) {
                mapCorners[1] = currentLoc;
                System.out.println("NE Corner found");
                return currentLoc;
            }
            if (dirsOnMap.equals(seCorner)) {
                mapCorners[2] = currentLoc;
                System.out.println("SE Corner found");
                return currentLoc;
            }
            if (dirsOnMap.equals(swCorner)) {
                mapCorners[3] = currentLoc;
                System.out.println("SW Corner found");
                return currentLoc;
            }
        }
        return null;
    }

    public void senseRobots(RobotInfo[] bots) throws GameActionException {
        if (bots != null) {
            for (RobotInfo bot : bots) {
                int botDistance = bot.location.distanceSquaredTo(myCurrentLoc);
                //if sensed ec, set flag message 1
                if (amIScout && bot.type == RobotType.ENLIGHTENMENT_CENTER && bot.team == enemyTeam && !settedEnemyECFlag) {
                    scoutFlag = comms.setFlag(Constants.FOUND_ENEMY_EC, bot.location);
                    System.out.println("Found enemy EC at " + bot.location);
                    if (scoutFlag != -1) {
                        settedEnemyECFlag = true;
                    }
                    /*else {
                        scoutFlag = comms.setFlag(Constants.FOUND_NEURAL_EC, bot.location);
                        System.out.println("Found neural EC at " + bot.location);
                        if (scoutFlag != -1) {
                            settedNeuralECFlag = true;
                        }
                    }*/

                }
                //if sensed slanderers and within action radius then just kill it
                else if (bot.team == enemyTeam && bot.type == RobotType.SLANDERER &&
                        botDistance <= RobotType.MUCKRAKER.actionRadiusSquared) {
                    if (bot.type.canBeExposed() && rc.canExpose(bot.location)) {
                        System.out.println("e x p o s e d");
                        rc.expose(bot.location);
                    }
                }
                //If couldn't expose, not near enemy ec, then chase the slanderer.
//                else if (!amIScout && bot.team == enemyTeam && bot.type == RobotType.SLANDERER) {
//                    nav.bugTwo(bot.getLocation());
//                }
            }
        }
    }
}
