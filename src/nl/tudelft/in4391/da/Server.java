package nl.tudelft.in4391.da;

import nl.tudelft.in4391.da.unit.Unit;


import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by arkkadhiratara on 4/5/16.
 */
public interface Server extends Remote {
    public boolean ping() throws RemoteException;
    public void register(Node node) throws RemoteException;
    public Player login(String username, String password, String type) throws RemoteException;
    public void logout(Player player) throws RemoteException;

    // Arena
    public Arena getArena() throws RemoteException;

    public void releaseDragons(int num)  throws RemoteException;

    // Unit
    public void moveUnit(Unit unit, int x, int y) throws RemoteException;

    public void actionUnit(Unit source, Unit target) throws RemoteException;

    public void damageUnit(Unit source, Unit target) throws RemoteException;

    public void healUnit(Unit source, Unit target) throws RemoteException;

}
