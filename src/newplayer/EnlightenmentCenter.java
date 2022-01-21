package newplayer;

import battlecode.common.*;
import scala.collection.immutable.Stream;

import java.util.*;


public class EnlightenmentCenter extends Robot {
    int numPoliticians = 0;
    int numScouts = 0;
    int numSlanderers = 0;
    int numMuckrakers = 0;
    int myID = rc.getID();
    boolean tryToSenseMapEdge = false;
    ArrayList<Integer> createdRobots = new ArrayList<>();
    //ArrayList<Integer> createdScouts = new ArrayList<>();
    Set<RobotInfo> createdPoliticians = new HashSet<>();
    Set<RobotInfo> createdSlanderers = new HashSet<>();
    Set<RobotInfo> createdScouts = new HashSet<>();
    Set<RobotInfo> createdMuckrakers = new HashSet<>();
    Direction slandererWallDir = Direction.CENTER;
    MapLocation whereIAm = rc.getLocation();
    int directionIndex = 0;
    static int[] messages;
    static MapLocation closestEdge;
    static Set<edgeMapLocation> edgeMapLocations;
    static boolean enemyECTrapped = false;
    static MapLocation targetEnemyEC;

    class edgeMapLocation implements Comparable<edgeMapLocation> {
        MapLocation ecLoc;
        MapLocation edgeLoc;

        public edgeMapLocation(MapLocation ecLoc, MapLocation edgeLoc) {
            this.ecLoc = ecLoc;
            this.edgeLoc = edgeLoc;
        }

        @Override
        public int compareTo(edgeMapLocation o) {
            return ecLoc.distanceSquaredTo(edgeLoc) < o.ecLoc.distanceSquaredTo(o.edgeLoc) ? -1 : 1;
        }
    }

    public EnlightenmentCenter(RobotController rc) {
        super(rc);
        messages = new int[2];
        edgeMapLocations = new TreeSet<>();
    }

    public void run() throws GameActionException {
        super.run();

        spawnRobot();

        // Set Flag to zero on first round, why?
        if (rc.getRoundNum() == 1) {
            comms.setFlag(7, whereIAm);
        }

        //if never tried find map edge with its own sense radius, try once
        if (!tryToSenseMapEdge) {
            senseMapEdge();
        }

        // Check flags of created units
        checkRobots();

        bid();

        //TODO Hide slanderers in wall instead of edge
        switchEdge();
    }

    private void switchEdge() {
        if (edgeMapLocations.size() > 1) {
            //Switch edges every 1000 rounds
            if (rc.getRoundNum() > 400 || rc.getRoundNum() > 800) {
                Iterator<edgeMapLocation> it = edgeMapLocations.iterator();
                it.next(); //move ahead one more time for 2nd closest edge

                messages[1] = comms.getFlag(Constants.EDGE, it.next().edgeLoc);
            } else {
                Iterator<edgeMapLocation> it = edgeMapLocations.iterator();

                messages[1] = comms.getFlag(Constants.EDGE, it.next().edgeLoc);
            }
        }
    }

    boolean tryBuildInEveryDirection (RobotType robotType, int cost) throws GameActionException {
        for (Direction dir : directions) {
            if (rc.canBuildRobot(robotType, dir, cost)) {
                rc.buildRobot(robotType, dir, cost);
                return true;
            }
        }
        return false;
    }
    public void spawnRobot() throws GameActionException {
        //before ec found, spawn 8 scouts, 4 slanderers, 2 politicians
        //after ec found, spawn 4 slanderers, 4 politicians, 4 muckrakers


        if (turnCount == 1) {
            tryBuildInEveryDirection(RobotType.SLANDERER, rc.getInfluence());
        }

        if (numScouts < 8) {
            if (rc.canBuildRobot(RobotType.MUCKRAKER, directions[directionIndex % 8], Constants.SCOUT_COST)) {
                buildRobot(RobotType.MUCKRAKER, directions[directionIndex % 8], Constants.SCOUT_COST);
                ++numScouts;
                ++directionIndex;
            }
        }

        int SLANDERER_COST = rc.getInfluence() / 2;
        final int MUCKRAKER_COST = 1;
        final int POLITICIAN_COST = 20; // These just protect muckrakers. Their target is most likely swarms of 1 hp mucks.
        final int EXTRA_SLANDERERS = 5;
        /*if (numSlanderers < EXTRA_SLANDERERS) {
            buildRobot(RobotType.SLANDERER, randomDirection(), SLANDERER_COST);
            ++numSlanderers;
        }*/

        double muckRand = Math.random();
        //int default value is 0. If message[0] != 0 - attack signal is present. Periodically send muckrakers to attack.
        if (messages[0] != 0 && muckRand < 0.5) {
            buildRobot(RobotType.MUCKRAKER, randomDirection(), MUCKRAKER_COST);
            ++numMuckrakers;
            //return
        }

        double rand = Math.random();

        if (rand < 0.9) {
            buildRobot(RobotType.POLITICIAN, randomDirection(), POLITICIAN_COST);
            ++numPoliticians;
        }
//        else {
//            if (enemyECTrapped) {
//                SLANDERER_COST = rc.getInfluence() > 1000 ? 949 : 41;
//            }
//            buildRobot(RobotType.SLANDERER, randomDirection(), SLANDERER_COST);
//            ++numSlanderers;
////        } else {
////            buildRobot(RobotType.MUCKRAKER, randomDirection(), MUCKRAKER_COST);
////            ++numMuckrakers;
//
//            /*for (Direction dir : Util.directions) {
//                if (rc.canBuildRobot(toBuild, dir, influence)) {
//                    createdRobots.add(buildRobot(toBuild, dir, 1));
//                } else {
//                    break;
//                }
//            }*/
//
//        }

//        comms.setFlag(Constants.ATTACK, new MapLocation(10005, 23926));
    }

