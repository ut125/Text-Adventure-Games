package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import edu.uob.CommandProcessor.PlayerDeathException;



public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    private final Map<String, GameEntity.Location> locations;
    private final Map<String, List<GameEntity.Item>> artefacts;
    private final Map<String, List<GameEntity.Item>> furniture;
    private final Map<String, List<GameEntity.Item>> characters;
    private List<GameAction> actions;
    private final MultiPlayerHandler multiPlayerHandler;
    private final Health health;
    private final BuiltInCommandHandler builtInCommandHandler;

    public static void main(String[] args) throws IOException {
        StringBuilder entitiesPath = new StringBuilder();
        entitiesPath.append("config").append(File.separator).append("extended-entities.dot");
        File entitiesFile = Paths.get(entitiesPath.toString()).toAbsolutePath().toFile();
        
        StringBuilder actionsPath = new StringBuilder();
        actionsPath.append("config").append(File.separator).append("extended-actions.xml");
        File actionsFile = Paths.get(actionsPath.toString()).toAbsolutePath().toFile();
        
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    

    
    public GameServer(File entitiesFile, File actionsFile) {
        System.out.println("[START] Creating GameServer");

        StringBuilder startingLocationHolder = new StringBuilder(); // 
        Set<String> validWords = new HashSet<>();
        Set<String> validTriggers = new HashSet<>();
        Set<String> validSubjects = new HashSet<>();
    
        //parsing XML
        try {
            this.actions = AnalyzeXML.AnalyzeXMLs(actionsFile, validTriggers, validSubjects);
        } catch (Exception e) {
            System.err.println(String.format("[Error] Error parsing actions file: " , e.getMessage()));
            this.actions = new LinkedList<>();
        }
    
        // parsing dot
        AnalyzeDot.AnalysisResult analysisResult = AnalyzeDot.analyzeDots(entitiesFile, startingLocationHolder, validWords);
        if (analysisResult == null || analysisResult.locations.isEmpty()) {
            throw new IllegalStateException("[Error] No locations found in entities file");
        }

        //initializing map
        this.locations = analysisResult.locations;
        this.artefacts = analysisResult.artefacts;
        this.furniture = analysisResult.furniture;
        this.characters = analysisResult.characters;
        System.out.println("\nLocations List: ");
        System.out.println(String.format(String.format("[LIST] Artefacts: %s" , artefacts.keySet())));
        System.out.println(String.format(String.format("[LIST] Furniture: %s" , furniture.keySet())));
        System.out.println(String.format(String.format("[LIST] Characters: %s" , characters.keySet())));

        Print.printAttributeMap();

        //initializing Start location
        String startingLocationName = startingLocationHolder.toString();
        System.out.println(String.format("[Debug] Starting location name: %s" , startingLocationName));
        GameEntity.Location startingLocation = locations.get(startingLocationName);
        if (startingLocation == null) {
            throw new IllegalStateException(String.format("[Error] Starting location not found: %s" , startingLocationName));
        }

        this.health = new Health(startingLocation);
        this.multiPlayerHandler = new MultiPlayerHandler(startingLocation, health);
        //this.actions = AnalyzeXML.loadActions(actionsFile, validWords);

        Set<String> internalCommands = Set.of("inventory", "inv", "get", "drop", "goto", "look", "health");
        HandleInputCommand.initializeValidCommands(locations, artefacts, furniture, characters, internalCommands, validTriggers, actions);

        this.builtInCommandHandler = new BuiltInCommandHandler(locations, multiPlayerHandler, health);
    }

    public String handleCommand(String command) {
        System.out.println(String.format("[COMMAND] Server receives command: %s" , command));

        if (command == null || command.isEmpty()) {
            return "[Error] Invalid command format. Please provide a command.";
        }

        List<String> commandParts = new LinkedList<>(List.of(command.split(":", 2)));
        if (commandParts.size() != 2) {
            return "[Error] Unable to process the command";
        }

        String playerName = commandParts.get(0).trim();
        String playerCommand = commandParts.get(1).trim();
        System.out.println(String.format("[Debug] playerName: %s" , playerName , ", Command: %s" , playerCommand));

        if (!isValidPlayerName(playerName)) {
            return "[Error] Invalid player name";
        }

        Player player = multiPlayerHandler.getOrCreatePlayer(playerName);
        System.out.println(String.format("[Debug] Creative players: %s" , player.getName()));

        CommandProcessor commandProcessor = new CommandProcessor(player, actions, locations, builtInCommandHandler, health);
        System.out.println("\n[Debug] Starting CommandProcessor");

        try {
            String response = commandProcessor.processCommand(playerCommand);
            System.out.println(String.format("[RESPONSE] ProcessCommand response: %s" , response));
            return response;
        } catch (PlayerDeathException e) {
            return e.getMessage();
        }
    }

    private boolean isValidPlayerName(String playerName) {
        String validPattern = "^[a-zA-Z0-9_'\\-@]+$";
        return playerName.matches(validPattern);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println(String.format("Server listening on port " , portNumber));
            while (!Thread.interrupted()) {
                try {
                    this.blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println(String.format("Received message from ",incomingCommand));
                String result = this.handleCommand(incomingCommand);
                writer.write(result);
                writer.write(String.format("\n" , END_OF_TRANSMISSION , "\n"));
                writer.flush();
            }
        }
    }
}