package templateBot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Slanderer extends Unit{
    RobotController rc;

    public Slanderer(RobotController rc){
        super(rc);
        this.rc = rc;
    }

    public void run() throws GameActionException {
        super.run();
    }
}
