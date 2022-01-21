package testPlayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Politician extends Unit {
    RobotController rc;
    int explode = 0;

    public Politician(RobotController rc){
        super(rc);
        this.rc = rc;
    }

    public void run() throws GameActionException {
        super.run();

        if(turnCount == 1){
            System.out.println(Arrays.toString(nearbyRobots));
        }

        System.out.println("Before: " + Clock.getBytecodesLeft());
        ArrayList<RobotInfo> attackable = new ArrayList<>();
        ArrayList<MapLocation> enemyBotsLocation = new ArrayList<>();
        for(int i = 0; i < nearbyRobots.length; ++i){
            // If robot is on my team get message
            if(nearbyRobots[i].team == myTeam){
                System.out.println("Found Friendly unit");
                Message robotMessage = comms.getMessage(nearbyRobots[i].ID);
                if(robotMessage != null){
                    if(robotMessage.id == 2){
                        enemyBotsLocation.add(robotMessage.location);
                    }
                    if(robotMessage.id == 3) {
                        if(!rc.canSenseLocation(robotMessage.location)){
                            int robotDistanceToEnemyBot = nearbyRobots[i].location.distanceSquaredTo(robotMessage.location);
                            int myDistanceToEnemyBot = myCurrentLoc.distanceSquaredTo(robotMessage.location);
                            if(robotDistanceToEnemyBot < myDistanceToEnemyBot){
                                enemyBotsLocation.add(robotMessage.location);
                            }
                        }
                    }
                }
            }
            // Else try to empower
            else {
                System.out.println("Found enemy Unit");
                // If enemy bot is in explode radius
                if(myCurrentLoc.isWithinDistanceSquared(nearbyRobots[i].location,actionRadius)){
                    // Explode if enlightenment center
                    if(nearbyRobots[i].type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(actionRadius)){
                        rc.empower(actionRadius);
                        System.out.println("empowered EC");
                    }
                    // Else explode if trailing after 3 turns
                    else {
                        if(explode >= 3 && rc.canEmpower(actionRadius)){
                            System.out.println("Exploding enemy unit");
                            rc.empower(actionRadius);
                        }
                        ++explode;
                    }
                }
                attackable.add(nearbyRobots[i]);
            }
        }

        System.out.println("After: " + Clock.getBytecodesLeft());

        // Move towards enemy
        if(!attackable.isEmpty()){
            System.out.println("Moving towards detected enemy bot");
            MapLocation enemyBotLocation = attackable.get(0).location;
            comms.setFlag(2,enemyBotLocation);
            nav.tryMove(myCurrentLoc.directionTo(enemyBotLocation));
        }

        // If no robots sensed. Find closest enemy bot from flag
        else if (!enemyBotsLocation.isEmpty()){
            System.out.println("Moving towards enemy bot at" + enemyBotsLocation.get(0));
            enemyBotsLocation.sort(new MapLocationDistanceSorter(rc));
            nav.tryMove(myCurrentLoc.directionTo(enemyBotsLocation.get(0)));
            comms.setFlag(3,enemyBotsLocation.get(0));
        }

        else if (enlightenmentCenterMessage.id == 2){
            System.out.println("Got message from EQ. Moving towards enemy bot at" + enlightenmentCenterMessage.location);
            nav.tryMove(myCurrentLoc.directionTo(enlightenmentCenterMessage.location));
        }

        else {
            System.out.println("NO BOTS DETECTED");
            rc.setFlag(0);
        }
        nav.tryMove(Util.randomDirection());
    }
}
