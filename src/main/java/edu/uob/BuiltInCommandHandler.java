package edu.uob;

import java.util.List;
import java.util.Map;

public class BuiltInCommandHandler {
    private final Map<String, GameEntity.Location> locations;
    private final MultiPlayerHandler multiPlayerHandler;
    private final Health health;

    public BuiltInCommandHandler(Map<String, GameEntity.Location> locations,
            MultiPlayerHandler multiPlayerHandler, Health health) {
        
        this.locations = locations;
        this.multiPlayerHandler = multiPlayerHandler;
        this.health = health;

    }

    public String handleBuiltInCommand(Player player, String action, List<String> object) {
        System.out.println("\n[Debug] Handling built-in command");
        switch (action) {
            case "inventory":
            case "inv":
                return player.showInventory();

            case "get":
                return this.handleGetCommand(player, object);

            case "drop":
                return this.handleDropCommand(player, object);

            case "goto":
                return this.handleGotoCommand(player, object);

            case "look":
                return player.lookAround(multiPlayerHandler);

            case "health":
                return player.checkHealth(health);

            default:
                return "[Invalid command] I don't understand that command";
        }
    }

    private String handleGetCommand(Player player, List<String> object) {
        if (object.isEmpty()) {
            return "[Invalid command] Get what?";
        }
        if (object.size() > 1) {
            return "[Invalid command] Compound command is invalid";
        }
        return player.GetItem(object.get(0));
    }


    private String handleDropCommand(Player player, List<String> object) {
        if (object.isEmpty()) {
            return "[Invalid command] Drop what?";
        }
        if (object.size() > 1) {
            return "[Invalid command] Compound command is invalid";
        }
        return player.dropItem(object.get(0));
    }


    private String handleGotoCommand(Player player, List<String> object) {
        if (object.isEmpty()) {
            return "[Invalid command] You need to write where you want to go";
        }
    
        //get current and target locations
        GameEntity.Location currentLocation = player.getLocation();
        String targetLocationName = object.get(0);
        GameEntity.Location targetLocation = locations.get(targetLocationName.toLowerCase());
    
        if (targetLocation == null) {
            return String.format("[Invalid command] The location '%s' does not exist", targetLocationName);
        }
    
        //check if the connection exists
        if (!currentLocation.getConnectedLocations().contains(targetLocation)) {
            return String.format("[Invalid command]You can't go directly to '%s' from '%s'", targetLocation.getName(), currentLocation.getName());
        }
    
        //update Player Location
        player.setLocation(targetLocation);
        return String.format("You moved to %s.", targetLocation.getName());
    }

}
