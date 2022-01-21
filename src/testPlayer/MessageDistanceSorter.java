package testPlayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.Comparator;

public class MessageDistanceSorter implements Comparator<Message> {
    RobotController rc;

    public MessageDistanceSorter(RobotController rc){
        this.rc = rc;
    }

    @Override
    public int compare(Message message1, Message message2){
        MapLocation currentLocation = rc.getLocation();
        Integer distanceToRobot1 = currentLocation.distanceSquaredTo(message1.location);
        Integer distanceToRobot2 = currentLocation.distanceSquaredTo(message2.location);
        return distanceToRobot1.compareTo(distanceToRobot2);
    }
}
