package edu.uob;

import java.util.LinkedList;
import java.util.List;

public abstract class GameEntity {
    private final String name;
    private final String description;

    public GameEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // child classes: Location
    public static class Location extends GameEntity {
        private final List<GameEntity> items;
        private final List<GameEntity> furniture;
        private final List<GameEntity> characters;
        private final List<Location> connectedLocations;

        public Location(String name, String description) {
            super(name, description);
            this.items = new LinkedList<>();
            this.furniture = new LinkedList<>();
            this.characters = new LinkedList<>();
            this.connectedLocations = new LinkedList<>();
        }

        public void addItem(GameEntity item) {
            items.add(item);
            System.out.println(String.format("[Debug] Add item %s in %s", item.getName(), this.getName()));
        }

        public void addFurniture(GameEntity item) {
            furniture.add(item);
        }

        public void addCharacter(GameEntity character) {
            if (!characters.contains(character)) {
                characters.add(character);
                if (character instanceof Character) {
                    //set the location to the character's current location
                    ((Character) character).setLocation(this);
                }
                System.out.println(String.format("[Debug] Add character %s in %s", character.getName(), this.getName()));
            }
        }

        public void addConnectedLocation(Location location) {
            connectedLocations.add(location);
        }

        //remove
        public void removeItem(String itemName) {
            for (GameEntity item : new LinkedList<>(items)) {
                if (item.getName().equals(itemName)) {
                    items.remove(item);
                    System.out.println(String.format("[Debug] Removed item -> %s", itemName));
                    break;
                }
            }
        }


        //get
        public List<GameEntity> getItems() {
            return items;
        }

        public List<GameEntity> getFurniture() {
            return furniture;
        }

        public List<GameEntity> getCharacters() {
            return characters;
        }

        public List<Location> getConnectedLocations() {
            return connectedLocations;
        }

        //check if the location contains the entity
        public boolean containsEntity(String entityName) {
            for (GameEntity item : items) {
                if (item.getName().equals(entityName)) {
                    return true;
                }
            }
            for (GameEntity furnitureItem : furniture) {
                if (furnitureItem.getName().equals(entityName)) {
                    return true;
                }
            }
            for (GameEntity character : characters) {
                if (character.getName().equals(entityName)) {
                    return true;
                }
            }
            return false;
        }
    }

    //child classes: Item
    public static class Item extends GameEntity {
        public Item(String name, String description) {
            super(name, description);
        }
    }

    //child classes:: Character
    public static class Character extends GameEntity {
        private Location location;

        public Character(String name, String description) {
            super(name, description);
        }

        public String getName() {
            return super.getName();
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }
    }
}




