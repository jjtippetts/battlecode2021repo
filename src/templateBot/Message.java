package templateBot;

import battlecode.common.MapLocation;

public class Message {
    public int id;
    public MapLocation location;
    public int other;

    // Communicate a signal
    public Message(int messageId){
        this.id = messageId;
        this.location = null;
        other = 0;
    }

    // Communicate a location
    public Message(int messageId, MapLocation location){
        this.id = messageId;
        this.location = location;
        this.other = 0;
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
