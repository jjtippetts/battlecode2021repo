package pathfindingplayer;

import battlecode.common.*;

import java.util.ArrayList;


public class EnlightenmentCenter extends Robot{
    ArrayList<Integer> createdRobots = new ArrayList<>();
    int numScouts = 0;
    boolean built = false;
    public EnlightenmentCenter(RobotController rc) {
        super(rc);
    }


    public void run() throws GameActionException {
        super.run();

        if (rc.getRoundNum() > 1500) {
            rc.resign();
        }
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        if (built) {
            return;
        }
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(RobotType.MUCKRAKER, dir, influence);
                built = true;
            } else {
                break;
            }
        }
    }
}