    // Builds a robot and returns its id
    public void buildRobot(RobotType robotType, Direction dir, int cost) throws GameActionException {
        RobotInfo createdRobot = null;

        for (int i = 0; i < 8; i++) {
            if (rc.canBuildRobot(robotType, dir, cost)) {
                rc.buildRobot(robotType, dir, cost);
                createdRobot = rc.senseRobotAtLocation(rc.getLocation().add(dir));
                break;
            }
            dir = dir.rotateLeft();
        }

        if (createdRobot == null) {
//            System.out.println("COULDN'T BUILD ROBOT!"); // EC is possibly surrounded. Do sth like
            // calling politicians to save ec instead of attacking
            return;
        }

        if (createdRobot.type == RobotType.SLANDERER) {
            createdSlanderers.add(createdRobot);
        } else if (createdRobot.type == RobotType.POLITICIAN) {
            createdPoliticians.add(createdRobot);
        } else if (createdRobot.type == RobotType.MUCKRAKER && createdRobot.influence == Constants.SCOUT_COST) {
            createdScouts.add(createdRobot);
        } else {
            createdMuckrakers.add(createdRobot);
        }
    }

    // Checks the flags of created robots
    public void checkRobots() throws GameActionException {
        for (RobotInfo bot : createdScouts) {
            if (rc.canGetFlag(bot.ID)) {
                int scoutFlag = rc.getFlag(bot.ID);
                Message decodedFlag = comms.decodeFlag(scoutFlag);
                // Check if flag has been set
                if (decodedFlag != null && decodedFlag.id != Constants.NOTHING) {
                    MapLocation decodedLocation = decodedFlag.location;
                    int messageIndex = 0;
                    if (decodedFlag.id == Constants.EDGE) {
                        if (closestEdge == null) {
                            closestEdge = decodedLocation;
                        } else if (myCurrentLoc.distanceSquaredTo(decodedLocation) < myCurrentLoc.distanceSquaredTo(closestEdge)) {
                            closestEdge = decodedLocation;
                        }
                        messageIndex = Constants.EDGE - 1;
                        edgeMapLocations.add(new edgeMapLocation(myCurrentLoc, decodedLocation));
                        comms.setFlag(decodedFlag.id, decodedLocation);
                        messages[messageIndex] = comms.getFlag(decodedFlag.id, closestEdge);
                    } else if (decodedFlag.id == Constants.FOUND_ENEMY_EC) {
                        messageIndex = Constants.FOUND_ENEMY_EC - 1;
                        //TODO maybe a better way to not switch targeting enemy EC?
                        if (messages[messageIndex] == 0) {
                            messages[messageIndex] = comms.getFlag(decodedFlag.id, decodedLocation);
                            targetEnemyEC = decodedLocation;
                        }
                        comms.setFlag(decodedFlag.id, targetEnemyEC);
                    }

                    //After all 8 edges are discovered, we'll be looping through a size 2 array
                    //containing target enemy EC and closest edge to update the newly built robots.

                } else {
                    //Set flag for enemy ec and closest edge in repeat if no new flag from scouts.
                    if (messages.length > 0) {
                        Message curFlag = comms.decodeFlag(messages[turnCount % 2]);
                        if (curFlag != null) {
                            comms.setFlag(curFlag.id, curFlag.location);
                        }
                    }
//                    if (messages.length == 2) {
//                        System.out.println("m1 " + messages[0]);
//                        System.out.println("m2 " + messages[1]);
//                    }
                }
            }
        }

//        checkIfEnemyECIsSurrounded();

        /*ArrayList<Integer> robotsToRemove = new ArrayList<>();

        for(int i = 0; i < createdRobots.size(); ++i){
            // Robot is alive
            if(rc.canGetFlag(createdRobots.get(i))){
                int flag = rc.getFlag(createdRobots.get(i));
                Message decodedMessage = comms.decodeFlag(flag);

                // If one of our messages print message
                if(decodedMessage != null){
                    System.out.println("Robot ID: " + createdRobots.get(i) + ", " + decodedMessage.toString());
                }
            }

            // Robot died, add list to remove
            else {
                System.out.println("Robot id: " + createdRobots.get(i) + " died");
                robotsToRemove.add(createdRobots.get(i));
            }
        }

        // Remove dead robots
        createdRobots.removeAll(robotsToRemove);*/
    }

