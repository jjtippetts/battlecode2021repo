package pathfindingplayer;

import battlecode.common.*;

public class Unit extends Robot {
    public Unit(RobotController rc){super(rc);}

    int flag = 0;
    MapLocation enlightenmentCenterSpawnedFromLoc = null;
    MapLocation myLoc =rc.getLocation();
    int myID=rc.getID();
    int enlightenmentCenterSpawnedFromId;
    int enlightenmentCenterSpawnedFromFlag=100000;
    Message enlightenmentCenterSpawnedFromMessage;

    public void run() throws GameActionException {
        // Robot.run()

        super.run();


        // TODO Change to something meaningful
        if(flag == 0){
            comms.setFlag(2,rc.getLocation());
        }

        // Get the location and ID of the spawn
        if(enlightenmentCenterSpawnedFromLoc == null){
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1,rc.getTeam());

            for(int i = 0; i < nearbyRobots.length; ++i){
                if(nearbyRobots[i].type == RobotType.ENLIGHTENMENT_CENTER){
                    RobotInfo enlightenmentCenter = nearbyRobots[i];
                    enlightenmentCenterSpawnedFromLoc = enlightenmentCenter.location;
                    enlightenmentCenterSpawnedFromId = enlightenmentCenter.ID;
                }
            }
        }

        // Get the message of the enlightenment center
        if(rc.canGetFlag(enlightenmentCenterSpawnedFromId)){
            int flag = rc.getFlag(enlightenmentCenterSpawnedFromId);
            enlightenmentCenterSpawnedFromMessage = comms.decodeFlag(flag);
            System.out.println(enlightenmentCenterSpawnedFromMessage.toString());
        }

    }

}
