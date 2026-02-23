package edu.uob;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class AnalyzeDot {

    //store different things
    public static class AnalysisResult {
        Map<String, GameEntity.Location> locations;
        Map<String, List<GameEntity.Item>> artefacts;
        Map<String, List<GameEntity.Item>> furniture;
        Map<String, List<GameEntity.Item>> characters;

        public AnalysisResult() {
            this.locations = new LinkedHashMap<>();
            this.artefacts = new LinkedHashMap<>();
            this.furniture = new LinkedHashMap<>();
            this.characters = new LinkedHashMap<>();
        }
    }

    // analysis of DOT files
    public static AnalysisResult analyzeDots(File dotFile, StringBuilder startingLocationHolder, Set<String> validWords) {
        try {
            //check if the file exists
            if (dotFile == null) {
                throw new IllegalArgumentException("[Error] File cannot be null");
            }
            if (!dotFile.exists()) {
                throw new FileNotFoundException(String.format("[Error] File not found: %s", dotFile.getAbsolutePath()));
            }

            BufferedReader reader = new BufferedReader(new FileReader(dotFile));

            String line;
            String currentCategory = null;
            GameEntity.Location currentLocation = null;
            //start location name
            String startingLocation = null;
            AnalysisResult result = new AnalysisResult();

            Pattern locationPattern = Pattern.compile("\\s*(\\S+)\\s*\\[description\\s*=\\s*\"([^\"]+)\"\\s*]");
            Pattern itemPattern = Pattern.compile("\\s*(\\w+)\\s*\\[description\\s*=\\s*\"([^\"]+)\"\\s*]");

            //remove the blanks and delete ;
            while ((line = reader.readLine()) != null) {
                line = line.trim().replace(";", "");

                if (line.startsWith("subgraph")) {
                    if (line.contains("artefacts")) {
                        currentCategory = "artefacts";
                    } else if (line.contains("furniture")) {
                        currentCategory = "furniture";
                    } else if (line.contains("characters")) {
                        currentCategory = "characters";
                    } else {
                        currentCategory = null;
                    }
                } else if (line.equals("}")) {
                    currentCategory = null;
                }

                //check if is a location
                Matcher locationMatcher = locationPattern.matcher(line);
                if (locationMatcher.matches() && currentCategory == null) {
                    String locationName = locationMatcher.group(1);
                    String description = locationMatcher.group(2);
                    //create a new location object
                    currentLocation = new GameEntity.Location(locationName, description);
                    //map for recording all locations
                    result.locations.put(locationName, currentLocation);
                    //add validWords
                    validWords.add(locationName);
                    System.out.println(String.format("[Debug] Analyze the location: %s, description: %s"
                                        , locationName, description));

                    //the first place to read
                    if (startingLocation == null) {
                        startingLocation = locationName;
                        System.out.println(String.format("[Debug] Set the starting location: %s", startingLocation));
                    }
                }

                //check if is an item :artefacts furniture characters
                Matcher itemMatcher = itemPattern.matcher(line);
                if (itemMatcher.matches() && currentLocation != null && currentCategory != null) {
                    String itemName = itemMatcher.group(1);
                    String itemDescription = itemMatcher.group(2);
                    GameEntity.Item item = new GameEntity.Item(itemName, itemDescription);

                    if (currentCategory.equals("artefacts")) {
                        currentLocation.addItem(item);
                        if (!result.artefacts.containsKey(currentLocation.getName())) {
                            result.artefacts.put(currentLocation.getName(), new LinkedList<>());
                        }
                        result.artefacts.get(currentLocation.getName()).add(item);
                    } else if (currentCategory.equals("furniture")) {
                        currentLocation.addFurniture(item);
                        if (!result.furniture.containsKey(currentLocation.getName())) {
                            result.furniture.put(currentLocation.getName(), new LinkedList<>());
                        }
                        result.furniture.get(currentLocation.getName()).add(item);
                    } else if (currentCategory.equals("characters")) {
                        currentLocation.addCharacter(item);
                        if (!result.characters.containsKey(currentLocation.getName())) {
                            result.characters.put(currentLocation.getName(), new LinkedList<>());
                        }
                        result.characters.get(currentLocation.getName()).add(item);
                    }
                }


                if (line.contains("->")) {

                    //turn the result into a list: place1 place2
                    LinkedList<String> parts = new LinkedList<>(List.of(line.split("->")));
                    if (parts.size() != 2) {
                        System.out.println(String.format("[Error] Invalid connection format: %s", line));
                        continue;
                    }

                    //the first value in the LinkedList is stored in start
                    Iterator<String> partIterator = parts.iterator();
                    String start = partIterator.next().trim();
                    String end = partIterator.next().trim();

                    GameEntity.Location startLocation = null;
                    GameEntity.Location endLocation = null;

                    for (Map.Entry<String, GameEntity.Location> entry : result.locations.entrySet()) {
                        if (entry.getKey().equals(start)) {
                            startLocation = entry.getValue();
                        }
                        if (entry.getKey().equals(end)) {
                            endLocation = entry.getValue();
                        }
                        //if both locations are found
                        if (startLocation != null && endLocation != null) {
                            break;
                        }
                    }

                    //check if the location exists and add the connection
                    if (startLocation != null && endLocation != null) {
                        startLocation.addConnectedLocation(endLocation);
                        System.out.println(String.format("[Debug] Added connection from %s to %s", start, end));
                    } else {
                        System.out.println(String.format("[Error] Connection failed -> %s", line));
                    }
                }

            }

            if (startingLocation != null) {
                startingLocationHolder.append(startingLocation);
            }

            reader.close();
            return result;
        } catch (Exception ex) {
            System.err.println(String.format("[Error] %s", ex.getMessage()));
        }
        

        return null;
    }

}
