package influencerforceplayer;

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

    public Muckraker(RobotController rc) {
        super(rc);
        myEnlightenmentCenter = getEnlightenmentCenterLoc(rc.getTeam());
        System.out.println("My Enlightenment Center at " + myEnlightenmentCenter);
    }

    public void run() throws GameActionException {
        super.run();
        myCurrentLoc = rc.getLocation();

        //see if I am a scout
        if (!amIScout && rc.getInfluence() == 1) {
            amIScout = true;
            Direction myECDir = myCurrentLoc.directionTo(myEnlightenmentCenter);
            scoutMovingDir = myCurrentLoc.directionTo(myCurrentLoc.subtract(myECDir));
        }


        if (!amIScout && targetEnemyECLoc != null) {
            if (rc.canMove(rc.getLocation().directionTo(targetEnemyECLoc))) {
                rc.move(rc.getLocation().directionTo(targetEnemyECLoc));
            }
        }
        //sense robots then kill slanderers or set flag for finding ec
        senseRobots();

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

    public void senseRobots() throws GameActionException {
        RobotInfo[] bots = rc.senseNearbyRobots();
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
                else if (bot.type == RobotType.ENLIGHTENMENT_CENTER && bot.team != myTeam && !settedEnemyECFlag) {
                    scoutFlag = comms.setFlag(1, bot.location);
                    System.out.println("Found enemy EC at " + bot.location);
                    if (scoutFlag != -1) {
                        settedEnemyECFlag = true;
                    }
                }
                //If couldn't expose, not near enemy ec, then chase the slanderer.
                else if (!amIScout && bot.team == enemyTeam && bot.type == RobotType.SLANDERER) {
                    nav.bugTwo(bot.getLocation());
                }
            }
        }
    }
}
