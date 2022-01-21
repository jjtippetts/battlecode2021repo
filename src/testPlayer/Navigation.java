package testPlayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Navigation {
    RobotController rc;

    public Navigation(RobotController rc){
        this.rc = rc;
    }

    boolean tryMove(Direction toGo) throws GameActionException {
        Direction [] directions = {toGo, toGo.rotateLeft(), toGo.rotateRight()};

        for(int i = 0; i < directions.length; ++i){
            if (rc.canMove(directions[i])) {
                rc.move(directions[i]);
                return true;
            }
        }

        return false;
    }

    boolean tryMoveAny(Direction toGo) throws GameActionException {
        Direction [] directions = {toGo, toGo.rotateLeft(), toGo.rotateRight(), toGo.rotateLeft().rotateLeft(), toGo.rotateRight().rotateRight()};

        for(int i = 0; i < directions.length; ++i){
            if (rc.canMove(directions[i])) {
                rc.move(directions[i]);
                return true;
            }
        }

        return false;
    }
}
