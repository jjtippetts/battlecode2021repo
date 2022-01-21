package pathfindingplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Muckracker extends Robot {
    public MapLocation myEnlightenmentCenter;
    static MapLocation obstacleLoc;
    static final double passabilityThreshold = 0.5;
    static Direction bugDirection = null;
    static int obstacleDistance;
    static MapLocation target; //for debugging purposes
    static Direction previousDirection;
    public Muckracker(RobotController rc) {
        super(rc);
        myEnlightenmentCenter = getEnlightenmentCenterLoc(rc.getTeam());
        System.out.println("My Enlightenment Center at " + myEnlightenmentCenter);
        target = new MapLocation(10002, 23948);
    }

    public void run() throws GameActionException {
        if (rc.getLocation().equals(target)) {
            System.out.println("--------------at target-----------------");
            return;
        }
//        if (rc.isReady()) {
//            bugTwo(target);
//        }
        Direction targetDir = rc.getLocation().directionTo(target);
        if (rc.canMove(targetDir)) {
            rc.move(targetDir);
        } else {
            tryMove(randomDirection());
        }
    }

    void bugTwo(MapLocation target) throws GameActionException {
        System.out.println(Clock.getBytecodesLeft());
        Direction targetDirection = rc.getLocation().directionTo(target);
        int currentToTargetLocationDistance = target.distanceSquaredTo(rc.getLocation());

        System.out.println("target " + target + " obstacle " + obstacleLoc);
        System.out.println("bug dir " + bugDirection + " target dir " + targetDirection);
        System.out.println("On line" + checkIfOnLine(rc.getLocation(), targetDirection));
        System.out.println("Cur distance " + currentToTargetLocationDistance + " " + obstacleDistance);
        if (rc.isReady()) {
            if (rc.canMove(targetDirection) && rc.sensePassability(rc.getLocation().add(targetDirection)) >= passabilityThreshold
            && ((checkIfOnLine(rc.getLocation(), targetDirection))
                    && currentToTargetLocationDistance < obstacleDistance)) {
                rc.move(targetDirection);
                bugDirection = null;
                obstacleLoc = null;
                obstacleDistance = 0;
            } else {
                if (bugDirection == null) {
                    bugDirection = targetDirection;
                    obstacleLoc = rc.getLocation();
                    obstacleDistance = rc.getLocation().distanceSquaredTo(target);
                }

                Direction tempPrev = previousDirection;
                for (int i = 0; i < 8; ++i) {
                    System.out.println(bugDirection);
                    if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold
                   ) {
                        rc.move(bugDirection);
                        System.out.println("Moved in bug " + bugDirection);
                        previousDirection = bugDirection;
                        bugDirection = bugDirection.rotateLeft();
                        break;
                    }
                    bugDirection = bugDirection.rotateRight();
                }

                //if prev didn't change then robot didn't move
//                if (tempPrev == previousDirection) {
//                    previousDirection = Direction.CENTER; //allow robot to backtrack in dead end
//                }
            }
        }
        System.out.println(Clock.getBytecodesLeft());
    }

    //Check if two points are on the same line for bug nav two
    boolean checkIfOnLine (MapLocation location, Direction direction) {
        MapLocation myLoc = rc.getLocation();
        if (direction ==  directions[0] || direction == directions[4]) {
            return location.x == myLoc.x;
        } else if (direction == directions[1] || direction == directions[5]) {
            return location.x - location.y == myLoc.x - myLoc.y;
        } else if (direction == directions[2] || direction == directions[6]) {
            return location.y == myLoc.y;
        } else {
            return (location.x - myLoc.x) * -1 == location.y - myLoc.y;
        }
    }
}
