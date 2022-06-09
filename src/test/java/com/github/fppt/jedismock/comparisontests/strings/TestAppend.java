package com.github.fppt.jedismock.comparisontests.strings;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ComparisonBase.class)
public class TestAppend {
    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushDB();
    }

    @TestTemplate
    public void testAppendEmpty(Jedis jedis) {
        jedis.append("foo", "bar");
        assertEquals("bar", jedis.get("foo"));
    }

    @TestTemplate
    public void testAppendNonEmpty(Jedis jedis) {
        jedis.set("baz", "foo");
        jedis.append("baz", "bar");
        assertEquals("foobar", jedis.get("baz"));
    }

    @TestTemplate
    public void testAppendBinary(Jedis jedis) {
        jedis.set("baz".getBytes(), new byte[]{(byte) 0x01, (byte) 0x02});
        jedis.append("baz".getBytes(), new byte[]{(byte) 0x03, (byte) 0x04});
        assertArrayEquals(new byte[]{
                (byte) 0x01, (byte) 0x02,
                (byte) 0x03, (byte) 0x04}, jedis.get("baz".getBytes()));
    }
}
