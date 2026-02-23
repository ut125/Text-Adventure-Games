package edu.uob;

import java.util.LinkedList;
import java.util.List;

public class Player {
    private final String name;
    private GameEntity.Location currentLocation;
    private final List<GameEntity> inventory;
    private int health;


    public Player(String name, GameEntity.Location startLocation) {
        this.name = name;
        this.currentLocation = startLocation;
        this.inventory = new LinkedList<>();
        this.health = 3;
    }

    //player status
    public String getName() {
        return name;
    }

    public GameEntity.Location getLocation() {
        return currentLocation;
    }

    public void setLocation(GameEntity.Location newLocation) {
        this.currentLocation = newLocation;
    }

    //player health
    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public String checkHealth(Health health) {
        return String.format("Your health is: %d", health.getHealth(this.name));
    }

    //player backpack
    public void addInPack(GameEntity item) {
        this.inventory.add(item);
    }

    public boolean checkPack(String itemName) {
        for (GameEntity item : this.inventory) {
            if (item.getName().equals(itemName)) {
                return true;
            }
        }
        return false;
    }

    public void removeItem(String itemName) {
        List<GameEntity> itemsToRemove = new LinkedList<>();
        for (GameEntity item : this.inventory) {
            if (item.getName().equals(itemName)) {
                itemsToRemove.add(item);
            }
        }
        this.inventory.removeAll(itemsToRemove);
    }

    public List<GameEntity> getInventory() {
        return this.inventory;
    }

    public String showInventory() {
        if (this.inventory.isEmpty()) {
            return "Your inventory is empty";
        }
        StringBuilder inventoryList = new StringBuilder("Your inventory contains:\n");
        for (GameEntity item : this.inventory) {
            inventoryList.append("- ").append(item.getName()).append(": ")
                         .append(item.getDescription()).append("\n");
        }
        return inventoryList.toString();
    }

    //get and drop items
    public String GetItem(String itemName) {
        for (GameEntity item : this.currentLocation.getItems()) {
            if (item.getName().equals(itemName)) {
                this.addInPack(item);
                this.currentLocation.removeItem(itemName);
                return String.format("You picked up %s", itemName);
            }
        }
        return "Item not found";
    }

    public String dropItem(String itemName) {
        if (this.checkPack(itemName)) {
            GameEntity itemToDrop = null;
            for (GameEntity item : this.inventory) {
                if (item.getName().equals(itemName)) {
                    itemToDrop = item;
                    break;
                }
            }
            if (itemToDrop != null) {
                this.removeItem(itemName);
                this.currentLocation.addItem(itemToDrop);
                return String.format("You dropped %s", itemName);
            }
        }
        return "You don't have that item";
    }

    //look other
    public String lookAround(MultiPlayerHandler multiPlayerHandler) {
        StringBuilder description = new StringBuilder(
            String.format("\nYou are at: %s\n", this.currentLocation.getDescription()));

        description.append("You see the following artefacts:\n");
        for (GameEntity item : this.currentLocation.getItems()) {
            description.append(String.format("- %s: %s\n", item.getName(), item.getDescription()));
        }

        description.append("You see the following furniture:\n");
        for (GameEntity furniture : this.currentLocation.getFurniture()) {
            description.append(String.format("- %s: %s\n", furniture.getName(), furniture.getDescription()));
        }

        description.append("You see the following characters:\n");
        for (GameEntity character : this.currentLocation.getCharacters()) {
            description.append(String.format("- %s: %s\n", character.getName(), character.getDescription()));
        }

        description.append("You can go to the following locations:\n");
        for (GameEntity.Location location : this.currentLocation.getConnectedLocations()) {
            description.append(String.format("- %s\n", location.getName()));
        }

        description.append("You see the other players:\n");
        for (Player otherPlayer : multiPlayerHandler.getPlayers().values()) {
            if (!otherPlayer.equals(this) && otherPlayer.getLocation().equals(this.currentLocation)) {
                description.append(String.format("- %s\n", otherPlayer.getName()));
            }
        }

        return description.toString();
    }

}


