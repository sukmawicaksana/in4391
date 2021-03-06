package nl.tudelft.in4391.da.unit;

import nl.tudelft.in4391.da.GameState;

/**
 * Created by arkkadhiratara on 3/22/16.
 */

public class Dragon extends Unit {

    // Minimum and maximum delay between turns
    public static final int MIN_TIME_BETWEEN_TURNS = 2;
    public static final int MAX_TIME_BETWEEN_TURNS = 7;

    // The minimum and maximum amount of hitpoints that a particular dragon starts with
    public static final int MIN_HITPOINTS = 50;
    public static final int MAX_HITPOINTS = 100;
    // The minimum and maximum amount of attackpoints that a particular dragon has
    public static final int MIN_ATTACKPOINTS = 5;
    public static final int MAX_ATTACKPOINTS = 20;

    public Dragon(String name) {
        super(name,"Dragon");

        // Initialize hitpoints and attackpoints for each Dragon
        this.hitPoints = (int) (Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS);
        this.attackPoints = (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS);

        // Assign max health for each Dragon
        this.maxHitPoints = this.hitPoints;

        // Create a random delay for each Dragon
        this.timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;

    }
}