    private void checkIfEnemyECIsSurrounded() throws GameActionException {
        for (RobotInfo bot : createdMuckrakers) {
            if (rc.canGetFlag(bot.ID)) {
                int scoutFlag = rc.getFlag(bot.ID);
                Message decodedFlag = comms.decodeFlag(scoutFlag);
                if (decodedFlag != null && decodedFlag.id == Constants.ENEMY_EC_TRAPPED) {
                    enemyECTrapped = true;
                }
            } else {
                //remove mucks from createdMuckrackers?
            }
        }
    }

    public void senseMapEdge() throws GameActionException {
        //6^2=36 closest to enlightment sense radius squared 40
        whereIAm = rc.getLocation();
        //build wall on the WEST
        if (!rc.onTheMap(new MapLocation(whereIAm.x - 6, whereIAm.y))) {
            slandererWallDir = Direction.WEST;
            comms.setFlag(2, new MapLocation(whereIAm.x - 6, whereIAm.y));
            if (rc.canGetFlag(myID)) {
                int myFlag = rc.getFlag(myID);
                Message decodedFlag = comms.decodeFlag(myFlag);
                if (decodedFlag.id == 2) {
                    tryToSenseMapEdge = true;
                }
            }
        }
        //build wall on the EAST
        if (slandererWallDir == Direction.CENTER && !rc.onTheMap(new MapLocation(whereIAm.x + 6, whereIAm.y))) {
            slandererWallDir = Direction.EAST;
            comms.setFlag(2, new MapLocation(whereIAm.x + 6, whereIAm.y));
            if (rc.canGetFlag(myID)) {
                int myFlag = rc.getFlag(myID);
                Message decodedFlag = comms.decodeFlag(myFlag);
                if (decodedFlag.id == 2) {
                    tryToSenseMapEdge = true;
                }
            }
        }
        //build wall on the SOUTH
        if (slandererWallDir == Direction.CENTER && !rc.onTheMap(new MapLocation(whereIAm.x, whereIAm.y - 6))) {
            slandererWallDir = Direction.SOUTH;
            comms.setFlag(2, new MapLocation(whereIAm.x, whereIAm.y - 6));
            if (rc.canGetFlag(myID)) {
                int myFlag = rc.getFlag(myID);
                Message decodedFlag = comms.decodeFlag(myFlag);
                if (decodedFlag.id == 2) {
                    tryToSenseMapEdge = true;
                }
            }
        }
        //build wall on the NORTH
        if (slandererWallDir == Direction.CENTER && !rc.onTheMap(new MapLocation(whereIAm.x, whereIAm.y + 6))) {
            slandererWallDir = Direction.NORTH;
            comms.setFlag(2, new MapLocation(whereIAm.x, whereIAm.y + 6));
            if (rc.canGetFlag(myID)) {
                int myFlag = rc.getFlag(myID);
                Message decodedFlag = comms.decodeFlag(myFlag);
                if (decodedFlag.id == 2) {
                    tryToSenseMapEdge = true;
                }
            }
        }
    }

    void bid() throws GameActionException {
        //TODO better bidding
        double percentage = 0.05;
        double bidAmount = myInfluence * percentage;
        if (rc.getRoundNum() < 50) {
            bidAmount = 2;
        }
        if (rc.canBid((int) bidAmount)) {
            rc.bid((int) bidAmount);
            System.out.println("BID " + (int) bidAmount);
        }
    }
}
