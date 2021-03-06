package nl.tudelft.in4391.da;

import nl.tudelft.in4391.da.unit.Dragon;
import nl.tudelft.in4391.da.unit.Knight;
import nl.tudelft.in4391.da.unit.Unit;

import javax.swing.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by arkkadhiratara on 4/6/16.
 */
public class Bot extends Thread {
    private static final double GAME_SPEED = .1; //ms
    private static Integer TURN_DELAY = 1000;

    public ArrayList<Node> serverNodes;

    public Server server;
    public Arena arena;
    public Player player;
    public Unit unit;

    public Unit adjacentUnit;

    public boolean gameRunning;
	public boolean foundUnit;

    Random rand = new Random();

    public Server findServer() {
        Server bestServer = null;
        Node bestNode = null;

        long t = 0;
        long latency = 0;
        long maxLatency = 10000; // 10 seconds
        long bestLatency = maxLatency;

        // Ping all server and find the best latency
        for (Node n : serverNodes) {
            Server s = ServerImpl.fromRemoteNode(n);
            if (s != null) {
                bestServer = s;

                break;
            }
        }

        return bestServer;
    }

    public Bot(String username, String type) {
        serverNodes = new ArrayList<Node>();
        serverNodes.add(new Node(1, "127.0.0.1", 1100, 1200));
        serverNodes.add(new Node(2, "127.0.0.1", 1101, 1201));

        // Server object based on latency
        server = findServer();
        player = null;
        arena = new Arena();

        try {
            player = server.login(username,"",type);

            arena = server.getArena();
            arena.syncUnits();
            unit = player.getUnit();

        } catch (RemoteException e) {
//            e.printStackTrace();
            Thread.currentThread().interrupt();
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("[System] Logout and disconnect from server...");
                try {
                    server.logout(player);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                System.out.println("Bye!");
            }
        });
    }

	// Up to total 2 distance
	public Unit scanSurrounding(Unit unit, Arena arena) {
		Unit adjacentUnit = null;

		Integer sourceX = unit.getX();
		Integer sourceY = unit.getY();

		// Random surrounding
		// Range (-1,1)
		Integer x = rand.nextInt(3) - 1;
		Integer y = rand.nextInt(3) - 1;

		// Random surrounding 2 vertical horizontal
		// Range (-2,2)
		Integer x2 = rand.nextInt(4) - 2;
		Integer y2 = rand.nextInt(4) - 2;

		foundUnit = false;

		try {
			// Get adjacent unit
			// When not pointing to itself
			// When not out of bound
			while (!foundUnit && (x != 0 && y != 0 ) && (sourceX + x < 25 || sourceX + x >= 0) && (sourceY + y < 25 || sourceY + y >= 0) ){
				adjacentUnit = arena.unitCell[sourceX + x][sourceY + y];

                if (adjacentUnit == null){ //Check horizontal and vertical 2 square
                    Integer direction = rand.nextInt(2);
                    switch(direction){
                        case 0:
                            //horizontal
                            adjacentUnit = arena.unitCell[sourceX + x2][sourceY];
                            break;
                        case 1:
                            //vertical
                            adjacentUnit = arena.unitCell[sourceX][sourceY + y2];
                            break;
                    }

                    if (adjacentUnit == null && unit instanceof Knight){
                        // Random surrounding 2 vertical horizontal
                        // Range (-2,2)
                        Integer x5 = rand.nextInt(10) - 5;
                        Integer y5 = rand.nextInt(10) - 5;

                        Integer direction2 = rand.nextInt(2);
                        switch(direction2){
                            case 0:
                                //horizontal
                                adjacentUnit = arena.unitCell[sourceX + x5][sourceY];
                                break;
                            case 1:
                                //vertical
                                adjacentUnit = arena.unitCell[sourceX][sourceY + y5];
                                break;
                        }
                    }

                }
				foundUnit = true;
			}

		} catch (ArrayIndexOutOfBoundsException e) {
//			System.out.println("Array is out of Bounds"+e);
//			System.out.println("Scan other direction");
			x = rand.nextInt(3) - 1;
			y = rand.nextInt(3) - 1;
			foundUnit = false;
		}

		return adjacentUnit;

	}


	public void run() {
        runBot();
    }

    public void runBot(){
        gameRunning = true;

        try {
            while (gameRunning && GameState.getRunningState()){

                arena = server.getArena();
                arena.syncUnits();

                unit = arena.getMyUnit(player);

                if (unit == null) {
                    Thread.currentThread().interrupt();//preserve the message
                    return;
//                    break;
                }

                player.setUnit(unit);

                if (player.getUnit() == null){
                    Thread.currentThread().interrupt();//preserve the message
                    return;
//                    break;
                }

                unit = player.getUnit();

                // Random surrounding
                // Range (-1,1)
                Integer x = rand.nextInt(3) - 1;
                Integer y = rand.nextInt(3) - 1;

                if (unit instanceof Dragon) {
                    adjacentUnit = scanSurrounding(unit, arena);

                    if (adjacentUnit != null && adjacentUnit instanceof Knight ){
                        ArrayList<Unit> units = new ArrayList<Unit>();
                        units.add(0, player.getUnit());
                        units.add(1, adjacentUnit);
                        server.sendEvent(UnitEvent.UNIT_ATTACK, units);
                    }

                } else { // Knight
                    adjacentUnit = scanSurrounding(unit, arena);

                    if (adjacentUnit == null ){
                        // Check whether random 0 for both axis
                        // Move accordingly

                        Unit source = player.getUnit();
                        Unit target = source.clone();

                        if (x == 0 || y == 0) {
                            target.setCoord(player.getUnit().getX() + x,player.getUnit().getY() + y);

                            ArrayList<Unit> units = new ArrayList<Unit>();
                            units.add(0, player.getUnit());
                            units.add(1, target);
                            server.sendEvent(UnitEvent.UNIT_MOVE, units);
                        } else {
                            // Move horizontally or vertically 1 block
                            // When random value is not zero for x y
                            Integer direction = rand.nextInt(2);
                            ArrayList<Unit> units = new ArrayList<Unit>();
                            switch(direction){
                                case 0:
                                    //horizontal
                                    target.setCoord(player.getUnit().getX() + x,player.getUnit().getY());

                                    units = new ArrayList<Unit>();
                                    units.add(0, player.getUnit());
                                    units.add(1, target);
                                    server.sendEvent(UnitEvent.UNIT_MOVE, units);
                                    break;
                                case 1:
                                    //vertical
                                    target.setCoord(player.getUnit().getX(), player.getUnit().getY() + y);

                                    units = new ArrayList<Unit>();
                                    units.add(0, player.getUnit());
                                    units.add(1, target);
                                    server.sendEvent(UnitEvent.UNIT_MOVE, units);
                                    break;
                            }
                        }

                    } else { // Adjacent Unit exists
                        // Do action
                        if (adjacentUnit instanceof Dragon && (  Math.abs(unit.getX() - adjacentUnit.getX()) <= 2 && Math.abs(unit.getY() - adjacentUnit.getY()) <= 2  )){

                            ArrayList<Unit> units = new ArrayList<Unit>();
                            units.add(0, unit);
                            units.add(1, adjacentUnit);
                            server.sendEvent(UnitEvent.UNIT_ATTACK, units);

                        } else {
                            if ( unit.getX() - adjacentUnit.getX() != 0 && unit.getY() - adjacentUnit.getY() != 0) {
                                if (adjacentUnit.getHitPoints() <= 0.5 * adjacentUnit.getMaxHitPoints() ){
                                    ArrayList<Unit> units = new ArrayList<Unit>();
                                    units.add(0, unit);
                                    units.add(0, adjacentUnit);
                                    server.sendEvent(UnitEvent.UNIT_HEAL, units);
                                }

                            }
                        }
                    }
                }

                gameRunning = unit.running;

                Thread.currentThread().sleep((int) (unit.getTurnDelay() * GAME_SPEED * TURN_DELAY));
            }
        } catch (Exception e) {
//            e.printStackTrace();
            server = findServer();
            runBot();
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
