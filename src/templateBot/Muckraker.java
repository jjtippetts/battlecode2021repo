package templateBot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Muckraker extends Unit{
    RobotController rc;

    public Muckraker(RobotController rc){
        super(rc);
        this.rc = rc;
    }

    public void run() throws GameActionException {
        super.run();

        tryExpose();
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
}
