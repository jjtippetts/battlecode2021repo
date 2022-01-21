package testPlayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Slanderer extends Unit {
    RobotController rc;

    public Slanderer(RobotController rc){
        super(rc);
        this.rc = rc;
    }

    public void run() throws GameActionException {
        super.run();

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, myTeam);
        for(int i = 0; i < nearbyRobots.length; ++i){
            if(nearbyRobots[i].type == RobotType.MUCKRAKER){
                Message message = comms.getMessage(nearbyRobots[i].ID);
                if(message != null && message.id == 2){
                    System.out.println("Found nearby Muckraker at: " + message.location);
                    nav.tryMove(myCurrentLoc.directionTo(message.location).opposite());
                }
            }
        }

        nav.tryMove(Util.randomDirection());
    }
}
