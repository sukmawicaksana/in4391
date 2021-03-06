package nl.tudelft.in4391.da.unit;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by arkkadhiratara on 3/22/16.
 */
public class Unit implements Serializable {
    private UUID id;

    String name;
    String type;
    Integer x;
    Integer y;

    protected Thread runnerThread;
    public boolean running = true;

    // Turn delay
    protected int timeBetweenTurns;

    // Health
    protected Integer maxHitPoints;
    protected Integer hitPoints;

    // Attack points
    protected Integer attackPoints;

    public Unit(String name) {
        this(UUID.randomUUID(), name, null, null, null);
    }

    public Unit(String name, String type) {
        this(UUID.randomUUID(), name, type, null, null);

    }

    public Unit(UUID id, String name, String type, Integer x, Integer y) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;

    }

    public UUID getId() {
        return id;
    }

    public void setDead (){
        this.running = false;
    }

    public String getName() {
        return this.name;
    }

    public String getFullName() { return this.type+" "+this.name; }

    public void setCoord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getCoord() {
        return "( "+this.x+", "+this.y+" )";
    }

    public void setHitPoints(int hitPoints) {
        if (hitPoints > this.getMaxHitPoints()) {
            this.hitPoints = this.getMaxHitPoints();
        } else {
            this.hitPoints = hitPoints;
        }
    }

    public int getHitPoints() {
        return hitPoints;
    }

    // Max HP for after heal and for status of user
    public Integer getMaxHitPoints() {
        return maxHitPoints;
    }

    public Integer getAttackPoints() {
        return attackPoints;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() { return y; }

    public int getTurnDelay(){
        return timeBetweenTurns;
    }

    public String getType() {
        return this.type;
    }

    public boolean equals(Object c) {
        if(!(c instanceof Unit)) {
            return false;
        }

        Unit that = (Unit) c;
        return this.getId().equals(that.getId());
    }

    @Override
    public Unit clone() {
        return new Unit(id, name, type, x, y);
    }
}
