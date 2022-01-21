package testPlayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Comparator;

public class MapLocationDistanceSorter implements Comparator<MapLocation> {
    RobotController rc;

    public MapLocationDistanceSorter(RobotController rc){
        this.rc = rc;
    }

    @Override
    public int compare(MapLocation loc1, MapLocation loc2){
        MapLocation currentLocation = rc.getLocation();
        Integer distanceToRobot1 = currentLocation.distanceSquaredTo(loc1);
        Integer distanceToRobot2 = currentLocation.distanceSquaredTo(loc2);
        return distanceToRobot1.compareTo(distanceToRobot2);
    }
}
