package testPlayer;

import battlecode.common.*;

import java.util.ArrayList;

public class EnlightenmentCenter extends Robot {

    ArrayList<Integer> createdRobotsId = new ArrayList<>();
    int numMuckrakers = 0;
    int numSlanderers = 0;
    int numPoliticians = 0;
    static int prevVotes = 0;
    static double bidAmount = 1;
    static int winCount = 0;

    public EnlightenmentCenter(RobotController rc) {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        // Builds robots
        buildRobots();

        // Check messages from created robots
        checkRobotMessages();

        bid();
    }


    // Build Robots in equal numbers and based on influence
    public void buildRobots() throws GameActionException {
        boolean muckrakerNear = false;
        for(int i = 0; i< nearbyRobots.length; ++i){
            if(nearbyRobots[i].team != myTeam && nearbyRobots[i].type == RobotType.MUCKRAKER){
                muckrakerNear = true;
                break;
            }
        }

        for(int i = 0; i < Util.directions.length; ++i){
            if (numMuckrakers <= numSlanderers && numMuckrakers <= numPoliticians){
                if(tryBuildRobot(RobotType.MUCKRAKER, Util.directions[i], 1))
                    ++numMuckrakers;
            }
            else if (numSlanderers <= numPoliticians && !muckrakerNear){
                if(tryBuildRobot(RobotType.SLANDERER, Util.directions[i], (int) (myInfluence * 0.9)))
                    ++numSlanderers;
            } else {
                if(tryBuildRobot(RobotType.POLITICIAN, Util.directions[i], (int) (myInfluence * 0.9)))
                    ++numPoliticians;
            }
        }
    }


    // Tries to build a robot. If built, add the robot to list of robots
    public boolean tryBuildRobot(RobotType robotType, Direction dir, int cost) throws GameActionException {
        RobotInfo createdRobot = null;

        // Checks if robot can be built
        if (rc.canBuildRobot(robotType, dir, cost)) {
            rc.buildRobot(robotType, dir, cost);
            // Add created robots id to list of robot ids
            createdRobot = rc.senseRobotAtLocation(rc.getLocation().add(dir));
            createdRobotsId.add(createdRobot.getID());
            return true;
        }
        return false;
    }


    // Checks the flags of robots
    public void checkRobotMessages() throws GameActionException {
        ArrayList<Integer> robotsToRemove = new ArrayList<>();
        boolean setEnemyBotLocation = false;

        for(int i = 0; i < createdRobotsId.size(); ++i){
            // Robot is alive
            Message robotMessage = comms.getMessage(createdRobotsId.get(i));

            // Print out robot message
            if(robotMessage != null){
                System.out.println("Robot ID: " + createdRobotsId.get(i) + "," + robotMessage.toString());
                if(!setEnemyBotLocation && robotMessage.id == 2){
                    comms.setFlag(2,robotMessage.location);
                    setEnemyBotLocation = true;
                }
            }
            // Robot died, add list to remove
            else {
                System.out.println("Robot id: " + createdRobotsId.get(i) + " died");
                robotsToRemove.add(createdRobotsId.get(i));
            }
        }

        if(!setEnemyBotLocation){
            if(rc.canSetFlag(0)){
                rc.setFlag(0);
            }
        }

        // Remove dead robots
        createdRobotsId.removeAll(robotsToRemove);
    }

    public void bid() throws GameActionException {
        if (rc.getTeamVotes() > prevVotes) {
            winCount++;
        } else {
            bidAmount *= 2;
        }

        if (winCount > 10) {
            bidAmount /= 2;
            winCount = 0;
        }

        if (bidAmount >= (double) rc.getInfluence() / 2) {
            bidAmount = (double) rc.getInfluence() * 0.05;
        }

        if (bidAmount <= 0) {
            bidAmount = 2;
        }

        System.out.println("BID " + (int) bidAmount);


        if (rc.canBid((int) bidAmount)) {
            rc.bid((int) bidAmount);
            System.out.println("BID " + (int) bidAmount);
        } else {
            if (rc.canBid((int) (bidAmount * 0.05))) {
                rc.bid((int) (bidAmount * 0.05));
            } else if (rc.canBid( 2)) {
                rc.bid((2));
            }
        }

        prevVotes = rc.getTeamVotes();
    }
}
