package nl.tudelft.in4391.da;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by arkkadhiratara on 3/5/16.
 */
public class ServerImpl implements Server {
    private Node node;
    private ArrayList<Node> activeNodes;
    private ArrayList<Player> activePlayers;

    private Event event;

    public ServerImpl(Node node) {
        this.node = node;

        // Init Array List
        this.activeNodes = new ArrayList<Node>();
        this.activePlayers = new ArrayList<Player>();

        // Add current node
        addActiveNode(node);

        // Init RMI Registry
        initRegistry();

        // Init Event Thread
        initEventThread(GameServer.DEFAULT_MULTICAST_GROUP,GameServer.DEFAULT_SOCKET_PORT);

        // Send Node Active State
        event.send(100,node);
    }

    // GETTERS SETTERS
    public Node getNode() { return this.node; }

    // Registry
    public void initRegistry(){
        // Create New Registry
        try {
            LocateRegistry.createRegistry(this.node.getRegistryPort());
        } catch (RemoteException e) {
            System.err.println("[Error] Exception: " + e.toString());
            e.printStackTrace();
        }

        // Get Registry
        Registry registry = null;
        Server stub = null;
        try {

            // Get Local Registry
            registry = LocateRegistry.getRegistry(this.node.getRegistryPort());

            // Stub and Skeleton
            stub = (Server) UnicastRemoteObject.exportObject(this,this.node.getCallbackPort());
            registry.bind(node.getName(), stub);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ACTIVE NODES
    public ArrayList<Node> getActiveNodes() {
        return this.activeNodes;
    }

    public void addActiveNode(Node node) {
        if(!activeNodes.contains(node)) {
            this.activeNodes.add(node);
            sortActiveNodes();
        }
    }

    public void removeActiveNode(Node node) {
        if(activeNodes.contains(node)) {
            this.getActiveNodes().remove(activeNodes.indexOf(node));
            sortActiveNodes();
        }
    }

    public void sortActiveNodes() {
        Collections.sort(this.activeNodes, new Comparator<Node>() {
            @Override
            public int compare(Node node2, Node node1)
            {
                return  node2.getID().compareTo(node1.getID());
            }
        });
    }

    // THREAD
    public void shutdown() {
        this.event.send(101,getNode());
    }

    // STATIC method
    public static Server fromRemoteNode(Node node) {
        Server component = null;
        try {
            Registry remoteRegistry = LocateRegistry.getRegistry(node.getHostAddress(), node.getRegistryPort());
            component = (Server) remoteRegistry.lookup(node.getName());
            return component;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return component;
    }

    private void initEventThread(String group, int port) {
        try {
            this.event = new Event(group,port);
            this.event.setListener(new EventListener() {
                @Override
                public void onReceiveData(byte[] receiveData, int length) {
                    // RAW Event Message
                    try {
                        EventMessage message = EventMessage.fromByte(receiveData);
                        switch(message.getCode()) {
                            // NODE
                            case 100: // Node Connected
                                onNodeConnected((Node) message.getObject());
                                break;
                            case 101: // Node Disconnected
                                onNodeDisconnected((Node) message.getObject());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });

            this.event.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Player> getActivePlayers() {
        return this.activePlayers;
    }

    public void addActivePlayer(Player player) {
        if(!activePlayers.contains(player)) {
            this.activePlayers.add(player);
        }
    }

    // EVENT
    public void onNodeConnected(Node n){
        addActiveNode(n);
        System.out.println("[System] " + n.getFullName() + " is connected.");

        if(!this.node.getID().equals(n.getID())) { // if the multicast not from himself
            Server remoteComponent = ServerImpl.fromRemoteNode(n);
            try {
                remoteComponent.register(this.node);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onNodeDisconnected(Node n){
        removeActiveNode(n);
        System.out.println("[System] " + n.getFullName() + " is disconnected.");
    }

    // REMOTE FUNCTIONS
    @Override
    public void register(Node remoteNode) {
        addActiveNode(remoteNode);
        System.out.println("[System] " + remoteNode.getFullName() + " is connected.");
    }

    @Override
    public Boolean connect() {
        return true;
    }

    @Override
    public Player login(String username, String password) throws RemoteException {
        // Init player object
        Player player = new Player(username, password);

        // TO-DO Check credentials
        if(true) {
            player.setAuthenticated(true);
            try {
                player.setHostAddress(RemoteServer.getClientHost());
                addActivePlayer(player);
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
            System.out.println("[System] Player " + player + " has logged in.");
        } else {
            System.out.println("[Error] Bad credentials.");
        }
        return player;
    }

    @Override
    public void logout(Player player) throws RemoteException {
        //player.setAuthenticated(false);
        //System.out.println("[System] Player '" + player + "' has logout.");
    }

}
