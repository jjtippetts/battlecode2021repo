package newerplayer;

import battlecode.common.MapLocation;

public class Message {
    public int id;
    public MapLocation location;
    public int other;

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
    public Message(int messageId, MapLocation location, int other){
        this.id = messageId;
        this.location = location;
        this.other = other;
    }

    // Return string representation
    public String toString(){
        return "MessageId: " + id + ", MapLocation: " + location + ", Other: " + other;
    }
}
