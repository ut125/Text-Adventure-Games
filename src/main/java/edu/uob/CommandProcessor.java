package edu.uob;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.uob.HandleInputCommand.Command;

public class CommandProcessor {
    private final Player player;
    private final List<GameAction> actions;
    private final Map<String, GameEntity.Location> locations;
    private final BuiltInCommandHandler builtInCommandHandler;
    private final Health health;

    public CommandProcessor(Player player, List<GameAction> actions, Map<String, GameEntity.Location> locations, 
                            BuiltInCommandHandler builtInCommandHandler, Health health) {
        this.player = player;
        this.actions = actions;
        this.locations = locations;
        this.builtInCommandHandler = builtInCommandHandler;
        this.health = health;
    }

    public String processCommand(String command) {

        try {
            Command parsedCommand = HandleInputCommand.parseCommand(command);

            String action = parsedCommand.getCommand();
            List<String> objects = parsedCommand.getObject();

            //match all behaviors (actions + key objects)
            List<GameAction> matchedActions = new LinkedList<>();

            for (GameAction gameAction : actions) {
                //check for trigger match any action
                if (gameAction.getTriggers().contains(action)) {
                    boolean allMatch = true;

                    //check all objects match
                    for (String obj : objects) {
                        if (!gameAction.getSubjects().contains(obj)) {
                            allMatch = false;
                            break;
                        }
                    }

                    if (allMatch) {
                        System.out.println(String.format("[Debug] Finding the matching action: %s", gameAction.getTriggers()));
                        matchedActions.add(gameAction);
                    }
                }
            }

            //check the number of matching actions
            if (matchedActions.size() > 1) {
                StringBuilder warningMessage = new StringBuilder("[Invalid command] There are multiple actions matching your command:\n");
                for (GameAction matchedAction : matchedActions) {
                    warningMessage.append("- ").append(matchedAction.getTriggers()).append(" (Narration: ")
                                .append(matchedAction.getNarration()).append(")\n");
                }
                warningMessage.append("Please specify your intent more clearly");
                return warningMessage.toString();

                //confirm that there is only one matching action then execute
            } else if (matchedActions.size() == 1) {
                GameAction matchedAction = matchedActions.get(0);
                if (checkConditions(matchedAction)) {
                    this.executeAction(matchedAction);
                    return matchedAction.getNarration();
                } else {
                    return String.format("[Invalid command] You cannot do '%s', because the required conditions are not met", action);
                }
            }

            //if there is no match, check the built-in commands
            String builtInCommandResult = builtInCommandHandler.handleBuiltInCommand(player, action, objects);
            return Objects.requireNonNullElse(builtInCommandResult, "[Invalid command] I don't understand that command");

        } catch (PlayerDeathException ex) {
            return ex.getMessage();
        } catch (RuntimeException ex) {
            return String.format("[Error] " , ex.getMessage());
        }
    }

