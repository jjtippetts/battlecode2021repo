package testPlayer;

import battlecode.common.*;

public class Muckraker extends Unit {
    RobotController rc;

    public Muckraker(RobotController rc){
        super(rc);
        this.rc = rc;
    }

    public void run() throws GameActionException {
        super.run();

        senseMuckraker();

        tryExpose();

        MapLocation enemySlanderer = null;
        for(int i = 0; i < nearbyRobots.length; ++i){
            if(nearbyRobots[i].team == enemyTeam){
                if(nearbyRobots[i].type == RobotType.SLANDERER){
                    enemySlanderer = nearbyRobots[i].location;
                    break;
                }
            }
        }

        if(enemySlanderer != null){
            System.out.println("Found enemy slanderer");
            nav.tryMove(myCurrentLoc.directionTo(enemySlanderer));
        }

		System.out.println("Moving towards: " + myCurrentLoc.directionTo(baseEnlightenmentCenterLocation).opposite());
        nav.tryMoveAny(myCurrentLoc.directionTo(baseEnlightenmentCenterLocation).opposite());
    }

    //Searches for enemy slanderers to expose and exposes them if found
    public boolean tryExpose() throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemyTeam)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return true;
                }
            }
        }
        return true;
    }

    public void senseMuckraker() throws GameActionException {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, enemyTeam);
        MapLocation loc = null;
        for(int i = 0; i < nearbyRobots.length; ++i){
            loc = nearbyRobots[i].location;
            break;
        }
        if(loc != null){
            comms.setFlag(2, loc);
        } else {
            rc.setFlag(0);
        }
    }
}
