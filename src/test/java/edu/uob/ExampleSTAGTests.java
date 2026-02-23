package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.Duration;

class ExampleSTAGTests {

  private GameServer server;

  // Create a new server _before_ every @Test
  @BeforeEach
  void setup() {
      File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
      File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
      server = new GameServer(entitiesFile, actionsFile);
    System.out.println("Game initialized successfully with entities and actions files.");
  }

  String sendCommandToServer(String command) {
    System.out.println("Sending command to server: " + command);
      // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
      return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
      "Server took too long to respond (probably stuck in an infinite loop)");
  }

  // A lot of tests will probably check the game state using 'look' - so we better make sure 'look' works well !
  @Test
  void testLook() {
  System.out.println("=== Testing 'look' command ===");
    String response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
  System.out.println("Response to 'look' command: " + response);
    assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
    assertTrue(response.contains("log cabin"), "Did not see a description of the room in response to look");
    assertTrue(response.contains("magic potion"), "Did not see a description of artifacts in response to look");
    assertTrue(response.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
    assertTrue(response.contains("forest"), "Did not see available paths in response to look");
  System.out.println("=== 'look' command test passed ===");
  }

  // Test that we can pick something up and that it appears in our inventory
  @Test
  void testGet()
  {
      System.out.println("=== Testing 'get' command ===");  
      String response;
      sendCommandToServer("simon: get potion");

      System.out.println("After 'get potion' command, checking inventory...");
      response = sendCommandToServer("simon: inv");
      response = response.toLowerCase();
      System.out.println("Inventory response: " + response);
      assertTrue(response.contains("potion"), "Did not see the potion in the inventory after an attempt was made to get it");
      
      System.out.println("Checking if the potion is still in the current room...");
      response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      System.out.println("Response to 'look' after getting potion: " + response);
      assertFalse(response.contains("potion"), "Potion is still present in the room after an attempt was made to get it");
      System.out.println("=== 'get' command test passed ===");
    }

  // Test that we can goto a different location (we won't get very far if we can't move around the game !)
  @Test
  void testGoto()
  {
      System.out.println("=== Testing 'goto' command ===");
      sendCommandToServer("simon: goto forest");

      System.out.println("After 'goto forest' command, checking the new location...");
      String response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      System.out.println("Response to 'look' after moving to forest: " + response);

      assertTrue(response.contains("key"), "Failed attempt to use 'goto' command to move to the forest - there is no key in the current location");
      System.out.println("=== 'goto' command test passed ===");
  }

  // Add more unit tests or integration tests here.

  @Test
  void testCustomAction_OpenTrapdoor() {
      System.out.println("=== Testing custom action: 'open trapdoor with key' ===");

    // Step 1: Goto forest to get the key
    String response = sendCommandToServer("simon: goto forest");
    response = response.toLowerCase();
    System.out.println("Response to 'goto forest': " + response);
    assertTrue(response.contains("forest"), "Failed to move to forest location");
    
    // Step 2: Get the key in the forest
    response = sendCommandToServer("simon: get key");
    response = response.toLowerCase();
    System.out.println("Response to 'get key': " + response);
    assertTrue(response.contains("you picked up key"), "Failed to pick up the key");
    response = sendCommandToServer("simon: inv");
    response = response.toLowerCase();
    System.out.println("Inventory response: " + response);

    // Step 3: Return to cabin
    response = sendCommandToServer("simon: goto cabin");
    response = response.toLowerCase();
    System.out.println("Response to 'goto cabin': " + response);
    assertTrue(response.contains("cabin"), "Failed to return to cabin");

    // Step 4: Open trapdoor with the key
    response = sendCommandToServer("simon: open trapdoor with key");
    response = response.toLowerCase();
    System.out.println("Response to 'open trapdoor with key': " + response);
    

    // Validate narration
    assertTrue(response.contains("you unlock the trapdoor and see steps leading down into a cellar"), 
        "Failed to execute 'open trapdoor with key' action");
    response = sendCommandToServer("simon: inv");
    response = response.toLowerCase();
    System.out.println("Inventory response: " + response);

    // Step 5: Look around to check the results
    response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
    System.out.println("Response to 'look' after opening trapdoor: " + response);

    // Validate that cellar is now accessible and key is consumed
    assertTrue(response.contains("cellar"), "Cellar was not produced after unlocking the trapdoor");
    assertFalse(response.contains("key"), "Key is still present after being consumed");
    
    System.out.println("=== 'open trapdoor with key' action test passed ===");
  }

  @Test
  void testActionRequiresItemAndCorrectLocation() {
      System.out.println("=== Testing action that requires an item and correct location ===");

      // Step 1: 嘗試在 cabin 執行動作，應該失敗
      String response = sendCommandToServer("simon: chop tree with axe");
      response = response.toLowerCase();
      System.out.println("Response to 'chop tree with axe' in cabin: " + response);
      assertTrue(response.contains("invalid command") || response.contains("missing"),
          "Action succeeded unexpectedly from wrong location");
      response = sendCommandToServer("simon: look");
        response = sendCommandToServer("simon: get key");
        assertTrue(response.contains("Invalid command")|| response.contains("Item not found"),
          "Action succeeded unexpectedly from wrong location"); 


      // Step 2: 移動到 forest
      response = sendCommandToServer("simon: goto forest");
      response = response.toLowerCase();
      System.out.println("Response to 'goto forest': " + response);
      assertTrue(response.contains("forest"), "Failed to move to forest");

      // Step 3: 在森林中確認 tree 存在
      response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      System.out.println("Response to 'look' in forest: " + response);
      assertTrue(response.contains("tree"), "Tree not found in forest");

      // Step 4: 嘗試執行動作但沒有 axe，應該失敗
      response = sendCommandToServer("simon: inv");
      response = response.toLowerCase();
      System.out.println("Inventory response: " + response);
      response = sendCommandToServer("simon: chop tree with axe");
      response = response.toLowerCase();
      System.out.println("Response to 'chop tree with axe' without axe: " + response);
      assertTrue(response.contains("invalid command") || response.contains("missing"),
          "Action succeeded unexpectedly without required item");

      // Step 5: 返回 cabin 撿起 axe
      response = sendCommandToServer("simon: goto cabin");
      response = response.toLowerCase();
      System.out.println("Response to 'goto cabin': " + response);
      assertTrue(response.contains("cabin"), "Failed to move back to cabin");

      response = sendCommandToServer("simon: get axe");
      response = response.toLowerCase();
      System.out.println("Response to 'get axe': " + response);
      assertTrue(response.contains("you picked up axe"), "Failed to pick up axe");

      // Step 6: 返回 forest 並執行動作
      response = sendCommandToServer("simon: goto forest");
      response = response.toLowerCase();
      System.out.println("Response to 'goto forest': " + response);
      assertTrue(response.contains("forest"), "Failed to move back to forest");
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("tree"), "tree in forest");
        response = sendCommandToServer("simon: inv");
        response = response.toLowerCase();
        System.out.println("Inventory response: " + response);
        // 確認 axe 在背包中
        assertTrue(response.contains("axe"), "axe in inventory");

      response = sendCommandToServer("simon: chop tree with axe");
      response = response.toLowerCase();
      System.out.println("Response to 'chop tree with axe' with axe: " + response);

      // 確認動作成功
      assertTrue(response.contains("you cut down the tree with the axe"), "Failed to execute action with required item");

      // Step 7: 確認 tree 被移除並生成 log
      response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      System.out.println("Response to 'look' in forest after chopping tree: " + response);
      assertFalse(response.contains("tree"), "Tree still present after chopping");

      System.out.println("=== 'Action requires item and correct location' test passed ===");
  }


  @Test
    void testCaseInsensitiveCommands() {
        System.out.println("=== Testing case-insensitive commands ===");
        String response = sendCommandToServer("SiMon: LoOk");
        response = response.toLowerCase();
        System.out.println("Response to 'SiMon: LoOk': " + response);
        assertTrue(response.contains("cabin"), "Failed to recognize case-insensitive command");
        System.out.println("=== Case-insensitive commands test passed ===");
    }

  @Test
  void testDecoratedCommands() {
      System.out.println("=== Testing decorated commands ===");
  
      // Step 1: 玩家撿起 axe（需要道具）
      String response = sendCommandToServer("simon: get axe");
      response = response.toLowerCase();
      System.out.println("Response to 'get axe': " + response);
      assertTrue(response.contains("you picked up axe"), "Failed to pick up axe");
  
      // Step 2: 移動到森林（因為 tree 在 forest 中）
      response = sendCommandToServer("simon: goto forest");
      response = response.toLowerCase();
      System.out.println("Response to 'goto forest': " + response);
      assertTrue(response.contains("forest"), "Failed to move to forest");
  
      // Step 3: 執行裝飾命令
      response = sendCommandToServer("simon:please chop the tree using the axe");
      response = response.toLowerCase();
      System.out.println("Response to 'please chop the tree using the axe': " + response);
  
      // 確認動作成功執行
      assertTrue(response.contains("you cut down the tree with the axe"), "Failed to interpret decorated command");
      System.out.println("=== Decorated commands test passed ===");
  }
  
  
  @Test
  void testWordOrderIndependence() {
      System.out.println("=== Testing word order independence ===");
  
      // Step 1: 玩家撿起 axe
      String response = sendCommandToServer("simon: get axe");
      response = response.toLowerCase();
      System.out.println("Response to 'get axe': " + response);
      assertTrue(response.contains("you picked up axe"), "Failed to pick up axe");
  
      // Step 2: 移動到森林
      response = sendCommandToServer("simon: goto forest");
      response = response.toLowerCase();
      System.out.println("Response to 'goto forest': " + response);
      assertTrue(response.contains("forest"), "Failed to move to forest");
  
      // Step 3: 測試重新排列的命令
      response = sendCommandToServer("simon:use axe to chop tree");
      response = response.toLowerCase();
      System.out.println("Response to 'use axe to chop tree': " + response);
  
      // 確認動作成功執行
      assertTrue(response.contains("you cut down the tree with the axe"), "Failed to interpret re-ordered command");
      System.out.println("=== Word order independence test passed ===");
  }
  
  
  @Test
  void testPartialCommands() {
      System.out.println("=== Testing partial commands ===");
  
      // Step 1: 移動到森林
      String response = sendCommandToServer("simon: goto forest");
      response = response.toLowerCase();
      System.out.println("Response to 'goto forest': " + response);
      assertTrue(response.contains("forest"), "Failed to move to forest");
  
      // Step 2: 撿取鑰匙（key）
      response = sendCommandToServer("simon: get key");
      response = response.toLowerCase();
      System.out.println("Response to 'get key': " + response);
      assertTrue(response.contains("you picked up key"), "Failed to pick up key");
  
      // Step 3: 返回木屋
      response = sendCommandToServer("simon: goto cabin");
      response = response.toLowerCase();
      System.out.println("Response to 'goto cabin': " + response);
      assertTrue(response.contains("cabin"), "Failed to return to cabin");
  
      // Step 4: 解鎖陷阱門（trapdoor）
      response = sendCommandToServer("simon: unlock trapdoor");
      response = response.toLowerCase();
      System.out.println("Response to 'unlock trapdoor': " + response);
  
      // 確認動作成功
      assertTrue(response.contains("you unlock the trapdoor"), "Failed to interpret partial command");
      System.out.println("=== Partial commands test passed ===");
  }
  
  
  @Test
  void testExcessEntities() {
      System.out.println("=== Testing commands with excess entities ===");
  
      // 玩家初始位置就在 cabin，直接執行包含多餘實體的命令
      String response = sendCommandToServer("simon: open trapdoor with hammer");
      response = response.toLowerCase();
      System.out.println("Response to 'open trapdoor with hammer': " + response);
  
      // 確認動作正確阻止
      assertTrue(response.contains("invalid command"), "Command with excess entities should not succeed");
      System.out.println("=== Excess entities test passed ===");
  }
  
  @Test
  void testAmbiguousCommands() {
      System.out.println("=== Testing ambiguous commands ===");

      // Step 1: 移動到森林
      String response = sendCommandToServer("simon: goto forest");
      response = response.toLowerCase();
      System.out.println("Response to 'goto forest': " + response);
      assertTrue(response.contains("forest"), "Failed to move to forest");

      // Step 2: 撿取鑰匙（key）
      response = sendCommandToServer("simon: get key");
      response = response.toLowerCase();
      System.out.println("Response to 'get key': " + response);
      assertTrue(response.contains("you picked up key"), "Failed to pick up key");
  
      // Step 3: 返回木屋
      response = sendCommandToServer("simon: goto cabin");
      response = response.toLowerCase();
      System.out.println("Response to 'goto cabin': " + response);
      assertTrue(response.contains("cabin"), "Failed to return to cabin");
      response = sendCommandToServer("simon: get potion");
        response = response.toLowerCase();
        System.out.println("Response to 'get potion': " + response);
        assertTrue(response.contains("you picked up potion"), "Failed to pick up potion");
        
  
      // 玩家初始位置就在 cabin，直接執行模糊命令
      response = sendCommandToServer("simon: open");
      response = response.toLowerCase();
      System.out.println("Response to 'open': " + response);
  
      // 確認提示模糊性
      assertTrue(response.contains("invalid command"), "Ambiguous command did not return proper warning");
      System.out.println("=== Ambiguous commands test passed ===");
  }
  
  @Test
  void testCompoundCommands() {
      System.out.println("=== Testing compound commands ===");
  
      // 玩家直接執行複合命令
      String response = sendCommandToServer("simon: get axe and coin");
      response = response.toLowerCase();
      System.out.println("Response to 'get axe and coin': " + response);

      response = sendCommandToServer("simon: get axe then go forest");
      response = response.toLowerCase();
      System.out.println("Response to 'get axe and coin': " + response);
  
      // 確認伺服器正確拒絕複合命令
      assertTrue(response.contains("invalid command"), "Compound commands should not be allowed");
      System.out.println("=== Compound commands test passed ===");
  }
  

  @Test
  void testErrorHandling() {
      System.out.println("=== Testing error handling ===");
      String response = sendCommandToServer("simon:fly to the moon");
      response = response.toLowerCase();
      System.out.println("Response to 'fly to the moon': " + response);
      assertTrue(response.contains("invalid command"), "Invalid commands should return proper error");
      System.out.println("=== Error handling test passed ===");
  }

  @Test
  void testMultiplePlayers() {
        System.out.println("=== Testing multiple players actions ===");

        // 測試新玩家 'simon'
        String response = server.handleCommand("simon: look");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You are at:"), "Failed to process look command for player 'simon'.");
        assertTrue(response.contains("cabin"), "Failed to verify initial location for player 'simon'.");

        // simon 拿起物品
        response = server.handleCommand("simon: get axe");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up axe"), "Failed to allow player 'simon' to pick up axe.");

        // simon 移動到另一個地點
        response = server.handleCommand("simon: goto forest");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to forest"), "Failed to move player 'simon' to the forest.");

        // 測試新玩家 'john'
        response = server.handleCommand("john: look");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You are at:"), "Failed to process look command for player 'john'.");
        assertTrue(response.contains("cabin"), "Failed to verify initial location for player 'john'.");

        // john 拿起物品
        response = server.handleCommand("john: get potion");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up potion"), "Failed to allow player 'john' to pick up potion.");

        // john 嘗試移動到 simon 的地點
        response = server.handleCommand("john: goto forest");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to forest"), "Failed to move player 'john' to the forest.");

        // 確認 simon 和 john 都在同一地點
        response = server.handleCommand("simon: look");
        System.out.println("Response: " + response);
        assertTrue(response.contains("john"), "Failed to see player 'john' in the same location as 'simon'.");

        response = server.handleCommand("john: look");
        System.out.println("Response: " + response);
        assertTrue(response.contains("simon"), "Failed to see player 'simon' in the same location as 'john'.");

        // simon 執行動作：砍樹
        response = server.handleCommand("simon: chop tree with axe");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You cut down the tree with the axe"), "Failed to process chop action for player 'simon'.");

        System.out.println("=== Multiple players actions test passed ===");
    }

    @Test
    void testHealth() {
        System.out.println("=== Testing player actions and health ===");

        // 玩家移動到 forest 撿起 key
        String response = server.handleCommand("simon: goto forest");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to forest."), "Failed to move to forest.");

        response = server.handleCommand("simon: get key");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up key"), "Failed to pick up key.");

        // 回到 cabin 撿起 potion 和 coin
        response = server.handleCommand("simon: goto cabin");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to cabin."), "Failed to move back to cabin.");

        response = server.handleCommand("simon: get potion");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up potion"), "Failed to pick up potion.");

        response = server.handleCommand("simon: get coin");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up coin"), "Failed to pick up coin.");

        // 使用 key 打開 trapdoor
        response = server.handleCommand("simon: open trapdoor with key");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You unlock the trapdoor and see steps leading down into a cellar"), "Failed to open trapdoor.");

        // 進入 cellar
        response = server.handleCommand("simon: goto cellar");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to cellar"), "Failed to move into cellar.");

        // 使用 coin 和 elf 交易 shovel
        response = server.handleCommand("simon: look");
        System.out.println("Response: " + response);
        response = server.handleCommand("simon: inv");
        System.out.println("Response: " + response);
        response = server.handleCommand("simon: pay elf with coin");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You pay the elf your silver coin and he produces a shovel"), "Failed to purchase shovel from elf.");
        // 撿起 shovel
        response = server.handleCommand("simon: get shovel");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up shovel"), "Failed to pick up shovel.");

        response = sendCommandToServer("simon: inv");
        response = response.toLowerCase();
        System.out.println("Inventory response: " + response);

        // 攻擊 elf（生命值降低）
        response = server.handleCommand("simon: fight elf");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"), "Failed to attack elf.");

        // 查看生命值（應為 2）
        response = server.handleCommand("simon: health");
        System.out.println("Response: " + response);
        assertTrue(response.contains("Your health is: 2"), "Health did not decrease correctly after first attack.");

        // 喝 potion 恢復生命值
        response = server.handleCommand("simon: inv");
        System.out.println("Response: " + response);
        response = server.handleCommand("simon: drink potion");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You drink the potion and your health improves"), "Failed to drink potion.");
        response = server.handleCommand("simon: health");
        System.out.println("Response: " + response);
        assertTrue(response.contains("Your health is: 3"), "Health did not increase correctly after drinking potion.");

        // 再次攻擊 elf（生命值下降到 0，死亡）
        response = server.handleCommand("simon: fight elf");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"), "Failed to attack elf.");
        response = server.handleCommand("simon: fight elf");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"), "Failed to attack elf.");
        response = server.handleCommand("simon: fight elf");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You're dead"), "Failed to handle player death.");

        // 查看生命值（應為 3）
        response = server.handleCommand("simon: health");
        System.out.println("Response: " + response);
        assertTrue(response.contains("Your health is: 3"), "Failed to reset health after death.");
        // 確認玩家回到起始位置
        response = server.handleCommand("simon: look");
        System.out.println("Response: " + response);
        assertTrue(response.contains("cabin"), "Failed to reset player location after death.");
        // 確認物品被清空
        response = server.handleCommand("simon: inv");
        response = response.toLowerCase();
        System.out.println("Inventory response: " + response);
        assertFalse(response.contains("axe"), "Axe should not be in inventory after death.");
        assertFalse(response.contains("coin"), "Coin should not be in inventory after death.");
        assertFalse(response.contains("potion"), "Potion should not be in inventory after death.");
        assertFalse(response.contains("shovel"), "Shovel should not be in inventory after death.");
        assertFalse(response.contains("key"), "Key should not be in inventory after death.");
        assertFalse(response.contains("horn"), "Horn should not be in inventory after death.");
        assertFalse(response.contains("log"), "Log should not be in inventory after death.");
        

        System.out.println("=== Player actions and health test passed ===");
    }
    
    @Test
    void testGameActionsWithMapLogic() {
        System.out.println("=== Testing game actions with map logic ===");

        // 1. 從森林獲取 Key
        String response = server.handleCommand("simon: goto forest");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to forest"), "Failed to move to forest.");
        
        response = server.handleCommand("simon: get key");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up key"), "Failed to pick up key.");
        response = server.handleCommand("simon: inv");
        response = response.toLowerCase();
        System.out.println("Inventory response: " + response);

        // 2. 返回 Cabin 拿 Potion 和 Axe coin
        response = server.handleCommand("simon: goto cabin");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to cabin"), "Failed to move to cabin.");

        response = server.handleCommand("simon: get potion");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up potion"), "Failed to pick up potion.");

        response = server.handleCommand("simon: get axe");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up axe"), "Failed to pick up axe.");

        response = server.handleCommand("simon: get coin");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up coin"), "Failed to pick up coin.");
        response = server.handleCommand("simon: inv");
        response = response.toLowerCase();
        System.out.println("Inventory response: " + response);
        assertTrue(response.contains("axe"), "Axe not found in inventory.");
        assertTrue(response.contains("coin"), "Coin not found in inventory.");

        // 使用 key 打開 trapdoor
        response = server.handleCommand("simon: open trapdoor with key");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You unlock the trapdoor and see steps leading down into a cellar"), "Failed to open trapdoor.");

        // 進入 cellar
        response = server.handleCommand("simon: goto cellar");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to cellar"), "Failed to move into cellar.");

        // 使用 coin 和 elf 交易 shovel
        response = server.handleCommand("simon: pay elf with coin");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You pay the elf your silver coin and he produces a shovel"), "Failed to purchase shovel from elf.");
        response = server.handleCommand("simon: get shovel");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up shovel"), "Failed to pick up shovel.");
        // 確認 shovel 在背包中
        response = sendCommandToServer("simon: inv");
        response = response.toLowerCase();
        System.out.println("Inventory response: " + response);
        assertTrue(response.contains("shovel"), "Shovel not found in inventory.");

        // 3. 去 Riverbank 獲取 Horn
        response = server.handleCommand("simon: goto cabin");
        System.out.println("Response: " + response);
        response = server.handleCommand("simon: goto forest");
        System.out.println("Response: " + response);
        response = server.handleCommand("simon: look");
        System.out.println("Response: " + response);
        response = server.handleCommand("simon: goto riverbank");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to riverbank."), "Failed to move to riverbank.");

        response = server.handleCommand("simon: get horn");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up horn"), "Failed to pick up horn.");

        // 4. 執行 Blow 動作
        response = server.handleCommand("simon: inv");
        System.out.println("Response: " + response);
        response = server.handleCommand("simon: blow horn");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You blow the horn and as if by magic, a lumberjack appears !"), "Failed to execute blow action.");

        response = server.handleCommand("simon: look");
        System.out.println("Response: " + response);
        assertTrue(response.contains("lumberjack"), "Failed to see lumberjack after blow action.");

        // 5. Chop Tree in Forest, 获得 Log
        response = server.handleCommand("simon: goto forest");
        System.out.println("Response: " + response);

        response = server.handleCommand("simon: chop tree with axe");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You cut down the tree with the axe"), "Failed to chop tree in forest.");
        response = server.handleCommand("simon: get log");
        System.out.println("Response: " + response);

        response = server.handleCommand("simon: inv");
        System.out.println("Response: " + response);
        assertTrue(response.contains("log"), "Failed to pick up log.");

        // 6. Bridge River with Log, 到 Clearing
        response = server.handleCommand("simon: goto riverbank");
        System.out.println("Response: " + response);

        response = server.handleCommand("simon: bridge river with log");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You bridge the river with the log and can now reach the other side"), "Failed to execute bridge action.");

        response = server.handleCommand("simon: goto clearing");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You moved to clearing"), "Failed to move to clearing.");

        // 7. Dig in Clearing and Retrieve Gold
        response = server.handleCommand("simon: dig ground with shovel");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You dig into the soft ground and unearth a pot of gold !!!"), "Failed to execute dig action.");
        response = server.handleCommand("simon: look");
        System.out.println("Response: " + response);
        assertTrue(response.contains("gold"), "Failed to see gold after dig action.");

        response = server.handleCommand("simon: get gold");
        System.out.println("Response: " + response);
        assertTrue(response.contains("You picked up gold"), "Failed to pick up gold.");
        response = sendCommandToServer("simon: inv");
        response = response.toLowerCase();
        System.out.println("Inventory response: " + response);
        
        System.out.println("=== All map-based actions tested successfully ===");
    }


}
 





