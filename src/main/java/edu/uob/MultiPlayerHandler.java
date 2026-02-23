package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class MultiPlayerHandler {
    private final Map<String, Player> players = new HashMap<>();
    private final GameEntity.Location startingLocation;
    private final Health health;

    public MultiPlayerHandler(GameEntity.Location startingLocation, Health health) {
        this.startingLocation = startingLocation;
        this.health = health;
    }

    //creative players
    public Player getOrCreatePlayer(String playerName) {
        //Check if the player already exists
        if (!players.containsKey(playerName)) {
            System.out.println( String.format("Creating new player: " , playerName));
            Player newPlayer = new Player(playerName, startingLocation);
            health.initializeHealth(playerName);
            // add the new player to the map
            players.put(playerName, newPlayer);
        }
        return players.get(playerName);
    }


    public Map<String, Player> getPlayers() {
        return players;
    }
}


