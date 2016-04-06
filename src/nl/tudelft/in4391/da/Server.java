package nl.tudelft.in4391.da;

import nl.tudelft.in4391.da.unit.Unit;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by arkkadhiratara on 3/5/16.
 */
public interface Server extends Remote {
    public boolean ping() throws RemoteException;
    public void register(Node node) throws RemoteException;
    public Boolean connect() throws RemoteException;
    public Player login(String username, String password) throws RemoteException;

    // Sync
    public Arena getArena() throws RemoteException;
    public ArrayList<Player> getPlayers() throws RemoteException;


    public Unit spawnUnit(Unit unit) throws RemoteException;

    public Unit moveUnit(Unit unit, int x, int y) throws RemoteException;

    public Unit removeUnit(Unit unit, int x, int y) throws RemoteException;

    public void deleteUnit(Unit unit) throws RemoteException;

    public boolean checkSurrounding(Unit unit, int x, int y) throws RemoteException;

    public boolean checkDead(Unit unit) throws RemoteException;

    public Unit actionToSurroundingUnit(Unit unit, int x, int y) throws RemoteException;

    public void logout(Player player) throws RemoteException;
}
