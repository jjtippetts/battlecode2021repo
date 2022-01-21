package newplayer;

import battlecode.common.*;

public class Unit extends Robot {
    Navigation nav;
    static MapLocation targetEnemyECLoc; //Slanderers need this to know where to go after they turn into politicians.
    int ecFlag = 0;
    MapLocation enlightenmentCenterSpawnedFromLoc = null;
    MapLocation myLoc = rc.getLocation();
    int myID = rc.getID();
    int enlightenmentCenterSpawnedFromId;
    int enlightenmentCenterSpawnedFromFlag=0;
    Message enlightenmentCenterSpawnedFromMessage;
    Team myTeam;
    Team enemy;

    public Unit(RobotController rc) {
        super(rc);
        nav = new Navigation(rc);
        myTeam=rc.getTeam();
        enemy = rc.getTeam().opponent();
    }


    public void run() throws GameActionException {
        // Robot.run()

        super.run();

        // TODO Change to something meaningful
//        if (flag == 0) {
//            comms.setFlag(2, rc.getLocation());
//        }

        // Get the location and ID of the spawn
        if (enlightenmentCenterSpawnedFromLoc == null) {
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

            for (int i = 0; i < nearbyRobots.length; ++i) {
                if (nearbyRobots[i].type == RobotType.ENLIGHTENMENT_CENTER) {
                    RobotInfo enlightenmentCenter = nearbyRobots[i];
                    enlightenmentCenterSpawnedFromLoc = enlightenmentCenter.location;
                    enlightenmentCenterSpawnedFromId = enlightenmentCenter.ID;
                }
            }
        }

        // Get the message of the enlightenment center
        if (rc.canGetFlag(enlightenmentCenterSpawnedFromId)) {
            ecFlag = rc.getFlag(enlightenmentCenterSpawnedFromId);
            enlightenmentCenterSpawnedFromFlag= ecFlag;
            enlightenmentCenterSpawnedFromMessage = comms.decodeFlag(ecFlag);

            //this if condition looks strange but it throws exception sometimes
            //if (enlightenmentCenterSpawnedFromMessage!=null)
                //System.out.println(enlightenmentCenterSpawnedFromMessage.toString());
        }

        //Set target Enemy EC Location for slanderers and politicians.
        if (enlightenmentCenterSpawnedFromMessage!= null) {
            if (enlightenmentCenterSpawnedFromMessage.id == Constants.FOUND_ENEMY_EC) {
                targetEnemyECLoc = enlightenmentCenterSpawnedFromMessage.location;
            }
            else if (rc.getRoundNum()>1000 && enlightenmentCenterSpawnedFromMessage.id == Constants.FOUND_NEURAL_EC ) {
                targetEnemyECLoc = enlightenmentCenterSpawnedFromMessage.location;
            }
        }
    }

    void attack() throws GameActionException {
        final int actionRadius = RobotType.POLITICIAN.actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius,enemy);

        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            //System.out.println("empowering...");
            rc.empower(actionRadius);
            //System.out.println("empowered");

        }

        RobotInfo[] neuralAttackable = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);

        if (neuralAttackable.length != 0 && rc.canEmpower(actionRadius)) {
            //System.out.println("empowering...");
            rc.empower(actionRadius);
            //System.out.println("empowered");
        }



        return;
    }

    void setNothingFlag () throws GameActionException {
        comms.setFlag(Constants.NOTHING, rc.getLocation());
    }
}
