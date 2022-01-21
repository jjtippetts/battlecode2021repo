package templateBot;

import battlecode.common.*;

public abstract class Robot {

    RobotController rc;
    Team myTeam;
    Team enemyTeam;
    MapLocation myCurrentLoc;
    int myID;
    Communication comms;
    int turnCount = 0;
    int myInfluence = 0;
    RobotInfo[] nearbyRobots;

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
}
