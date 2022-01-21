package templateBot;

import battlecode.common.*;

public class Unit extends Robot{
    RobotController rc;
    Navigation nav;
    int baseEnlightenmentCenterId = 0;
    int baseEnlightenmentCenterFlag = 0;
    MapLocation baseEnlightenmentCenterLocation = null;

    public Unit(RobotController rc){
        super(rc);
        this.rc = rc;
        nav = new Navigation(rc);
    }

    public void run() throws GameActionException {
        super.run();

        // Get the location and ID of the Enlightenment Center the unit spawned from
        if (baseEnlightenmentCenterId == 0) {
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

            for (int i = 0; i < nearbyRobots.length; ++i) {
                if (nearbyRobots[i].type == RobotType.ENLIGHTENMENT_CENTER) {
                    RobotInfo enlightenmentCenter = nearbyRobots[i];
                    baseEnlightenmentCenterId = enlightenmentCenter.ID;
                    baseEnlightenmentCenterLocation = enlightenmentCenter.location;
                    System.out.println("Got flag of base location and id of base enlightenment center!");
                }
            }
        }

        // Get enlightenment center Message
        Message enlightenmentCenterMessage = comms.getMessage(baseEnlightenmentCenterId);
        if(enlightenmentCenterMessage != null){
            System.out.println(enlightenmentCenterMessage.toString());
        }

        nav.tryMove(Util.randomDirection());
    }
}
