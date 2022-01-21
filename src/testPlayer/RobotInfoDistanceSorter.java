package testPlayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.Comparator;

public class RobotInfoDistanceSorter implements Comparator<RobotInfo> {
    RobotController rc;

    public RobotInfoDistanceSorter(RobotController rc){
        this.rc = rc;
    }

    @Override
    public int compare(RobotInfo robot1, RobotInfo robot2){
        MapLocation currentLocation = rc.getLocation();
        Integer distanceToRobot1 = currentLocation.distanceSquaredTo(robot1.location);
        Integer distanceToRobot2 = currentLocation.distanceSquaredTo(robot2.location);
        return distanceToRobot1.compareTo(distanceToRobot2);
    }
}
