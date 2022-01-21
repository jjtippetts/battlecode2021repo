package templateBot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {
    RobotController rc;

    public Communication(RobotController rc){
        this.rc = rc;
    }

    // encodes a message id and map location
    public int setFlag(int messageId, MapLocation location) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = 128 * (x % 128) + (y % 128) + messageId * 1048576;
        if(rc.canSetFlag(encodedLocation)){
            rc.setFlag(encodedLocation);
        } else {
            encodedLocation = -1; //failed to set flag
        }

        return encodedLocation;
    }

    // encodes a message id. other information and map location
    public int setFlag(int messageId, MapLocation location, int other) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = 128 * (x % 128) + (y % 128) + (other % 64) * 16384 + messageId * 1048576;
        if(rc.canSetFlag(encodedLocation)){
            rc.setFlag(encodedLocation);
        } else {
            encodedLocation = -1; //failed to set flag
        }

        return encodedLocation;
    }

    // Takes in a robotId and returns a message
    public Message getMessage(int robotId) throws GameActionException {
        if(rc.canGetFlag(robotId)){
            return decodeFlag(rc.getFlag(robotId));
        }
        else{
            return null;
        }
    }

    // Decodes an encoded message and returns a message object
    public Message decodeFlag(int flag){
        int x = (flag / 128) % 128;
        int y = flag % 128;
        int other = (flag / 16384) % 64;
        int messageId = flag / 1048576;

        // If location is not set
        if(messageId == 0){
            return new Message(messageId);
        }

        MapLocation currentLocation = rc.getLocation();
        int offsetX128 = currentLocation.x / 128;
        int offsetY128 = currentLocation.y / 128;
        MapLocation actualLocation = new MapLocation(offsetX128 * 128 + x, offsetY128 * 128 + y);

        MapLocation alternative = actualLocation.translate(-128, 0);
        if(currentLocation.distanceSquaredTo(alternative) < currentLocation.distanceSquaredTo(actualLocation)){
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(128, 0);
        if(currentLocation.distanceSquaredTo(alternative) < currentLocation.distanceSquaredTo(actualLocation)){
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, -128);
        if(currentLocation.distanceSquaredTo(alternative) < currentLocation.distanceSquaredTo(actualLocation)){
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, 128);
        if(currentLocation.distanceSquaredTo(alternative) < currentLocation.distanceSquaredTo(actualLocation)){
            actualLocation = alternative;
        }

        return new Message(messageId, actualLocation, other);
    }
}