package edu.uob;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

class qtyTests {

    private GameServer server;

    // 初始化服务器，加载扩展的配置
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "qty-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "qty-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }

    // 发送命令并处理超时
    private String sendCommand(String command) {
        return assertTimeoutPreemptively(
                Duration.ofMillis(1000),
                () -> server.handleCommand(command),
                "Server timeout (可能进入死循环)"
        );
    }

    @Test
    public void testGetAndDrop() {
        String response = "";
        response = sendCommand("qtyohh: get");
        assertTrue(response.contains("?"), "get没有正确entity无法执行"); // 自己修改报错信息
        response = sendCommand("qtyohh: inv");
        assertFalse(response.contains("bug"), "只输入get,不应该在inv里检查到东西");
        response = sendCommand("qtyohh: look");
        assertTrue(response.contains("bug"), "只输入get,应该look到bug");
        response = sendCommand("qtyohh: get bug");
        assertFalse(response.contains("Invalid command"), "get bug成功执行，不应该报错"); // 自己修改报错信息
        response = sendCommand("qtyohh: inv");
        assertTrue(response.contains("bug"), "没有正确get到bug");
        response = sendCommand("tyq: inv");
        assertFalse(response.contains("bug"), "不应该在别人包里找到bug");
        response = sendCommand("tyq: get bug");
        assertTrue(response.contains("not found"), "不应该拿走别人包里的bug");// 自己修改报错信息
        response = sendCommand("qtyohh: look");
        assertFalse(response.contains("bug"), "bug应该被get走了");

        response = sendCommand("qtyohh: drop");
        assertTrue(response.contains("Invalid command"), "drop没有正确entity无法执行"); // 自己修改报错信息
        response = sendCommand("qtyohh: inv");
        assertTrue(response.contains("bug"), "只输入drop不应该执行");
        response = sendCommand("tyq: drop bug");
        assertTrue(response.contains(" don't"), "别人不应该drop自己包里的bug");// 自己修改报错信息
        response = sendCommand("qtyohh: drop bug");
        assertFalse(response.contains("Invalid command"), "get bug成功执行，不应该报错"); // 自己修改报错信息
        response = sendCommand("qtyohh: inv");
        assertFalse(response.contains("bug"), "没有正确drop到bug");
        response = sendCommand("tyq: inv");
        assertFalse(response.contains("bug"), "不应该在别人包里找到bug");
        response = sendCommand("qtyohh: look");
        assertTrue(response.contains("bug"), "bug应该被drop回了location");

        response = sendCommand("qtyohh: get bug and shit");
        assertTrue(response.contains("Invalid command"), "不能同时get多个东西"); // 自己修改报错信息
        sendCommand("qtyohh: get bug");
        sendCommand("qtyohh: bibibobo get shit bbbbbb");
        response = sendCommand("qtyohh: drop bug and shit");
        assertTrue(response.contains("Invalid command"), "不能同时drop多个东西"); // 自己修改报错信息


        sendCommand("qtyohh: goto cave");
        response = sendCommand("qtyohh: get goblin");
        assertTrue(response.contains("not found"), "不能get charactor"); // 自己修改报错信息
        response = sendCommand("qtyohh: get rock");
        assertTrue(response.contains("not found"), "不能get furniture"); // 自己修改报错信息

    }

    @Test
    public void testHealth() {
        String response = "";
        sendCommand("qtyohh: get bug");
        sendCommand("qtyohh: goto cave");
        response = sendCommand("qtyohh: health");
        assertTrue(response.contains("3"), "应该满血");

        sendCommand("qtyohh: hit goblin");


        response = sendCommand("qtyohh: health");
        assertTrue(response.contains("2"), "应该扣了1滴血");

        sendCommand("qtyohh: hit goblin");
        response = sendCommand("qtyohh: hit goblin");
        assertTrue(response.contains("You're dead"), "应该寄了"); //死亡自己改

        response = sendCommand("qtyohh: look");
        assertTrue(response.contains("forest"), "没有被传送到起点复活");

        response = sendCommand("qtyohh: inv");
        assertFalse(response.contains("bug"), "没有死亡掉落");


        sendCommand("qtyohh: goto cave");
        response = sendCommand("qtyohh: look");
        assertTrue(response.contains("bug"), "bug应该掉落");
    }

    @Test
    void testSpecialCharacterUsername() {
        // 特殊字符用户名
        String response = sendCommand("a!b@c#: look");
        assertTrue(response.contains("Invalid"), "应拒绝非法用户名"); // 自己修改报错信息

        response = sendCommand("O'Conner-a: look");
        assertFalse(response.contains("Invalid"), "应允许合法特殊字符"); // 自己修改报错信息
    }

    @Test
    public void testKey() {
        String response = "";
        sendCommand("qtyohh: get bug");
        sendCommand("qtyohh: goto cave");
        response = sendCommand("qtyohh: get key");
        assertTrue(response.contains("not found"), "没有钥匙"); // 自己修改报错信息
        response = sendCommand("qtyohh: attack magic goblin");
        assertTrue(response.contains("Invalid command"), "attack magic命令输入错误"); // 自己修改报错信息
        response = sendCommand("qtyohh:magicattack goblin using bug");
        assertFalse(response.contains("Invalid command"), "执行action"); // 自己修改报错信息
        response = sendCommand("qtyohh: look");
        assertTrue(response.contains("key"), "key没有正确生成");
        assertFalse(response.contains("goblin"), "goblin应该被消耗了");
        sendCommand("qtyohh: get key");
        response = sendCommand("qtyohh: open caveDoor use key");
        assertTrue(response.contains("caveDoor"), "没有正确输出narration");
        sendCommand("qtyohh: goto secretsChamber");
        response = sendCommand("qtyohh: look");
        assertTrue(response.contains("secretsChamber"), "没有正确走到密室");

    }

}
