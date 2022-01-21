package influencerforceplayer;

import battlecode.common.*;

public class Slanderer extends Unit {

    public Slanderer(RobotController rc) {
        super(rc);
    }

    Direction myWall=Direction.CENTER;
    MapLocation edgeLoc;
    MapLocation myLoc=rc.getLocation();
    int flagMessage=0;
    static int stuckCount = 0;

    public void run() throws GameActionException {
        super.run();

        if (enlightenmentCenterSpawnedFromMessage!=null &&
                enlightenmentCenterSpawnedFromMessage.id == Constants.FOUND_EC) {
            targetEnemyECLoc = enlightenmentCenterSpawnedFromMessage.location;
        }

        //If slanderer turns into Politician, attack enemy ec.
        if (rc.getType() == RobotType.POLITICIAN && targetEnemyECLoc != null) {
//            System.out.println("Previous slanderer attacking");
            attack();
//            if (rc.isReady() && !tryMove(rc.getLocation().directionTo(targetEnemyECLoc))){
//              stuckCount++;
//            };
//
//            if (stuckCount >= 3) {
//                comms.setFlag(Constants.STUCK, rc.getLocation());
//            }
            tryMove(rc.getLocation().directionTo(targetEnemyECLoc));
        }

        if (enlightenmentCenterSpawnedFromFlag!=0){
            Message decodeECFlag= comms.decodeFlag(enlightenmentCenterSpawnedFromFlag);
            flagMessage=decodeECFlag.id;
            edgeLoc=decodeECFlag.location;
        }

        //I don't know the edge but EnlighenmentCenter had found it
        if (myWall==Direction.CENTER && flagMessage==2) {
            myWall=myLoc.directionTo(edgeLoc);
            if (tryMove(myWall)) {
                System.out.println("Go to my wall");
            }
            else {
                tryMove(randomDirection());
            }
        }
        //nah gotta find the edge myself
        else if (myWall==Direction.CENTER) {

            tryMove(Util.randomDirection());

            //3^2+3^2=18 and 4^2=16 closest to slanderer sense radius squared 20
            //why there are 8 directions
            //build wall on the WEST
            if (!rc.onTheMap(new MapLocation(rc.getLocation().x-3, rc.getLocation().y-3))){
                comms.setFlag(2,new MapLocation(rc.getLocation().x-3, rc.getLocation().y-3));
                myWall=Direction.WEST;
                tryMove(Direction.WEST);
            }
            if (myWall==Direction.CENTER && !rc.onTheMap(new MapLocation(rc.getLocation().x-4, rc.getLocation().y))) {
                comms.setFlag(2,new MapLocation(rc.getLocation().x-4, rc.getLocation().y));
                myWall=Direction.WEST;
                tryMove(Direction.WEST);
            }
            if (myWall==Direction.CENTER && !rc.onTheMap(new MapLocation(rc.getLocation().x-3, rc.getLocation().y+3))){
                comms.setFlag(2,new MapLocation(rc.getLocation().x-3, rc.getLocation().y+3));
                myWall=Direction.WEST;
                tryMove(Direction.WEST);
            }
            //build wall on the EAST
            if (myWall==Direction.CENTER && !rc.onTheMap(new MapLocation(rc.getLocation().x+3, rc.getLocation().y-3))){
                comms.setFlag(2,new MapLocation(rc.getLocation().x+3, rc.getLocation().y-3));
                myWall=Direction.EAST;
                tryMove(Direction.EAST);
            }
            if (myWall==Direction.CENTER && !rc.onTheMap(new MapLocation(rc.getLocation().x+4, rc.getLocation().y))) {
                comms.setFlag(2,new MapLocation(rc.getLocation().x+4, rc.getLocation().y));
                myWall=Direction.EAST;
                tryMove(Direction.EAST);
            }
            if (myWall==Direction.CENTER && !rc.onTheMap(new MapLocation(rc.getLocation().x+3, rc.getLocation().y+3))){
                comms.setFlag(2,new MapLocation(rc.getLocation().x+3, rc.getLocation().y+3));
                myWall=Direction.EAST;
                tryMove(Direction.EAST);
            }
            //build wall on the SOUTH
            if (myWall==Direction.CENTER && !rc.onTheMap(new MapLocation(rc.getLocation().x, rc.getLocation().y-4))){
                comms.setFlag(2,new MapLocation(rc.getLocation().x, rc.getLocation().y-4));
                myWall=Direction.SOUTH;
                tryMove(Direction.SOUTH);
            }
            //build wall on the NORTH
            if (myWall==Direction.CENTER && !rc.onTheMap(new MapLocation(rc.getLocation().x, rc.getLocation().y+4))){
                comms.setFlag(2,new MapLocation(rc.getLocation().x, rc.getLocation().y+4));
                myWall=Direction.NORTH;
                tryMove(Direction.NORTH);
            }
        }

        else {
            if (tryMove(myWall)){}
            else {
                if (myWall!=Direction.NORTH && myWall!=Direction.SOUTH ) {
                    if (tryMove(Direction.NORTH)){}
                    else {
                        tryMove(Direction.SOUTH);
                    }
                }
                else {
                    if (tryMove(Direction.WEST)){}
                    else {
                        tryMove(Direction.EAST);
                    }
                }

            }
        }
    }
}
