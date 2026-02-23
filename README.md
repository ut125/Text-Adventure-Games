# Text-Adventure-Games
Text adventure games, with Zork being a classic example of this genre, see:  https://tinyurl.com/zork-game

# 執行流程
## 在第一個終端機執行
1. 打開終端機編譯: `.\mvnw clean compile` ，編譯後顯示 BUILD SUCCESS<br>
2. 啟動伺服器 (Server): `.\mvnw exec:java@server -D exec.args="config/basic-entities.dot config/basic-actions.json"` ，視窗會停在一個地方（不會回到輸入狀態），這代表伺服器成功跑起來了。<br>

## 在第二個終端機執行
1. 啟動客戶端 (Client): `.\mvnw exec:java@client -D exec.args="player1"`<br>
(如有多名玩家同時操作可以再開一個終端機後輸入: `.\mvnw exec:java@client -D exec.args="player2"`，以此類推)

# 玩法指令
- `"inventory"` 或 `"inv"`：列出玩家當前擁有的所有 **物品（artefacts）**。<br>
- `"get"`：從當前地點拾取指定的 **物品**，並將其加入玩家的 **物品欄（inventory）**。<br>
- `"drop"`：從玩家的 **物品欄** 放下 **物品**，並將其放置在當前地點。<br>
- `"goto"`：移動玩家到 **新地點**（前提是有可行的路徑）。<br>
- `"look"`：描述當前地點。<br>
- `"health"` : 查看當前生命值。<br>
