package newplayer;

import battlecode.common.*;

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
    static int left = 4;
    static int right = 4;
    static int top = 4;
    static int bottom = 4;

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

        checkIfNeedToExpand();

        if (!onDefenseLine(myCurrentLoc, distanceToEC)) {
            tryMove(myCurrentLoc.directionTo(enlightenmentCenterSpawnedFromLoc).opposite());
        } else {
            //Get roam direction
            if (roam == Direction.CENTER) {
                roam = getRoamDirection(myCurrentLoc.directionTo(enlightenmentCenterSpawnedFromLoc));
            }

            //If your next position is not on the defense line, turn right
            if (!onDefenseLine(myCurrentLoc.add(roam), distanceToEC)) {
                roam = roam.rotateRight().rotateRight();
            }

            //If can't move, increment stuck count
            if (!tryMove(roam)) {
                stuckCount++;
            } else {
                stuckCount = 0;
            }

            //If stuck, send signal to expand.
            if (stuckCount >= 20) {
                signalExpand();
                stuckCount = 0;
            }
        }
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
                        if (msg.id == Constants.FOUND_ENEMY_EC) {
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
                        if (msg != null && msg.id == Constants.EDGE) {
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
            if (slandererEdge != null && senseLoc.isWithinDistanceSquared(rc.getLocation(), RobotType.POLITICIAN.sensorRadiusSquared) && !rc.canSenseLocation(senseLoc)
                    && (slandererLoc.x == slandererEdge.x || slandererLoc.y == slandererEdge.y)) {
                return directions[i];
            }
        }

        return Direction.CENTER; //Slanderer isn't on the edge yet
    }

    public boolean onDefenseLine(MapLocation myLoc, int distance) {
        boolean onTopLine = (myLoc.x == enlightenmentCenterSpawnedFromLoc.x + distance
                && Math.abs(myLoc.y - enlightenmentCenterSpawnedFromLoc.y) <= distance);
        boolean onBottomLine = (myLoc.x == enlightenmentCenterSpawnedFromLoc.x - distance
                && Math.abs(myLoc.y - enlightenmentCenterSpawnedFromLoc.y) <= distance);
        boolean onLeftLine = (myLoc.y == enlightenmentCenterSpawnedFromLoc.y - distance
                && Math.abs(myLoc.x - enlightenmentCenterSpawnedFromLoc.x) <= distance);
        boolean onRightLine = (myLoc.y == enlightenmentCenterSpawnedFromLoc.y + distance
                && Math.abs(myLoc.x - enlightenmentCenterSpawnedFromLoc.x) <= distance);
        if (onBottomLine || onLeftLine || onRightLine || onTopLine) {
            return true;
        }

        return false;
    }

    Direction getRoamDirection(Direction dirToEC) {
        if (dirToEC == Util.directions[0]
                || dirToEC == Util.directions[2] || dirToEC == Util.directions[4] || dirToEC == Util.directions[6]) {
            return dirToEC.rotateLeft().rotateLeft();
        } else {
            return dirToEC.rotateLeft();
        }
    }

    void checkIfNeedToExpand() throws GameActionException {
        RobotInfo[] nearbyRobts = rc.senseNearbyRobots(RobotType.POLITICIAN.sensorRadiusSquared, myTeam);

        //If next to the border it's bound to be stuck, shouldn't report need to expand
        for (Direction dir : directions) {
            if (!rc.onTheMap(myCurrentLoc.add(dir))) {
                return;
            }
        }

        //Check if any nearby politicians on the same wall reported to expand. If yes, relay to adjacent politicians.
        for (RobotInfo bot : nearbyRobts) {
            if (bot.type == RobotType.POLITICIAN && rc.canGetFlag(bot.ID)) {
                Message decodedMessage = comms.decodeFlag(rc.getFlag(bot.ID));
                if (decodedMessage != null && decodedMessage.id == Constants.INCREASE_DISTANCE) {
                    int newDistance = decodedMessage.other;
                    if (newDistance > 0 && newDistance == distanceToEC + 1 && bot.location.isWithinDistanceSquared(myCurrentLoc, 4)) {
                        distanceToEC = decodedMessage.other;
                        stuckCount = 0;
                        comms.setFlag(Constants.INCREASE_DISTANCE, rc.getLocation(), distanceToEC);
                    }
                }
            }
        }

    }

    void signalExpand() throws GameActionException {
        comms.setFlag(Constants.INCREASE_DISTANCE, rc.getLocation(), distanceToEC + 1);
    }

    void signalEveryoneOnSameWallSomeoneMoved() throws GameActionException {
        comms.setFlag(Constants.SOMEONE_MOVED, rc.getLocation(), distanceToEC);
    }

    boolean checkIfSomeoneOnSameWallMoved(RobotInfo[] nearbyRobots) throws GameActionException {
        for (RobotInfo bot : nearbyRobots) {
            if (bot.type == RobotType.POLITICIAN && rc.canGetFlag(bot.ID)) {
                Message decodedMessage = comms.decodeFlag(rc.getFlag(bot.ID));
                if (decodedMessage != null && decodedMessage.id == Constants.SOMEONE_MOVED) {
                    int distance = decodedMessage.other;
                    if (distance == distanceToEC) {
                        System.out.println("WTF");
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
