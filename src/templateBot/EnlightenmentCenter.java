package templateBot;

import battlecode.common.*;

import java.util.ArrayList;

public class EnlightenmentCenter extends Robot{

    ArrayList<Integer> createdRobotsId = new ArrayList<>();
    int numMuckrakers = 0;
    int numSlanderers = 0;
    int numPoliticians = 0;

    public EnlightenmentCenter(RobotController rc) {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        // Builds robots
        buildRobots();

        // Check messages from created robots
        checkRobotMessages();
    }


    // Build Robots in equal numbers and based on influence
    public void buildRobots() throws GameActionException {
        for(int i = 0; i < Util.directions.length; ++i){
            if (numMuckrakers <= numSlanderers && numMuckrakers <= numPoliticians){
                tryBuildRobot(RobotType.MUCKRAKER, Util.directions[i], 1);
                ++numMuckrakers;
            }
            else if (numSlanderers <= numPoliticians){
                tryBuildRobot(RobotType.SLANDERER,Util.directions[i], (int) (myInfluence * 0.9));
                ++numSlanderers;
            } else {
                tryBuildRobot(RobotType.POLITICIAN, Util.directions[i], (int) (myInfluence * 0.9));
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

        for(int i = 0; i < createdRobotsId.size(); ++i){
            // Robot is alive
            Message robotMessage = comms.getMessage(createdRobotsId.get(i));

            // Print out robot message
            if(robotMessage != null){
                System.out.println("Robot ID: " + createdRobotsId.get(i) + "," + robotMessage.toString());
            }
            // Robot died, add list to remove
            else {
                System.out.println("Robot id: " + createdRobotsId.get(i) + " died");
                robotsToRemove.add(createdRobotsId.get(i));
            }
        }
        // Remove dead robots
        createdRobotsId.removeAll(robotsToRemove);
    }
}
