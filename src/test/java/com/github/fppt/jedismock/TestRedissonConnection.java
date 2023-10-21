package com.github.fppt.jedismock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.IOException;
import java.util.Arrays;

public class TestRedissonConnection {
    private static RedisServer redisServer;
    private static Config config;

    @BeforeAll
    static void setUp() throws IOException {
        redisServer = RedisServer.newRedisServer();
        redisServer.start();
        config = new Config();
        config.useSingleServer().setAddress(
                String.format("redis://%s:%d",
                        redisServer.getHost(), redisServer.getBindPort()));
    }

    @AfterAll
    static void tearDown() throws IOException {
        redisServer.stop();
    }

    @Test
    public void testStringMap() {
        RedissonClient client = Redisson.create(config);
        RMap<String, String> map = client.getMap("stringMap");
        Assertions.assertNull(map.put("foo", "bar"));
        Assertions.assertEquals("bar", map.get("foo"));
        Assertions.assertEquals("bar", map.put("foo", "baz"));
    }

    @Test
    public void testIntegerMap() {
        RedissonClient client = Redisson.create(config);
        RMap<String, Integer> map = client.getMap("intMap");
        Assertions.assertNull(map.put("foo", 42));
        Assertions.assertEquals(42, map.get("foo"));
        Assertions.assertEquals(42, map.put("foo", 43));
    }
    @Test
    public void testIntList() {
        RedissonClient client = Redisson.create(config);
        RList<Integer> list = client.getList("intList");
        Assertions.assertTrue(list.add(11));
        Assertions.assertTrue(list.add(15));
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(Arrays.asList(11, 15), list.readAll());
    }

}
