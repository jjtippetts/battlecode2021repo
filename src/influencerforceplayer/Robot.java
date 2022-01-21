package influencerforceplayer;
import battlecode.common.*;

public class Robot {
    RobotController rc;
    static int turnCount = 0;
    int myID;
    Team myTeam;
    Team enemyTeam;
    MapLocation myCurrentLoc;
    Communication comms;
    Unit unit;
    RobotInfo[] nearbyRobots;
    int myInfluence;

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    public Robot(RobotController rc) {
        this.rc = rc;
        this.myTeam = rc.getTeam();
        this.enemyTeam = rc.getTeam().opponent();
        this.myCurrentLoc = rc.getLocation();
        this.myID=rc.getID();
        comms = new Communication(rc);
    }

    public void run() throws GameActionException {
        turnCount += 1;
        nearbyRobots = rc.senseNearbyRobots();
        this.myInfluence = rc.getInfluence();
        this.myCurrentLoc = rc.getLocation();
    }

    /* get enlightenment center location, can specify team/enemy in parameter*/
    public MapLocation getEnlightenmentCenterLoc(Team team) {
        RobotInfo[] bots = rc.senseNearbyRobots();
        int numBots = bots.length;
        if (numBots > 0) {
            for (int i = 0; i < numBots; i++) {
                if (bots[i].getType() == RobotType.ENLIGHTENMENT_CENTER && bots[i].getTeam() == team) {
                    return bots[i].getLocation();
                }
            }
        }
        return null;
    }

    public int getEnlightenmentCenterID(Team team) {
        RobotInfo[] bots = rc.senseNearbyRobots();
        int numBots = bots.length;
        if (numBots > 0) {
            for (int i = 0; i < numBots; i++) {
                if (bots[i].getType() == RobotType.ENLIGHTENMENT_CENTER && bots[i].getTeam() == team) {
                    return bots[i].ID;
                }
            }
        }
        return 0;
    }
    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir) throws GameActionException {
        //System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }
}
