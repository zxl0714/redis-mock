package com.github.fppt.jedismock.comparisontests.strings;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ComparisonBase.class)
public class TestMSetNX {
    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
    }

    @TestTemplate
    public void msetnx(Jedis jedis) {
        assertEquals(1, jedis.msetnx("key1", "Hello", "key2", "there"));
        assertEquals(0, jedis.msetnx("key2", "new", "key3", "world"));
        assertEquals(Arrays.asList("Hello", "there", null), jedis.mget("key1", "key2", "key3"));
    }
}
