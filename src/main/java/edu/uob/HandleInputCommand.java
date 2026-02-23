package edu.uob;

import java.util.*;

public class HandleInputCommand {

    // Built-in instruction set
    private static final Set<String> HandleInputCommands = new HashSet<>(Set.of("inventory", "inv", "get", "drop", "goto", "look", "health"));

    private static Map<String, GameEntity.Location> locations = new HashMap<>();
    private static Map<String, List<GameEntity.Item>> artefacts = new HashMap<>();
    private static Map<String, List<GameEntity.Item>> furniture = new HashMap<>();
    private static Map<String, List<GameEntity.Item>> characters = new HashMap<>();
    private static final Set<String> validTriggers = new HashSet<>();

    //for storing all game actions
    private static final LinkedList<GameAction> actions = new LinkedList<>();

    //clear old game data
    public static void initializeValidCommands(Map<String, GameEntity.Location> loc, Map<String, List<GameEntity.Item>> art,
                                               Map<String, List<GameEntity.Item>> fur, Map<String, List<GameEntity.Item>> cha,
                                               Set<String> newInternalCommands, Set<String> newValidTriggers,
                                               List<GameAction> newActions) {

        actions.clear();
        actions.addAll(newActions);

        validTriggers.clear();
        validTriggers.addAll(newValidTriggers);

        HandleInputCommands.clear();
        HandleInputCommands.addAll(newInternalCommands);

        locations = loc;
        artefacts = art;
        furniture = fur;
        characters = cha;
    }


    //parsing player input commands
    public static Command parseCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new Command("[Error] Null command or invalid input received", new LinkedList<>());
        }
    
        System.out.println(String.format("[Debug] original command: %s", input));

        String cleanedInput = input.trim().replaceAll("^\\w+:\\s*", "");
    
        //cut command
        List<String> words = new LinkedList<>(List.of(cleanedInput.trim().toLowerCase().split("\\s+")));

        //retain valid game words
        words = HandleInputCommand.filterWordsUsingMaps(words);
        System.out.println(String.format("[Debug] retain valid game words: %s", words));
    
        if (words.isEmpty()) {
            return new Command("[Error] No single words are valid", new LinkedList<>());
        }
    
        //check HandleInputCommands
        String firstWord = words.get(0);
        if (HandleInputCommands.contains(firstWord)) {
            System.out.println(String.format("[Debug] Match built-in commands %s", firstWord));
            return new Command(firstWord, words.subList(1, words.size()));
        }

        Map<String, List<String>> attributeMap = AnalyzeXML.getAttributeMap();

        String matchedTrigger = null;
        List<String> matchedSubjects = new LinkedList<>();
        List<String> matchedConsumed = new LinkedList<>();
        List<String> matchedProduced = new LinkedList<>();

        //check triggers
        for (String word : words) {
            if (attributeMap.get("triggers").contains(word)) {
                matchedTrigger = word;
                System.out.println(String.format("[Debug] Match to trigger: %s", word));
                break;
            }
        }

        if (matchedTrigger == null) {
            return new Command("[Error] Invalid command", new LinkedList<>());
        }
    
        // check Subjects Consumed Produced
        for (String word : words) {
            if (attributeMap.get("subjects").contains(word)) {
                matchedSubjects.add(word);
            } else if (attributeMap.get("consumed").contains(word)) {
                matchedConsumed.add(word);
            } else if (attributeMap.get("produced").contains(word)) {
                matchedProduced.add(word);
            }
        }

        List<String> objects = new LinkedList<>();
        objects.addAll(matchedSubjects);
        objects.addAll(matchedConsumed);
        objects.addAll(matchedProduced);
    
        return new Command(matchedTrigger, objects);
    }
    
    


    //use Map data and built-in commands to filter for valid words
    public static List<String> filterWordsUsingMaps(List<String> words) {
        List<String> filteredWords = new LinkedList<>();
        for (String word : words) {
            boolean isValidWord = locations.containsKey(word) || HandleInputCommand.containsInEntityMap(word, artefacts) ||
            HandleInputCommand.containsInEntityMap(word, furniture) || HandleInputCommand.containsInEntityMap(word, characters) ||
                    HandleInputCommands.contains(word)|| validTriggers.contains(word);

            if (isValidWord) {
                filteredWords.add(word);
            } else {
                System.out.println(String.format("[Debug] Invalid word: %s (filtered)", word));
            }
        }
        return filteredWords;
    }


    //checks if it exists in a specific Map value
    private static boolean containsInEntityMap(String word, Map<String, List<GameEntity.Item>> entityMap) {
        for (List<GameEntity.Item> itemList : entityMap.values()) {
            for (GameEntity.Item entity : itemList) {
                if (entity.getName().equalsIgnoreCase(word)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static class Command {
        private final String command;
        private final List<String> object;

        public Command(String command, List<String> object) {
            this.command = command;
            this.object = object;
        }

        public String getCommand() {
            return command;
        }

        public List<String> getObject() {
            return object;
        }
    }
}
