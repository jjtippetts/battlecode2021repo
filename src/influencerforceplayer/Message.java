package influencerforceplayer;

import battlecode.common.MapLocation;

public class Message {
    public int id;
    public MapLocation location;
    public int robotId;

    // Message IDS
        // 1: Neural or Enemy Enlightenment Center
        // 2: Edge of map


    // Communicate a signal
    public Message(int messageId){
        this.id = messageId;
    }

    // Communicate a location
    public Message(int messageId, MapLocation location){
        this.id = messageId;
        this.location = location;
    }

    // Communicate to a specific robot
    public Message(int messageId, MapLocation location, int robotId){
        this.id = messageId;
        this.location = location;
        this.robotId = robotId;
    }

    // Return string representation
    public String toString(){
        return "MessageId: " + id + ", MapLocation: " + location;
    }
}