    public static class PlayerDeathException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public PlayerDeathException(String message) {
            super(message);
        }
    }

    private boolean checkConditions(GameAction action) {
        System.out.println(String.format("[Debug] Checking the action conditions \n  Triggers: %s, Consumed: %s, Subjects: %s",
                action.getTriggers(), action.getConsumed(), action.getSubjects()));
    
        //check consumed item exists in the player's backpack or location
        for (String requiredItem : action.getConsumed()) {
            if (!"health".equalsIgnoreCase(requiredItem)) {
                boolean foundInPack = player.checkPack(requiredItem);

                boolean foundInLocation = false;
                for (GameEntity furniture : player.getLocation().getFurniture()) {
                    if (furniture.getName().equalsIgnoreCase(requiredItem)) {
                        foundInLocation = true;
                        break;
                    }
                }

                boolean foundInCharacters = false;
                for (GameEntity character : player.getLocation().getCharacters()) {
                    if (character.getName().equalsIgnoreCase(requiredItem)) {
                        foundInCharacters = true;
                        break;
                    }
                }
    
                if (!foundInPack && !foundInLocation && !foundInCharacters) {
                    System.out.println(String.format("[Debug] Missing the necessary items: %s", requiredItem));
                    return false;
                }
    
                /*if (foundInPack) {
                    System.out.println(String.format("[Debug] consumed in backpack: %s", requiredItem));
                }
    
                if (foundInLocation) {
                    System.out.println(String.format("[Debug] consumed in location: %s", requiredItem));
                }
    
                if (foundInCharacters) {
                    System.out.println(String.format("[Debug] consumed character in location: %s", requiredItem));
                }*/
            }
        }
    
        // check subjects: objects to be acted on by the action
        for (String requiredSubject : action.getSubjects()) {
            if (!"health".equalsIgnoreCase(requiredSubject)) {
                boolean foundInPack = player.checkPack(requiredSubject);
                boolean foundInLocation = player.getLocation().containsEntity(requiredSubject);

                boolean foundInCharacters = false;
                for (GameEntity character : player.getLocation().getCharacters()) {
                    if (character.getName().equalsIgnoreCase(requiredSubject)) {
                        foundInCharacters = true;
                        break;
                    }
                }
    
                if (!foundInPack && !foundInLocation && !foundInCharacters) {
                    System.out.println(String.format("[Debug] subjects not found: %s", requiredSubject));
                    return false;
                }

            }
        }
    
        System.out.println("[Debug] Action condition check passed");
        return true;
    }
    

    
    private void executeAction(GameAction action) {
        System.out.println(String.format("[Debug] The action will be executed: %s", action.getTriggers()));
    
        try {
            // handling of consumed
            for (String consumed : action.getConsumed()) {

                //consumption health
                if ("health".equals(consumed)) {
                    int beforeHealth = health.getHealth(player.getName());
                    String healthResult = health.modifyHealth(player.getName(), -1);
                    int afterHealth = health.getHealth(player.getName());
    
                    System.out.println(String.format("[Debug] health: %s (before: %d, after: %d)", healthResult, beforeHealth, afterHealth));
    
                    //detects player death and quickly stops
                    if ("You're dead".equals(healthResult)) {
                        Health.handlePlayerDeath(player, health);
                        throw new PlayerDeathException(healthResult);
                    }

                } else if (player.checkPack(consumed)) {
                    //check backpack
                    player.removeItem(consumed);
                    System.out.println(String.format("[Debug] Removed consumables from backpack: %s", consumed));

                } else {
                    //check location
                    boolean removed = false;
    
                    //check item
                    for (GameEntity item : new LinkedList<>(player.getLocation().getItems())) {
                        if (item.getName().equals(consumed)) {
                            player.getLocation().getItems().remove(item);
                            removed = true;
                            System.out.println(String.format("[Debug] Removed item from location: %s", consumed));
                            break;
                        }
                    }
    
                    //check furniture
                    if (!removed) {
                        for (GameEntity furniture : new LinkedList<>(player.getLocation().getFurniture())) {
                            if (furniture.getName().equals(consumed)) {
                                player.getLocation().getFurniture().remove(furniture);
                                removed = true;
                                System.out.println(String.format("[Debug] Removed furniture from location: %s", consumed));
                                break;
                            }
                        }
                    }
    
                    //check character
                    if (!removed) {
                        for (GameEntity character : new LinkedList<>(player.getLocation().getCharacters())) {
                            if (character.getName().equalsIgnoreCase(consumed)) {
                                player.getLocation().getCharacters().remove(character);
                                removed = true;
                                System.out.println(String.format("[Debug] Removed furniture from character: %s", consumed));
                                break;
                            }
                        }
                    }
    
                    if (!removed) {
                        System.out.println(String.format("[Error] Consumables not found: %s", consumed));
                    }
                }
            }
    
            //generate a new item or location
            for (String produced : action.getProduced()) {
    
                if ("health".equalsIgnoreCase(produced)) {
                    int beforeHealth = health.getHealth(player.getName());
                    String healthResult = health.modifyHealth(player.getName(), 1);
                    int afterHealth = health.getHealth(player.getName());
    
                    System.out.println(String.format("[Debug] health: %s (before: %d, after: %d)", healthResult, beforeHealth, afterHealth));

                } else {
                    //check location connection
                    GameEntity.Location newLocation = locations.get(produced);
                    if (newLocation != null) {
                        if (!player.getLocation().getConnectedLocations().contains(newLocation)) {
                            player.getLocation().addConnectedLocation(newLocation);
                        } else {
                            System.out.println(String.format("[Debug] %s already existed", produced));
                        }
                    } else {
                        //check item
                        GameEntity.Item newItem = new GameEntity.Item(produced, "Generated by action");
                        if (!player.getLocation().getItems().contains(newItem)) {
                            player.getLocation().addItem(newItem);
                            player.addInPack(newItem);
                        } else {
                            System.out.println(String.format("Debug: %s already existed", produced));
                        }
                    }
                }
            }

        } catch (PlayerDeathException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println(String.format("[Error] Unknown Error: %s", ex.getMessage()));
        }
    }

}