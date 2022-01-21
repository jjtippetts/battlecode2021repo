package newplayer;

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
    static int expandCounter = 10;
    static int campAtEnemyECCounter = 0;
    static boolean expand = false;

    public Muckraker(RobotController rc) {
        super(rc);
        myEnlightenmentCenter = getEnlightenmentCenterLoc(rc.getTeam());
        System.out.println("My Enlightenment Center at " + myEnlightenmentCenter);
    }

    public void run() throws GameActionException {
        super.run();
        myCurrentLoc = rc.getLocation();

        //see if I am a scout
        if (!amIScout && rc.getInfluence() == Constants.SCOUT_COST) {
            amIScout = true;
            Direction myECDir = myCurrentLoc.directionTo(myEnlightenmentCenter);
            scoutMovingDir = myCurrentLoc.directionTo(myCurrentLoc.subtract(myECDir));
        }

        RobotInfo[] bots = rc.senseNearbyRobots();

        //sense robots then kill slanderers or set flag for finding ec
        senseRobots(bots);

        //Only true if muck is adjacent to enemy HQ, and we don't want them to move.
//        if (checkIfEnemyECIsSurrounded()) return;

        if (!amIScout && targetEnemyECLoc != null) {
            Direction directionToEnemyEC = myCurrentLoc.directionTo(targetEnemyECLoc);
            if (myCurrentLoc.isAdjacentTo(targetEnemyECLoc)) {
                return;
            }
            if (!expand && rc.canMove(directionToEnemyEC)) {
//                tryMove(rc.getLocation().directionTo(targetEnemyECLoc));
                nav.bugTwo(targetEnemyECLoc);
            } else {
                Direction oppositeDirectionToEnemyLoc = directionToEnemyEC.opposite();
                Direction directionToEC = myCurrentLoc.directionTo(enlightenmentCenterSpawnedFromLoc);
                //If your on the side facing your own EC, move to the other side of enemy HC during expansion.
                if (oppositeDirectionToEnemyLoc == directionToEC || oppositeDirectionToEnemyLoc == directionToEC.rotateLeft()
                || oppositeDirectionToEnemyLoc == directionToEC.rotateRight()) {
                    tryMove(targetEnemyECLoc.directionTo(enlightenmentCenterSpawnedFromLoc).opposite());
                }
                //Expand - Try moving away from enemy ec for 15 rounds. we can play with these numbers
                if (expandCounter-- > 15) {
                    tryMove(oppositeDirectionToEnemyLoc);
                } else if (expandCounter == 0) {
                  expand = false;
                } else {
                    tryMove(randomDirection());
                }
            }

            //After 200 rounds, expand for 20 rounds every 60 rounds. we can play with these numbers
            if (rc.getRoundNum() > 200 && rc.getRoundNum() % 60 == 0) {
                expand = true;
                expandCounter = 20;
            }
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


//    public void returnHome() throws GameActionException {
//        myCurrentLoc = rc.getLocation();
////        if ()
//        if (tryMove(rc.getLocation().directionTo(myEnlightenmentCenter))){
//            System.out.println("returning home");
//        }
//    }

    public void senseRobots(RobotInfo[] bots) throws GameActionException {
        if (bots != null) {
            for (RobotInfo bot : bots) {
                int botDistance = bot.location.distanceSquaredTo(myCurrentLoc);
                //if sensed slanderers and within action radius then just kill it
                if (bot.team == enemyTeam && bot.type == RobotType.SLANDERER &&
                        botDistance <= RobotType.MUCKRAKER.actionRadiusSquared) {
                    if (bot.type.canBeExposed() && rc.canExpose(bot.location)) {
                        System.out.println("e x p o s e d");
                        rc.expose(bot.location);
                    }
                }
                //if sensed ec, set flag message 1
                else if (amIScout && bot.type == RobotType.ENLIGHTENMENT_CENTER && bot.team == enemyTeam && !settedEnemyECFlag) {
                    if (bot.team == enemyTeam) {
                        scoutFlag = comms.setFlag(Constants.FOUND_ENEMY_EC, bot.location);
                        System.out.println("Found enemy EC at " + bot.location);
                        if (scoutFlag != -1) {
                            settedEnemyECFlag = true;
                        }
                    }
                    /*else {
                        scoutFlag = comms.setFlag(Constants.FOUND_NEURAL_EC, bot.location);
                        System.out.println("Found neural EC at " + bot.location);
                        if (scoutFlag != -1) {
                            settedNeuralECFlag = true;
                        }
                    }*/

                }
                //If couldn't expose, not near enemy ec, then chase the slanderer.
//                else if (!amIScout && bot.team == enemyTeam && bot.type == RobotType.SLANDERER) {
//                    nav.bugTwo(bot.getLocation());
//                }
            }
        }
    }
}
