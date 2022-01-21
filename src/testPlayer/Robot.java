package testPlayer;

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
    int actionRadius = 0;
    RobotInfo[] nearbyRobots;

    public Robot(RobotController rc) {
        this.rc = rc;
        this.myTeam = rc.getTeam();
        this.enemyTeam = rc.getTeam().opponent();
        this.myCurrentLoc = rc.getLocation();
        this.myID=rc.getID();
        this.actionRadius = rc.getType().actionRadiusSquared;
        comms = new Communication(rc);
    }

    public void run() throws GameActionException {
        turnCount += 1;
        this.myInfluence = rc.getInfluence();
        this.myCurrentLoc = rc.getLocation();
        nearbyRobots = rc.senseNearbyRobots(myCurrentLoc,-1,null);
    }
}
