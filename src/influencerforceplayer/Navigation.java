package influencerforceplayer;

import battlecode.common.*;

import static influencerforceplayer.Robot.*;

public class Navigation {
    RobotController rc;
    static MapLocation obstacleLoc; //not used
    static final double passabilityThreshold = 0.2;
    static Direction bugDirection = null;
    static int obstacleDistance;
    static MapLocation target; //for debugging purposes

    public Navigation (RobotController rc) {
        this.rc = rc;
    }

    void bugTwo(MapLocation target) throws GameActionException {
        //TODO check is location occupied by robot
        Direction targetDirection = rc.getLocation().directionTo(target);
        int currentToTargetLocationDistance = target.distanceSquaredTo(rc.getLocation());
        boolean passable = rc.sensePassability(rc.getLocation().add(targetDirection)) >= passabilityThreshold;
        boolean onTheSameLine = checkIfOnLine(rc.getLocation(), targetDirection);
        boolean closerThanLastObstacle = currentToTargetLocationDistance < obstacleDistance;

        if (rc.isReady()) {
            if (rc.canMove(targetDirection) && passable
                    && onTheSameLine && closerThanLastObstacle) {
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

                for (int i = 0; i < 8; ++i) {
                    System.out.println(bugDirection);
                    if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold
                    ) {
                        rc.move(bugDirection);
                        System.out.println("Moved in bug " + bugDirection);
                        bugDirection = bugDirection.rotateLeft();
                        break;
                    }
                    bugDirection = bugDirection.rotateRight();
                }
            }
        }
    }

    //Check if two points are on the same line for bug nav two
    boolean checkIfOnLine (MapLocation location, Direction direction) {
        MapLocation myLoc = rc.getLocation();
        if (direction ==  Util.directions[0] || direction == Util.directions[4]) {
            // North - South
            // On the same line if on the same column
            return location.x == myLoc.x;
        } else if (direction == Util.directions[1] || direction == Util.directions[5]) {
            // Southwest - Northeast
            // On the same line if have the same y-intercept
            return location.x - location.y == myLoc.x - myLoc.y;
        } else if (direction == Util.directions[2] || direction == Util.directions[6]) {
            // West - East
            // On the same line if on the same row
            return location.y == myLoc.y;
        } else {
            // Northwest - Southeast
            // On the same line delta x = -(delta y)
            return (location.x - myLoc.x) * -1 == location.y - myLoc.y;
        }
    }

    // Tangent Bug
    // if all 3 clear, proceed 3 steps
    // else when obstacle hit, 'bug' head 3 steps
    // if bug-left on 4th step is clear, tangent to step 3
    // else if bug-right on 4th step is clear, tangent to step 3
    // else tangent to one or the other and loop

    public void tangentBug(MapLocation target) throws GameActionException {
        MapLocation myCurrentLoc = rc.getLocation();
        MapLocation lookAheadLoc = myCurrentLoc;
        double minPass = 0.2;
        Direction currentDir = lookAheadLoc.directionTo(target);
        Direction bugDir = null;
        MapLocation obstacle = null;
        //look ahead 3 steps ( do this every move? )
        for (int i = 0; i < 3; i++) {
            currentDir = lookAheadLoc.directionTo(target);
            // if next step is clear, continue checking further steps
            if (rc.onTheMap(lookAheadLoc.add(currentDir)) && rc.sensePassability(lookAheadLoc.add(currentDir)) >= minPass && rc.isLocationOccupied(lookAheadLoc.add(currentDir))) {
                System.out.println(lookAheadLoc.add(currentDir) + " is clear");
                lookAheadLoc = lookAheadLoc.add(currentDir);
                continue;
            } else {
                // break when MapLocation with obstacle in next step is found
                System.out.println("Obstacle found at " + lookAheadLoc);
                obstacle = lookAheadLoc;
                break;
            }
        }
        // if step ahead of current loc is impassable
        if (obstacle != null) {
            System.out.println("entering look ahead bug");
            MapLocation closestNextLocation = bugAhead(myCurrentLoc, target, currentDir);
            currentDir = myCurrentLoc.directionTo(closestNextLocation);
        }
        if (rc.canMove(currentDir)) {
            rc.move(currentDir);
        }
    }

    public MapLocation bugAhead(MapLocation currentLocation, MapLocation target, Direction dirToObstacle) throws GameActionException {
        MapLocation bugLeft, bugRight;
        Direction dirToMove = dirToObstacle.rotateLeft();
        MapLocation nextStep = null;
        int numSteps = 0;
        double minPass = 0.2;

        // bug left
        for(int i = 0; i < 8; i++) {
            if (rc.onTheMap(currentLocation.add(dirToMove)) && rc.sensePassability(currentLocation.add(dirToMove)) >= minPass && rc.isLocationOccupied(currentLocation.add(dirToMove))) {
                nextStep = currentLocation.add(dirToMove);
                System.out.println(nextStep + " in bug left is clear");
                dirToMove = dirToMove.rotateRight();
                numSteps++;
                if (numSteps == 3) {
                    break;
                }
            } else {
                System.out.println("rotate left again");
                dirToMove = dirToMove.rotateLeft();
            }
        }

        // if no steps can be taken
        if (nextStep == null) {
            bugLeft = currentLocation;
        } else {
            bugLeft = nextStep.add(dirToMove);
        }
        System.out.println("Bug Left: " + bugLeft);

        // bug right
        dirToMove = dirToObstacle.rotateRight();
        nextStep = null;
        numSteps = 0;
        for(int i = 0; i < 8; i++) {
            if (rc.onTheMap(currentLocation.add(dirToMove)) && rc.sensePassability(currentLocation.add(dirToMove)) >= minPass && rc.isLocationOccupied(currentLocation.add(dirToMove))) {
                nextStep = currentLocation.add(dirToMove);
                System.out.println(nextStep + " in bug right is clear");
                dirToMove = dirToMove.rotateLeft();
                numSteps++;
                if (numSteps == 3) {
                    break;
                }
            } else {
                System.out.println("rotate right again");
                dirToMove = dirToMove.rotateRight();
            }
        }

        // if no steps can be taken
        if (nextStep == null) {
            bugRight = currentLocation;
        } else {
            bugRight = nextStep.add(dirToMove);
        }
        System.out.println("Bug Right: " + bugRight);

        // if bugging left and right both land back in the same location
        if (bugLeft == bugRight) {
            return (currentLocation.add(currentLocation.directionTo(target)));
        }
        else {
            int distBugLeft = bugLeft.compareTo(target);
            int distBugRight = bugRight.compareTo(target);
            System.out.println("Distance from bugleft: " + distBugLeft + "\nDistance from bugright: " + distBugRight);
            return distBugLeft <= distBugRight ? bugLeft : bugRight;
        }
    }

    public void roamMap() throws GameActionException{
        for (int i = 0; i <= Util.directions.length; i++) {
//            i = i % Util.directions.length;
            Direction dir = Util.directions[i];
            while (rc.onTheMap(rc.getLocation().add(dir)) && !rc.isLocationOccupied(rc.getLocation().add(dir))){
                if (rc.canMove(dir)){
                    rc.move(dir);
                } else {
                    Clock.yield();
                }
            }
        }
    }

}
