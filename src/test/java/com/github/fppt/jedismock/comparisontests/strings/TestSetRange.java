package com.github.fppt.jedismock.comparisontests.strings;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ComparisonBase.class)
public class TestSetRange {
    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
    }

    @TestTemplate
    public void setRange(Jedis jedis) {
        jedis.set("key1", "Hello World");
        final long l = jedis.setrange("key1", 6, "Redis");
        assertEquals(11, l);
        assertEquals("Hello Redis", jedis.get("key1"));
    }

    @TestTemplate
    public void setRangeAppend(Jedis jedis) {
        jedis.set("key1", "Hello World");
        final long l = jedis.setrange("key1", 6, "Redis Redis");
        assertEquals(17, l);
        assertEquals("Hello Redis Redis", jedis.get("key1"));
    }

    @TestTemplate
    public void setRangeInTheMiddle(Jedis jedis) {
        jedis.set("key1", "Hello World");
        final long l = jedis.setrange("key1", 2, "FOO");
        assertEquals(11, l);
        assertEquals("HeFOO World", jedis.get("key1"));
    }


    @TestTemplate
    public void setRangeZeroPadding(Jedis jedis) {
        final long l = jedis.setrange("key2", 6, "Redis");
        assertEquals(11, l);
        assertEquals(new String(new byte[6]) + "Redis", jedis.get("key2"));
    }
}
