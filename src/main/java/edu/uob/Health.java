package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class Health {
    private static final int MAX_HEALTH = 3;
    private static final int MIN_HEALTH = 0;
    private final Map<String, Integer> playerHealth;
    private final GameEntity.Location startingLocation;

    public GameEntity.Location getStartingLocation() {
        return startingLocation;
    }
    
    public Health(GameEntity.Location startingLocation) {
        this.startingLocation = startingLocation;
        this.playerHealth = new HashMap<>();
    }

    //initialize player life value
    public void initializeHealth(String playerName) {
        playerHealth.put(playerName, MAX_HEALTH);
    }

    //get the current player's life value
    public int getHealth(String playerName) {
        if (!playerHealth.containsKey(playerName)) {
            System.err.println(String.format("[Error] Player health not found: " , playerName));
        }
        return playerHealth.getOrDefault(playerName, MAX_HEALTH);
    }

    //modify player's life value
    public String modifyHealth(String playerName, int delta) {
        int currentHealth = this.getHealth(playerName);
        int newHealth = Math.min(MAX_HEALTH, Math.max(MIN_HEALTH, currentHealth + delta));
        
        playerHealth.put(playerName, newHealth);
        //if health is 0 return death message
        if (newHealth == MIN_HEALTH) {
            return "You're dead";
        }
        
        return String.format("Your health is now: %d", newHealth);
    }

    public static void handlePlayerDeath(Player player, Health health) {
        System.out.println("[START] Enter handlePlayerDeath");

        if (health.getHealth(player.getName()) == 0) {

            //dropping all player's items
            for (GameEntity item : player.getInventory()) {
                player.getLocation().addItem(item);
                System.out.println(String.format("[Debug] Drop item %s to %s", item.getName(), player.getLocation().getName()));
            }
            player.getInventory().clear();

            //transport the player to the starting location
            player.setLocation(health.getStartingLocation());
            System.out.println(String.format("[Debug] player %s be transported to %s", player.getName(), health.getStartingLocation().getName()));

            //reset Health
            health.initializeHealth(player.getName());
            System.out.println(String.format("[Debug] Reset Health to %d", health.getHealth(player.getName())));

        }
    }

}
