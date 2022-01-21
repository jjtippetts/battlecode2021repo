package templateBot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Politician extends Unit{
    RobotController rc;

    public Politician(RobotController rc){
        super(rc);
        this.rc = rc;
    }

    public void run() throws GameActionException {
        super.run();
    }
}
